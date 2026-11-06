package com.cosmocats.market.service;

import com.cosmocats.market.domain.Order;
import com.cosmocats.market.domain.OrderStatus;
import com.cosmocats.market.exception.ProductNotFoundException;
import com.cosmocats.market.repository.OrderRepository;
import com.cosmocats.market.repository.ProductRepository;
import com.cosmocats.market.repository.entity.OrderEntity;
import com.cosmocats.market.repository.entity.OrderItemEntity;
import com.cosmocats.market.repository.entity.ProductEntity;
import com.cosmocats.market.repository.projection.TopSellingProductProjection;
import com.cosmocats.market.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Nested
    @DisplayName("placeOrder(String customerEmail, List<Long> productIds)")
    class PlaceOrderTests {

        @Test
        @DisplayName("should place order successfully and calculate total price")
        void shouldPlaceOrderSuccessfully() {
            ProductEntity p1 = ProductEntity.builder()
                    .id(1L)
                    .name("Laser Toy")
                    .description("Fun laser")
                    .price(new BigDecimal("20.00"))
                    .build();
            ProductEntity p2 = ProductEntity.builder()
                    .id(2L)
                    .name("Space Salmon")
                    .description("Tasty fish")
                    .price(new BigDecimal("35.00"))
                    .build();

            when(productRepository.findById(1L)).thenReturn(Optional.of(p1));
            when(productRepository.findById(2L)).thenReturn(Optional.of(p2));

            when(orderRepository.save(any(OrderEntity.class))).thenAnswer(invocation -> {
                OrderEntity entity = invocation.getArgument(0);
                return entity.toBuilder().id(100L).build();
            });

            Order result = orderService.placeOrder("cat@galaxy.com", List.of(1L, 2L));

            assertThat(result.getId()).isEqualTo(100L);
            assertThat(result.getTotalPrice()).isEqualByComparingTo(new BigDecimal("55.00"));
            assertThat(result.getStatus()).isEqualTo(OrderStatus.CREATED);
            assertThat(result.getProducts()).hasSize(2);
            assertThat(result.getProducts()).extracting("name").containsExactly("Laser Toy", "Space Salmon");
        }

        @Test
        @DisplayName("should group multiple quantities of the same product to respect unique constraint")
        void shouldGroupMultipleQuantitiesOfSameProduct() {
            ProductEntity p1 = ProductEntity.builder()
                    .id(1L)
                    .name("Laser Toy")
                    .price(new BigDecimal("20.00"))
                    .build();

            when(productRepository.findById(1L)).thenReturn(Optional.of(p1));
            when(orderRepository.save(any(OrderEntity.class))).thenAnswer(invocation -> {
                OrderEntity entity = invocation.getArgument(0);
                assertThat(entity.getItems()).hasSize(1);
                assertThat(entity.getItems().getFirst().getQuantity()).isEqualTo(3);
                return entity.toBuilder().id(101L).build();
            });

            Order result = orderService.placeOrder("cat@galaxy.com", List.of(1L, 1L, 1L));

            assertThat(result.getId()).isEqualTo(101L);
            assertThat(result.getTotalPrice()).isEqualByComparingTo(new BigDecimal("60.00"));
            assertThat(result.getProducts()).hasSize(3);
        }

        @Test
        @DisplayName("should throw ProductNotFoundException when any product does not exist")
        void shouldThrowWhenProductNotFound() {
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.placeOrder("cat@galaxy.com", List.of(999L)))
                    .isInstanceOf(ProductNotFoundException.class)
                    .hasMessage("Product with id 999 was not found");
        }
    }

    @Nested
    @DisplayName("getOrderByNaturalId(String orderNumber)")
    class GetOrderByNaturalIdTests {

        @Test
        @DisplayName("should return domain Order when found by natural ID")
        void shouldReturnOrderWhenFound() {
            String naturalId = "ORD-TEST-123";
            ProductEntity productEntity = ProductEntity.builder()
                    .id(5L)
                    .name("Cosmic Bed")
                    .description("Warm bed")
                    .price(new BigDecimal("99.99"))
                    .build();

            OrderEntity orderEntity = OrderEntity.builder()
                    .id(42L)
                    .orderNumber(naturalId)
                    .customerEmail("captain@galaxy.com")
                    .totalPrice(new BigDecimal("99.99"))
                    .status("PAID")
                    .items(new ArrayList<>())
                    .build();

            OrderItemEntity item = OrderItemEntity.builder()
                    .order(orderEntity)
                    .product(productEntity)
                    .quantity(1)
                    .price(new BigDecimal("99.99"))
                    .build();
            orderEntity.getItems().add(item);

            when(orderRepository.findByNaturalId(naturalId)).thenReturn(Optional.of(orderEntity));

            Order result = orderService.getOrderByNaturalId(naturalId);

            assertThat(result.getId()).isEqualTo(42L);
            assertThat(result.getStatus()).isEqualTo(OrderStatus.PAID);
            assertThat(result.getTotalPrice()).isEqualByComparingTo("99.99");
            assertThat(result.getProducts()).hasSize(1);
            assertThat(result.getProducts().getFirst().getName()).isEqualTo("Cosmic Bed");
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when order not found by natural ID")
        void shouldThrowWhenNotFound() {
            when(orderRepository.findByNaturalId("ORD-UNKNOWN")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.getOrderByNaturalId("ORD-UNKNOWN"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Order not found: ORD-UNKNOWN");
        }
    }

    @Nested
    @DisplayName("cancelOrder(String orderNumber)")
    class CancelOrderTests {

        @Test
        @DisplayName("should cancel existing order by setting status CANCELLED")
        void shouldCancelOrderSuccessfully() {
            String naturalId = "ORD-CANCEL-1";
            OrderEntity orderEntity = OrderEntity.builder()
                    .id(10L)
                    .orderNumber(naturalId)
                    .status("CREATED")
                    .build();

            when(orderRepository.findByNaturalId(naturalId)).thenReturn(Optional.of(orderEntity));

            orderService.cancelOrder(naturalId);

            assertThat(orderEntity.getStatus()).isEqualTo(OrderStatus.CANCELLED.name());
            verify(orderRepository).save(orderEntity);
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when cancelling non-existent order")
        void shouldThrowWhenCancellingNonExistentOrder() {
            when(orderRepository.findByNaturalId("ORD-NOT-FOUND")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.cancelOrder("ORD-NOT-FOUND"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Order not found: ORD-NOT-FOUND");
        }
    }

    @Nested
    @DisplayName("getTopSellingProductsReport()")
    class ReportTests {

        @Test
        @DisplayName("should delegate to repository projection query")
        void shouldReturnTopSellingReport() {
            TopSellingProductProjection p1 = mock(TopSellingProductProjection.class);
            when(p1.getProductId()).thenReturn(1L);
            when(p1.getProductName()).thenReturn("Star Yarn");
            when(p1.getTotalQuantitySold()).thenReturn(42L);

            when(orderRepository.findTopSellingProducts()).thenReturn(List.of(p1));

            List<TopSellingProductProjection> report = orderService.getTopSellingProductsReport();

            assertThat(report).hasSize(1);
            assertThat(report.getFirst().getProductId()).isEqualTo(1L);
            assertThat(report.getFirst().getProductName()).isEqualTo("Star Yarn");
            assertThat(report.getFirst().getTotalQuantitySold()).isEqualTo(42L);
        }
    }
}
