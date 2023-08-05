package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.in.com.constants.Text;
import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.NotificationConfigIO;
import org.in.com.controller.web.io.NotificationTemplateConfigIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.NamespaceDTO;
import org.in.com.dto.NotificationConfigDTO;
import org.in.com.dto.NotificationTemplateConfigDTO;
import org.in.com.dto.enumeration.NotificationMediumEM;
import org.in.com.dto.enumeration.NotificationTypeEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.NotificationConfigService;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/{authtoken}/{namespaceCode}/notification/sms")
public class NotificationConfigController extends BaseController {

	@Autowired
	NotificationConfigService notificationSmsConfigService;

	@RequestMapping(value = "/config", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<NotificationConfigIO>> getAllNotificationSMSConfig(@PathVariable("authtoken") String authtoken, @PathVariable("namespaceCode") String namespaceCode) throws Exception {
		List<NotificationConfigIO> smsConfigList = new ArrayList<NotificationConfigIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		validateNativeNamespace(authDTO, namespaceCode);
		NamespaceDTO namespace = new NamespaceDTO();
		namespace.setCode(namespaceCode);

		List<NotificationConfigDTO> list = notificationSmsConfigService.getAll(authDTO, namespace);
		for (NotificationConfigDTO smsConfigDTO : list) {
			NotificationConfigIO smsConfigIO = new NotificationConfigIO();
			smsConfigIO.setCode(smsConfigDTO.getCode());
			smsConfigIO.setEntityCode(smsConfigDTO.getEntityCode());
			smsConfigIO.setHeaderDltCode(smsConfigDTO.getHeaderDltCode());
			smsConfigIO.setHeader(smsConfigDTO.getHeader());
			smsConfigIO.setNotificationMode(smsConfigDTO.getNotificationMode().getCode());
			smsConfigIO.setActiveFlag(smsConfigDTO.getActiveFlag());
			smsConfigList.add(smsConfigIO);
		}
		return ResponseIO.success(smsConfigList);
	}

	@RequestMapping(value = "/config/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<NotificationConfigIO> updateNotificationSMSConfig(@PathVariable("authtoken") String authtoken, @PathVariable("namespaceCode") String namespaceCode, @RequestBody NotificationConfigIO smsConfigIO) throws Exception {
		NotificationConfigIO smsConfig = new NotificationConfigIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		validateNativeNamespace(authDTO, namespaceCode);

		NamespaceDTO namespace = new NamespaceDTO();
		namespace.setCode(namespaceCode);

		NotificationConfigDTO smsConfigDTO = new NotificationConfigDTO();
		smsConfigDTO.setCode(smsConfigIO.getCode());
		smsConfigDTO.setEntityCode(smsConfigIO.getEntityCode());
		smsConfigDTO.setHeaderDltCode(smsConfigIO.getHeaderDltCode());
		smsConfigDTO.setHeader(smsConfigIO.getHeader());
		smsConfigDTO.setNotificationMode(NotificationMediumEM.getNotificationMediumEM(smsConfigIO.getNotificationMode()));
		smsConfigDTO.setActiveFlag(smsConfigIO.getActiveFlag());
		notificationSmsConfigService.Update(authDTO, namespace, smsConfigDTO);
		smsConfig.setCode(smsConfigDTO.getCode());
		return ResponseIO.success(smsConfig);
	}

	@RequestMapping(value = "/template/config/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<NotificationTemplateConfigIO> updateNotificationTemplateSettings(@PathVariable("authtoken") String authtoken, @PathVariable("namespaceCode") String namespaceCode, @RequestBody NotificationTemplateConfigIO smsTemplateConfigIO) throws Exception {
		NotificationTemplateConfigIO smsTemplateConfig = new NotificationTemplateConfigIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		validateNativeNamespace(authDTO, namespaceCode);

		NamespaceDTO namespace = new NamespaceDTO();
		namespace.setCode(namespaceCode);

		NotificationTemplateConfigDTO smsTemplateConfigDTO = new NotificationTemplateConfigDTO();
		smsTemplateConfigDTO.setCode(smsTemplateConfigIO.getCode());
		smsTemplateConfigDTO.setName(StringUtil.isNull(smsTemplateConfigIO.getName(), Text.EMPTY));
		smsTemplateConfigDTO.setTemplateDltCode(smsTemplateConfigIO.getTemplateDltCode());
		smsTemplateConfigDTO.setNotificationType(NotificationTypeEM.getNotificationTypeEM(smsTemplateConfigIO.getNotificationType() != null ? smsTemplateConfigIO.getNotificationType().getCode() : null));
		smsTemplateConfigDTO.setContent(smsTemplateConfigIO.getContent());
		smsTemplateConfigDTO.setActiveFlag(smsTemplateConfigIO.getActiveFlag());

		NotificationConfigDTO smsConfigDTO = new NotificationConfigDTO();
		smsConfigDTO.setCode(smsTemplateConfigIO.getNotificationSMSConfig() != null ? smsTemplateConfigIO.getNotificationSMSConfig().getCode() : null);
		smsTemplateConfigDTO.setNotificationSMSConfig(smsConfigDTO);

		notificationSmsConfigService.updateNotificationTemplateConfig(authDTO, namespace, smsTemplateConfigDTO);
		smsTemplateConfig.setCode(smsTemplateConfigDTO.getCode());
		return ResponseIO.success(smsTemplateConfig);
	}

	@RequestMapping(value = "/template/config/{smsConfigCode}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<NotificationTemplateConfigIO>> getAllNotificationTemplateSettings(@PathVariable("authtoken") String authtoken, @PathVariable("namespaceCode") String namespaceCode, @PathVariable("smsConfigCode") String smsConfigCode) throws Exception {
		List<NotificationTemplateConfigIO> smsTemplateConfigList = new ArrayList<NotificationTemplateConfigIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		validateNativeNamespace(authDTO, namespaceCode);

		NamespaceDTO namespace = new NamespaceDTO();
		namespace.setCode(namespaceCode);

		NotificationConfigDTO smsConfigDTO = new NotificationConfigDTO();
		smsConfigDTO.setCode(smsConfigCode);

		List<NotificationTemplateConfigDTO> list = notificationSmsConfigService.getAllNotificationTemplateConfig(authDTO, namespace, smsConfigDTO);
		for (NotificationTemplateConfigDTO smsTemplateConfigDTO : list) {
			NotificationTemplateConfigIO smsTemplateConfigIO = new NotificationTemplateConfigIO();
			smsTemplateConfigIO.setCode(smsTemplateConfigDTO.getCode());
			smsTemplateConfigIO.setName(smsTemplateConfigDTO.getName());
			smsTemplateConfigIO.setTemplateDltCode(smsTemplateConfigDTO.getTemplateDltCode());

			BaseIO notificationType = new BaseIO();
			notificationType.setCode(smsTemplateConfigDTO.getNotificationType().getCode());
			notificationType.setName(smsTemplateConfigDTO.getNotificationType().getDescription());
			smsTemplateConfigIO.setNotificationType(notificationType);

			smsTemplateConfigIO.setContent(smsTemplateConfigDTO.getContent());
			smsTemplateConfigIO.setActiveFlag(smsTemplateConfigDTO.getActiveFlag());

			// NotificationSMSConfigIO smsConfigIO = new
			// NotificationSMSConfigIO();
			// smsConfigIO.setCode(smsTemplateConfigDTO.getNotificationSMSConfig().getCode());
			// smsConfigIO.setEntityCode(smsTemplateConfigDTO.getNotificationSMSConfig().getEntityCode());
			// smsConfigIO.setHeaderDltCode(smsTemplateConfigDTO.getNotificationSMSConfig().getHeaderDltCode());
			// smsConfigIO.setHeader(smsTemplateConfigDTO.getNotificationSMSConfig().getHeader());
			// smsTemplateConfigIO.setNotificationSMSConfig(smsConfigIO);

			smsTemplateConfigList.add(smsTemplateConfigIO);
		}
		return ResponseIO.success(smsTemplateConfigList);
	}

	@RequestMapping(value = "/types", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<Map<String, Object>>> getNotificationTypes(@PathVariable("authtoken") String authtoken) throws Exception {
		authService.getAuthDTO(authtoken);
		List<Map<String, Object>> notificationTypes = new ArrayList<Map<String, Object>>();
		for (NotificationTypeEM notificationTypeEM : NotificationTypeEM.values()) {
			Map<String, Object> dataMap = new HashMap<String, Object>();
			dataMap.put("code", notificationTypeEM.getCode());
			dataMap.put("name", notificationTypeEM.getDescription());
			dataMap.put("isDynamic", notificationTypeEM.isDynamic());
			notificationTypes.add(dataMap);
		}
		return ResponseIO.success(notificationTypes);

	}

	private void validateNativeNamespace(AuthDTO authDTO, String namespaceCode) {
		if (StringUtil.isNull(namespaceCode) || !authDTO.getNativeNamespaceCode().equals(authDTO.getNamespaceCode())) {
			throw new ServiceException(ErrorCode.UNAUTHORIZED);
		}
	}

}
