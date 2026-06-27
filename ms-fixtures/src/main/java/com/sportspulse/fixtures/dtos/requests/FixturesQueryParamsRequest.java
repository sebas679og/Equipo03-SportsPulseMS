package com.sportspulse.fixtures.dtos.requests;

import com.sportspulse.fixtures.utils.Status;
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
    @Builder.Default
    private LocalDate date = LocalDate.now();

    private Status status;
}
