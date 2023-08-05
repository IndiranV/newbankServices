package org.in.com.controller.web.io;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NotificationConfigIO extends BaseIO {
	private String entityCode;
	private String headerDltCode;
	private String header;
	private String notificationMode;
}
