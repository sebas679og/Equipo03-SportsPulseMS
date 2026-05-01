package com.sportspulse.fixtures.dto.request;

import com.sportspulse.fixtures.enums.FixtureStatusRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

/**
 * Data Transfer Object (DTO) used for filtering football fixtures based on specific criteria.
 *
 * <p>This record acts as a query container, allowing clients to narrow down fixture lists by
 * league, team, date, or match status. It includes validation constraints to ensure data integrity
 * and Swagger/OpenAPI annotations for documentation.
 *
 * @param league The unique ID of the league to filter by (must be positive).
 * @param team The unique ID of the team to filter by (must be positive).
 * @param date The specific date for the fixtures in {@code YYYY-MM-DD} format.
 * @param status The {@link FixtureStatusRequest} enum or object representing the desired match
 *     state.
 */
@Builder
public record FixtureFilterRequest(
    @Min(value = 1, message = "League must be greater than 0")
        @Schema(description = "League ID", example = "140")
        Integer league,
    @Min(value = 1, message = "Team must be greater than 0")
        @Schema(description = "Team ID", example = "529")
        Integer team,
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Date must be in format YYYY-MM-DD")
        @Schema(description = "Match date", example = "2025-01-20")
        String date,
    @Schema(description = "Match status", example = "NS") FixtureStatusRequest status) {}
