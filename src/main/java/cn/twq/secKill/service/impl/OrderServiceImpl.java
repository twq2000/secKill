package cn.twq.secKill.service.impl;

import cn.twq.secKill.entity.Order;
import cn.twq.secKill.entity.SeckillGoods;
import cn.twq.secKill.entity.SeckillOrder;
import cn.twq.secKill.entity.User;
import cn.twq.secKill.exception.GlobalException;
import cn.twq.secKill.mapper.OrderMapper;
import cn.twq.secKill.service.IGoodsService;
import cn.twq.secKill.service.IOrderService;
import cn.twq.secKill.service.ISeckillGoodsService;
import cn.twq.secKill.service.ISeckillOrderService;
import cn.twq.secKill.util.MD5Utils;
import cn.twq.secKill.vo.GoodsVO;
import cn.twq.secKill.vo.OrderDetailVO;
import cn.twq.secKill.vo.ResultEnum;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

  private final OrderMapper orderMapper;
  private final IGoodsService goodsService;
  private final ISeckillOrderService seckillOrderService;
  private final ISeckillGoodsService seckillGoodsService;
  private final RedisTemplate<String, Object> redisTemplate;
  private static final int EMPTY = 0;

  public OrderServiceImpl(
      IGoodsService goodsService,
      ISeckillOrderService seckillOrderService,
      ISeckillGoodsService seckillGoodsService,
      OrderMapper orderMapper,
      RedisTemplate<String, Object> redisTemplate) {
    this.goodsService = goodsService;
    this.seckillOrderService = seckillOrderService;
    this.seckillGoodsService = seckillGoodsService;
    this.orderMapper = orderMapper;
    this.redisTemplate = redisTemplate;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Order secKill(User user, GoodsVO goods) {
    ValueOperations<String, Object> opsForValue = redisTemplate.opsForValue();
    // 更新秒杀商品的库存数
    boolean updateResult =
        seckillGoodsService.update(
            new UpdateWrapper<SeckillGoods>()
                .setSql("goods_count = goods_count - 1")
                .eq("goods_id", goods.getId())
                .gt("goods_count", 0));
    if (!updateResult) {
      // 如果更新数据库中的库存失败，那么我希望抛出一个异常信息，然后返回给前端作为提示
      // 但是，当前方法的调用者是消息队列，它的机制是：遇到异常后会再次尝试消费该消息。这就导致了死循环尝试。
      // 解决办法是：抛出一个 fatal 的致命异常（如 MessageConversionException），那么消息队列就不会再尝试消费这个消息了
      throw new MessageConversionException(ResultEnum.UPDATE_STOCK_FAIL.getMessage());
    }
    if (((int) opsForValue.get("secKillGoods: " + goods.getId())) <= 0) {
      opsForValue.set("emptyGoods: " + goods.getId(), EMPTY);
      throw new MessageConversionException(ResultEnum.STOCK_EMPTY.getMessage());
    }
    // 生成订单
    Order order = new Order();
    order.setUserId(user.getId());
    order.setGoodsId(goods.getId());
    order.setAddrId(0L);
    order.setGoodsName(goods.getGoodsName());
    order.setGoodsCount(1);
    order.setGoodsPrice(goods.getSeckillPrice());
    order.setOrderChannel(1);
    order.setStatus(0);
    order.setCreateDate(new Date());
    if (orderMapper.insert(order) != 1) {
      throw new MessageConversionException(ResultEnum.CREATE_ORDER_FAIL.getMessage());
    }
    // 生成秒杀订单
    SeckillOrder seckillOrder = new SeckillOrder();
    seckillOrder.setUserId(user.getId());
    seckillOrder.setOrderId(order.getId());
    seckillOrder.setGoodsId(goods.getId());
    if (!seckillOrderService.save(seckillOrder)) {
      throw new MessageConversionException(ResultEnum.INSERT_SECKILL_ORDER_FAIL.getMessage());
    }
    // 将秒杀订单信息存入 redis 缓存
    // 这样做的好处是：在判断“同一用户是否多次下单同一商品”这一逻辑时，可以不用再从数据库去查询是否存在对应的数据，而是直接通过redis查询
    opsForValue.set(
        new StringBuilder()
            .append("order: ")
            .append(user.getId())
            .append(": ")
            .append(goods.getId())
            .toString(),
        seckillOrder);
    return order;
  }

  @Override
  public OrderDetailVO getDetailById(Long orderId) {
    if (orderId == null) {
      throw new GlobalException(ResultEnum.ORDER_NOT_EXIST);
    }
    Order order = getById(orderId);
    GoodsVO goodsVO = goodsService.getGoodsVOByGoodsId(order.getGoodsId());
    return new OrderDetailVO(order, goodsVO);
  }

  @Override
  public String createPath(User user, Long goodsId) {
    String randomStr = MD5Utils.encodePassword2Backend(UUID.randomUUID().toString());
    // 60秒内有效
    redisTemplate
        .opsForValue()
        .set("secKillPath: " + user.getId() + ": " + goodsId, randomStr, 60L, TimeUnit.SECONDS);
    return randomStr;
  }

  @Override
  public boolean checkPath(User user, Long goodsId, String path) {
    if (user == null || goodsId <= 0 || ObjectUtils.isEmpty(path)) {
      return false;
    }
    String actualPath =
        (String) redisTemplate.opsForValue().get("secKillPath: " + user.getId() + ": " + goodsId);
    return path.equals(actualPath);
  }

  @Override
  public boolean checkCaptcha(User user, Long goodsId, String captchaInput) {
    String captcha =
        (String) redisTemplate.opsForValue().get("captcha: " + user.getId() + ": " + goodsId);
    return captchaInput.equals(captcha);
  }
}
