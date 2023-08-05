package org.in.com.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.in.com.aggregator.mail.EmailServiceImpl;
import org.in.com.constants.Text;

import com.google.common.collect.Maps;

public class BitsShortURL {
	public enum TYPE {
		TMP("TMP"), PER("PER"), MXD("MXD");

		private String code;

		TYPE(final String code) {
			this.code = code;
		}

		public String getCode() {
			return code;
		}
	}

	public static String getUrlshortener(String url, TYPE type) {
		String shorternData = Text.EMPTY;
		String requestURL = Text.EMPTY;
		String shorternURL = Text.EMPTY;
		try {
			requestURL = "http://shorturl.trackbus.in/url/shortenV2";
			HttpServiceClient httpClient = new HttpServiceClient();
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("type", type.getCode()));
			params.add(new BasicNameValuePair("url", url));

			shorternData = httpClient.post(requestURL, params, null);
			JSONObject shorternObj = JSONObject.fromObject(shorternData);

			if (shorternObj != null && shorternObj.getInt("status") == 1 && shorternObj.has("data") && shorternObj.getJSONObject("data") != null && shorternObj.getJSONObject("data").has("url") && StringUtil.isNotNull(shorternObj.getJSONObject("data").getString("url"))) {
				shorternURL = shorternObj.getJSONObject("data").getString("url");
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		finally {
			if (StringUtil.isNull(shorternURL)) {
				Map<String, Object> dataModel = Maps.newHashMap();
				dataModel.put("data", url);
				dataModel.put("request", requestURL);
				dataModel.put("response", shorternData);

				EmailServiceImpl emailServiceImpl = new EmailServiceImpl();
				emailServiceImpl.sendBitsShortURLFailureEmail(dataModel);
			}
		}
		return shorternURL;
	}

	public static String getUrlshortenerOTP(String data, TYPE type) {
		String shorternData = Text.EMPTY;
		String requestURL = Text.EMPTY;
		String otp = Text.EMPTY;
		try {
			requestURL = "http://shorturl.trackbus.in/url/shortenV2";
			HttpServiceClient httpClient = new HttpServiceClient();
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("type", type.getCode()));
			params.add(new BasicNameValuePair("data", data));

			shorternData = httpClient.post(requestURL, params, null);
			JSONObject shorternObj = JSONObject.fromObject(shorternData);

			if (shorternObj != null && shorternObj.getInt("status") == 1 && shorternObj.has("data") && shorternObj.getJSONObject("data") != null && shorternObj.getJSONObject("data").has("slug") && StringUtil.isNotNull(shorternObj.getJSONObject("data").getString("slug"))) {
				otp = shorternObj.getJSONObject("data").getString("slug");
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return otp;
	}

	public static JSONObject decryptShorternOTP(String otp, TYPE type, int expireFlag) {
		JSONObject jsonObject = null;
		try {
			HttpServiceClient httpClient = new HttpServiceClient();
			String shorterndata = httpClient.get("http://shorturl.trackbus.in/url/lengthen?slug=" + otp + "&type=" + type.getCode() + "&expireFlag=" + expireFlag);
			jsonObject = JSONObject.fromObject(shorterndata);
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return jsonObject;
	}

	public static void main(String args[]) throws Exception {
		System.out.println(getUrlshortener("http://www.google.com?a=1234&b=1234&c=098", TYPE.TMP));
	}
}
