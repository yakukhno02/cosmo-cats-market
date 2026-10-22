package com.cosmocats.market.service;

import com.cosmocats.market.domain.Category;
import com.cosmocats.market.domain.Product;
import com.cosmocats.market.exception.ProductNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService();
    }

    @Nested
    @DisplayName("getAllProducts()")
    class GetAllProducts {

        @Test
        @DisplayName("should return all initial products")
        void shouldReturnAllInitialProducts() {
            List<Product> products = productService.getAllProducts();

            assertThat(products).hasSize(2);
            assertThat(products).extracting(Product::getName)
                    .containsExactly("Galaxy Milk", "Star Yarn");
        }
    }

    @Nested
    @DisplayName("getProductById(Long id)")
    class GetProductById {

        @Test
        @DisplayName("should return product when found by existing ID")
        void shouldReturnProductWhenIdExists() {
            Product product = productService.getProductById(1L);

            assertThat(product).isNotNull();
            assertThat(product.getId()).isEqualTo(1L);
            assertThat(product.getName()).isEqualTo("Galaxy Milk");
            assertThat(product.getPrice()).isEqualByComparingTo(new BigDecimal("15.99"));
            assertThat(product.getCategory().getName()).isEqualTo("Food");
        }

        @Test
        @DisplayName("should throw ProductNotFoundException when ID does not exist")
        void shouldThrowExceptionWhenIdDoesNotExist() {
            assertThatThrownBy(() -> productService.getProductById(999L))
                    .isInstanceOf(ProductNotFoundException.class)
                    .hasMessage("Product with id 999 was not found");
        }
    }

    @Nested
    @DisplayName("createProduct(Product product)")
    class CreateProduct {

        @Test
        @DisplayName("should assign nextId and store new product")
        void shouldAssignNextIdAndStoreProduct() {
            Product newProduct = new Product(
                    null,
                    "Comet Laser",
                    "Cosmic laser pointer",
                    new BigDecimal("30.00"),
                    new Category(3L, "Gadgets", "Cosmic gadgets")
            );

            Product created = productService.createProduct(newProduct);

            assertThat(created.getId()).isEqualTo(3L);
            assertThat(productService.getTotalProductsCount()).isEqualTo(3L);
            assertThat(productService.getProductById(3L)).isEqualTo(created);
        }

        @Test
        @DisplayName("should increment ID for each new product")
        void shouldIncrementIdSequentially() {
            Product p1 = productService.createProduct(new Product(null, "Item 1", "Desc", BigDecimal.ONE, null));
            Product p2 = productService.createProduct(new Product(null, "Item 2", "Desc", BigDecimal.TEN, null));

            assertThat(p1.getId()).isEqualTo(3L);
            assertThat(p2.getId()).isEqualTo(4L);
        }
    }

    @Nested
    @DisplayName("updateProduct(Long id, Product updatedProduct)")
    class UpdateProduct {

        @Test
        @DisplayName("should update all fields of existing product")
        void shouldUpdateExistingProduct() {
            Category newCategory = new Category(4L, "Accessories", "Cosmic items");
            Product updatedInfo = new Product(
                    null,
                    "Super Galaxy Milk",
                    "New description",
                    new BigDecimal("19.99"),
                    newCategory
            );

            Product result = productService.updateProduct(1L, updatedInfo);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("Super Galaxy Milk");
            assertThat(result.getDescription()).isEqualTo("New description");
            assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("19.99"));
            assertThat(result.getCategory()).isEqualTo(newCategory);

            Product inService = productService.getProductById(1L);
            assertThat(inService.getName()).isEqualTo("Super Galaxy Milk");
        }

        @Test
        @DisplayName("should throw ProductNotFoundException when updating non-existent product")
        void shouldThrowExceptionWhenUpdatingNonExistentProduct() {
            Product updatedInfo = new Product(
                    null,
                    "Non-existent",
                    "Desc",
                    BigDecimal.TEN,
                    null
            );

            assertThatThrownBy(() -> productService.updateProduct(888L, updatedInfo))
                    .isInstanceOf(ProductNotFoundException.class)
                    .hasMessage("Product with id 888 was not found");
        }
    }

    @Nested
    @DisplayName("getProductsPage(int page, int size)")
    class GetProductsPage {

        @Test
        @DisplayName("should return first page with requested size")
        void shouldReturnFirstPage() {
            List<Product> page = productService.getProductsPage(0, 1);

            assertThat(page).hasSize(1);
            assertThat(page.get(0).getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("should return second page with correct item")
        void shouldReturnSecondPage() {
            List<Product> page = productService.getProductsPage(1, 1);

            assertThat(page).hasSize(1);
            assertThat(page.get(0).getId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("should return empty list when offset exceeds products count")
        void shouldReturnEmptyListWhenOffsetExceedsSize() {
            List<Product> page = productService.getProductsPage(5, 10);

            assertThat(page).isEmpty();
        }

        @Test
        @DisplayName("should return partial page when size exceeds remaining elements")
        void shouldReturnPartialPageWhenSizeExceedsRemaining() {
            List<Product> page = productService.getProductsPage(0, 10);

            assertThat(page).hasSize(2);
        }
    }

    @Nested
    @DisplayName("getTotalProductsCount()")
    class GetTotalProductsCount {

        @Test
        @DisplayName("should return accurate count of stored products")
        void shouldReturnCorrectTotalCount() {
            assertThat(productService.getTotalProductsCount()).isEqualTo(2L);

            productService.createProduct(new Product(null, "Comet Paws", "Desc", BigDecimal.ONE, null));
            assertThat(productService.getTotalProductsCount()).isEqualTo(3L);
        }
    }

    @Nested
    @DisplayName("deleteProduct(Long id)")
    class DeleteProduct {

        @Test
        @DisplayName("should remove existing product from list")
        void shouldDeleteExistingProduct() {
            productService.deleteProduct(1L);

            assertThat(productService.getTotalProductsCount()).isEqualTo(1L);
            assertThatThrownBy(() -> productService.getProductById(1L))
                    .isInstanceOf(ProductNotFoundException.class);
        }

        @Test
        @DisplayName("should throw ProductNotFoundException when deleting non-existent product")
        void shouldThrowExceptionWhenDeletingNonExistentProduct() {
            assertThatThrownBy(() -> productService.deleteProduct(777L))
                    .isInstanceOf(ProductNotFoundException.class)
                    .hasMessage("Product with id 777 was not found");
        }
    }
}
