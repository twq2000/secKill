package cn.twq.secKill.service.impl;

import cn.twq.secKill.entity.SeckillGoods;
import cn.twq.secKill.mapper.SeckillGoodsMapper;
import cn.twq.secKill.service.ISeckillGoodsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class SeckillGoodsServiceImpl extends ServiceImpl<SeckillGoodsMapper, SeckillGoods>
    implements ISeckillGoodsService {}
