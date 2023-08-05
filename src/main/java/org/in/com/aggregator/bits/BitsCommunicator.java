package org.in.com.aggregator.bits;

import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.in.com.config.ApplicationConfig;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.IntegrationDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.NamespaceZoneEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.utils.HttpServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class BitsCommunicator {
	private static final Logger TICKE_TEVENT_PUSH_LOGGER = LoggerFactory.getLogger("org.in.com.aggregator.bits.BitsCommunicator");

	private static String API_URL = "http://app.ezeebits.com/busservices";

	private String getZoneURL() {
		return API_URL;
	}

	public JSONObject getZoneSyncAmenties(String authToken, String syncDate) {
		JSONObject json = null;
		try {
			String url = "/" + authToken + "/amenties/zonesync?syncDate=" + URLEncoder.encode(syncDate, "UTF-8");
			HttpServiceClient httpClient = new HttpServiceClient();
			String jsonData = httpClient.get(getZoneURL() + url);
			json = JSONObject.fromObject(jsonData);
		}
		catch (Exception e) {
			throw new ServiceException(ErrorCode.INVALID_STATION);
		}
		return json;
	}

	public JSONObject getZoneSyncStation(String authToken, String syncDate) {
		JSONObject json = null;
		try {
			String url = "/" + authToken + "/stations/zonesync?syncDate=" + URLEncoder.encode(syncDate, "UTF-8");
			HttpServiceClient httpClient = new HttpServiceClient();
			String jsonData = httpClient.get(getZoneURL() + url);
			json = JSONObject.fromObject(jsonData);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(ErrorCode.INVALID_STATION);
		}
		return json;
	}

	public JSONObject getZoneSyncReportQuery(String authToken, String syncDate) {
		JSONObject json = null;
		try {
			String url = "/" + authToken + "/report/zonesync?syncDate=" + URLEncoder.encode(syncDate, "UTF-8");
			HttpServiceClient httpClient = new HttpServiceClient();
			String jsonData = httpClient.get(getZoneURL() + url);
			json = JSONObject.fromObject(jsonData);
		}
		catch (Exception e) {
			throw new ServiceException(ErrorCode.INVALID_TRIP_CODE);
		}
		return json;
	}

	public JSONObject getZoneSyncMenu(String authToken, String syncDate) {
		JSONObject json = null;
		try {
			String url = "/" + authToken + "/menu/zonesync?syncDate=" + URLEncoder.encode(syncDate, "UTF-8");
			HttpServiceClient httpClient = new HttpServiceClient();
			String jsonData = httpClient.get(getZoneURL() + url);
			json = JSONObject.fromObject(jsonData);
		}
		catch (Exception e) {
			throw new ServiceException(ErrorCode.INVALID_STATION);
		}
		return json;
	}

	public JSONObject getBitsConfigure(AuthDTO authDTO) {
		JSONObject json = null;
		try {
			HttpServiceClient httpClient = new HttpServiceClient();
			String jsonData = httpClient.get("http://bits.config.ezeebits.com/" + authDTO.getNamespaceCode() + ".json");
			json = JSONObject.fromObject(jsonData);
		}
		catch (java.io.IOException e) {
			System.out.println(authDTO.getNamespaceCode() + " config not found");
			throw new ServiceException(ErrorCode.INVALID_USER_CODE);
		}
		catch (ServiceException e) {
			throw new ServiceException(ErrorCode.INVALID_USER_CODE);
		}
		catch (Exception e) {
			System.out.println(authDTO.getNamespaceCode() + " config not found");
			e.printStackTrace();
			throw new ServiceException(ErrorCode.INVALID_USER_CODE);
		}
		return json;
	}

	public JSONObject getNamespaceConfigure(AuthDTO authDTO) {
		JSONObject json = null;
		try {
			HttpServiceClient httpClient = new HttpServiceClient();
			String jsonData = httpClient.get("http://bits.config.ezeebits.com/namespace.json");
			json = JSONObject.fromObject(jsonData);
		}
		catch (Exception e) {
			throw new ServiceException(ErrorCode.UNABLE_TO_PROVIDE_DATA);
		}
		return json;
	}

	public JSONObject getNotificationConfigure() {
		JSONObject json = null;
		try {
			HttpServiceClient httpClient = new HttpServiceClient();
			String jsonData = httpClient.get("http://bits.config.ezeebits.com/app/sms/" + ApplicationConfig.getServerZoneCode() + ".json");
			json = JSONObject.fromObject(jsonData);
		}
		catch (Exception e) {
			throw new ServiceException(ErrorCode.UNABLE_TO_PROVIDE_DATA);
		}
		return json;
	}

	public void serverPaymentResponseHandler(NamespaceZoneEM namespaceZone, Map<String, String> responseParam) {
		try {
			HttpClient client = new HttpServiceClient().getHttpClient();
			HttpPost httpPost = new HttpPost(namespaceZone.getDomainURL() + "/busservices/commerce/payment/status/notification");
			httpPost.addHeader("accept", "application/x-www-form-urlencoded");
			httpPost.addHeader("content-type", "application/x-www-form-urlencoded");

			StringBuilder requestParam = new StringBuilder();
			for (Entry<String, String> param : responseParam.entrySet()) {
				requestParam.append(param.getKey());
				requestParam.append("=");
				requestParam.append(param.getValue());
				requestParam.append("&");
			}

			StringEntity input = new StringEntity(requestParam.toString(), ContentType.APPLICATION_FORM_URLENCODED);
			input.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded"));
			httpPost.setEntity(input);

			HttpResponse responseData = client.execute(httpPost);
			HttpEntity entity = responseData.getEntity();
			String response = EntityUtils.toString(entity, "UTF-8");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void razorpayServerPaymentResponseHandler(NamespaceZoneEM namespaceZone, JSONObject data) {
		try {
			String url = namespaceZone.getDomainURL() + "/busservices/commerce/payment/status/notification/razorpay";
			HttpServiceClient httpClient = new HttpServiceClient();
			String response = httpClient.post(url, data.toString());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public JSONArray getReportConfigure(AuthDTO authDTO) {
		JSONArray json = null;
		try {
			HttpServiceClient httpClient = new HttpServiceClient();
			String jsonData = httpClient.get("http://config.ezeebits.com/json/report/" + authDTO.getNamespaceCode() + ".json");
			json = JSONArray.fromObject(jsonData);
		}
		catch (Exception e) {
			throw new ServiceException(ErrorCode.UNABLE_TO_PROVIDE_DATA);
		}
		return json;
	}

	public JSONObject getUsers(AuthDTO authDTO, IntegrationDTO integration) {
		JSONObject json = null;
		try {
			String url = "/bits/" + integration.getAccount() + "/users";
			HttpServiceClient httpClient = new HttpServiceClient();
			String jsonData = httpClient.get(integration.getAccessUrl() + url);
			json = JSONObject.fromObject(jsonData);
		}
		catch (Exception e) {
			throw new ServiceException(ErrorCode.INVALID_CREDENTIALS);
		}
		return json;
	}

	public JSONObject getAuthToken(IntegrationDTO integration, UserDTO user) {
		JSONObject json = null;
		try {
			String url = "/auth/getAuthToken?namespaceCode=" + integration.getAccount() + "&username=" + user.getUsername() + "&password=" + user.getOldPassword() + "&devicemedium=WEB&authenticationTypeCode=BITSUP&userFirstName=" + user.getUsername();
			HttpServiceClient httpClient = new HttpServiceClient();
			String jsonData = httpClient.post(integration.getAccessUrl() + url, new JSONObject().toString());
			json = JSONObject.fromObject(jsonData);
		}
		catch (Exception e) {
			throw new ServiceException(ErrorCode.INVALID_USER_CODE);
		}
		return json;
	}

	public JSONObject getVerifyAuthToken(IntegrationDTO integration, String authToken) {
		JSONObject json = null;
		try {
			String url = "/auth/" + authToken + "/verify/profile";
			HttpServiceClient httpClient = new HttpServiceClient();
			String jsonData = httpClient.post(integration.getAccessUrl() + url, new JSONObject().toString());
			json = JSONObject.fromObject(jsonData);
		}
		catch (Exception e) {
			throw new ServiceException(ErrorCode.INVALID_CREDENTIALS);
		}
		return json;
	}

	public JSONObject getAuthTokenV2(IntegrationDTO integration, UserDTO user) {
		JSONObject json = null;
		try {
			String url = "/auth/authenticate?namespaceCode=" + integration.getAccount() + "&username=" + user.getUsername() + "&password=" + user.getOldPassword() + "&devicemedium=WEB";
			HttpServiceClient httpClient = new HttpServiceClient();
			String jsonData = httpClient.post(integration.getAccessUrl() + url, new JSONObject().toString());
			json = JSONObject.fromObject(jsonData);
		}
		catch (Exception e) {
			throw new ServiceException(ErrorCode.INVALID_USER_CODE);
		}
		return json;
	}

	public JSONObject getVerifyAuthTokenV2(IntegrationDTO integration, String authToken) {
		JSONObject json = null;
		try {
			String url = "/auth/internal/verify/profile";
			String jsonData = makeAPIRequestConnection(integration.getAccessUrl() + url, new JSONObject(), authToken);
			json = JSONObject.fromObject(jsonData);
		}
		catch (Exception e) {
			throw new ServiceException(ErrorCode.INVALID_CREDENTIALS);
		}
		return json;
	}

	private String makeAPIRequestConnection(String url, JSONObject jsonObject, String authToken) {
		String response = null;
		try {
			HttpClient client = new HttpServiceClient().getHttpClient();
			HttpPost httpPost = new HttpPost(url);
			httpPost.addHeader("Authorization", "Bearer " + authToken);

			StringEntity input = new StringEntity(jsonObject.toString(), ContentType.APPLICATION_JSON);
			input.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
			httpPost.setEntity(input);

			HttpResponse responseData = client.execute(httpPost);
			HttpEntity entity = responseData.getEntity();
			response = EntityUtils.toString(entity, "UTF-8");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	public JSONObject syncVertexFareRule(String fareRuleCode) {
		JSONObject json = null;
		try {
			String url = "https://vertex.ezeeinfo.in/api/farerule/" + fareRuleCode;
			HttpServiceClient httpClient = new HttpServiceClient();
			String jsonData = httpClient.getSSL(url);
			json = JSONObject.fromObject(jsonData);
		}
		catch (Exception e) {
			throw new ServiceException(ErrorCode.INVALID_STATION);
		}
		return json;
	}

	public JSONObject getZoneSyncFareRuleDetails(String authToken, String fareRuleCode, String syncDate) {
		JSONObject json = null;
		try {
			String url = "/" + authToken + "/fare/rules/" + fareRuleCode + "/details/zonesync?syncDate=" + URLEncoder.encode(syncDate, "UTF-8");
			HttpServiceClient httpClient = new HttpServiceClient();
			String jsonData = httpClient.get("http://app.ezeebits.com/busservices" + url);
			json = JSONObject.fromObject(jsonData);
		}
		catch (Exception e) {
			throw new ServiceException(ErrorCode.INVALID_CREDENTIALS);
		}
		return json;
	}

	public JSONObject getZoneSyncStationOtaPartner(String authToken, String syncDate) {
		JSONObject json = null;
		try {
			String url = "/" + authToken + "/stations/ota/zonesync?syncDate=" + URLEncoder.encode(syncDate, "UTF-8");
			HttpServiceClient httpClient = new HttpServiceClient();
			String jsonData = httpClient.get("http://app.ezeebits.com/busservices" + url);
			json = JSONObject.fromObject(jsonData);
		}
		catch (Exception e) {
			throw new ServiceException(ErrorCode.INVALID_CREDENTIALS);
		}
		return json;
	}

	public JSONObject getZoneSyncStationArea(String authToken, String syncDate) {
		JSONObject json = null;
		try {
			String url = "/" + authToken + "/stations/area/zonesync?syncDate=" + URLEncoder.encode(syncDate, "UTF-8");
			HttpServiceClient httpClient = new HttpServiceClient();
			String jsonData = httpClient.get("http://app.ezeebits.com/busservices" + url);
			json = JSONObject.fromObject(jsonData);
		}
		catch (Exception e) {
			throw new ServiceException(ErrorCode.INVALID_CREDENTIALS);
		}
		return json;
	}
	
	public JSONObject getZoneSyncCalendarAnnouncement(String bitsAccessToken, String syncDate) {
		JSONObject json = null;
		try {
			String url = "/api/json/" + bitsAccessToken + "/cron/calendar/announcement/zonesync?syncDate=" + URLEncoder.encode(syncDate, "UTF-8");
			HttpServiceClient httpClient = new HttpServiceClient();
			String jsonData = httpClient.get("http://app.ezeebits.com/busservices" + url);
			json = JSONObject.fromObject(jsonData);
		}
		catch (Exception e) {
			throw new ServiceException(ErrorCode.INVALID_CREDENTIALS);
		}
		return json;
	}

}