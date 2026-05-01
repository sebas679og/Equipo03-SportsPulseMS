package com.sportspulse.fixtures.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sportspulse.fixtures.client.FootballClient;
import com.sportspulse.fixtures.dto.external.FootballFixtureItem;
import com.sportspulse.fixtures.dto.request.FixtureFilterRequest;
import com.sportspulse.fixtures.dto.response.FixtureResponse;
import com.sportspulse.fixtures.mapper.FixtureMapper;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FixtureServiceTest {

  @Mock private FootballClient footballClient;

  @Mock private FixtureMapper fixtureMapper;

  @InjectMocks private FixtureService fixtureService;

  @Test
  void getFixtures_ShouldSetCurrentDate_WhenDateIsNull() {

    FixtureFilterRequest requestWithoutDate = new FixtureFilterRequest(1, null, null, null);

    when(footballClient.getFixtures(any())).thenReturn(List.of());

    fixtureService.getFixtures(requestWithoutDate);

    ArgumentCaptor<FixtureFilterRequest> captor =
        ArgumentCaptor.forClass(FixtureFilterRequest.class);
    verify(footballClient).getFixtures(captor.capture());

    FixtureFilterRequest capturedRequest = captor.getValue();

    assertNotNull(capturedRequest.date());
    assertEquals(LocalDate.now().toString(), capturedRequest.date());
    assertEquals(1, capturedRequest.league());
  }

  @Test
  void getFixtures_ShouldMapAllItems_WhenDataExists() {

    FixtureFilterRequest request = new FixtureFilterRequest(1, null, "2024-05-20", null);

    FootballFixtureItem externalItem = mock(FootballFixtureItem.class);
    FixtureResponse responseItem = mock(FixtureResponse.class);

    when(footballClient.getFixtures(request)).thenReturn(List.of(externalItem));
    when(fixtureMapper.toFixtureResponse(externalItem)).thenReturn(responseItem);

    List<FixtureResponse> result = fixtureService.getFixtures(request);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(responseItem, result.get(0));
    verify(fixtureMapper, times(1)).toFixtureResponse(any());
  }

  @Test
  void getLiveFixtures_ShouldCallClientAndMapper() {

    when(footballClient.getFixtureLive())
        .thenReturn(
            List.of(mock(com.sportspulse.fixtures.dto.external.live.FootballLiveItem.class)));
    when(fixtureMapper.toLiveFixtureResponse(any()))
        .thenReturn(mock(com.sportspulse.fixtures.dto.response.live.FixtureLiveResponse.class));

    var result = fixtureService.getLiveFixtures();

    assertFalse(result.isEmpty());
    verify(footballClient).getFixtureLive();
    verify(fixtureMapper).toLiveFixtureResponse(any());
  }
}
