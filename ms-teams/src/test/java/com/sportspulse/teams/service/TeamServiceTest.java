package com.sportspulse.teams.service;

import com.sportspulse.teams.dto.external.ApiFootballResponse;
import com.sportspulse.teams.dto.external.TeamData;
import com.sportspulse.teams.dto.external.TeamVenueWrapper;
import com.sportspulse.teams.dto.external.VenueData;
import com.sportspulse.teams.dto.response.TeamResponse;
import com.sportspulse.teams.dto.response.VenueResponse;
import com.sportspulse.teams.mappers.TeamMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private WebClient footballWebClient;

    @Mock
    private TeamMapper teamMapper;

    @Mock
    private WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private TeamService teamService;

    private TeamVenueWrapper buildWrapper() {
        TeamData team = new TeamData(33, "Manchester United", "England",
                "https://logo.png", 1878);
        VenueData venue = new VenueData("Old Trafford", "Manchester", 76212);
        return new TeamVenueWrapper(team, venue);
    }

    private TeamResponse buildTeamResponse() {
        return new TeamResponse(33, "Manchester United", "England",
                "https://logo.png", 1878,
                new VenueResponse("Old Trafford", "Manchester", 76212));
    }

    private void mockWebClient(ApiFootballResponse apiResponse) {
        doReturn(requestHeadersUriSpec).when(footballWebClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec)
                .uri(ArgumentMatchers.<Function<UriBuilder, URI>>any());
        doReturn(responseSpec).when(requestHeadersSpec).retrieve();
        when(responseSpec.bodyToMono(ApiFootballResponse.class))
                .thenReturn(Mono.just(apiResponse));
    }

    @Test
    void getTeams_shouldReturnMappedTeams() {

        TeamVenueWrapper wrapper = buildWrapper();
        TeamResponse teamResponse = buildTeamResponse();
        ApiFootballResponse apiResponse = new ApiFootballResponse(List.of(wrapper));

        mockWebClient(apiResponse);
        when(teamMapper.toTeamResponse(wrapper)).thenReturn(teamResponse);

        List<TeamResponse> result = teamService.getTeams(39, 2023);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(teamResponse);
        verify(teamMapper, times(1)).toTeamResponse(wrapper);
    }

    @Test
    void getTeams_shouldCallApiWithCorrectParams() {

        ApiFootballResponse apiResponse = new ApiFootballResponse(List.of(buildWrapper()));
        mockWebClient(apiResponse);
        when(teamMapper.toTeamResponse(any())).thenReturn(buildTeamResponse());

        teamService.getTeams(39, 2023);

        verify(footballWebClient).get();
        verify(requestHeadersUriSpec).uri(
                ArgumentMatchers.<Function<UriBuilder, URI>>any());
    }

    @Test
    void getTeams_shouldReturnEmptyList_whenApiReturnsNoTeams() {

        ApiFootballResponse apiResponse = new ApiFootballResponse(List.of());
        mockWebClient(apiResponse);

        List<TeamResponse> result = teamService.getTeams(39, 2023);

        assertThat(result).isEmpty();
        verify(teamMapper, never()).toTeamResponse(any());
    }

    @Test
    void getTeams_shouldThrowIllegalStateException_whenApiReturnsEmptyBody() {
        doReturn(requestHeadersUriSpec).when(footballWebClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec)
                .uri(ArgumentMatchers.<Function<UriBuilder, URI>>any());
        doReturn(responseSpec).when(requestHeadersSpec).retrieve();
        when(responseSpec.bodyToMono(ApiFootballResponse.class))
                .thenReturn(Mono.empty());

        assertThatThrownBy(() -> teamService.getTeams(39, 2023))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void getTeams_shouldThrowWebClientResponseException_whenApiFails() {
        doReturn(requestHeadersUriSpec).when(footballWebClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec)
                .uri(ArgumentMatchers.<Function<UriBuilder, URI>>any());
        doReturn(responseSpec).when(requestHeadersSpec).retrieve();
        when(responseSpec.bodyToMono(ApiFootballResponse.class))
                .thenThrow(WebClientResponseException.create(
                        503, "Service Unavailable", null, null, null));

        assertThatThrownBy(() -> teamService.getTeams(39, 2023))
                .isInstanceOf(WebClientResponseException.class);
    }
}