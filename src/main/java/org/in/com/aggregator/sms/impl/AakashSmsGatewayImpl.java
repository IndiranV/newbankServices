package org.in.com.aggregator.sms.impl;

import java.net.URLEncoder;

import org.in.com.aggregator.sms.SmsClient;
import org.in.com.aggregator.sms.SmsResponse;
import org.in.com.constants.Constants;
import org.in.com.dto.NotificationTemplateConfigDTO;
import org.in.com.utils.HttpServiceClient;

public class AakashSmsGatewayImpl extends SmsClient {

	@Override
	public SmsResponse send(NotificationTemplateConfigDTO templateConfig, String mobileNumber, String content) throws Exception {
		SmsResponse smsResponse = new SmsResponse();
		HttpServiceClient httpClient = new HttpServiceClient();
		String url = config.get("aakash.props.url") + "?auth_token=" + config.get("aakash.props.token") + "&to=" + mobileNumber + "&text=" + URLEncoder.encode(content, "UTF-8");
		String response = httpClient.get(url);
		smsResponse.setId(response);
		smsResponse.setCode(Constants.SMS_PROVIDER_AAKASHSMS);
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
