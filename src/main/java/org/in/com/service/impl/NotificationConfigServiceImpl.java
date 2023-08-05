package org.in.com.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.in.com.cache.NotificationTemplateConfigCache;
import org.in.com.config.ApplicationConfig;
import org.in.com.dao.NotificationConfigDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.NamespaceDTO;
import org.in.com.dto.NotificationConfigDTO;
import org.in.com.dto.NotificationTemplateConfigDTO;
import org.in.com.dto.enumeration.NotificationMediumEM;
import org.in.com.dto.enumeration.NotificationTypeEM;
import org.in.com.service.NamespaceService;
import org.in.com.service.NotificationConfigService;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationConfigServiceImpl extends NotificationTemplateConfigCache implements NotificationConfigService {

	@Autowired
	NamespaceService namespaceService;

	@Override
	public List<NotificationConfigDTO> getAll(AuthDTO authDTO, NamespaceDTO namespace) {
		namespace = namespaceService.getNamespace(namespace);

		NotificationConfigDAO smsConfigDAO = new NotificationConfigDAO();
		return smsConfigDAO.getAllNotificationConfig(authDTO, namespace);
	}

	@Override
	public NotificationConfigDTO Update(AuthDTO authDTO, NamespaceDTO namespace, NotificationConfigDTO dto) {
		namespace = namespaceService.getNamespace(namespace);

		NotificationConfigDAO smsConfigDAO = new NotificationConfigDAO();
		return smsConfigDAO.updateNotificationConfig(authDTO, namespace, dto);
	}

	@Override
	public void updateNotificationTemplateConfig(AuthDTO authDTO, NamespaceDTO namespace, NotificationTemplateConfigDTO smsTemplateConfigDTO) {
		namespace = namespaceService.getNamespace(namespace);

		NotificationConfigDAO smsConfigDAO = new NotificationConfigDAO();
		smsConfigDAO.updateNotificationTemplateConfig(authDTO, namespace, smsTemplateConfigDTO);

		removeNotificationTemplateConfigCache(namespace, smsTemplateConfigDTO.getNotificationType());
	}

	@Override
	public NotificationTemplateConfigDTO getNotificationTemplateConfig(AuthDTO authDTO, NamespaceDTO namespace, NotificationTemplateConfigDTO smsTemplateConfigDTO) {
		namespace = namespaceService.getNamespace(namespace);

		NotificationConfigDAO smsConfigDAO = new NotificationConfigDAO();
		return smsConfigDAO.getNotificationTemplateConfig(authDTO, namespace, smsTemplateConfigDTO);
	}

	@Override
	public List<NotificationTemplateConfigDTO> getAllNotificationTemplateConfig(AuthDTO authDTO, NamespaceDTO namespace, NotificationConfigDTO dto) {
		namespace = namespaceService.getNamespace(namespace);
		NotificationConfigDAO smsConfigDAO = new NotificationConfigDAO();
		return smsConfigDAO.getAllNotificationTemplateConfig(authDTO, namespace, dto);
	}

	public NotificationTemplateConfigDTO getNotificationTemplateConfig(AuthDTO authDTO, NotificationTypeEM notificationType, NotificationMediumEM notificationMode) {
		NotificationTemplateConfigDTO config = getCacheNotificationTemplateConfig(authDTO, notificationType, notificationMode);
		if (StringUtil.isNull(config.getCode())) {
			NotificationConfigDAO smsConfigDAO = new NotificationConfigDAO();
			config = smsConfigDAO.getNotificationTemplateConfig(authDTO, notificationType, notificationMode);

			// Default configure
			if (StringUtil.isNull(config.getCode())) {
				config = smsConfigDAO.getNotificationTemplateConfigDefault(notificationType, notificationMode);
			}

			putCacheNotificationTemplateConfig(authDTO, config, notificationType, notificationMode);
		}

		return config;
	}
	
	public NotificationTemplateConfigDTO getNotificationTemplateConfigByCode(AuthDTO authDTO, NotificationTemplateConfigDTO templteConfig) {
		NotificationTemplateConfigDTO config = getCacheNotificationTemplateConfig(authDTO, templteConfig);
		if (StringUtil.isNull(config.getCode())) {
			NotificationConfigDAO smsConfigDAO = new NotificationConfigDAO();
			config = smsConfigDAO.getNotificationTemplateConfig(authDTO, authDTO.getNamespace(), templteConfig);

			// Default configure
			if (StringUtil.isNull(config.getCode()) || config.getId() == 0) {
				NamespaceDTO namespace = new NamespaceDTO();
				namespace.setCode(ApplicationConfig.getServerZoneCode());
				namespace = namespaceService.getNamespace(namespace);
				config = smsConfigDAO.getNotificationTemplateConfig(authDTO, namespace, templteConfig);
			}

			putCacheNotificationTemplateConfig(authDTO, config);
		}

		return config;
	}
	
	@Override
	public List<NotificationTemplateConfigDTO> getNotificationTemplateConfigList(AuthDTO authDTO, NotificationTypeEM notificationType) {
		List<NotificationTemplateConfigDTO> configList = new ArrayList<NotificationTemplateConfigDTO>();
		
		NotificationConfigDAO smsConfigDAO = new NotificationConfigDAO();

		if (notificationType == null) {
			notificationType = NotificationTypeEM.COMMON_NOTIFICATION;
		}
		configList = smsConfigDAO.getNotificationTemplateConfigList(authDTO, notificationType);
		if (configList.isEmpty()) {
			configList = smsConfigDAO.getNotificationTemplateConfigListDefault(notificationType);
		}
		return configList;
	}

}
