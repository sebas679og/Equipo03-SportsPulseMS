package com.sportspulse.gateway.controller;

import com.sportspulse.gateway.AbstractIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.reactive.server.WebTestClient;

class HealthControllerIntegrationTest extends AbstractIntegrationTest {

  @Autowired private WebTestClient webTestClient;
}
