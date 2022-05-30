package cn.twq.secKill.service;

import cn.twq.secKill.entity.Order;
import cn.twq.secKill.entity.User;
import cn.twq.secKill.vo.GoodsVO;
import cn.twq.secKill.vo.OrderDetailVO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IOrderService extends IService<Order> {

  /**
   * 创建秒杀商品的订单
   *
   * @param user
   * @param goods
   * @return
   */
  Order secKill(User user, GoodsVO goods);

  /**
   * 根据订单id，查询订单详情展示数据
   *
   * @param orderId
   * @return
   */
  OrderDetailVO getDetailById(Long orderId);

  /**
   * 生成秒杀接口的随机拼接路径
   *
   * @param user
   * @param goodsId
   * @return
   */
  String createPath(User user, Long goodsId);

  /**
   * 校验秒杀接口地址
   *
   * @param user
   * @param goodsId
   * @param path
   * @return
   */
  boolean checkPath(User user, Long goodsId, String path);

  /**
   * 判断验证码是否输入正确
   *
   * @param user
   * @param goodsId
   * @param captchaInput
   * @return
   */
  boolean checkCaptcha(User user, Long goodsId, String captchaInput);
}
