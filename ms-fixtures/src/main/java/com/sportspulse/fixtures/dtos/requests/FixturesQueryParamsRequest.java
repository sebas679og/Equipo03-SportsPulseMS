package com.sportspulse.fixtures.dtos.requests;

import com.sportspulse.fixtures.utils.Status;
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
    private Integer league;
    private Integer team;
    private LocalDate date;
    private Status status;
}
