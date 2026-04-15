package com.sportspulse.gateway.integration.dtos.responses;

import com.sportspulse.gateway.utils.enums.ServiceStatus;

/**
 * ServiceResponse Represents the response status of a service. Encapsulates the current {@link
 * ServiceStatus} for monitoring or reporting purposes.
 *
 * @param status the status of the service
 */
public record ServiceResponse(ServiceStatus status) {}
