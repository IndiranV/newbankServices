/**
 *
 */
package org.in.com.aggregator.sms;

import java.util.HashMap;
import java.util.Map;

import org.in.com.dto.NotificationTemplateConfigDTO;

public abstract class SmsClient {
	public static Map<String, String> config = new HashMap<>();

	public abstract SmsResponse send(NotificationTemplateConfigDTO templateConfig, String mobileNumber, String content) throws Exception;

	public abstract String getSMSStatus(String messageId) throws Exception;

	public String get(String key) {
		return config.get(key);
	}

}
