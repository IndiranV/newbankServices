package org.in.com.controller.web.io;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.in.com.controller.web.io.BaseIO;

@Data
@EqualsAndHashCode(callSuper = true)
public class EventTriggerIO extends BaseIO {
	private String eventTime;
	private BaseIO triggerType;

}
