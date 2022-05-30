package cn.twq.secKill.vo;

import cn.twq.secKill.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailVO {
  private Order order;
  private GoodsVO goodsVO;
}
