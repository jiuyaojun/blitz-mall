package com.blitz.product.application.service;

import cn.hutool.json.JSONUtil;
import com.blitz.common.exception.BusinessException;
import com.blitz.common.exception.ErrorCode;
import com.blitz.product.api.dto.PageResult;
import com.blitz.product.api.dto.ProductDTO;
import com.blitz.product.api.dto.ProductVO;
import com.blitz.product.api.dto.SkuDTO;
import com.blitz.product.domain.model.Category;
import com.blitz.product.domain.model.Product;
import com.blitz.product.domain.model.Sku;
import com.blitz.product.domain.model.SkuStock;
import com.blitz.product.domain.port.CategoryRepository;
import com.blitz.product.domain.port.ProductRepository;
import com.blitz.product.domain.port.SkuRepository;
import com.blitz.product.domain.port.SkuStockRepository;
import com.blitz.product.infrastructure.search.ProductSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductApplicationService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SkuRepository skuRepository;
    private final SkuStockRepository skuStockRepository;
    private final ProductSearchService productSearchService;

    @Transactional
    public ProductVO create(ProductDTO dto) {
        categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND.getCode(), "分类不存在"));

        Product product = toEntity(dto);
        product.setStatus(0); // off shelf by default
        product.setSaleCount(0);
        productRepository.save(product);

        buildSkus(product.getId(), dto.skus());

        return toVO(product);
    }

    @Transactional
    public ProductVO update(Long id, ProductDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND.getCode(), "商品不存在"));

        categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND.getCode(), "分类不存在"));

        product.setName(dto.name());
        product.setCategoryId(dto.categoryId());
        product.setBrand(dto.brand());
        product.setDescription(dto.description());
        product.setMainImage(dto.mainImage());
        product.setImages(JSONUtil.toJsonStr(dto.images()));
        productRepository.save(product);

        // Replace SKUs: delete old ones, create new ones
        replaceSkus(product.getId(), dto.skus());

        return toVO(product);
    }

    @Transactional
    public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND.getCode(), "商品不存在"));
        // Soft delete SKUs and their stocks first
        List<Sku> skus = skuRepository.findByProductId(id);
        for (Sku sku : skus) {
            skuStockRepository.deleteBySkuId(sku.getId());
        }
        skuRepository.deleteByProductId(id);
        // MyBatis-Plus @TableLogic handles soft delete for product
        productRepository.save(product);
        // Trigger delete via mapper for @TableLogic
    }

    public ProductVO getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND.getCode(), "商品不存在"));
        return toVO(product);
    }

    public PageResult<ProductVO> listByCategory(Long categoryId, int page, int size) {
        List<Product> products = productRepository.findByCategoryId(categoryId, page, size);
        long total = productRepository.countByCategoryId(categoryId);
        List<ProductVO> vos = products.stream().map(this::toVO).toList();
        return new PageResult<>(vos, total, page, size);
    }

    public PageResult<ProductVO> search(String keyword, int page, int size) {
        List<Product> products = productSearchService.search(keyword, page, size);
        long total = productSearchService.count(keyword);
        List<ProductVO> vos = products.stream().map(this::toVO).toList();
        return new PageResult<>(vos, total, page, size);
    }

    @Transactional
    public void publish(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND.getCode(), "商品不存在"));
        product.publish();
        productRepository.save(product);
    }

    @Transactional
    public void withdraw(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND.getCode(), "商品不存在"));
        product.withdraw();
        productRepository.save(product);
    }

    // ---- helper methods ----

    private Product toEntity(ProductDTO dto) {
        Product product = new Product();
        product.setName(dto.name());
        product.setCategoryId(dto.categoryId());
        product.setBrand(dto.brand());
        product.setDescription(dto.description());
        product.setMainImage(dto.mainImage());
        product.setImages(JSONUtil.toJsonStr(dto.images()));
        return product;
    }

    private ProductVO toVO(Product product) {
        List<Sku> skus = skuRepository.findByProductId(product.getId());
        List<SkuDTO> skuDTOs = new ArrayList<>();
        for (Sku sku : skus) {
            skuDTOs.add(toSkuDTO(sku));
        }
        return new ProductVO(
            product.getId(),
            product.getName(),
            product.getCategoryId(),
            product.getBrand(),
            product.getStatus(),
            skuDTOs,
            product.getCreateTime()
        );
    }

    private SkuDTO toSkuDTO(Sku sku) {
        Integer stock = skuStockRepository.findBySkuId(sku.getId())
                .map(SkuStock::getStock)
                .orElse(0);
        return new SkuDTO(sku.getName(), sku.getSpecMap(), sku.getPrice(), stock);
    }

    private void buildSkus(Long productId, List<SkuDTO> skuDTOs) {
        if (skuDTOs == null || skuDTOs.isEmpty()) return;
        for (SkuDTO dto : skuDTOs) {
            Sku sku = new Sku();
            sku.setProductId(productId);
            sku.setName(dto.name());
            sku.setSpec(JSONUtil.toJsonStr(dto.spec()));
            sku.setPrice(dto.price());
            skuRepository.save(sku);

            SkuStock stock = new SkuStock();
            stock.setSkuId(sku.getId());
            stock.setStock(dto.stock() != null ? dto.stock() : 0);
            stock.setLockStock(0);
            skuStockRepository.save(stock);
        }
    }

    private void replaceSkus(Long productId, List<SkuDTO> skuDTOs) {
        // Delete existing stocks
        List<Sku> oldSkus = skuRepository.findByProductId(productId);
        for (Sku sku : oldSkus) {
            skuStockRepository.deleteBySkuId(sku.getId());
        }
        // Delete existing SKUs
        skuRepository.deleteByProductId(productId);
        // Create new SKUs
        buildSkus(productId, skuDTOs);
    }
}
