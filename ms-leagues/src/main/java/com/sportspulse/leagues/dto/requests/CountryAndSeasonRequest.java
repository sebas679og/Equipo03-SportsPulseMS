package com.sportspulse.leagues.dto.requests;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** LeagueAndSeasonRequest DTO for capturing league and season parameters in API requests. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Query parameters to filter leagues by country and season")
public class CountryAndSeasonRequest {

  @Schema(description = "Country", example = "spain", requiredMode = NOT_REQUIRED)
  private String country;

  @Pattern(regexp = "\\d{4}", message = "Season must be numeric and 4 digits")
  @Schema(
      description = "Season year",
      example = "2023",
      pattern = "\\d{4}",
      requiredMode = NOT_REQUIRED)
  private String season;
}
