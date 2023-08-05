package org.in.com.aggregator.whatsapp;

import java.util.HashMap;
import java.util.Map;

import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.enumeration.NotificationTypeEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WhatsappClientFactory {
	private static final Logger LOGGER = LoggerFactory.getLogger("org.in.com.aggregator.whatsapp.WhatsappService");
	public static Map<String, String> WHATSAPP_CONFIG_MAP = new HashMap<>();

	private static WhatsappClientFactory _instance;

	private WhatsappClientFactory() {
	}

	public static synchronized WhatsappClientFactory getInstance() {
		if (null == _instance) {
			_instance = new WhatsappClientFactory();
		}
		return _instance;
	}

	public WhatsappClient getWhatsappClient(WhatsappProviderEM whatsappProvider, String namespaceCode, String notificationType) throws Exception {
		return getWhatsappGatewayInstance(whatsappProvider.getImpl());
	}

	private static WhatsappClient getWhatsappGatewayInstance(String implName) {
		WhatsappClient gatewayInstance = null;
		String pgClassName = "org.in.com.aggregator.whatsapp.impl." + implName;
		try {
			if (StringUtil.isNotNull(implName)) {
				Class<?> gatewayClass = Class.forName(pgClassName);
				gatewayInstance = (WhatsappClient) gatewayClass.newInstance();
			}
			if (gatewayInstance == null) {
				LOGGER.error("WhatsappClient is not found");
				throw new ServiceException(ErrorCode.NO_GATEWAY_FOUND);
			}
		}
		catch (ClassNotFoundException e) {
			LOGGER.error("{} does not exist ,please create one in the same package,if exists check for the class name", e, pgClassName);
			throw new ServiceException(ErrorCode.NO_GATEWAY_FOUND);
		}
		catch (Exception e) {
			LOGGER.error("There is a problem in instatiating the class {} ,check for the modifiers of the class", e, pgClassName);
			throw new ServiceException(ErrorCode.NO_GATEWAY_FOUND);
		}
		return gatewayInstance;
	}

	public static String getSenderCode(AuthDTO authDTO, NotificationTypeEM notificationType, String defaultSenderCode) {
		String namespaceSender = authDTO.getNamespace().getProfile().getWhatsappSenderName();
		if (StringUtil.isNull(namespaceSender)) {
			namespaceSender = defaultSenderCode;
		}
		return namespaceSender;
	}

	public static void clearSMSConfig() {
		WHATSAPP_CONFIG_MAP.clear();
	}
}
