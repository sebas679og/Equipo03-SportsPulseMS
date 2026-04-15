package com.sportspulse.gateway.integration.dtos.responses;

import com.sportspulse.gateway.utils.enums.ServiceStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;

/**
 * ServiceResponse Represents the response status of a service. Encapsulates the current {@link
 * ServiceStatus} for monitoring or reporting purposes.
 */
@Value
@Getter
@Builder
public class ServiceResponse {
  ServiceStatus status;
}
