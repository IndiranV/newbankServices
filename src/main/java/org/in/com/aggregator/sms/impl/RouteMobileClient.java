package org.in.com.aggregator.sms.impl;

import java.util.Map;
import java.util.Properties;

import org.in.com.aggregator.sms.SmsResponse;

import com.google.common.collect.Maps;

public abstract class RouteMobileClient {

	protected Map<String, String> config;

	public RouteMobileClient() throws Exception {
		Properties smsProps = new Properties();
		smsProps.load(this.getClass().getResourceAsStream("/sms.properties"));
		config = Maps.newLinkedHashMap(Maps.fromProperties(smsProps));
	}

	public abstract SmsResponse sendMessage(String mobileNumber, String content) throws Exception;

	public String get(String key) {
		return config.get(key);
	}
}
