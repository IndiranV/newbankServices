package org.in.com.aggregator.sms.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.in.com.aggregator.sms.SmsClient;
import org.in.com.aggregator.sms.SmsResponse;
import org.in.com.constants.Constants;
import org.in.com.constants.Text;
import org.in.com.dto.NotificationTemplateConfigDTO;
import org.in.com.utils.StringUtil;

public class SolutionsInfiniImpl extends SmsClient {

	public void addSslCertificate() throws NoSuchAlgorithmException, KeyManagementException {

		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

			}

			public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

			}

		} };

		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

		// Create all-trusting host name verifier
		HostnameVerifier allHostsValid = new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};
		// Install the all-trusting host verifier
		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	}

	/*
	 * (non-Javadoc)
	 */
	@Override
	public SmsResponse send(NotificationTemplateConfigDTO templateConfig, String mobileNumber, String content) throws Exception {
		SmsResponse smsResponse = new SmsResponse();
		String response = Text.EMPTY;

		JSONObject dataJSON = new JSONObject();
		dataJSON.put("sender", templateConfig.getNotificationSMSConfig().getHeader());

		List<String> mobileNumbers = Arrays.asList(mobileNumber.split(Text.COMMA));
		JSONArray smsArray = new JSONArray();
		for (String mobile : mobileNumbers) {
			JSONObject smsJSON = new JSONObject();
			smsJSON.put("to", mobile);
			smsJSON.put("message", content);
			smsArray.add(smsJSON);
		}
		dataJSON.put("sms", smsArray);
		String jsonEncode = URLEncoder.encode(dataJSON.toString(), "UTF-8");
		String requestURL = config.get("infini.props.url.json") + "/v4/?api_key=" + config.get("infini.props.key") + "&method=sms.json&json=" + jsonEncode;

		if (StringUtil.isNotNull(templateConfig.getNotificationSMSConfig().getEntityCode()) && StringUtil.isNotNull(templateConfig.getTemplateDltCode())) {
			requestURL = requestURL.concat("&entity_id=" + templateConfig.getNotificationSMSConfig().getEntityCode() + "&template_id=" + templateConfig.getTemplateDltCode());
		}

		try {
			addSslCertificate();

			URL url = new URL(requestURL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.getOutputStream();
			con.getInputStream();
			BufferedReader rd;
			String line;
			rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
			while ((line = rd.readLine()) != null) {
				response += line;
			}
			rd.close();
		}
		catch (Exception e) {
			response = e.getMessage();
			e.printStackTrace();
		}
		smsResponse.setId(response);
		smsResponse.setCode(Constants.SMS_PROVIDER_INFINI);
		smsResponse.setContent(content);
		smsResponse.setUrl(requestURL);
		smsResponse.setRequest(dataJSON.toString());
		smsResponse.setResponse(response);
		return smsResponse;
	}

	public SmsResponse getSMSLog(String mobileNumber) throws Exception {
		SmsResponse smsResponse = new SmsResponse();
		try {
			addSslCertificate();
			URL url = new URL(config.get("infini.props.url") + "/api/v3/index.php?method=lookup&api_key=" + config.get("infini.props.key") + "&to=" + mobileNumber);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.getOutputStream();
			con.getInputStream();
			BufferedReader rd;
			String line;
			String response = "";
			rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
			while ((line = rd.readLine()) != null) {
				response += line;
			}
			rd.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return smsResponse;
	}

	public String getSMSStatus(String messageId) throws Exception {
		String response = "";
		try {
			addSslCertificate();
			URL url = new URL(config.get("infini.props.url") + "/api/v3/index.php?method=sms.status&api_key=" + config.get("infini.props.key") + "&id=" + messageId);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.getOutputStream();
			con.getInputStream();
			BufferedReader rd;
			String line;
			rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
			while ((line = rd.readLine()) != null) {
				response += line;
			}
			rd.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	public static void main(String a[]) {
		try {
			SolutionsInfiniImpl impl = new SolutionsInfiniImpl();
			// impl.getSMSLog("917667931001");
			impl.getSMSStatus("3655215285-1");
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
