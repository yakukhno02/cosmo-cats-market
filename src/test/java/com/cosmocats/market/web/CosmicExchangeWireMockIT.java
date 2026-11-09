package com.cosmocats.market.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Cosmic Exchange Service - WireMock Stubbing IT")
class CosmicExchangeWireMockIT {

    @RegisterExtension
    static WireMockExtension wireMockServer = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .configureStaticDsl(true)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestClient restClient = RestClient.create();

    @Nested
    @DisplayName("GET /api/v1/galactic-rates (Exchange Rate Stubbing)")
    class GetExchangeRateStubbing {

        @Test
        @DisplayName("should return 200 OK and cosmic exchange rate when external service responds successfully")
        void shouldReturnExchangeRateWhenExternalServiceIsAvailable() throws Exception {
            String stubbedResponseBody = """
                    {
                        "baseCurrency": "USD",
                        "targetCurrency": "GALACTIC_CREDITS",
                        "rate": 1.42,
                        "status": "ACTIVE"
                    }
                    """;

            stubFor(get(urlEqualTo("/api/v1/galactic-rates"))
                    .willReturn(okJson(stubbedResponseBody)));

            String responseString = restClient.get()
                    .uri(wireMockServer.baseUrl() + "/api/v1/galactic-rates")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(String.class);

            JsonNode responseJson = objectMapper.readTree(responseString);
            assertThat(responseJson.get("targetCurrency").asText()).isEqualTo("GALACTIC_CREDITS");
            assertThat(responseJson.get("rate").asDouble()).isEqualTo(1.42);
            assertThat(responseJson.get("status").asText()).isEqualTo("ACTIVE");

            wireMockServer.verify(1, getRequestedFor(urlEqualTo("/api/v1/galactic-rates")));
        }

        @Test
        @DisplayName("should handle 500 Internal Server Error when external galactic service fails")
        void shouldHandleServerErrorWhenExternalServiceFails() {
            stubFor(get(urlEqualTo("/api/v1/galactic-rates"))
                    .willReturn(aResponse()
                            .withStatus(500)
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                            .withBody("{\"error\": \"Galactic central bank connection timeout\"}")));

            assertThatThrownBy(() -> restClient.get()
                    .uri(wireMockServer.baseUrl() + "/api/v1/galactic-rates")
                    .retrieve()
                    .body(String.class))
                    .isInstanceOf(HttpServerErrorException.class)
                    .satisfies(ex -> {
                        HttpServerErrorException serverError = (HttpServerErrorException) ex;
                        assertThat(serverError.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(500));
                        assertThat(serverError.getResponseBodyAsString()).contains("Galactic central bank connection timeout");
                    });

            wireMockServer.verify(1, getRequestedFor(urlEqualTo("/api/v1/galactic-rates")));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/galactic-payments (Payment Gateway Stubbing)")
    class PostPaymentStubbing {

        @Test
        @DisplayName("should verify request body and return 200 OK with transaction UUID")
        void shouldVerifyPayloadAndReturnTransactionConfirmation() throws Exception {
            UUID expectedTransactionId = UUID.randomUUID();
            String requestPayload = """
                    {
                        "cartId": "cart-cosmo-1234",
                        "customerReference": "cat-admiral-fluffy",
                        "amount": 42.50
                    }
                    """;

            String stubbedResponse = String.format("""
                    {
                        "transactionId": "%s",
                        "status": "APPROVED",
                        "message": "Payment verified by Intergalactic Cat Federation"
                    }
                    """, expectedTransactionId);

            stubFor(post(urlEqualTo("/api/v1/galactic-payments"))
                    .withHeader("Content-Type", equalTo(MediaType.APPLICATION_JSON_VALUE))
                    .withRequestBody(equalToJson(requestPayload))
                    .willReturn(okJson(stubbedResponse)));

            String responseString = restClient.post()
                    .uri(wireMockServer.baseUrl() + "/api/v1/galactic-payments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestPayload)
                    .retrieve()
                    .body(String.class);

            JsonNode responseJson = objectMapper.readTree(responseString);
            assertThat(responseJson.get("transactionId").asText()).isEqualTo(expectedTransactionId.toString());
            assertThat(responseJson.get("status").asText()).isEqualTo("APPROVED");

            wireMockServer.verify(1, postRequestedFor(urlEqualTo("/api/v1/galactic-payments"))
                    .withHeader("Content-Type", equalTo(MediaType.APPLICATION_JSON_VALUE)));
        }

        @Test
        @DisplayName("should handle 400 Bad Request when payment request is rejected by gateway")
        void shouldHandleBadRequestWhenPaymentRejected() {
            String rejectedPayload = "{\"cartId\": \"invalid-cart\", \"amount\": -10.00}";

            stubFor(post(urlEqualTo("/api/v1/galactic-payments"))
                    .withRequestBody(equalToJson(rejectedPayload))
                    .willReturn(aResponse()
                            .withStatus(400)
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                            .withBody("{\"error\": \"Invalid payment amount or non-existent cart\"}")));

            assertThatThrownBy(() -> restClient.post()
                    .uri(wireMockServer.baseUrl() + "/api/v1/galactic-payments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(rejectedPayload)
                    .retrieve()
                    .body(String.class))
                    .isInstanceOf(HttpClientErrorException.BadRequest.class);

            wireMockServer.verify(1, postRequestedFor(urlEqualTo("/api/v1/galactic-payments")));
        }
    }
}
