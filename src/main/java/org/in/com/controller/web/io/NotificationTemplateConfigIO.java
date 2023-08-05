package org.in.com.controller.web.io;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NotificationTemplateConfigIO extends BaseIO {
	private NotificationConfigIO notificationSMSConfig;
	private String templateDltCode;
	private BaseIO notificationType;
	private String content;
}
