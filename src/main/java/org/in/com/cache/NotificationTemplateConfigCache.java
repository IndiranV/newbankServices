package org.in.com.cache;

import org.in.com.cache.dto.NotificationTemplateConfigCacheDTO;
import org.in.com.config.ApplicationConfig;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.NamespaceDTO;
import org.in.com.dto.NotificationConfigDTO;
import org.in.com.dto.NotificationTemplateConfigDTO;
import org.in.com.dto.enumeration.NotificationMediumEM;
import org.in.com.dto.enumeration.NotificationTypeEM;
import org.in.com.utils.StringUtil;

import net.sf.ehcache.Element;

public class NotificationTemplateConfigCache {

	public NotificationTemplateConfigDTO getCacheNotificationTemplateConfig(AuthDTO authDTO, NotificationTypeEM notificationType, NotificationMediumEM notificationMode) {
		String key = authDTO.getNamespaceCode() + Text.UNDER_SCORE + notificationType.getCode() + Text.UNDER_SCORE + notificationMode.getCode();

		NotificationTemplateConfigDTO config = new NotificationTemplateConfigDTO();
		Element element = EhcacheManager.getSMSTemplateConfigCache().get(key);
		if (element != null) {
			NotificationTemplateConfigCacheDTO cacheDTO = (NotificationTemplateConfigCacheDTO) element.getObjectValue();
			copyNotificationTemplateConfigFromCache(cacheDTO, config);
		}

		return config;
	}

	public void putCacheNotificationTemplateConfig(AuthDTO authDTO, NotificationTemplateConfigDTO config, NotificationTypeEM notificationType, NotificationMediumEM notificationMode) {
		String key = authDTO.getNamespaceCode() + Text.UNDER_SCORE + notificationType.getCode() + Text.UNDER_SCORE + notificationMode.getCode();

		NotificationTemplateConfigCacheDTO configCache = copyNotificationTemplateConfigToCache(config);
		Element element = new Element(key, configCache);
		EhcacheManager.getSMSTemplateConfigCache().put(element);
	}

	public NotificationTemplateConfigDTO getCacheNotificationTemplateConfig(AuthDTO authDTO, NotificationTemplateConfigDTO templateConfig) {
		if (templateConfig.getId() != 0 && StringUtil.isNull(templateConfig.getCode())) {
			String cacheKey = authDTO.getNamespaceCode() + Text.UNDER_SCORE + templateConfig.getId();
			Element element = EhcacheManager.getSMSTemplateConfigCache().get(cacheKey);
			if (element != null) {
				String templateConfigCode = (String) element.getObjectValue();
				templateConfig.setCode(templateConfigCode);
			}
		}

		String key = authDTO.getNamespaceCode() + Text.UNDER_SCORE + templateConfig.getCode();

		NotificationTemplateConfigDTO config = new NotificationTemplateConfigDTO();
		Element element = EhcacheManager.getSMSTemplateConfigCache().get(key);
		if (element != null) {
			NotificationTemplateConfigCacheDTO cacheDTO = (NotificationTemplateConfigCacheDTO) element.getObjectValue();
			copyNotificationTemplateConfigFromCache(cacheDTO, config);
		}

		return config;
	}

	public void putCacheNotificationTemplateConfig(AuthDTO authDTO, NotificationTemplateConfigDTO templateConfig) {
		String key = authDTO.getNamespaceCode() + Text.UNDER_SCORE + templateConfig.getCode();

		NotificationTemplateConfigCacheDTO configCache = copyNotificationTemplateConfigToCache(templateConfig);
		Element element = new Element(key, configCache);
		EhcacheManager.getSMSTemplateConfigCache().put(element);

		if (templateConfig.getId() != 0 && StringUtil.isNotNull(templateConfig.getCode())) {
			String cacheKey = authDTO.getNamespaceCode() + Text.UNDER_SCORE + templateConfig.getId();
			element = new Element(cacheKey, templateConfig.getCode());
			EhcacheManager.getSMSTemplateConfigCache().put(element);
		}
	}

	private NotificationTemplateConfigCacheDTO copyNotificationTemplateConfigToCache(NotificationTemplateConfigDTO config) {
		NotificationTemplateConfigCacheDTO cache = new NotificationTemplateConfigCacheDTO();
		if (StringUtil.isNotNull(config.getCode()) && config.getId() != 0) {
			cache.setId(config.getId());
			cache.setCode(config.getCode());
			cache.setName(config.getName());
			cache.setContent(config.getContent());
			cache.setTemplateDltCode(config.getTemplateDltCode());
			cache.setNotificationType(config.getNotificationType().getCode());
			cache.setNotificationMode(config.getNotificationSMSConfig().getNotificationMode().getCode());
			cache.setEntityCode(config.getNotificationSMSConfig().getEntityCode());
			cache.setHeader(config.getNotificationSMSConfig().getHeader());
			cache.setHeaderDltCode(config.getNotificationSMSConfig().getHeaderDltCode());
		}
		return cache;
	}

	private void copyNotificationTemplateConfigFromCache(NotificationTemplateConfigCacheDTO cache, NotificationTemplateConfigDTO config) {
		if (StringUtil.isNotNull(cache.getCode()) && cache.getId() != 0) {
			config.setId(cache.getId());
			config.setCode(cache.getCode());
			config.setName(cache.getName());
			config.setContent(cache.getContent());
			config.setTemplateDltCode(cache.getTemplateDltCode());
			config.setNotificationType(NotificationTypeEM.getNotificationTypeEM(cache.getNotificationType()));

			NotificationConfigDTO notificationSMSConfig = new NotificationConfigDTO();
			notificationSMSConfig.setEntityCode(cache.getEntityCode());
			notificationSMSConfig.setHeader(cache.getHeader());
			notificationSMSConfig.setHeaderDltCode(cache.getHeaderDltCode());
			notificationSMSConfig.setNotificationMode(NotificationMediumEM.getNotificationMediumEM(cache.getNotificationMode()));
			config.setNotificationSMSConfig(notificationSMSConfig);
		}
	}

	public void removeNotificationTemplateConfigCache(NamespaceDTO namespaceDTO, NotificationTypeEM notificationType) {
		if (namespaceDTO.getCode().equals(ApplicationConfig.getServerZoneCode())) {
			EhcacheManager.getSMSTemplateConfigCache().removeAll();
		}
		else {
			String key = namespaceDTO.getCode() + Text.UNDER_SCORE + notificationType.getCode();
			EhcacheManager.getSMSTemplateConfigCache().remove(key);
		}
	}
}
