package com.sportspulse.fixtures.dto.external.live;

/**
 * Data Transfer Object (DTO) representing the status and identification of a live fixture.
 *
 * <p>This record is used to track the current state of an ongoing match, linking the unique fixture
 * identifier with its real-time operational status.
 *
 * @param id The unique identifier for the fixture.
 * @param status The {@link FootballLiveStatus} containing real-time match state information.
 */
public record FootballLiveFixture(Long id, FootballLiveStatus status) {}
