package org.in.com.aggregator.sms.impl;

import java.util.Arrays;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.in.com.aggregator.sms.SmsClient;
import org.in.com.aggregator.sms.SmsResponse;
import org.in.com.constants.Constants;
import org.in.com.constants.Text;
import org.in.com.dto.NotificationTemplateConfigDTO;
import org.in.com.utils.HttpServiceClient;
import org.in.com.utils.StringUtil;

public class QikberrySmsGatewayImpl extends SmsClient {

	@Override
	public SmsResponse send(NotificationTemplateConfigDTO templateConfig, String mobileNumber, String content) throws Exception {
		SmsResponse smsResponse = new SmsResponse();
		try {
			JSONObject dataJSON = new JSONObject();

			JSONObject rootJSON = new JSONObject();
			rootJSON.put("sender", templateConfig.getNotificationSMSConfig().getHeader());
			rootJSON.put("service", templateConfig.getNotificationSMSConfig().isPromotionalSMSType() ? Text.P_UPPER : config.get("qikberry.props.type"));
			rootJSON.put("message", content);
//			rootJSON.put("dlr_url", "http://localhost:9080/busservices/commerce/sms/gateway/status/notification?cid=");
			if (StringUtil.isNotNull(templateConfig.getNotificationSMSConfig().getEntityCode()) && StringUtil.isNotNull(templateConfig.getTemplateDltCode()) && StringUtil.isNotNull(templateConfig.getNotificationSMSConfig().getHeaderDltCode())) {
				rootJSON.put("entity_id", templateConfig.getNotificationSMSConfig().getEntityCode());
				rootJSON.put("header_id", templateConfig.getNotificationSMSConfig().getHeaderDltCode());
				rootJSON.put("template_id", templateConfig.getTemplateDltCode());
			}
			dataJSON.put("root", rootJSON);

			List<String> mobileNumbers = Arrays.asList(mobileNumber.split(Text.COMMA));
			JSONArray mobileArray = new JSONArray();
			for (String mobile : mobileNumbers) {
				JSONObject nodesJSON = new JSONObject();
				nodesJSON.put("to", mobile);
				mobileArray.add(nodesJSON);
			}
			dataJSON.put("nodes", mobileArray);

			String url = config.get("qikberry.props.url") + "/sms/send/json";
			String request = dataJSON.toString();
			String response = Text.EMPTY;
			try {
				HttpClient client = new HttpServiceClient().getHttpClient();
				HttpPost httpPost = new HttpPost(url);
				httpPost.addHeader("Authorization", config.get("qikberry.props.key"));
				httpPost.addHeader("accept", "application/json");
				httpPost.addHeader("content-type", "application/json");
				StringEntity input = new StringEntity(request, ContentType.APPLICATION_JSON);
				input.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
				httpPost.setEntity(input);

				HttpResponse responseData = client.execute(httpPost);
				HttpEntity entity = responseData.getEntity();
				response = EntityUtils.toString(entity, "UTF-8");
			}
			catch (Exception e) {
				response = e.getMessage();
				e.printStackTrace();
			}
			smsResponse.setId(response);
			smsResponse.setCode(Constants.SMS_PROVIDER_QIKBERRY);
			smsResponse.setContent(content);
			smsResponse.setUrl(url);
			smsResponse.setRequest(request);
			smsResponse.setResponse(response);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return smsResponse;
	}

	@Override
	public String getSMSStatus(String messageId) throws Exception {
		String response = Text.EMPTY;
		try {
			String url = config.get("qikberry.props.url") + "/sms/status?cid=" + messageId;

			HttpClient client = new HttpServiceClient().getHttpClient();
			HttpGet httpGet = new HttpGet(url);
			httpGet.addHeader("Authorization", config.get("qikberry.props.key"));
			httpGet.addHeader("accept", "application/json");
			httpGet.addHeader("content-type", "application/json");

			HttpResponse responseData = client.execute(httpGet);
			HttpEntity entity = responseData.getEntity();
			response = EntityUtils.toString(entity, "UTF-8");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}
}
