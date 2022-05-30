package cn.twq.secKill.mapper;

import cn.twq.secKill.entity.Goods;
import cn.twq.secKill.vo.GoodsVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface GoodsMapper extends BaseMapper<Goods> {

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
