package cn.twq.secKill.vo;

import cn.twq.secKill.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoodsDetailVO {
  private User user;
  private GoodsVO goodsVO;
  private Integer secKillStatus;
  private Integer remainSecond;
}
