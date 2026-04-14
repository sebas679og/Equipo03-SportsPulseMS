package com.sportspulse.gateway;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.AfterAll;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

/** Abstraction Configuration Redis and wiremock Test. */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class AbstractIntegrationTest {

  protected static final WireMockServer wireMock;
  private static final RedisContainer REDIS;

  static {
    REDIS =
        new RedisContainer(DockerImageName.parse("redis:8.6.2-alpine"))
            .waitingFor(Wait.forListeningPort());
    REDIS.start();

    wireMock = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
    wireMock.start();
  }

  @AfterAll
  static void stopContainers() {
    wireMock.stop();
    if (REDIS.isRunning()) {
      REDIS.stop();
    }
  }

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.redis.host", REDIS::getHost);
    registry.add("spring.data.redis.port", REDIS::getFirstMappedPort);
    registry.add("sportspulse.gateway.services.auth", () -> "http://localhost:" + wireMock.port());
  }
}
