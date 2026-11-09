package com.cosmocats.market.repository;

import com.cosmocats.market.AbstractIt;
import com.cosmocats.market.repository.entity.CategoryEntity;
import com.cosmocats.market.repository.entity.OrderEntity;
import com.cosmocats.market.repository.entity.OrderItemEntity;
import com.cosmocats.market.repository.entity.ProductEntity;
import com.cosmocats.market.repository.projection.TopSellingProductProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JPA Repositories Integration Tests (PostgreSQL Testcontainers)")
class RepositoryIntegrationIT extends AbstractIt {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @BeforeEach
    void setUp() {
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Nested
    @DisplayName("CategoryRepository CRUD Tests")
    class CategoryRepositoryTests {

        @Test
        @DisplayName("should save and find category by name")
        void shouldSaveAndFindCategoryByName() {
            CategoryEntity category = CategoryEntity.builder()
                    .name("Space Food")
                    .description("Tasty meals for cosmic cats")
                    .build();

            CategoryEntity saved = categoryRepository.save(category);

            assertThat(saved.getId()).isNotNull();
            Optional<CategoryEntity> found = categoryRepository.findByName("Space Food");
            assertThat(found).isPresent();
            assertThat(found.get().getDescription()).isEqualTo("Tasty meals for cosmic cats");
        }

        @Test
        @DisplayName("should enforce unique constraint on category name")
        void shouldEnforceUniqueConstraintOnCategoryName() {
            categoryRepository.save(CategoryEntity.builder().name("Cosmic Toys").build());

            CategoryEntity duplicate = CategoryEntity.builder().name("Cosmic Toys").build();

            assertThatThrownBy(() -> categoryRepository.saveAndFlush(duplicate))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    @Nested
    @DisplayName("ProductRepository CRUD and Constraint Tests")
    class ProductRepositoryTests {

        @Test
        @DisplayName("should save, find, and update product")
        void shouldSaveFindAndUpdateProduct() {
            CategoryEntity category = categoryRepository.save(
                    CategoryEntity.builder().name("Navigation").description("Star maps").build()
            );

            ProductEntity product = ProductEntity.builder()
                    .name("Quantum Compass")
                    .description("Find your way in any galaxy")
                    .price(new BigDecimal("199.99"))
                    .category(category)
                    .build();

            ProductEntity saved = productRepository.save(product);
            assertThat(saved.getId()).isNotNull();

            saved.setPrice(new BigDecimal("179.99"));
            ProductEntity updated = productRepository.save(saved);
            assertThat(updated.getPrice()).isEqualByComparingTo("179.99");

            List<ProductEntity> byCategory = productRepository.findByCategoryId(category.getId());
            assertThat(byCategory).hasSize(1);
            assertThat(byCategory.getFirst().getName()).isEqualTo("Quantum Compass");
        }

        @Test
        @DisplayName("should enforce unique constraint on (name, category_id)")
        void shouldEnforceUniqueConstraintOnNameAndCategory() {
            CategoryEntity category = categoryRepository.save(
                    CategoryEntity.builder().name("Armor").description("Space suits").build()
            );

            ProductEntity p1 = ProductEntity.builder()
                    .name("Nano Helmet")
                    .price(new BigDecimal("99.00"))
                    .category(category)
                    .build();
            productRepository.saveAndFlush(p1);

            ProductEntity duplicate = ProductEntity.builder()
                    .name("Nano Helmet")
                    .price(new BigDecimal("120.00"))
                    .category(category)
                    .build();

            assertThatThrownBy(() -> productRepository.saveAndFlush(duplicate))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    @Nested
    @DisplayName("Natural ID Tests on OrderRepository")
    class NaturalIdTests {

        @Test
        @DisplayName("should find and delete order by Natural ID (orderNumber)")
        void shouldFindAndDeleteOrderByNaturalId() {
            String naturalId = "ORD-" + UUID.randomUUID();

            OrderEntity order = OrderEntity.builder()
                    .orderNumber(naturalId)
                    .customerEmail("cat.captain@cosmo.cat")
                    .totalPrice(new BigDecimal("350.00"))
                    .status("PAID")
                    .build();

            orderRepository.save(order);

            Optional<OrderEntity> loaded = orderRepository.findByNaturalId(naturalId);
            assertThat(loaded).isPresent();
            assertThat(loaded.get().getCustomerEmail()).isEqualTo("cat.captain@cosmo.cat");
            assertThat(loaded.get().getTotalPrice()).isEqualByComparingTo("350.00");

            orderRepository.deleteByNaturalId(naturalId);

            Optional<OrderEntity> afterDelete = orderRepository.findByNaturalId(naturalId);
            assertThat(afterDelete).isEmpty();
        }

        @Test
        @DisplayName("should enforce unique constraint on order Natural ID")
        void shouldEnforceUniqueConstraintOnOrderNumber() {
            String naturalId = "ORD-UNIQUE-101";

            OrderEntity order1 = OrderEntity.builder()
                    .orderNumber(naturalId)
                    .customerEmail("felix@galaxy.org")
                    .totalPrice(new BigDecimal("50.00"))
                    .status("CREATED")
                    .build();
            orderRepository.saveAndFlush(order1);

            OrderEntity duplicate = OrderEntity.builder()
                    .orderNumber(naturalId)
                    .customerEmail("tom@galaxy.org")
                    .totalPrice(new BigDecimal("75.00"))
                    .status("CREATED")
                    .build();

            assertThatThrownBy(() -> orderRepository.saveAndFlush(duplicate))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    @Nested
    @DisplayName("Projections & JPQL Custom Queries (Intergalactic Committee Report)")
    class ProjectionReportTests {

        @Test
        @DisplayName("should report top selling products ordered by quantity sold and filter cancelled orders")
        void shouldReportTopSellingProductsWithFilterAndOrdering() {
            CategoryEntity category = categoryRepository.save(
                    CategoryEntity.builder().name("Gadgets").build()
            );

            ProductEntity p1 = productRepository.save(
                    ProductEntity.builder().name("Laser Pointer 3000").price(new BigDecimal("15.00")).category(category).build()
            );
            ProductEntity p2 = productRepository.save(
                    ProductEntity.builder().name("Antigravity Scratch Post").price(new BigDecimal("80.00")).category(category).build()
            );
            ProductEntity p3 = productRepository.save(
                    ProductEntity.builder().name("Cosmic Nip").price(new BigDecimal("10.00")).category(category).build()
            );

            // Valid order 1
            OrderEntity order1 = OrderEntity.builder()
                    .orderNumber("ORD-REPORT-001")
                    .customerEmail("luna@star.org")
                    .totalPrice(new BigDecimal("130.00"))
                    .status("COMPLETED")
                    .build();
            order1.getItems().add(OrderItemEntity.builder().order(order1).product(p1).quantity(5).price(p1.getPrice()).build());
            order1.getItems().add(OrderItemEntity.builder().order(order1).product(p2).quantity(1).price(p2.getPrice()).build());
            orderRepository.save(order1);

            // Valid order 2 (more Laser Pointers and some Cosmic Nip)
            OrderEntity order2 = OrderEntity.builder()
                    .orderNumber("ORD-REPORT-002")
                    .customerEmail("orion@star.org")
                    .totalPrice(new BigDecimal("70.00"))
                    .status("PAID")
                    .build();
            order2.getItems().add(OrderItemEntity.builder().order(order2).product(p1).quantity(2).price(p1.getPrice()).build());
            order2.getItems().add(OrderItemEntity.builder().order(order2).product(p3).quantity(4).price(p3.getPrice()).build());
            orderRepository.save(order2);

            // Cancelled order (should be excluded by WHERE clause)
            OrderEntity cancelledOrder = OrderEntity.builder()
                    .orderNumber("ORD-CANCELLED-999")
                    .customerEmail("bad.actor@star.org")
                    .totalPrice(new BigDecimal("1000.00"))
                    .status("CANCELLED")
                    .build();
            cancelledOrder.getItems().add(OrderItemEntity.builder().order(cancelledOrder).product(p3).quantity(100).price(p3.getPrice()).build());
            orderRepository.save(cancelledOrder);

            // Execute custom JPQL projection query
            List<TopSellingProductProjection> report = orderRepository.findTopSellingProducts();

            assertThat(report).hasSize(3);

            // 1st: Laser Pointer: 5 + 2 = 7 units
            assertThat(report.get(0).getProductId()).isEqualTo(p1.getId());
            assertThat(report.get(0).getProductName()).isEqualTo("Laser Pointer 3000");
            assertThat(report.get(0).getTotalQuantitySold()).isEqualTo(7L);

            // 2nd: Cosmic Nip: 4 units (the 100 from cancelled order were filtered out!)
            assertThat(report.get(1).getProductId()).isEqualTo(p3.getId());
            assertThat(report.get(1).getProductName()).isEqualTo("Cosmic Nip");
            assertThat(report.get(1).getTotalQuantitySold()).isEqualTo(4L);

            // 3rd: Antigravity Scratch Post: 1 unit
            assertThat(report.get(2).getProductId()).isEqualTo(p2.getId());
            assertThat(report.get(2).getProductName()).isEqualTo("Antigravity Scratch Post");
            assertThat(report.get(2).getTotalQuantitySold()).isEqualTo(1L);
        }
    }
}
