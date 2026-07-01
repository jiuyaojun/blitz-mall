package com.blitz.user.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blitz.user.domain.model.Shop;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ShopMapper extends BaseMapper<Shop> {
}
