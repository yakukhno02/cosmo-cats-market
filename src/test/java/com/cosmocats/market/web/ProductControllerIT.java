package com.cosmocats.market.web;

import com.cosmocats.market.controller.ProductController;
import com.cosmocats.market.domain.Category;
import com.cosmocats.market.domain.Product;
import com.cosmocats.market.dto.ProductRequestDto;
import com.cosmocats.market.exception.ProductNotFoundException;
import com.cosmocats.market.mapper.ProductMapper;
import com.cosmocats.market.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import(ProductMapper.class)
@DisplayName("ProductController Integration Tests (Thin IT)")
class ProductControllerIT {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ProductService productService;

    @Nested
    @DisplayName("GET /api/v1/products (Pagination)")
    class GetAllProducts {

        @Test
        @DisplayName("should return 200 and paginated products when parameters are valid")
        void shouldReturn200WhenParametersAreValid() throws Exception {
            Product product = new Product(
                    1L,
                    "Galaxy Milk",
                    "Cosmic milk",
                    new BigDecimal("15.99"),
                    new Category(1L, "Food", "Cosmic food")
            );

            when(productService.getProductsPage(0, 10)).thenReturn(List.of(product));
            when(productService.getTotalProductsCount()).thenReturn(1L);

            mockMvc.perform(get("/api/v1/products")
                            .param("page", "0")
                            .param("size", "10")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.content[0].name").value("Galaxy Milk"))
                    .andExpect(jsonPath("$.page").value(0))
                    .andExpect(jsonPath("$.size").value(10))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        @DisplayName("should use default pagination parameters when omitted")
        void shouldUseDefaultParameters() throws Exception {
            when(productService.getProductsPage(0, 10)).thenReturn(List.of());
            when(productService.getTotalProductsCount()).thenReturn(0L);

            mockMvc.perform(get("/api/v1/products")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.page").value(0))
                    .andExpect(jsonPath("$.size").value(10));
        }

        @Test
        @DisplayName("should return 400 when page parameter is negative")
        void shouldReturn400WhenPageIsNegative() throws Exception {
            mockMvc.perform(get("/api/v1/products")
                            .param("page", "-1")
                            .param("size", "10"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.title").value("Validation failed"));
        }

        @Test
        @DisplayName("should return 400 when size parameter is less than 1")
        void shouldReturn400WhenSizeIsZero() throws Exception {
            mockMvc.perform(get("/api/v1/products")
                            .param("page", "0")
                            .param("size", "0"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("should return 400 when size parameter exceeds 100")
        void shouldReturn400WhenSizeExceedsMax() throws Exception {
            mockMvc.perform(get("/api/v1/products")
                            .param("page", "0")
                            .param("size", "101"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/products/{id}")
    class GetProductById {

        @Test
        @DisplayName("should return 200 and product when found")
        void shouldReturn200WhenProductExists() throws Exception {
            Product product = new Product(
                    1L,
                    "Galaxy Milk",
                    "Cosmic milk",
                    new BigDecimal("15.99"),
                    new Category(1L, "Food", "Cosmic food")
            );

            when(productService.getProductById(1L)).thenReturn(product);

            mockMvc.perform(get("/api/v1/products/1")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Galaxy Milk"))
                    .andExpect(jsonPath("$.price").value(15.99))
                    .andExpect(jsonPath("$.category").value("Food"));
        }

        @Test
        @DisplayName("should return 404 when product is not found")
        void shouldReturn404WhenProductDoesNotExist() throws Exception {
            when(productService.getProductById(999L))
                    .thenThrow(new ProductNotFoundException(999L));

            mockMvc.perform(get("/api/v1/products/999")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.title").value("Product not found"))
                    .andExpect(jsonPath("$.detail").value(containsString("999")));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/products (Data Validation)")
    class CreateProduct {

        @Test
        @DisplayName("should return 201 Created when request data is valid")
        void shouldReturn201WhenRequestIsValid() throws Exception {
            ProductRequestDto requestDto = new ProductRequestDto(
                    "Star Collar",
                    "Cosmic collar for cats",
                    new BigDecimal("19.99"),
                    "Accessories"
            );

            Product createdProduct = new Product(
                    3L,
                    "Star Collar",
                    "Cosmic collar for cats",
                    new BigDecimal("19.99"),
                    new Category(null, "Accessories", null)
            );

            when(productService.createProduct(any(Product.class))).thenReturn(createdProduct);

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(3))
                    .andExpect(jsonPath("$.name").value("Star Collar"))
                    .andExpect(jsonPath("$.price").value(19.99))
                    .andExpect(jsonPath("$.category").value("Accessories"));
        }

        @Test
        @DisplayName("should return 400 when name is blank")
        void shouldReturn400WhenNameIsBlank() throws Exception {
            ProductRequestDto requestDto = new ProductRequestDto(
                    "   ",
                    "Valid description",
                    new BigDecimal("10.00"),
                    "Food"
            );

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.detail").value(containsString("Name is required")));
        }

        @Test
        @DisplayName("should return 400 when name lacks required cosmic word")
        void shouldReturn400WhenNameLacksCosmicWord() throws Exception {
            ProductRequestDto requestDto = new ProductRequestDto(
                    "Simple Cat Food",
                    "Ordinary milk",
                    new BigDecimal("10.00"),
                    "Food"
            );

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.detail").value(containsString("name")));
        }

        @Test
        @DisplayName("should return 400 when name exceeds 100 characters")
        void shouldReturn400WhenNameExceedsMaxLength() throws Exception {
            String longName = "Star " + "A".repeat(100);
            ProductRequestDto requestDto = new ProductRequestDto(
                    longName,
                    "Desc",
                    new BigDecimal("10.00"),
                    "Food"
            );

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.detail").value(containsString("Name must not exceed 100 characters")));
        }

        @Test
        @DisplayName("should return 400 when price is null")
        void shouldReturn400WhenPriceIsNull() throws Exception {
            ProductRequestDto requestDto = new ProductRequestDto(
                    "Galaxy Fish",
                    "Desc",
                    null,
                    "Food"
            );

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.detail").value(containsString("Price is required")));
        }

        @Test
        @DisplayName("should return 400 when price is zero")
        void shouldReturn400WhenPriceIsZero() throws Exception {
            ProductRequestDto requestDto = new ProductRequestDto(
                    "Galaxy Fish",
                    "Desc",
                    BigDecimal.ZERO,
                    "Food"
            );

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.detail").value(containsString("Price must be greater than 0")));
        }

        @Test
        @DisplayName("should return 400 when price is negative")
        void shouldReturn400WhenPriceIsNegative() throws Exception {
            ProductRequestDto requestDto = new ProductRequestDto(
                    "Galaxy Fish",
                    "Desc",
                    new BigDecimal("-5.00"),
                    "Food"
            );

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.detail").value(containsString("Price must be greater than 0")));
        }

        @Test
        @DisplayName("should return 400 when category is blank")
        void shouldReturn400WhenCategoryIsBlank() throws Exception {
            ProductRequestDto requestDto = new ProductRequestDto(
                    "Comet Ball",
                    "Desc",
                    new BigDecimal("12.00"),
                    "   "
            );

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.detail").value(containsString("Category is required")));
        }

        @Test
        @DisplayName("should return 400 when category exceeds 50 characters")
        void shouldReturn400WhenCategoryExceedsMaxLength() throws Exception {
            ProductRequestDto requestDto = new ProductRequestDto(
                    "Comet Ball",
                    "Desc",
                    new BigDecimal("12.00"),
                    "C".repeat(51)
            );

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.detail").value(containsString("Category must not exceed 50 characters")));
        }

        @Test
        @DisplayName("should return 400 when description exceeds 500 characters")
        void shouldReturn400WhenDescriptionExceedsMaxLength() throws Exception {
            ProductRequestDto requestDto = new ProductRequestDto(
                    "Comet Ball",
                    "D".repeat(501),
                    new BigDecimal("12.00"),
                    "Toys"
            );

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.detail").value(containsString("Description must not exceed 500 characters")));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/products/{id}")
    class UpdateProduct {

        @Test
        @DisplayName("should return 200 when updating with valid data")
        void shouldReturn200WhenUpdateIsValid() throws Exception {
            ProductRequestDto requestDto = new ProductRequestDto(
                    "Super Galaxy Milk",
                    "Upgraded milk",
                    new BigDecimal("18.50"),
                    "Food"
            );

            Product updated = new Product(
                    1L,
                    "Super Galaxy Milk",
                    "Upgraded milk",
                    new BigDecimal("18.50"),
                    new Category(null, "Food", null)
            );

            when(productService.updateProduct(eq(1L), any(Product.class))).thenReturn(updated);

            mockMvc.perform(put("/api/v1/products/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Super Galaxy Milk"))
                    .andExpect(jsonPath("$.price").value(18.50));
        }

        @Test
        @DisplayName("should return 400 when update data is invalid")
        void shouldReturn400WhenUpdateDataIsInvalid() throws Exception {
            ProductRequestDto requestDto = new ProductRequestDto(
                    "Ordinary Milk", // missing cosmic word
                    "Desc",
                    new BigDecimal("10.00"),
                    "Food"
            );

            mockMvc.perform(put("/api/v1/products/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("should return 404 when updating non-existent product")
        void shouldReturn404WhenUpdatingNonExistentProduct() throws Exception {
            ProductRequestDto requestDto = new ProductRequestDto(
                    "Galaxy Milk",
                    "Desc",
                    new BigDecimal("10.00"),
                    "Food"
            );

            when(productService.updateProduct(eq(888L), any(Product.class)))
                    .thenThrow(new ProductNotFoundException(888L));

            mockMvc.perform(put("/api/v1/products/888")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/products/{id}")
    class DeleteProduct {

        @Test
        @DisplayName("should return 204 No Content when product deleted successfully")
        void shouldReturn204WhenProductDeleted() throws Exception {
            doNothing().when(productService).deleteProduct(1L);

            mockMvc.perform(delete("/api/v1/products/1"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("should return 404 Not Found when deleting non-existent product")
        void shouldReturn404WhenDeletingNonExistentProduct() throws Exception {
            doThrow(new ProductNotFoundException(777L)).when(productService).deleteProduct(777L);

            mockMvc.perform(delete("/api/v1/products/777"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }
    }
}
