package com.example.inventory;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public class WireMockLifecycle {
    protected static WireMockServer wireMock;
    @BeforeAll public static void start() { wireMock = new WireMockServer(WireMockConfiguration.wireMockConfig().port(9099)); wireMock.start(); }
    @AfterAll public static void stop() { if (wireMock != null) wireMock.stop(); }
}
