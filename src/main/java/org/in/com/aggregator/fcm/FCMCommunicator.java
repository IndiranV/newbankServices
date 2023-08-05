package org.in.com.aggregator.fcm;

import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.in.com.utils.HttpServiceClient;
import org.in.com.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class FCMCommunicator {
	public final static String API_URL_FCM = "https://fcm.googleapis.com/fcm/send";
	public final static String API_URL_ALERTZ = "http://alertz.ezeeinfo.in/alertz/ALERTZZY94YCM2XAJ7/";
	private static final Logger logger = LoggerFactory.getLogger("org.in.com.aggregator.fcm.FCMCommunicator");

	public void pushFCMNotification(FCMServerKeyEM serverKeyEM, String userDeviceIdKey, String title, String body, String image, JSONObject data) {
		try {

			JSONObject json = new JSONObject();
			json.put("to", userDeviceIdKey.trim());
			json.put("priority", "normal");
			json.put("collapse_key", StringUtil.removeSymbol(title));

			JSONObject info = new JSONObject();
			info.put("title", title);
			info.put("body", body);
			info.put("sound", "default");
			if (StringUtil.isNotNull(image)) {
				info.put("image", image);
			}
			json.put("notification", info);
			// Data
			json.put("data", data);

			HttpClient httpClient = new HttpServiceClient().getHttpClient();

			HttpPost request = new HttpPost(API_URL_FCM);
			request.addHeader("content-type", "application/json");
			request.addHeader("Authorization", "key=" + serverKeyEM.getKey());

			StringEntity params = new StringEntity(json.toString());
			request.setEntity(params);
			logger.info("{} - {}", serverKeyEM.getCode(), json);
			HttpResponse response = httpClient.execute(request);
			logger.info("{} - {}", serverKeyEM.getCode(), response);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void pushFlutterNotification(FCMServerKeyEM serverKeyEM, List<String> userDeviceIdKey, String title, String body, String image, JSONObject data) {
		try {

			JSONObject json = new JSONObject();
			if (userDeviceIdKey.size() == 1) {
				json.put("to", userDeviceIdKey.get(0).trim());
			}
			else {
				JSONArray keyList = new JSONArray();
				for (String gcm : userDeviceIdKey) {
					keyList.add(gcm);
				}
				json.put("registration_ids", keyList);
			}
			JSONObject info = new JSONObject();
			info.put("title", title);
			info.put("body", body);
			info.put("sound", "default");
			if (StringUtil.isNotNull(image)) {
				info.put("image", image);
			}
			json.put("priority", "high");
			json.put("collapse_key", StringUtil.removeSymbol(title));
			json.put("click_action", "FLUTTER_NOTIFICATION_CLICK");
			json.put("content_available", true);
			// Data
			json.put("data", data);
			json.put("notification", info);

			HttpClient httpClient = new HttpServiceClient().getHttpClient();

			HttpPost request = new HttpPost(API_URL_FCM);
			request.addHeader("content-type", "application/json");
			request.addHeader("Authorization", "key=" + serverKeyEM.getKey());

			StringEntity params = new StringEntity(json.toString());
			request.setEntity(params);
			logger.info("{} - {}", serverKeyEM.getCode(), json);
			HttpResponse response = httpClient.execute(request);
			logger.info("{} - {}", serverKeyEM.getCode(), response);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void alertzService(String url, JSONArray data) {
		try {
			HttpPost request = new HttpPost(API_URL_ALERTZ + url);
			request.addHeader("content-type", "application/json");
			HttpClient httpClient = new HttpServiceClient().getHttpClient();
			StringEntity params = new StringEntity(data.toString());
			request.setEntity(params);
			HttpResponse response = httpClient.execute(request);
			logger.info("{} - {}", data.toString(), response);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		FCMCommunicator fcmCommunicator = new FCMCommunicator();
		fcmCommunicator.pushFCMNotification(FCMServerKeyEM.TRANZKING, "fcCV-nHdHOQ:APA91bEZq4QChL5PDmZSKfHf-GvhREGZJlx4xzchIoBrACj873oADKJhGukWMNOKF9pcbsiLGu-ex4X7hqQh_ywVy1gBcSrLGTkiO1_k8E0L4LtMqYUQoFOoDfPtAgFb76XLJAPKHMqA", "10% Offer", "Book today, get 10%", "", null);
	}
}
