package com.sportspulse.fixtures.dtos.requests;

import com.sportspulse.fixtures.utils.Status;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

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

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date;

    private Status status;

    @Min(value = 1900, message = "season must be greater than or equal to 1900")
    @Max(value = 9999, message = "season must have 4 digits")
    private Integer season;
}
