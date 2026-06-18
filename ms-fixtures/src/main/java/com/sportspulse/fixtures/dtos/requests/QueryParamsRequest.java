package com.sportspulse.fixtures.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryParamsRequest {
    private int league;
    private int team;
    private String date;
    private String status;
}
