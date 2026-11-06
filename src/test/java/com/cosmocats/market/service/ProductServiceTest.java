package com.cosmocats.market.service;

import com.cosmocats.market.domain.Category;
import com.cosmocats.market.domain.Product;
import com.cosmocats.market.exception.ProductNotFoundException;
import com.cosmocats.market.repository.CategoryRepository;
import com.cosmocats.market.repository.ProductRepository;
import com.cosmocats.market.repository.entity.CategoryEntity;
import com.cosmocats.market.repository.entity.ProductEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    @Nested
    @DisplayName("getAllProducts()")
    class GetAllProducts {

        @Test
        @DisplayName("should return all products mapped from repository")
        void shouldReturnAllProducts() {
            CategoryEntity catEntity = CategoryEntity.builder().id(1L).name("Food").description("Food desc").build();
            ProductEntity entity1 = ProductEntity.builder().id(1L).name("Galaxy Milk").price(new BigDecimal("15.99")).category(catEntity).build();
            ProductEntity entity2 = ProductEntity.builder().id(2L).name("Star Yarn").price(new BigDecimal("24.99")).category(null).build();

            when(productRepository.findAll()).thenReturn(List.of(entity1, entity2));

            List<Product> products = productService.getAllProducts();

            assertThat(products).hasSize(2);
            assertThat(products.get(0).getName()).isEqualTo("Galaxy Milk");
            assertThat(products.get(0).getCategory().getName()).isEqualTo("Food");
            assertThat(products.get(1).getName()).isEqualTo("Star Yarn");
            assertThat(products.get(1).getCategory()).isNull();
        }
    }

    @Nested
    @DisplayName("getProductById(Long id)")
    class GetProductById {

        @Test
        @DisplayName("should return product when found by existing ID")
        void shouldReturnProductWhenIdExists() {
            CategoryEntity catEntity = CategoryEntity.builder().id(1L).name("Food").description("Food desc").build();
            ProductEntity entity = ProductEntity.builder().id(1L).name("Galaxy Milk").price(new BigDecimal("15.99")).category(catEntity).build();

            when(productRepository.findById(1L)).thenReturn(Optional.of(entity));

            Product product = productService.getProductById(1L);

            assertThat(product.getId()).isEqualTo(1L);
            assertThat(product.getName()).isEqualTo("Galaxy Milk");
            assertThat(product.getPrice()).isEqualByComparingTo(new BigDecimal("15.99"));
            assertThat(product.getCategory().getName()).isEqualTo("Food");
        }

        @Test
        @DisplayName("should throw ProductNotFoundException when ID does not exist")
        void shouldThrowExceptionWhenIdDoesNotExist() {
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.getProductById(999L))
                    .isInstanceOf(ProductNotFoundException.class)
                    .hasMessage("Product with id 999 was not found");
        }
    }

    @Nested
    @DisplayName("createProduct(Product product)")
    class CreateProduct {

        @Test
        @DisplayName("should create product with existing category")
        void shouldCreateProductWithExistingCategory() {
            Category category = new Category(null, "Food", "Food desc");
            Product input = new Product(null, "Comet Fish", "Tasty", new BigDecimal("12.50"), category);

            CategoryEntity existingCat = CategoryEntity.builder().id(1L).name("Food").description("Food desc").build();
            when(categoryRepository.findByName("Food")).thenReturn(Optional.of(existingCat));

            ProductEntity savedEntity = ProductEntity.builder()
                    .id(10L)
                    .name("Comet Fish")
                    .description("Tasty")
                    .price(new BigDecimal("12.50"))
                    .category(existingCat)
                    .build();
            when(productRepository.save(any(ProductEntity.class))).thenReturn(savedEntity);

            Product result = productService.createProduct(input);

            assertThat(result.getId()).isEqualTo(10L);
            assertThat(result.getName()).isEqualTo("Comet Fish");
            assertThat(result.getCategory().getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("should create product with new category when category does not exist")
        void shouldCreateProductWithNewCategory() {
            Category category = new Category(null, "NewCategory", "New desc");
            Product input = new Product(null, "New Item", "Desc", BigDecimal.ONE, category);

            CategoryEntity createdCat = CategoryEntity.builder().id(5L).name("NewCategory").description("New desc").build();
            when(categoryRepository.findByName("NewCategory")).thenReturn(Optional.empty());
            when(categoryRepository.save(any(CategoryEntity.class))).thenReturn(createdCat);

            ProductEntity savedEntity = ProductEntity.builder()
                    .id(20L)
                    .name("New Item")
                    .description("Desc")
                    .price(BigDecimal.ONE)
                    .category(createdCat)
                    .build();
            when(productRepository.save(any(ProductEntity.class))).thenReturn(savedEntity);

            Product result = productService.createProduct(input);

            assertThat(result.getId()).isEqualTo(20L);
            assertThat(result.getCategory().getName()).isEqualTo("NewCategory");
        }

        @Test
        @DisplayName("should create product without category when category is null")
        void shouldCreateProductWithoutCategory() {
            Product input = new Product(null, "No Cat Item", "Desc", BigDecimal.TEN, null);

            ProductEntity savedEntity = ProductEntity.builder()
                    .id(30L)
                    .name("No Cat Item")
                    .description("Desc")
                    .price(BigDecimal.TEN)
                    .category(null)
                    .build();
            when(productRepository.save(any(ProductEntity.class))).thenReturn(savedEntity);

            Product result = productService.createProduct(input);

            assertThat(result.getId()).isEqualTo(30L);
            assertThat(result.getCategory()).isNull();
        }
    }

    @Nested
    @DisplayName("updateProduct(Long id, Product updatedProduct)")
    class UpdateProduct {

        @Test
        @DisplayName("should update all fields of existing product")
        void shouldUpdateExistingProduct() {
            Category newCategory = new Category(null, "Toys", "Toy desc");
            Product updatedInfo = new Product(null, "Super Yarn", "Better yarn", new BigDecimal("29.99"), newCategory);

            CategoryEntity catEntity = CategoryEntity.builder().id(2L).name("Toys").description("Toy desc").build();
            when(categoryRepository.findByName("Toys")).thenReturn(Optional.of(catEntity));

            ProductEntity existingEntity = ProductEntity.builder()
                    .id(1L)
                    .name("Old Yarn")
                    .description("Old")
                    .price(new BigDecimal("10.00"))
                    .category(null)
                    .build();
            when(productRepository.findById(1L)).thenReturn(Optional.of(existingEntity));

            when(productRepository.save(any(ProductEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Product result = productService.updateProduct(1L, updatedInfo);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("Super Yarn");
            assertThat(result.getDescription()).isEqualTo("Better yarn");
            assertThat(result.getPrice()).isEqualByComparingTo("29.99");
            assertThat(result.getCategory().getName()).isEqualTo("Toys");
        }

        @Test
        @DisplayName("should throw ProductNotFoundException when updating non-existent product")
        void shouldThrowExceptionWhenUpdatingNonExistentProduct() {
            when(productRepository.findById(888L)).thenReturn(Optional.empty());

            Product updatedInfo = new Product(null, "Non-existent", "Desc", BigDecimal.TEN, null);

            assertThatThrownBy(() -> productService.updateProduct(888L, updatedInfo))
                    .isInstanceOf(ProductNotFoundException.class)
                    .hasMessage("Product with id 888 was not found");
        }
    }

    @Nested
    @DisplayName("getProductsPage(int page, int size)")
    class GetProductsPage {

        @Test
        @DisplayName("should return requested page from repository")
        void shouldReturnRequestedPage() {
            ProductEntity entity = ProductEntity.builder().id(1L).name("Galaxy Milk").price(new BigDecimal("15.99")).build();
            when(productRepository.findAll(PageRequest.of(0, 10))).thenReturn(new PageImpl<>(List.of(entity)));

            List<Product> page = productService.getProductsPage(0, 10);

            assertThat(page).hasSize(1);
            assertThat(page.get(0).getName()).isEqualTo("Galaxy Milk");
        }
    }

    @Nested
    @DisplayName("getTotalProductsCount()")
    class GetTotalProductsCount {

        @Test
        @DisplayName("should return accurate count of stored products from repository")
        void shouldReturnCorrectTotalCount() {
            when(productRepository.count()).thenReturn(42L);

            assertThat(productService.getTotalProductsCount()).isEqualTo(42L);
        }
    }

    @Nested
    @DisplayName("deleteProduct(Long id)")
    class DeleteProduct {

        @Test
        @DisplayName("should delete existing product from repository")
        void shouldDeleteExistingProduct() {
            when(productRepository.existsById(1L)).thenReturn(true);

            productService.deleteProduct(1L);

            verify(productRepository).deleteById(1L);
        }

        @Test
        @DisplayName("should throw ProductNotFoundException when deleting non-existent product")
        void shouldThrowExceptionWhenDeletingNonExistentProduct() {
            when(productRepository.existsById(777L)).thenReturn(false);

            assertThatThrownBy(() -> productService.deleteProduct(777L))
                    .isInstanceOf(ProductNotFoundException.class)
                    .hasMessage("Product with id 777 was not found");
        }
    }
}
