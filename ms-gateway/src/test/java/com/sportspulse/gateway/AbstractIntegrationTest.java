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
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/** Base integration test configuration. */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Testcontainers
public class AbstractIntegrationTest {

  static final boolean IS_CI = System.getenv("CI") != null;

  @Container
  static final RedisContainer REDIS =
      IS_CI
          ? null
          : new RedisContainer(DockerImageName.parse("redis:8.6.2-alpine"))
              .waitingFor(Wait.forListeningPort());

  protected static WireMockServer wireMock;

  static {
    REDIS.start();
    wireMock = new WireMockServer(WireMockConfiguration.options().dynamicPort());
    wireMock.start();
  }

  @AfterAll
  static void stopWireMock() {
    wireMock.stop();
  }

  @DynamicPropertySource
  static void redisProperties(DynamicPropertyRegistry registry) {
    if (System.getenv("CI") == null) {
      registry.add("spring.data.redis.host", REDIS::getHost);
      registry.add("spring.data.redis.port", REDIS::getFirstMappedPort);
    } else {
      registry.add("spring.data.redis.host", () -> "localhost");
      registry.add("spring.data.redis.port", () -> 6379);
    }
    registry.add("sportspulse.gateway.services.auth", () -> "http://localhost:" + wireMock.port());
  }
}
