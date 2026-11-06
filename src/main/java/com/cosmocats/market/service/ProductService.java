package com.cosmocats.market.service;

import com.cosmocats.market.domain.Category;
import com.cosmocats.market.domain.Product;
import com.cosmocats.market.exception.ProductNotFoundException;
import com.cosmocats.market.repository.CategoryRepository;
import com.cosmocats.market.repository.ProductRepository;
import com.cosmocats.market.repository.entity.CategoryEntity;
import com.cosmocats.market.repository.entity.ProductEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .map(this::toDomain)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Transactional
    public Product createProduct(Product product) {
        CategoryEntity categoryEntity = resolveCategory(product.getCategory());

        ProductEntity entity = ProductEntity.builder()
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(categoryEntity)
                .build();

        ProductEntity saved = productRepository.save(entity);
        return toDomain(saved);
    }

    @Transactional
    public Product updateProduct(Long id, Product updatedProduct) {
        ProductEntity existingEntity = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        CategoryEntity categoryEntity = resolveCategory(updatedProduct.getCategory());

        existingEntity.setName(updatedProduct.getName());
        existingEntity.setDescription(updatedProduct.getDescription());
        existingEntity.setPrice(updatedProduct.getPrice());
        existingEntity.setCategory(categoryEntity);

        ProductEntity saved = productRepository.save(existingEntity);
        return toDomain(saved);
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsPage(int page, int size) {
        return productRepository.findAll(PageRequest.of(page, size)).getContent().stream()
                .map(this::toDomain)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getTotalProductsCount() {
        return productRepository.count();
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);
    }

    private CategoryEntity resolveCategory(Category category) {
        if (category == null || category.getName() == null) {
            return null;
        }
        return categoryRepository.findByName(category.getName())
                .orElseGet(() -> categoryRepository.save(
                        CategoryEntity.builder()
                                .name(category.getName())
                                .description(category.getDescription())
                                .build()
                ));
    }

    private Product toDomain(ProductEntity entity) {
        Category category = null;
        if (entity.getCategory() != null) {
            category = new Category(
                    entity.getCategory().getId(),
                    entity.getCategory().getName(),
                    entity.getCategory().getDescription()
            );
        }
        return new Product(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getPrice(),
                category
        );
    }
}
