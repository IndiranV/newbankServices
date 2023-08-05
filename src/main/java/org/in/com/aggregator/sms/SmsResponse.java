package org.in.com.aggregator.sms;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Data;

@Data
public class SmsResponse implements Serializable {

	/** System generated Serial Version UID. */
	private static final long serialVersionUID = 7341298540670043881L;
	private static final Pattern PATTERN = Pattern.compile("<sms><id>(.*?)</id>");

	private String id;
	private String code;
	private String content;
	private String response;
	private String request;
	private String url;

	public String toString() {
		return "code: " + code + " Content: " + content + " Req: " + request + " Res:" + response + " URL: " + url;
	}

	public String getResponse() {
		String responseId = null;
		try {
			Matcher matcher = PATTERN.matcher(response);
			if (matcher.find()) {
				responseId = matcher.group(1);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return responseId;
	}

	public String getResponseV2() {
		return response;
	}
}
