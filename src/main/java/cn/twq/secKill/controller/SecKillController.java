package cn.twq.secKill.controller;

import cn.twq.secKill.config.RequestLimit;
import cn.twq.secKill.entity.SecKillMessage;
import cn.twq.secKill.entity.SeckillOrder;
import cn.twq.secKill.entity.User;
import cn.twq.secKill.exception.GlobalException;
import cn.twq.secKill.message.MessageSupplier;
import cn.twq.secKill.service.IGoodsService;
import cn.twq.secKill.service.IOrderService;
import cn.twq.secKill.service.ISeckillOrderService;
import cn.twq.secKill.vo.GoodsVO;
import cn.twq.secKill.vo.Result;
import cn.twq.secKill.vo.ResultEnum;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wf.captcha.ArithmeticCaptcha;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Controller
@RequestMapping("/secKill")
public class SecKillController implements InitializingBean {

  private final IOrderService orderService;
  private final IGoodsService goodsService;
  private final ISeckillOrderService seckillOrderService;
  private final RedisTemplate<String, Object> redisTemplate;
  private final MessageSupplier messageSupplier;
  private final RedisScript<Long> redisScript;

  /** 记录每个商品的库存情况(仍有or已空)，以减少不必要的redis请求 */
  private final Map<Long, Boolean> goodsId2Status = new HashMap<>();
  private static final boolean HAS = true;
  private static final boolean EMPTY = false;
  private static final int WAITING = 0;

  public SecKillController(
      IOrderService orderService,
      IGoodsService goodsService,
      RedisTemplate<String, Object> redisTemplate,
      MessageSupplier messageSupplier,
      ISeckillOrderService seckillOrderService,
      RedisScript<Long> redisScript) {
    this.orderService = orderService;
    this.seckillOrderService = seckillOrderService;
    this.goodsService = goodsService;
    this.redisTemplate = redisTemplate;
    this.messageSupplier = messageSupplier;
    this.redisScript = redisScript;
  }

  /**
   * Windows优化前QPS：1356
   * 缓存后QPS：1127
   * 消息队列优化后QPS：2456
   *
   * @param model
   * @param goodsId
   * @param user
   * @return
   */
  @RequestMapping(value = "/{path}/doSecKill", method = RequestMethod.POST)
  @ResponseBody
  public Result doSecKill(@PathVariable("path") String path, Long goodsId, User user) {
    if (user == null) {
      return Result.error(ResultEnum.USER_NOT_LOGIN);
    }
    if (!orderService.checkPath(user, goodsId, path)) {
      return Result.error(ResultEnum.REQUEST_INVALID);
    }
    // 使用map作为内存标记，减少不必要的redis操作
    if (goodsId2Status.get(goodsId) == EMPTY) {
      return Result.error(ResultEnum.STOCK_EMPTY);
    }
    // 判断同一用户是否多次抢购同一商品
    // 这里不用再从数据库去查询是否存在对应数据，而是直接从redis查找
    ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
    SeckillOrder seckillOrder =
        ((SeckillOrder)
            valueOperations.get(
                new StringBuilder()
                    .append("order: ")
                    .append(user.getId())
                    .append(": ")
                    .append(goodsId)
                    .toString()));
    if (seckillOrder != null) {
      return Result.error(ResultEnum.REPEATED_ORDER);
    }
    // 预减redis的库存，并判断库存剩余数量
    // 为了保证这里的 decrease 更新操作是原子的，所以用了一个 lua脚本
    Long goodsCount =
        redisTemplate.execute(
            redisScript,
            Collections.singletonList("secKillGoods: " + goodsId),
            Collections.EMPTY_LIST);
    if (Objects.equals(goodsCount, 0L)) {
      goodsId2Status.put(goodsId, EMPTY);
    }
    // 至此，允许该用户进行抢购
    // 将抢购请求交给消息队列来异步处理
    // 这样做的好处：如果同一时刻有大量请求过来时，可以先快速返回一个响应消息（等待中……）
    // 另外，即使同一时刻涌入大量请求，消息队列也可以异步地、平缓地一个个处理，从而达到流量削峰的作用，不至于瞬间压垮数据库
    SecKillMessage secKillMessage = new SecKillMessage(goodsId, user);
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      messageSupplier.sendSecKillMessage(objectMapper.writeValueAsString(secKillMessage));
    } catch (JsonProcessingException e) {
      throw new GlobalException(ResultEnum.ERROR);
    }
    // 响应结果是 "处理中..."
    return Result.success(WAITING);
  }

  /**
   * 轮询秒杀结果
   *
   * @param user
   * @param goodsId
   * @return
   */
  @RequestMapping(value = "/result", method = RequestMethod.GET)
  @ResponseBody
  public Result getResult(User user, Long goodsId) {
    if (user == null) {
      return Result.error(ResultEnum.USER_NOT_LOGIN);
    }
    return Result.success(seckillOrderService.getResult(user, goodsId));
  }

  /**
   * 生成并返回秒杀接口的随机拼接路径
   *
   * @param user
   * @param goodsId
   * @param captchaInput
   * @return
   */
  @RequestLimit(second = 5, limit = 5)
  @RequestMapping(value = "/getPath", method = RequestMethod.GET)
  @ResponseBody
  public Result getPath(User user, Long goodsId, String captchaInput) {
    if (user == null) {
      return Result.error(ResultEnum.USER_NOT_LOGIN);
    }
    if (ObjectUtils.isEmpty(captchaInput) || !orderService.checkCaptcha(user, goodsId, captchaInput)) {
      return Result.error(ResultEnum.CAPTCHA_WRONG);
    }
    System.out.println();
    String path = orderService.createPath(user, goodsId);
    return Result.success(path);
  }

  @RequestMapping(value = "/captcha", method = RequestMethod.GET)
  public void captcha(User user, Long goodsId, HttpServletResponse response) {
    if (user == null || goodsId <= 0) {
      throw new GlobalException(ResultEnum.REQUEST_INVALID);
    }
    response.setContentType("image/jpg");
    response.setHeader("Pragma", "No-cache");
    response.setHeader("Cache-Control", "no-cache");
    response.setDateHeader("Expires", 0);
    // 设置验证码图片的长、宽、高
    ArithmeticCaptcha captcha = new ArithmeticCaptcha(130, 32, 3);
    redisTemplate
        .opsForValue()
        .set("captcha: " + user.getId() + ": " + goodsId, captcha.text(), 300L, TimeUnit.SECONDS);
    try {
      captcha.out(response.getOutputStream());
    } catch (IOException e) {
      log.error("验证码生成失败 {}", e.getMessage());
    }
  }


  /**
   * 初始化执行方法
   *
   */
  @Override
  public void afterPropertiesSet() {
    // 把商品库存数量预先加载入 redis 中
    List<GoodsVO> goodsVOList = goodsService.getGoodsVO();
    if (!CollectionUtils.isEmpty(goodsVOList)) {
      goodsVOList.forEach(
          goodsVO -> {
            redisTemplate
                .opsForValue()
                .set("secKillGoods: " + goodsVO.getId(), goodsVO.getGoodsCount());
            goodsId2Status.put(goodsVO.getId(), HAS);
          });
    }
  }
}
