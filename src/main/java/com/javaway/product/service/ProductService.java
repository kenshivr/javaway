package com.javaway.product.service;

import com.javaway.product.dto.ProductImageResponse;
import com.javaway.product.dto.ProductRequest;
import com.javaway.product.dto.ProductResponse;
import com.javaway.product.mapper.ProductMapper;
import com.javaway.product.model.Product;
import com.javaway.product.model.ProductImage;
import com.javaway.product.repository.CategoryRepository;
import com.javaway.product.repository.ProductImageRepository;
import com.javaway.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final CloudinaryService cloudinaryService;

    public Page<ProductResponse> getAll(Pageable pageable) {
        return productRepository.findAll(pageable).map(productMapper::toResponse);
    }

    public Page<ProductResponse> search(String name, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCase(name, pageable).map(productMapper::toResponse);
    }

    public Page<ProductResponse> getByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable).map(productMapper::toResponse);
    }

    public ProductResponse getById(Long id) {
        return productMapper.toResponse(findById(id));
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = Product.builder()
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .stock(request.stock())
                .category(request.categoryId() != null
                        ? categoryRepository.findById(request.categoryId())
                            .orElseThrow(() -> new IllegalArgumentException("Category not found"))
                        : null)
                .build();
        return productMapper.toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findById(id);
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setCategory(request.categoryId() != null
                ? categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"))
                : null);
        return productMapper.toResponse(productRepository.save(product));
    }

    @Transactional
    public void delete(Long id) {
        Product product = findById(id);
        product.getImages().forEach(img -> cloudinaryService.delete(img.getPublicId()));
        productRepository.delete(product);
    }

    @Transactional
    public ProductImageResponse uploadImage(Long productId, MultipartFile file) {
        Product product = findById(productId);
        Map<String, String> uploaded = cloudinaryService.upload(file);

        boolean isPrimary = product.getImages().isEmpty();
        ProductImage image = ProductImage.builder()
                .product(product)
                .url(uploaded.get("url"))
                .publicId(uploaded.get("publicId"))
                .isPrimary(isPrimary)
                .build();

        return productMapper.toImageResponse(productImageRepository.save(image));
    }

    @Transactional
    public void deleteImage(Long productId, Long imageId) {
        ProductImage image = productImageRepository.findByIdAndProductId(imageId, productId)
                .orElseThrow(() -> new IllegalArgumentException("Image not found"));
        cloudinaryService.delete(image.getPublicId());
        productImageRepository.delete(image);
    }

    private Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
    }
}
