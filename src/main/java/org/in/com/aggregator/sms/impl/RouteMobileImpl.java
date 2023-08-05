package org.in.com.aggregator.sms.impl;

import java.net.URLEncoder;

import org.in.com.aggregator.sms.SmsResponse;
import org.in.com.utils.HttpServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RouteMobileImpl extends RouteMobileClient {

	String mobileNumber = "656232";
	String content = "sjdhjd";

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(RouteMobileImpl.class);

	public RouteMobileImpl() throws Exception {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public SmsResponse sendMessage(String mobileNumber, String content) throws Exception {
		SmsResponse smsResponse = new SmsResponse();

		config.put("msg.mobileNumber", mobileNumber);
		config.put("msg.content", content);

		try {
			String url = config.get("routesms.props.url") + "?username=" + config.get("routesms.user.name") + "&password=" + config.get("routesms.user.password") + "&type=0&dlr=0&destination=" + URLEncoder.encode(mobileNumber, "UTF-8") + "&source=TSTSMS&message=" + URLEncoder.encode(content, "UTF-8") + "&url=KKKK";

			HttpServiceClient httpClient = new HttpServiceClient();

			String response = httpClient.get(url);

			smsResponse.setContent(content);
			smsResponse.setResponse(response);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return smsResponse;
	}

	@SuppressWarnings("unused")
	private static StringBuffer convertToUnicode(String regText) {
		char[] chars = regText.toCharArray();
		StringBuffer hexString = new StringBuffer();
		for (int i = 0; i < chars.length; i++) {
			String iniHexString = Integer.toHexString((int) chars[i]);
			if (iniHexString.length() == 1)
				iniHexString = "000" + iniHexString;
			else if (iniHexString.length() == 2)
				iniHexString = "00" + iniHexString;
			else if (iniHexString.length() == 3)
				iniHexString = "0" + iniHexString;
			hexString.append(iniHexString);
		}
		return hexString;
	}
}
