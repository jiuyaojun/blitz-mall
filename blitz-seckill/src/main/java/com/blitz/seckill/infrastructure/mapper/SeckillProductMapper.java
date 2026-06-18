package com.blitz.seckill.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blitz.seckill.domain.model.SeckillProduct;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SeckillProductMapper extends BaseMapper<SeckillProduct> {}
