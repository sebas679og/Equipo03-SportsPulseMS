package com.sportspulse.fixtures.controllers;

import com.sportspulse.fixtures.config.constants.ApiPaths;
import com.sportspulse.fixtures.dtos.requests.FixturesQueryParamsRequest;
import com.sportspulse.fixtures.dtos.responses.fixtures.FixturesResponse;
import com.sportspulse.fixtures.services.FixtureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping
@RequiredArgsConstructor
class FixtureController {

    private final FixtureService fixtureService;

    @GetMapping(ApiPaths.Fixtures.FIXTURES)
    public ResponseEntity<FixturesResponse> getFixtures(@ModelAttribute @Valid FixturesQueryParamsRequest queryParams){
        return ResponseEntity.status(HttpStatus.OK).body(fixtureService.getFixtures(queryParams));
    }
}
