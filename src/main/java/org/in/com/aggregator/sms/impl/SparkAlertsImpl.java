package org.in.com.aggregator.sms.impl;

import java.net.URLEncoder;

import org.in.com.aggregator.sms.SmsClient;
import org.in.com.aggregator.sms.SmsResponse;
import org.in.com.constants.Constants;
import org.in.com.dto.NotificationTemplateConfigDTO;
import org.in.com.utils.HttpServiceClient;

public class SparkAlertsImpl extends SmsClient {

	// MMM Travels Own SMS Gateway
	public SmsResponse send(NotificationTemplateConfigDTO templateConfig, String mobileNumber, String content) throws Exception {
		SmsResponse smsResponse = new SmsResponse();
		String url = config.get("sparksms.props.url") + "?user=" + config.get("sparksms.user.name") + "&pass=" + config.get("sparksms.user.password") + "&service=TRANS&sender=" + templateConfig.getNotificationSMSConfig().getHeader() + "&phone=" + URLEncoder.encode(mobileNumber, "UTF-8") + "&text=" + URLEncoder.encode(content, "UTF-8") + "&stype=normal&popup=false";
		try {
			HttpServiceClient httpClient = new HttpServiceClient();
			String response = httpClient.get(url);
			smsResponse.setId(response);
			smsResponse.setCode(Constants.SMS_PROVIDER_SPARK);
			smsResponse.setContent(content);
			smsResponse.setUrl(url);
			smsResponse.setRequest(content);
			smsResponse.setResponse(response);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return smsResponse;
	}

	@Override
	public String getSMSStatus(String messageId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
