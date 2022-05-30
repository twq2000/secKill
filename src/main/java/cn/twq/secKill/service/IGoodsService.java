package cn.twq.secKill.service;

import cn.twq.secKill.entity.Goods;
import cn.twq.secKill.vo.GoodsVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface IGoodsService extends IService<Goods> {

  /**
   * 获取商品列表数据
   *
   * @return
   */
  List<GoodsVO> getGoodsVO();

  /**
   * 获取商品详情页的数据
   *
   * @param goodsId
   * @return
   */
  GoodsVO getGoodsVOByGoodsId(Long goodsId);

}
