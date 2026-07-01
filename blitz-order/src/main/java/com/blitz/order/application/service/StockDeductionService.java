package com.blitz.order.application.service;

import com.blitz.common.exception.BusinessException;
import com.blitz.common.exception.ErrorCode;
import com.blitz.order.application.service.OrderApplicationService.CreateOrderItem;
import com.blitz.order.domain.model.OrderItem;
import com.blitz.order.domain.port.OrderItemRepository;
import com.blitz.product.domain.model.SkuStock;
import com.blitz.product.domain.port.SkuStockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockDeductionService {

    private final SkuStockRepository skuStockRepository;
    private final OrderItemRepository orderItemRepository;

    /**
     * 扣减库存（乐观锁重试）
     */
    public void deductWithRetry(List<CreateOrderItem> items, int maxRetries) {
        for (CreateOrderItem item : items) {
            boolean deducted = false;
            for (int retry = 0; retry < maxRetries; retry++) {
                SkuStock stock = skuStockRepository.findBySkuId(item.skuId())
                    .orElseThrow(() -> new BusinessException(
                        ErrorCode.PARAM_INVALID.getCode(), "SKU 不存在: " + item.skuId()));

                if (stock.getStock() < item.quantity()) {
                    throw new BusinessException(ErrorCode.STOCK_INSUFFICIENT);
                }

                int affected = skuStockRepository.deductStock(
                    stock.getSkuId(), item.quantity(), stock.getVersion());

                if (affected > 0) {
                    deducted = true;
                    break;
                }

                log.warn("Stock deduction retry {}/{}: skuId={}, version={}",
                    retry + 1, maxRetries, item.skuId(), stock.getVersion());

                // 短暂等待后重试
                try { Thread.sleep(50L * (retry + 1)); } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "库存扣减被中断");
                }
            }

            if (!deducted) {
                throw new BusinessException(
                    ErrorCode.STOCK_INSUFFICIENT.getCode(),
                    "库存扣减失败（重试" + maxRetries + "次后仍失败）: skuId=" + item.skuId());
            }
        }
    }

    /**
     * 恢复库存（取消订单时调用）— 使用乐观锁防止并发覆盖
     */
    public void restoreStock(Long orderId) {
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        for (OrderItem item : items) {
            boolean restored = false;
            for (int retry = 0; retry < 3; retry++) {
                SkuStock stock = skuStockRepository.findBySkuId(item.getSkuId()).orElse(null);
                if (stock == null) break;
                int affected = skuStockRepository.restoreStock(
                    stock.getSkuId(), item.getQuantity(), stock.getVersion());
                if (affected > 0) {
                    log.info("Stock restored: skuId={}, qty={}", item.getSkuId(), item.getQuantity());
                    restored = true;
                    break;
                }
                log.warn("Stock restore retry {}/3: skuId={}", retry + 1, item.getSkuId());
            }
            if (!restored) {
                log.error("Stock restore failed after 3 retries: skuId={}", item.getSkuId());
            }
        }
    }
}
