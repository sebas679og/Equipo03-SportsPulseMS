package com.sportspulse.teams.dto.requests;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LeagueAndSeasonRequest Data transfer object (DTO) representing a request to query teams by league
 * and season.
 *
 * <p>Contains validation constraints to ensure that:
 *
 * <ul>
 *   <li>The league identifier is greater than 0.
 *   <li>The season is not blank and must be a 4-digit numeric string.
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Query parameters to filter teams by league and season")
public class LeagueAndSeasonRequest {

  @NotNull(message = "League is required")
  @Min(value = 1, message = "League must be greater than 0")
  @Schema(description = "League ID", example = "39", minimum = "1", requiredMode = REQUIRED)
  private int league;

  @NotBlank(message = "Season cannot be blank")
  @Pattern(regexp = "\\d{4}", message = "Season must be numeric and 4 digits")
  @Schema(
      description = "Season year",
      example = "2023",
      pattern = "\\d{4}",
      requiredMode = REQUIRED)
  private String season;
}
