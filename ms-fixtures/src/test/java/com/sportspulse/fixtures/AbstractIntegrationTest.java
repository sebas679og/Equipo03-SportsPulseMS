package com.sportspulse.fixtures;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
public abstract class AbstractIntegrationTest {

    @RegisterExtension
    static WireMockExtension wireMockExtension =
            WireMockExtension.newInstance()
                    .options(wireMockConfig().dynamicPort().usingFilesUnderClasspath("src/test/resources"))
                    .build();

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry){
        registry.add("sportspulse.fixtures.api.football.base-url", wireMockExtension::baseUrl);
    }

    @BeforeEach
    void setUpBase(){}

    @AfterEach
    void tearDown(){}

}
