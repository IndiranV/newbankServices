package org.in.com.aggregator.whatsapp.impl;

import java.util.List;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.in.com.aggregator.sms.SmsResponse;
import org.in.com.aggregator.whatsapp.WhatsappClient;
import org.in.com.constants.Constants;
import org.in.com.constants.Text;
import org.in.com.dto.NotificationTemplateConfigDTO;
import org.in.com.utils.HttpServiceClient;
import org.in.com.utils.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import net.sf.json.JSONObject;

public class MyOperatorServiceImpl extends WhatsappClient {
	private static final Logger LOGGER = LoggerFactory.getLogger("org.in.com.aggregator.whatsapp.WhatsappService");

	@Override
	public SmsResponse send(NotificationTemplateConfigDTO templateConfig, String mobileNumber, String header, List<String> placeholders) throws Exception {
		SmsResponse smsResponse = new SmsResponse();
		String response = Text.EMPTY;
		String url = Text.EMPTY;
		try {
			for (String mobile : mobileNumber.split(Text.COMMA)) {
				url = config.get("myoperator.props.url");

				JSONObject jsonObject = new JSONObject();
				jsonObject.put("apiKey", config.get("myoperator.service.key"));
				jsonObject.put("campaignName", templateConfig.getTemplateDltCode());
				jsonObject.put("destination", "+91" + mobile);
				jsonObject.put("userName", mobile);
				jsonObject.put("templateParams", placeholders);

				HttpServiceClient client = new HttpServiceClient();
				Header[] headers = { new BasicHeader(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE) };
				LOGGER.info("req: {} {}", "+91" + mobileNumber, jsonObject.toString());
				response = client.postSSLV2(url, jsonObject.toString(), headers);
			}
		}
		catch (Exception e) {
			response = e.getMessage();
			LOGGER.error("", e);
		}
		finally {
			smsResponse.setId(response);
			smsResponse.setCode(Constants.WHATSAPP_PROVIDER_MYOPERATOR);
			smsResponse.setContent(JSONUtil.listToJsonString(placeholders));
			smsResponse.setUrl(url);
			smsResponse.setResponse(response);
		}
		return smsResponse;
	}

}
