package com.cosmocats.market.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class CosmicWordValidator implements ConstraintValidator<CosmicWordCheck, String> {

    private static final List<String> COSMIC_WORDS = List.of(
            "star",
            "galaxy",
            "comet"
    );

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }

        String normalizedValue = value.toLowerCase();

        return COSMIC_WORDS.stream()
                .anyMatch(normalizedValue::contains);
    }
}