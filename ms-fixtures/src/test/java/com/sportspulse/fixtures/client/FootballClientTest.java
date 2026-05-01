package com.sportspulse.fixtures.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sportspulse.fixtures.dto.external.event.FootballEventTime;
import com.sportspulse.fixtures.dto.external.event.FootballFixtureEventItem;
import com.sportspulse.fixtures.dto.external.event.FootballFixtureEventsResponse;
import com.sportspulse.fixtures.exceptions.FixtureNotFoundException;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class FootballClientTest {

  @Mock private WebClient webClient;

  @Mock private WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;

  @Mock private WebClient.RequestHeadersSpec<?> requestHeadersSpec;

  @Mock private WebClient.ResponseSpec responseSpec;

  private FootballClient footballClient;

  @BeforeEach
  void setUp() {
    footballClient = new FootballClient(webClient);

    doReturn(requestHeadersUriSpec).when(webClient).get();

    doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(any(Function.class));
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
  }

  @Test
  void getFixtureEvents_shouldReturnSortedEvents_whenIdExists() {

    Long fixtureId = 123L;

    FootballFixtureEventItem event1 = mock(FootballFixtureEventItem.class);
    FootballFixtureEventItem event2 = mock(FootballFixtureEventItem.class);

    when(event1.time()).thenReturn(new FootballEventTime(20));
    when(event2.time()).thenReturn(new FootballEventTime(10));

    FootballFixtureEventsResponse mockResponse =
        new FootballFixtureEventsResponse(List.of(event1, event2));

    when(responseSpec.bodyToMono(FootballFixtureEventsResponse.class))
        .thenReturn(Mono.just(mockResponse));

    List<FootballFixtureEventItem> result = footballClient.getFixtureEvents(fixtureId);

    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(10, result.get(0).time().elapsed());
    assertEquals(20, result.get(1).time().elapsed());
  }

  @Test
  void getFixtureEvents_shouldThrowException_whenListIsEmpty() {

    FootballFixtureEventsResponse emptyResponse = new FootballFixtureEventsResponse(List.of());
    when(responseSpec.bodyToMono(FootballFixtureEventsResponse.class))
        .thenReturn(Mono.just(emptyResponse));

    assertThrows(FixtureNotFoundException.class, () -> footballClient.getFixtureEvents(999L));
  }
}
