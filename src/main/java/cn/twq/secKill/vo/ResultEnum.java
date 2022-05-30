package cn.twq.secKill.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public enum ResultEnum {
  /** 通用消息 */
  SUCCESS(200, "SUCCESS"),
  ERROR(500, "服务器异常"),
  /** 用户消息 */
  LOGIN_ERROR(500210, "用户名或密码错误！"),
  MOBILE_ERROR(500211, "手机号码格式错误！"),
  BIND_ERROR(500212, "提交参数错误！"),
  USER_NOT_EXIST(500213, "用户不存在！"),
  PASSWORD_UPDATE_FAIL(500214, "密码修改失败！"),
  USER_NOT_LOGIN(500215, "您尚未登录！"),
  /** 秒杀消息 */
  STOCK_EMPTY(500500, "库存不足！"),
  REPEATED_ORDER(500501, "每人限购一件！"),
  UPDATE_STOCK_FAIL(500502, "库存更新失败！"),
  INSERT_SECKILL_ORDER_FAIL(500504, "秒杀订单生成失败！"),
  REQUEST_INVALID(500505, "非法的请求！"),
  CAPTCHA_WRONG(500506, "验证码输入错误！"),
  REQUEST_ACCESS_LIMIT(500507, "访问过于频繁，请稍后再试！"),
  /** 商品消息 */
  QUERY_GOODS_DETAIL_FAIL(500600, "获取商品详情失败！"),
  /** 订单消息 */
  ORDER_NOT_EXIST(500700, "订单信息不存在！"),
  CREATE_ORDER_FAIL(500701, "订单生成失败！"),
  ;

  private final Integer code;
  private final String message;

}
