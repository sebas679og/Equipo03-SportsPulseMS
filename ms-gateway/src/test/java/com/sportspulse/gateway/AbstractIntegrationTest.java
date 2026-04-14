package com.sportspulse.gateway;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.extension.RegisterExtension;
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

  static final boolean IS_CI = System.getenv("CI") != null;

  static RedisContainer REDIS;

  protected static final WireMockServer wireMock;

  static {
    wireMock = new WireMockServer(WireMockConfiguration.wireMockConfig()
            .dynamicPort()
            .notifier(new Slf4jNotifier(true)));
    wireMock.start();

    if (!IS_CI) {
      REDIS = new RedisContainer(DockerImageName.parse("redis:8.6.2-alpine"))
              .waitingFor(Wait.forListeningPort());
      REDIS.start();
    }
  }

  @AfterAll
  static void stopContainers() {
    if (REDIS != null && REDIS.isRunning()) {
      REDIS.stop();
    }
  }

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    if (!IS_CI) {
      registry.add("spring.data.redis.host", REDIS::getHost);
      registry.add("spring.data.redis.port", REDIS::getFirstMappedPort);
    } else {
      registry.add("spring.data.redis.host", () -> "localhost");
      registry.add("spring.data.redis.port", () -> 6379);
    }
    registry.add("sportspulse.gateway.services.auth",
            () -> "http://localhost:" + wireMock.port());
  }
}
