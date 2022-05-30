package cn.twq.secKill.message;

import cn.twq.secKill.config.RabbitMQConfig;
import cn.twq.secKill.entity.SecKillMessage;
import cn.twq.secKill.entity.SeckillOrder;
import cn.twq.secKill.entity.User;
import cn.twq.secKill.exception.GlobalException;
import cn.twq.secKill.service.IGoodsService;
import cn.twq.secKill.service.IOrderService;
import cn.twq.secKill.vo.GoodsVO;
import cn.twq.secKill.vo.ResultEnum;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class MessageConsumer {

  private final IOrderService orderService;
  private final IGoodsService goodsService;
  private final RedisTemplate<String, Object> redisTemplate;

  public MessageConsumer(
      IOrderService orderService,
      IGoodsService goodsService,
      RedisTemplate<String, Object> redisTemplate) {
    this.orderService = orderService;
    this.goodsService = goodsService;
    this.redisTemplate = redisTemplate;
  }

  /** 进行下单操作 */
  @RabbitListener(queues = RabbitMQConfig.QUEUE)
  public void consumeSecKillMessage(String message) {
    ObjectMapper objectMapper = new ObjectMapper();
    SecKillMessage secKillMessage;
    try {
      secKillMessage = objectMapper.readValue(message, SecKillMessage.class);
    } catch (JsonProcessingException e) {
      throw new GlobalException(ResultEnum.ERROR);
    }
    User user = secKillMessage.getUser();
    long goodsId = secKillMessage.getGoodsId();
    GoodsVO goodsVO = goodsService.getGoodsVOByGoodsId(goodsId);
    // 判断库存剩余数量
    if (goodsVO.getGoodsCount() <= 0) {
      return;
    }
    // 判断同一用户是否多次抢购同一商品
    SeckillOrder seckillOrder =
        ((SeckillOrder)
            redisTemplate
                .opsForValue()
                .get(
                    new StringBuilder()
                        .append("order: ")
                        .append(user.getId())
                        .append(": ")
                        .append(goodsId)
                        .toString()));
    if (seckillOrder != null) {
      return;
    }
    // 至此，允许该用户进行下单
    orderService.secKill(user, goodsVO);
  }
}
