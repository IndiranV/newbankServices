package org.in.com.dto;

import org.in.com.dto.enumeration.EventTriggerTypeEM;

import hirondelle.date4j.DateTime;
import lombok.Data;

@Data
public class EventTriggerDTO {
	private DateTime eventTime;
	private EventTriggerTypeEM triggerType;
	private String code;
	private String name;
}
