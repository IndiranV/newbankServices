package org.in.com.aggregator.mercservices;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.in.com.config.ApplicationConfig;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.utils.HttpServiceClient;
import org.in.com.utils.JsonArrayBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

@Component
public class MercCommunicator {
	private static final Logger logger = LoggerFactory.getLogger("org.in.com.aggregator.mercservices.MercCommunicator");

	public void index(AuthDTO auth, MercEntityTypeEM entityType, String indexDate, JsonArrayBuilder indexJson) {
		try {
			String url = ApplicationConfig.getMercServiceAccessurl() + Text.SLASH + ApplicationConfig.getMercServiceAccessToken() + "/ezeebus/" + entityType.getUrl() + "/index/" + auth.getNamespaceCode() + Text.SLASH + indexDate;
			HttpClient httpClient = new HttpServiceClient().getHttpClient();

			HttpPost request = new HttpPost(url);

			request.addHeader("content-type", "application/json");

			StringEntity params = new StringEntity(indexJson.toJson().toString());
			request.setEntity(params);

			logger.info("url {} {} - {}", url, auth.getNamespaceCode(), indexJson.toJson().toString());
			HttpResponse response = httpClient.execute(request);
			HttpEntity entity = response.getEntity();
			String responseData = EntityUtils.toString(entity, "UTF-8");

			logger.info("{} - {}", auth.getNamespaceCode(), responseData);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public JsonObject search(AuthDTO auth, MercEntityTypeEM entityType, String indexDate, String query) {
		JsonObject json = null;
		try {
			String url = ApplicationConfig.getMercServiceAccessurl() + Text.SLASH + ApplicationConfig.getMercServiceAccessToken() + "/ezeebus/" + entityType.getUrl() + "/search/" + auth.getNamespaceCode() + Text.SLASH + indexDate;

			HttpClient httpClient = new HttpServiceClient().getHttpClient();

			HttpGet request = new HttpGet(url);

			request.addHeader("content-type", "application/json");

			logger.info("{} - {}", auth.getNamespaceCode(), request.getURI());
			HttpResponse response = httpClient.execute(request);
			HttpEntity entity = response.getEntity();
			String responseData = EntityUtils.toString(entity, "UTF-8");
			logger.info("Response : {} ", response);
			json = new Gson().fromJson(responseData, JsonObject.class);

			logger.info("{} - {}", auth.getNamespaceCode(), response);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return json;
	}

	public static void main(String[] args) {
		MercCommunicator fcmCommunicator = new MercCommunicator();
	}
}
