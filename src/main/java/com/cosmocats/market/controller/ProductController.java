package com.cosmocats.market.controller;

import com.cosmocats.market.dto.ProductRequestDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    @PostMapping
    public ResponseEntity<ProductRequestDto> createProduct(
            @Valid @RequestBody ProductRequestDto product) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(product);
    }
}