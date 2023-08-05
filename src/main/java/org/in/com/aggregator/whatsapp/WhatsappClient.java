package org.in.com.aggregator.whatsapp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.in.com.aggregator.sms.SmsResponse;
import org.in.com.dto.NotificationTemplateConfigDTO;

public abstract class WhatsappClient {

	public static Map<String, String> config = new HashMap<>();

	public abstract SmsResponse send(NotificationTemplateConfigDTO templateConfig, String mobileNumber, String header, List<String> placeholders) throws Exception;

	public String get(String key) {
		return config.get(key);
	}
}
