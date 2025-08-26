package com.example.inventory;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.matcher.ResponseAwareMatcher;
import io.restassured.response.Response;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductResourceTest extends WireMockLifecycle {
    static String productId;

    @BeforeAll public static void stubs() {
        configureFor("localhost", 9099);
        stubFor(get(urlPathMatching("/api/v1/price-rules/rules/.*")).willReturn(okJson("{\"category\":\"electronics\",\"currency\":\"INR\",\"min\":1000,\"max\":50000,\"recommended\":20000}")));
        stubFor(post(urlEqualTo("/api/v1/price-rules/validate")).withRequestBody(matchingJsonPath("$.price", equalTo("19999"))).willReturn(okJson("{\"valid\":true}")));
        stubFor(post(urlEqualTo("/api/v1/price-rules/validate")).withRequestBody(matchingJsonPath("$.price", equalTo("10"))).willReturn(okJson("{\"valid\":false,\"reason\":\"Price below minimum\"}")));
        stubFor(post(urlEqualTo("/api/v1/price-rules/validate")).withRequestBody(matchingJsonPath("$.price", equalTo("-1"))).willReturn(serverError()));
    }

    @Test @Order(1) public void create_validates_price_ok() {
        productId = given().contentType("application/json").body("{\"name\":\"Phone\",\"sku\":\"SKU-1\",\"category\":\"electronics\",\"price\":19999,\"currency\":\"INR\",\"stock\":5}")
          .when().post("/api/v1/products").then().statusCode(201).body("name", (ResponseAwareMatcher<Response>) equalTo("Phone")).extract().path("id");
        Assertions.assertNotNull(productId);
    }

    @Test @Order(2) public void create_rejects_invalid_price() {
        given().contentType("application/json").body("{\"name\":\"Cable\",\"sku\":\"SKU-2\",\"category\":\"electronics\",\"price\":10,\"currency\":\"INR\"}")
        .when().post("/api/v1/products")
        .then().statusCode(422);
    }

    @Test @Order(3) public void update_handles_external_error() {
        String id = given().contentType("application/json").body("{\"name\":\"USB\",\"sku\":\"SKU-3\",\"category\":\"electronics\",\"price\":19999,\"currency\":\"INR\"}")
                .post("/api/v1/products").then().statusCode(201).extract().path("id");
        given().contentType("application/json").body("{\"name\":\"USB\",\"sku\":\"SKU-3\",\"category\":\"electronics\",\"price\":-1,\"currency\":\"INR\"}")
        .when().put("/api/v1/products/" + id)
        .then().statusCode(422);
    }

    @Test @Order(4) public void list_get_delete() {
        when().get("/api/v1/products").then().statusCode(200).body("size()", greaterThan(0));
        when().get("/api/v1/products/" + productId).then().statusCode(anyOf(is(200), is(404)));
        when().delete("/api/v1/products/" + productId).then().statusCode(anyOf(is(204), is(404)));
    }
}
