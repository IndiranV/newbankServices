package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.NamespaceDTO;
import org.in.com.dto.NotificationConfigDTO;
import org.in.com.dto.NotificationTemplateConfigDTO;
import org.in.com.dto.enumeration.NotificationMediumEM;
import org.in.com.dto.enumeration.NotificationTypeEM;

public interface NotificationConfigService {

	public void updateNotificationTemplateConfig(AuthDTO authDTO, NamespaceDTO namespace, NotificationTemplateConfigDTO smsTemplateConfigDTO);

	public NotificationTemplateConfigDTO getNotificationTemplateConfig(AuthDTO authDTO, NamespaceDTO namespace, NotificationTemplateConfigDTO smsTemplateConfigDTO);

	public List<NotificationTemplateConfigDTO> getAllNotificationTemplateConfig(AuthDTO authDTO, NamespaceDTO namespace, NotificationConfigDTO smsConfigDTO);

	List<NotificationConfigDTO> getAll(AuthDTO authDTO, NamespaceDTO namespace);

	NotificationConfigDTO Update(AuthDTO authDTO, NamespaceDTO namespace, NotificationConfigDTO dto);

	public NotificationTemplateConfigDTO getNotificationTemplateConfig(AuthDTO authDTO, NotificationTypeEM notificationType, NotificationMediumEM notificationMedium);

	public List<NotificationTemplateConfigDTO> getNotificationTemplateConfigList(AuthDTO authDTO, NotificationTypeEM notificationType);

	public NotificationTemplateConfigDTO getNotificationTemplateConfigByCode(AuthDTO authDTO, NotificationTemplateConfigDTO templteConfig);
}
