package cn.twq.secKill.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class GoodsVO {
  private Long id;
  private String goodsName;
  private String goodsTitle;
  private String goodsImg;
  private String goodsDetail;
  private BigDecimal goodsPrice;
  /** 商品库存 */
  private Integer goodsStock;
  private BigDecimal seckillPrice;
  /** 秒杀商品库存 */
  private Integer goodsCount;
  private Date startDate;
  private Date endDate;
}
