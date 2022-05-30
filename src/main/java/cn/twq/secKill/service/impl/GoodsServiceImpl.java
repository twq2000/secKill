package cn.twq.secKill.service.impl;

import cn.twq.secKill.entity.Goods;
import cn.twq.secKill.mapper.GoodsMapper;
import cn.twq.secKill.service.IGoodsService;
import cn.twq.secKill.vo.GoodsVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements IGoodsService {

  private final GoodsMapper goodsMapper;

  public GoodsServiceImpl(GoodsMapper goodsMapper) {
    this.goodsMapper = goodsMapper;
  }

  @Override
  public List<GoodsVO> getGoodsVO() {
    return goodsMapper.getGoodsVO();
  }

  @Override
  public GoodsVO getGoodsVOByGoodsId(Long goodsId) {
    return goodsMapper.getGoodsVOByGoodsId(goodsId);
  }

}
