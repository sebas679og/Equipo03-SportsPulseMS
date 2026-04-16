package com.sportspulse.teams.integration.dto.response.teamid;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;

/**
 * ApiFootballTeamResponse Represents the API response structure for football team data. Contains
 * request metadata, parameters, errors, pagination details, and the list of team and venue response
 * items.
 */
@Value
@Getter
@Builder
public class ApiFootballTeamResponse {
  String get;
  Map<String, String> parameters;
  List<Object> errors;
  int results;
  PagingResponse paging;
  List<ResponseItem> response;
}
