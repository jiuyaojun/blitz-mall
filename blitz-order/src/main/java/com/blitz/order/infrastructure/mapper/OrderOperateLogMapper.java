package com.blitz.order.infrastructure.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blitz.order.domain.model.OrderOperateLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderOperateLogMapper extends BaseMapper<OrderOperateLog> {

    default List<OrderOperateLog> findByOrderId(Long orderId) {
        return selectList(
            new LambdaQueryWrapper<OrderOperateLog>()
                .eq(OrderOperateLog::getOrderId, orderId)
                .orderByDesc(OrderOperateLog::getCreatedAt)
        );
    }
}
