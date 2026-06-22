package com.sportspulse.fixtures.dtos.requests;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sportspulse.fixtures.utils.Status;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FixturesQueryParamsRequest {
    @Positive(message = "league must be greater than 0")
    private Integer league;

    @Positive(message = "team must be greater than 0")
    private Integer team;

    @JsonFormat(pattern = "YYYY-MM-DD")
    private LocalDate date;

    private Status status;
}
