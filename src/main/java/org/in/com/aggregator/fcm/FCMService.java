package org.in.com.aggregator.fcm;

import java.util.List;

import org.in.com.dto.AuthDTO;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public interface FCMService {
	public void pushNotification(AuthDTO authDTO, String serverCode, String userDeviceIdKey, String title, String body, String image, JSONObject data);

	public void pushFlutterNotification(AuthDTO authDTO, String serverCode, List<String> userDeviceIdKey, String title, String body, String image, JSONObject data);

	public void alertzService(AuthDTO authDTO, String channelCode, JSONArray data);
}
