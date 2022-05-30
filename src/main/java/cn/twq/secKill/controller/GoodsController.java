package cn.twq.secKill.controller;

import cn.twq.secKill.entity.User;
import cn.twq.secKill.exception.GlobalException;
import cn.twq.secKill.service.IGoodsService;
import cn.twq.secKill.vo.GoodsDetailVO;
import cn.twq.secKill.vo.GoodsVO;
import cn.twq.secKill.vo.Result;
import cn.twq.secKill.vo.ResultEnum;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/goods")
public class GoodsController {

  private final IGoodsService goodsService;
  private final RedisTemplate<String, Object> redisTemplate;
  private final ThymeleafViewResolver htmlResolver;

  public GoodsController(
      IGoodsService goodsService,
      RedisTemplate<String, Object> redisTemplate,
      ThymeleafViewResolver htmlResolver) {
    this.goodsService = goodsService;
    this.redisTemplate = redisTemplate;
    this.htmlResolver = htmlResolver;
  }

  /**
   * 访问商品列表页面
   * 优化前QPS：1854
   * 使用redis缓存后QPS：2784
   *
   * @param model
   * @param user
   * @return
   */
  @RequestMapping(value = "/toList", produces = "text/html;charset=utf-8")
  @ResponseBody
  public String toList(
      Model model, User user, HttpServletRequest request, HttpServletResponse response) {
    ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
    if (user == null) {
      // 对于尚未登录的用户，强制跳转到登陆页面
      String loginHtml = ((String) valueOperations.get("login"));
      if (ObjectUtils.isEmpty(loginHtml)) {
        // 手动对页面进行渲染
        loginHtml =
            htmlResolver
                .getTemplateEngine()
                .process("login", new WebContext(request, response, request.getServletContext()));
        if (ObjectUtils.isEmpty(loginHtml)) {
          throw new GlobalException(ResultEnum.ERROR);
        }
        // 将页面结果存入缓存
        valueOperations.set("login", loginHtml);
      }
      return loginHtml;
    }
    // 首先，尝试从 redis 缓存中获取该页面
    String goodsListHtml = ((String) valueOperations.get("goodsList"));
    if (ObjectUtils.isEmpty(goodsListHtml)) {
      // 手动对页面进行渲染
      model.addAttribute("user", user);
      model.addAttribute("goodsList", goodsService.getGoodsVO());
      WebContext webContext =
          new WebContext(
              request, response, request.getServletContext(), request.getLocale(), model.asMap());
      goodsListHtml = htmlResolver.getTemplateEngine().process("goodsList", webContext);
      if (ObjectUtils.isEmpty(goodsListHtml)) {
        throw new GlobalException(ResultEnum.ERROR);
      }
      // 将最终结果存入缓存
      valueOperations.set("goodsList", goodsListHtml, 60, TimeUnit.SECONDS);
    }
    return goodsListHtml;
  }

  /**
   * 访问商品详情页
   *
   * @param goodsId
   * @return
   */
  @RequestMapping("/detail/{goodsId}")
  @ResponseBody
  public Result detail(@PathVariable("goodsId") Long goodsId, User user) {
    GoodsVO goodsVO = goodsService.getGoodsVOByGoodsId(goodsId);
    if (goodsVO == null) {
      return Result.error(ResultEnum.QUERY_GOODS_DETAIL_FAIL);
    }
    // 判断该秒杀活动的当前状态：0代表秒杀前；1代表秒杀中；2代表秒杀后
    Date curDate = new Date(), startDate = goodsVO.getStartDate(), endDate = goodsVO.getEndDate();
    int secKillStatus, remainSecond;
    if (curDate.before(startDate)) {
      // 处于秒杀前。此时需要动态展示倒计时
      secKillStatus = 0;
      remainSecond = (int) ((startDate.getTime() - curDate.getTime()) / 1000);
    } else {
      // 处于秒杀中 or 秒杀后
      secKillStatus = curDate.before(endDate) ? 1 : 2;
      remainSecond = secKillStatus == 1 ? 0 : -1;
    }
    GoodsDetailVO goodsDetailVO = new GoodsDetailVO(user, goodsVO, secKillStatus, remainSecond);
    return Result.success(goodsDetailVO);
  }
}
