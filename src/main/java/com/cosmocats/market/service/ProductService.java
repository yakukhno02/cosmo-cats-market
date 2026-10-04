package com.cosmocats.market.service;

import com.cosmocats.market.domain.Category;
import com.cosmocats.market.domain.Product;
import com.cosmocats.market.exception.ProductNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    private final List<Product> products = new ArrayList<>();
    private Long nextId = 3L;

    public ProductService() {
        products.add(new Product(
                1L,
                "Galaxy Milk",
                "Cosmic milk for cats",
                15.99,
                new Category(1L, "Food", "Cosmic food")
        ));

        products.add(new Product(
                2L,
                "Star Yarn",
                "Anti-gravity yarn for cosmic cats",
                24.99,
                new Category(2L, "Toys", "Space toys")
        ));
    }

    public List<Product> getAllProducts() {
        return products;
    }

    public Product getProductById(Long id) {
        return products.stream()
                .filter(product -> product.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    public Product createProduct(Product product) {
        product.setId(nextId++);
        products.add(product);

        return product;
    }

    public Product updateProduct(Long id, Product updatedProduct) {
        Product existingProduct = getProductById(id);

        existingProduct.setName(updatedProduct.getName());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setCategory(updatedProduct.getCategory());

        return existingProduct;
    }

    public List<Product> getProductsPage(int page, int size) {
        int fromIndex = page * size;

        if (fromIndex >= products.size()) {
            return List.of();
        }

        int toIndex = Math.min(fromIndex + size, products.size());

        return products.subList(fromIndex, toIndex);
    }

    public long getTotalProductsCount() {
        return products.size();
    }

    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        products.remove(product);
    }
}