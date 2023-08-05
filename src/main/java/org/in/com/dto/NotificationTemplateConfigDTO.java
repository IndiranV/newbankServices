package org.in.com.dto;

import org.in.com.dto.enumeration.NotificationTypeEM;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NotificationTemplateConfigDTO extends BaseDTO<NotificationTemplateConfigDTO> {
	private NotificationConfigDTO notificationSMSConfig;
	private String templateDltCode;
	private NotificationTypeEM notificationType;
	private String content;
}
