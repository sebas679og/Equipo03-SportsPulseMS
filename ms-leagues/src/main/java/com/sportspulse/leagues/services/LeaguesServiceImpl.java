package com.sportspulse.leagues.services;

import com.sportspulse.leagues.dto.requests.CountryAndSeasonRequest;
import com.sportspulse.leagues.dto.responses.LeagueDetailResponse;
import com.sportspulse.leagues.dto.responses.LeaguesResponse;
import com.sportspulse.leagues.exceptions.CustomBadGatewayException;
import com.sportspulse.leagues.exceptions.CustomBadRequestException;
import com.sportspulse.leagues.exceptions.CustomNotFoundException;
import com.sportspulse.leagues.exceptions.CustomTooManyRequestsException;
import com.sportspulse.leagues.integration.football.FootballApiClient;
import com.sportspulse.leagues.integration.football.dto.ApiLeagueResponse;
import com.sportspulse.leagues.integration.football.dto.ApiResponse;
import com.sportspulse.leagues.utils.mappers.LeaguesMapper;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Default leagues service implementation. */
@Slf4j
@Service
@RequiredArgsConstructor
public class LeaguesServiceImpl implements LeaguesService {

  private final FootballApiClient footballApiClient;
  private final LeaguesMapper leaguesMapper;

  @Override
  public LeaguesResponse getLeagues(CountryAndSeasonRequest request) {

    if (request.getCountry() == null && request.getSeason() == null) {
      throw new CustomBadRequestException("At least one filter is required: country or season");
    }

    ApiLeagueResponse apiResponse =
        footballApiClient.getLeaguesCountryAndSeason(
            request.getCountry(),
            request.getSeason() != null ? Integer.parseInt(request.getSeason()) : null);
    handleApiFootballErrors(apiResponse);

    if (apiResponse.response() == null || apiResponse.response().isEmpty()) {
      throw new CustomNotFoundException(
          String.format(
              "No leagues found for country '%s' and season '%s'",
              request.getCountry(), request.getSeason()));
    }

    return LeaguesResponse.builder()
        .data(leaguesMapper.toSummaryList(apiResponse.response()))
        .build();
  }

  @Override
  public LeagueDetailResponse getLeagueById(int leagueId) {
    ApiLeagueResponse apiResponse = footballApiClient.getLeagueById(leagueId);
    handleApiFootballErrors(apiResponse);

    if (apiResponse.response() == null || apiResponse.response().isEmpty()) {
      throw new CustomNotFoundException(String.format("No league found with id %d", leagueId));
    }

    ApiResponse response = apiResponse.response().getFirst();
    return leaguesMapper.toDetail(response);
  }

  private void handleApiFootballErrors(ApiLeagueResponse api) {
    if (api.errors() == null || api.errors().isEmpty()) {
      return;
    }

    @SuppressWarnings("unchecked")
    Map<String, Object> errorDetail = (Map<String, Object>) api.errors().getFirst();

    if (errorDetail.containsKey("requests")) {
      log.warn(
          "Api-Football 429 Rate Limit exceeded. "
              + "Available requests have been exhausted. Body: {}",
          errorDetail);
      throw new CustomTooManyRequestsException(
          "The daily request limit to Api-Football has been "
              + "reached, please try again tomorrow");
    } else if (errorDetail.containsKey("plan")) {
      String planMessage = (String) errorDetail.get("plan");
      log.warn(
          "Api-Football, the request limit per season has been exceeded. "
              + "Available requests have been exhausted. Body: {}",
          planMessage);
      throw new CustomTooManyRequestsException(planMessage);
    } else {
      log.error("Api-Football returned errors in the response. Body: {}", errorDetail);
      throw new CustomBadGatewayException(
          "An error occurred while processing the request to Api-Football. "
              + "Please try again later.");
    }
  }
}
