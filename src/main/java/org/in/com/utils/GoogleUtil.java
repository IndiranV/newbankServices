package org.in.com.utils;

import javax.ws.rs.core.MediaType;

import net.sf.json.JSONObject;

public class GoogleUtil {
	public static String getUrlshortener(String trackUrl) {
		try {
			JSONObject longUrl = new JSONObject();
			longUrl.put("longUrl", trackUrl);
			HttpServiceClient httpClient = new HttpServiceClient();
			// Account javakarsoft at Gmail.com
			String shorterndata = httpClient.post("https://www.googleapis.com/urlshortener/v1/url?key=AIzaSyD9wBzVd9lLMRhoPXyauSktJk1S6z4Qefs", longUrl.toString(), MediaType.APPLICATION_JSON);
			JSONObject shorternObj = JSONObject.fromObject(shorterndata);
			trackUrl = shorternObj.getString("id");
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return trackUrl;
	}
}
