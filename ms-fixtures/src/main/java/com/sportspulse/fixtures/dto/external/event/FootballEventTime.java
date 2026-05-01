package com.sportspulse.fixtures.dto.external.event;

/**
 * Data Transfer Object (DTO) representing the timing of a match event.
 *
 * <p>This record encapsulates the temporal information of an incident, primarily the minute in
 * which it occurred during the match.
 *
 * @param elapsed The number of minutes passed since the start of the match when the event took
 *     place.
 */
public record FootballEventTime(Integer elapsed) {}
