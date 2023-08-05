package org.in.com.dto;

import org.in.com.dto.enumeration.ScheduleEventTypeEM;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class ScheduleAuditLogDTO extends BaseDTO<ScheduleAuditLogDTO> {
	private String scheduleCode;
	private String tableName;
	private String event;
	private ScheduleEventTypeEM eventType;
	private String log;
}
