package com.cosmocats.market.domain;

import com.cosmocats.market.dto.ProductPageDto;
import com.cosmocats.market.dto.ProductRequestDto;
import com.cosmocats.market.dto.ProductResponseDto;
import com.cosmocats.market.exception.ErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Domain and DTO Unit Tests")
class DomainAndDtoTest {

    @Test
    @DisplayName("should test Cart model creation and getters/setters")
    void testCart() {
        Cart defaultCart = new Cart();
        assertThat(defaultCart.getId()).isNull();
        assertThat(defaultCart.getProducts()).isNotNull();

        Product product = new Product(1L, "Galaxy Milk", "Desc", BigDecimal.TEN, null);
        List<Product> products = new ArrayList<>(List.of(product));
        Cart cart = new Cart(10L, products);

        assertThat(cart.getId()).isEqualTo(10L);
        assertThat(cart.getProducts()).containsExactly(product);

        cart.setId(20L);
        cart.setProducts(List.of());
        assertThat(cart.getId()).isEqualTo(20L);
        assertThat(cart.getProducts()).isEmpty();
    }

    @Test
    @DisplayName("should test Order model and OrderStatus enum")
    void testOrderAndOrderStatus() {
        Order defaultOrder = new Order();
        assertThat(defaultOrder.getId()).isNull();

        Order order = new Order(1L, List.of(), new BigDecimal("100.00"), OrderStatus.CREATED);
        assertThat(order.getId()).isEqualTo(1L);
        assertThat(order.getProducts()).isEmpty();
        assertThat(order.getTotalPrice()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);

        order.setId(2L);
        order.setProducts(List.of(new Product()));
        order.setTotalPrice(BigDecimal.ONE);
        order.setStatus(OrderStatus.PAID);

        assertThat(order.getId()).isEqualTo(2L);
        assertThat(order.getProducts()).hasSize(1);
        assertThat(order.getTotalPrice()).isEqualTo(BigDecimal.ONE);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);

        assertThat(OrderStatus.valueOf("SHIPPED")).isEqualTo(OrderStatus.SHIPPED);
        assertThat(OrderStatus.values()).contains(OrderStatus.COMPLETED, OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("should test Category model creation and getters/setters")
    void testCategory() {
        Category defaultCategory = new Category();
        assertThat(defaultCategory.getId()).isNull();

        Category category = new Category(5L, "Cosmic Food", "Food for cats");
        assertThat(category.getId()).isEqualTo(5L);
        assertThat(category.getName()).isEqualTo("Cosmic Food");
        assertThat(category.getDescription()).isEqualTo("Food for cats");

        category.setId(6L);
        category.setName("Space Toys");
        category.setDescription("Toys for cats");

        assertThat(category.getId()).isEqualTo(6L);
        assertThat(category.getName()).isEqualTo("Space Toys");
        assertThat(category.getDescription()).isEqualTo("Toys for cats");
    }

    @Test
    @DisplayName("should test Product model creation and getters/setters")
    void testProduct() {
        Product defaultProduct = new Product();
        assertThat(defaultProduct.getId()).isNull();

        Category cat = new Category(1L, "Food", "Food desc");
        Product product = new Product(1L, "Star Yarn", "Desc", BigDecimal.TEN, cat);

        assertThat(product.getId()).isEqualTo(1L);
        assertThat(product.getName()).isEqualTo("Star Yarn");
        assertThat(product.getDescription()).isEqualTo("Desc");
        assertThat(product.getPrice()).isEqualTo(BigDecimal.TEN);
        assertThat(product.getCategory()).isEqualTo(cat);

        product.setId(2L);
        product.setName("Galaxy Ball");
        product.setDescription("New Desc");
        product.setPrice(BigDecimal.TWO);
        product.setCategory(null);

        assertThat(product.getId()).isEqualTo(2L);
        assertThat(product.getName()).isEqualTo("Galaxy Ball");
        assertThat(product.getDescription()).isEqualTo("New Desc");
        assertThat(product.getPrice()).isEqualTo(BigDecimal.TWO);
        assertThat(product.getCategory()).isNull();
    }

    @Test
    @DisplayName("should test ProductRequestDto creation and getters/setters")
    void testProductRequestDto() {
        ProductRequestDto defaultDto = new ProductRequestDto();
        assertThat(defaultDto.getName()).isNull();

        ProductRequestDto dto = new ProductRequestDto("Comet Tail", "Desc", BigDecimal.TEN, "Toys");
        assertThat(dto.getName()).isEqualTo("Comet Tail");
        assertThat(dto.getDescription()).isEqualTo("Desc");
        assertThat(dto.getPrice()).isEqualTo(BigDecimal.TEN);
        assertThat(dto.getCategory()).isEqualTo("Toys");

        dto.setName("New Name");
        dto.setDescription("New Desc");
        dto.setPrice(BigDecimal.ONE);
        dto.setCategory("Food");

        assertThat(dto.getName()).isEqualTo("New Name");
        assertThat(dto.getDescription()).isEqualTo("New Desc");
        assertThat(dto.getPrice()).isEqualTo(BigDecimal.ONE);
        assertThat(dto.getCategory()).isEqualTo("Food");
    }

    @Test
    @DisplayName("should test ProductResponseDto creation and getters/setters")
    void testProductResponseDto() {
        ProductResponseDto defaultDto = new ProductResponseDto();
        assertThat(defaultDto.getId()).isNull();

        ProductResponseDto dto = new ProductResponseDto(1L, "Star Catnip", "Desc", BigDecimal.TEN, "Herbs");
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Star Catnip");
        assertThat(dto.getDescription()).isEqualTo("Desc");
        assertThat(dto.getPrice()).isEqualTo(BigDecimal.TEN);
        assertThat(dto.getCategory()).isEqualTo("Herbs");

        dto.setId(2L);
        dto.setName("New Catnip");
        dto.setDescription("New Desc");
        dto.setPrice(BigDecimal.TWO);
        dto.setCategory("Food");

        assertThat(dto.getId()).isEqualTo(2L);
        assertThat(dto.getName()).isEqualTo("New Catnip");
        assertThat(dto.getDescription()).isEqualTo("New Desc");
        assertThat(dto.getPrice()).isEqualTo(BigDecimal.TWO);
        assertThat(dto.getCategory()).isEqualTo("Food");
    }

    @Test
    @DisplayName("should test ProductPageDto creation and getters")
    void testProductPageDto() {
        ProductResponseDto item = new ProductResponseDto(1L, "Star", "Desc", BigDecimal.TEN, "Toys");
        ProductPageDto pageDto = new ProductPageDto(List.of(item), 0, 10, 1L);

        assertThat(pageDto.getContent()).containsExactly(item);
        assertThat(pageDto.getPage()).isEqualTo(0);
        assertThat(pageDto.getSize()).isEqualTo(10);
        assertThat(pageDto.getTotalElements()).isEqualTo(1L);
    }

    @Test
    @DisplayName("should test ErrorResponse model")
    void testErrorResponse() {
        ErrorResponse error = new ErrorResponse(
                "https://cosmo-cats.market/errors/validation",
                "Validation failed",
                400,
                "Detail message",
                "/api/v1/products"
        );

        assertThat(error.getType()).isEqualTo("https://cosmo-cats.market/errors/validation");
        assertThat(error.getTitle()).isEqualTo("Validation failed");
        assertThat(error.getStatus()).isEqualTo(400);
        assertThat(error.getDetail()).isEqualTo("Detail message");
        assertThat(error.getInstance()).isEqualTo("/api/v1/products");
    }
}
