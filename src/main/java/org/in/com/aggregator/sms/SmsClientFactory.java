package org.in.com.aggregator.sms;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import org.in.com.aggregator.bits.BitsServiceImpl;
import org.in.com.constants.Text;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmsClientFactory {
	private static final Logger LOGGER = LoggerFactory.getLogger(SmsClientFactory.class);
	public static Map<String, String> SMS_CONFIG_MAP = new HashMap<>();

	private static SmsClientFactory _instance;

	private SmsClientFactory() {
	}

	public static synchronized SmsClientFactory getInstance() {
		if (null == _instance) {
			_instance = new SmsClientFactory();
		}
		return _instance;
	}

	public SmsClient getSmsClient(SMSProviderEM smsProvider, String namespaceCode, String notificationType) throws Exception {
		if (smsProvider.getId() == SMSProviderEM.DEFAULT.getId()) {
			if (SMS_CONFIG_MAP == null || SMS_CONFIG_MAP.isEmpty() || StringUtil.isNull(SMS_CONFIG_MAP.get(notificationType))) {
				loadSMSGatewayConfig(notificationType);
			}

			String providerCode = SMS_CONFIG_MAP.get(notificationType);
			if (StringUtil.isNull(providerCode) || StringUtil.isNull(providerCode)) {
				providerCode = SMS_CONFIG_MAP.get(Text.DEFAULT);
			}
			smsProvider = SMSProviderEM.getSMSProviderEM(providerCode);
		}

		return getSMSGatewayInstance(smsProvider.getImpl());
	}

	private static SmsClient getSMSGatewayInstance(String implName) {
		SmsClient gatewayInstance = null;
		String pgClassName = "org.in.com.aggregator.sms.impl." + implName;
		try {
			Class<?> gatewayClass = Class.forName(pgClassName);
			gatewayInstance = (SmsClient) gatewayClass.newInstance();
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

	private static void loadSMSGatewayConfig(String notificationType) {
		BitsServiceImpl smsServiceImpl = new BitsServiceImpl();
		JSONObject jsonObject = smsServiceImpl.getNotificationConfigure();

		// Namespace_Notification Mapping
		JSONObject providerJSON = jsonObject.getJSONObject(Text.PROVIDER_SMS);
		String providerCode = null;
		if (providerJSON != null && providerJSON.has(notificationType) && StringUtil.isNotNull(providerJSON.getString(notificationType))) {
			providerCode = providerJSON.getString(notificationType);
		}
		// Load Default Provider
		if (StringUtil.isNull(providerCode)) {
			providerCode = providerJSON.getString(Text.DEFAULT);
		}

		SMS_CONFIG_MAP.put(notificationType, providerCode);
		System.out.println(notificationType + Text.COLON + providerCode);
	}

	public static void clearSMSConfig() {
		SMS_CONFIG_MAP.clear();
	}
}
