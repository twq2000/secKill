package cn.twq.secKill.service.impl;

import cn.twq.secKill.entity.SeckillOrder;
import cn.twq.secKill.entity.User;
import cn.twq.secKill.mapper.SeckillOrderMapper;
import cn.twq.secKill.service.ISeckillOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class SeckillOrderServiceImpl extends ServiceImpl<SeckillOrderMapper, SeckillOrder>
    implements ISeckillOrderService {

  private final RedisTemplate<String, Object> redisTemplate;

  public SeckillOrderServiceImpl(RedisTemplate<String, Object> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  @Override
  public Long getResult(User user, Long goodsId) {
    // 如果能从redis查到对应数据，说明已经秒杀成功了
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
      return seckillOrder.getOrderId();
    }
    // 否则，要么是秒杀失败了(由于库存已空)，要么就是仍在等待中
    long fail = -1L, waiting = 0L;
    return Boolean.TRUE.equals(redisTemplate.hasKey("emptyGoods: " + goodsId)) ? fail : waiting;
  }
}
