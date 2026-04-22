package com.sportspulse.leagues;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    properties = {
      "SPORT_PULSE_API_BASE_URL=https://v3.football.api-sports.io",
      "FOOTBALL_API_KEY=test-key",
      "SPORTS_PULSE_JWT_SECRET=dGVzdC1zZWNyZXQtdGVzdC1zZWNyZXQtdGVzdC1zZWNyZXQ="
    })
class MsLeaguesApplicationTests {

  @Test
  void contextLoads() {}
}
