package com.blitz.merchant.application;

import cn.hutool.json.JSONUtil;
import com.blitz.common.exception.BusinessException;
import com.blitz.common.exception.ErrorCode;
import com.blitz.order.domain.model.Order;
import com.blitz.order.domain.model.OrderItem;
import com.blitz.order.domain.port.OrderItemRepository;
import com.blitz.order.infrastructure.mapper.OrderMapper;
import com.blitz.product.api.dto.ProductVO;
import com.blitz.product.api.dto.SkuDTO;
import com.blitz.product.domain.model.Product;
import com.blitz.product.domain.model.Sku;
import com.blitz.product.domain.model.SkuStock;
import com.blitz.product.domain.port.ProductRepository;
import com.blitz.product.domain.port.SkuRepository;
import com.blitz.product.domain.port.SkuStockRepository;
import com.blitz.user.domain.model.Shop;
import com.blitz.user.domain.port.ShopRepository;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MerchantApplicationService {

    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;
    private final SkuRepository skuRepository;
    private final SkuStockRepository skuStockRepository;
    private final OrderMapper orderMapper;
    private final OrderItemRepository orderItemRepository;

    /** Get or create shop for a merchant user */
    public Shop getShop(Long userId) {
        return shopRepository.findByUserId(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND.getCode(), "店铺不存在，请先创建店铺"));
    }

    /** Create a shop for a merchant user */
    @Transactional
    public Shop createShop(Long userId, String name, String description) {
        if (shopRepository.findByUserId(userId).isPresent()) {
            throw new BusinessException(ErrorCode.PARAM_INVALID.getCode(), "您已拥有店铺");
        }
        Shop shop = new Shop();
        shop.setUserId(userId);
        shop.setName(name);
        shop.setDescription(description);
        shop.setStatus(1);
        shopRepository.save(shop);
        return shop;
    }

    /** Update shop info */
    @Transactional
    public Shop updateShop(Long userId, String name, String description, String logo) {
        Shop shop = getShop(userId);
        if (name != null && !name.isBlank()) shop.setName(name);
        if (description != null) shop.setDescription(description);
        if (logo != null) shop.setLogo(logo);
        shopRepository.save(shop);
        return shop;
    }

    /** Get dashboard stats for a merchant */
    public Map<String, Object> getDashboard(Long userId) {
        Shop shop = getShop(userId);
        List<Product> products = productRepository.findByShopId(shop.getId());
        int productCount = products.size();
        int onSaleCount = (int) products.stream().filter(p -> p.getStatus() != null && p.getStatus() == 1).count();
        int totalSales = products.stream().mapToInt(p -> p.getSaleCount() != null ? p.getSaleCount() : 0).sum();

        return Map.of(
            "shopName", shop.getName(),
            "productCount", productCount,
            "onSaleCount", onSaleCount,
            "totalSales", totalSales,
            "todayOrders", 0,
            "todaySales", 0
        );
    }

    /** List merchant's products */
    public List<ProductVO> listProducts(Long userId) {
        Shop shop = getShop(userId);
        List<Product> products = productRepository.findByShopId(shop.getId());
        return products.stream().map(this::toVO).toList();
    }

    /** Create a product for merchant */
    @Transactional
    public ProductVO createProduct(Long userId, String name, Long categoryId, String brand,
                                    String description, String mainImage, List<SkuInput> skus) {
        Shop shop = getShop(userId);
        Product product = new Product();
        product.setName(name);
        product.setCategoryId(categoryId);
        product.setShopId(shop.getId());
        product.setBrand(brand);
        product.setDescription(description);
        product.setMainImage(mainImage);
        product.setStatus(1);
        product.setSaleCount(0);
        productRepository.save(product);

        if (skus != null) {
            for (SkuInput si : skus) {
                Sku sku = new Sku();
                sku.setProductId(product.getId());
                sku.setName(si.name());
                sku.setSpec(JSONUtil.toJsonStr(si.spec()));
                sku.setPrice(si.price());
                skuRepository.save(sku);

                SkuStock stock = new SkuStock();
                stock.setSkuId(sku.getId());
                stock.setStock(si.stock() != null ? si.stock() : 0);
                stock.setLockStock(0);
                skuStockRepository.save(stock);
            }
        }

        return toVO(product);
    }

    /** Update a product (merchant can only update own products) */
    @Transactional
    public ProductVO updateProduct(Long userId, Long productId, String name, Long categoryId,
                                    String brand, String description, String mainImage) {
        Shop shop = getShop(userId);
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        if (!shop.getId().equals(product.getShopId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (name != null) product.setName(name);
        if (categoryId != null) product.setCategoryId(categoryId);
        if (brand != null) product.setBrand(brand);
        if (description != null) product.setDescription(description);
        if (mainImage != null) product.setMainImage(mainImage);
        productRepository.save(product);
        return toVO(product);
    }

    /** Delete a product (merchant can only delete own products) */
    @Transactional
    public void deleteProduct(Long userId, Long productId) {
        Shop shop = getShop(userId);
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        if (!shop.getId().equals(product.getShopId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        // Soft delete SKUs and stocks first
        List<Sku> skus = skuRepository.findByProductId(productId);
        for (Sku sku : skus) {
            skuStockRepository.deleteBySkuId(sku.getId());
        }
        skuRepository.deleteByProductId(productId);
        // Soft delete product via MyBatis-Plus @TableLogic (must use deleteById, not save)
        productRepository.deleteById(productId);
    }

    /** Toggle product on/off sale */
    @Transactional
    public void toggleProduct(Long userId, Long productId) {
        Shop shop = getShop(userId);
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        if (!shop.getId().equals(product.getShopId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        product.setStatus(product.getStatus() == 1 ? 0 : 1);
        productRepository.save(product);
    }

    private ProductVO toVO(Product product) {
        List<Sku> skus = skuRepository.findByProductId(product.getId());
        List<SkuDTO> skuDTOs = new ArrayList<>();
        BigDecimal minPrice = null;
        Long defaultSkuId = null;

        for (Sku sku : skus) {
            Integer stock = skuStockRepository.findBySkuId(sku.getId())
                .map(SkuStock::getStock).orElse(0);
            skuDTOs.add(new SkuDTO(sku.getId(), sku.getName(), sku.getSpecMap(), sku.getPrice(), stock));
            if (minPrice == null || (sku.getPrice() != null && sku.getPrice().compareTo(minPrice) < 0)) {
                minPrice = sku.getPrice();
                defaultSkuId = sku.getId();
            }
        }
        return new ProductVO(
            product.getId(), product.getName(), product.getCategoryId(),
            product.getBrand(), product.getStatus(),
            product.getMainImage(), product.getDescription(),
            minPrice, product.getSaleCount(),
            skuDTOs, product.getCreateTime()
        );
    }

    /** List orders for merchant's shop (filters through order items → products → shop) */
    public List<Order> listOrders(Long userId) {
        Shop shop = getShop(userId);
        List<Product> products = productRepository.findByShopId(shop.getId());
        if (products.isEmpty()) return List.of();
        List<Long> productIds = products.stream().map(Product::getId).toList();
        List<Order> orders = orderMapper.selectList(
            new LambdaQueryWrapper<Order>()
                .inSql(Order::getId,
                    "SELECT DISTINCT oi.order_id FROM order_item oi WHERE oi.sku_id IN " +
                    "(SELECT s.id FROM sku s WHERE s.product_id IN " +
                    "(" + productIds.stream().map(String::valueOf).reduce((a,b) -> a+","+b).orElse("0") + "))")
                .orderByDesc(Order::getCreateTime)
                .last("LIMIT 50")
        );
        // Populate order items for each order
        for (Order order : orders) {
            List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
            order.setItems(items);
        }
        return orders;
    }

    public record SkuInput(String name, Map<String, String> spec, BigDecimal price, Integer stock) {}
}
