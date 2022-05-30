package cn.twq.secKill.service;

import cn.twq.secKill.entity.SeckillOrder;
import cn.twq.secKill.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

public interface ISeckillOrderService extends IService<SeckillOrder> {

  /**
   * 获取秒杀结果
   *
   * @param user
   * @param goodsId
   * @return -1：秒杀失败；0：排队中
   */
  Long getResult(User user, Long goodsId);
}
