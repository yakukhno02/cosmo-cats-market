package com.cosmocats.market.exception;

public record ErrorResponse(
        String type,
        String title,
        int status,
        String detail,
        String instance
) {}
