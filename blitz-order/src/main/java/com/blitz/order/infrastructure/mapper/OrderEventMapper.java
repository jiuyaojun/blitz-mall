package com.blitz.order.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blitz.order.domain.model.OrderEvent;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderEventMapper extends BaseMapper<OrderEvent> {
}
