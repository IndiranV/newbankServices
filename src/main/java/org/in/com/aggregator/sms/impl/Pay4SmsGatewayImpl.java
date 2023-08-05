package org.in.com.aggregator.sms.impl;

import java.net.URLEncoder;

import org.in.com.aggregator.sms.SmsClient;
import org.in.com.aggregator.sms.SmsResponse;
import org.in.com.constants.Constants;
import org.in.com.dto.NotificationTemplateConfigDTO;
import org.in.com.utils.HttpServiceClient;

public class Pay4SmsGatewayImpl extends SmsClient {

	@Override
	public SmsResponse send(NotificationTemplateConfigDTO templateConfig, String mobileNumber, String content) throws Exception {
		SmsResponse smsResponse = new SmsResponse();
		HttpServiceClient httpClient = new HttpServiceClient();
		String url = config.get("pay4sms.props.url") + "?token=" + config.get("pay4sms.props.token") + "&credit=2&sender=" + templateConfig.getNotificationSMSConfig().getHeader() + "&message=" + URLEncoder.encode(content, "UTF-8") + "&number=" + URLEncoder.encode(mobileNumber, "UTF-8") + "&templateid="+templateConfig.getTemplateDltCode();
		String response = httpClient.get(url);
		smsResponse.setId(response);
		smsResponse.setCode(Constants.SMS_PROVIDER_PAY4SMS);
		smsResponse.setContent(content);
		smsResponse.setUrl(url);
		smsResponse.setRequest(content);
		smsResponse.setResponse(response);
		return smsResponse;
	}

	@Override
	public String getSMSStatus(String messageId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
