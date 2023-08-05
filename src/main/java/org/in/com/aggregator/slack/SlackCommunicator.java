package org.in.com.aggregator.slack;

import net.sf.json.JSONObject;

import org.in.com.utils.HttpServiceClient;

public class SlackCommunicator {
	private static String BASE_URL = "https://hooks.slack.com/services/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";
	private static String TOKEN = "xoxp-632221425168-632181699605-730744062579-ec526043d0f9ec82054bb81449b35422";

	public void sendAlert(String message) {
		try {
			JSONObject requestJSON = new JSONObject();
			requestJSON.put("channel", "#bits");
			requestJSON.put("text", message);

			HttpServiceClient httpClient = new HttpServiceClient();
			String jsonData = httpClient.post(BASE_URL, requestJSON.toString(), "application/json", "OAuth 2.0 " + TOKEN);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		SlackCommunicator comm = new SlackCommunicator();
		comm.sendAlert("Test");
	}
}
