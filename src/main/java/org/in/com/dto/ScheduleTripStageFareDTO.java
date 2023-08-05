package org.in.com.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleTripStageFareDTO extends BaseDTO<ScheduleTripStageFareDTO> {
	private String tripDate;
	private String fareDetails;
	private ScheduleDTO schedule;
	private RouteDTO route;
	private AuditDTO audit;
}