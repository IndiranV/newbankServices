package org.in.com.aggregator.sms.impl;

import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.in.com.aggregator.sms.SmsClient;
import org.in.com.aggregator.sms.SmsResponse;
import org.in.com.constants.Constants;
import org.in.com.constants.Text;
import org.in.com.dto.NotificationTemplateConfigDTO;
import org.in.com.utils.HttpServiceClient;

import net.sf.json.JSONObject;

public class KaleyraSmsGatewayImpl extends SmsClient {

	@Override
	public SmsResponse send(NotificationTemplateConfigDTO templateConfig, String mobileNumber, String content) throws Exception {
		SmsResponse smsResponse = new SmsResponse();
		String response = Text.EMPTY;
		String url = Text.EMPTY;
		try {
			url = config.get("kaleyra.props.url") + config.get("kaleyra.props.sid") + "/messages";
			JSONObject jsonObject = new JSONObject();
			String phoneNumber = mobileNumber.replaceAll("\\b(\\d{10})\\b", "+91$1");
			jsonObject.put("to", phoneNumber);
			jsonObject.put("sender", templateConfig.getNotificationSMSConfig().getHeader());
			String type = null;
			boolean isMatch = true;
			Pattern pattern = Pattern.compile(".*\\d.*");
			isMatch = pattern.matcher(templateConfig.getNotificationSMSConfig().getHeader()).matches();
			if (isMatch) {
				type = "MKT";
			}
			else {
				type = "TXN";
			}
			jsonObject.put("type", type);
			jsonObject.put("body", content);
			jsonObject.put("template_id", templateConfig.getTemplateDltCode());
			HttpClient httpClient = new HttpServiceClient().getHttpClient();

			HttpPost request = new HttpPost(url);
			request.addHeader("api-key", config.get("kaleyra.props.key"));
			request.addHeader("content-type", "application/json");

			StringEntity params = new StringEntity(jsonObject.toString());
			request.setEntity(params);

			HttpResponse httpResponse = httpClient.execute(request);
			HttpEntity entity = httpResponse.getEntity();
			response = EntityUtils.toString(entity, "UTF-8");

		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			smsResponse.setId(response);
			smsResponse.setCode(Constants.SMS_PROVIDER_KALEYRA);
			smsResponse.setContent(content);
			smsResponse.setUrl(url);
			smsResponse.setRequest(content);
			smsResponse.setResponse(response);
		}
		return smsResponse;
	}

	@Override
	public String getSMSStatus(String messageId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
