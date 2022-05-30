package cn.twq.secKill.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecKillMessage {
  private Long goodsId;
  private User user;
}
