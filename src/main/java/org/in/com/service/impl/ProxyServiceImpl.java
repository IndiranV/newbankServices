package org.in.com.service.impl;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.in.com.dto.ProxyDTO;
import org.in.com.service.ProxyService;
import org.in.com.utils.HttpServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Service
public class ProxyServiceImpl implements ProxyService {

	private static final Logger proxylogger = LoggerFactory.getLogger("org.in.com.proxy");

	@Override
	public String processRequest(ProxyDTO proxy) {
		String response = null;

		try {
			HttpClient client = new HttpServiceClient().getHttpClient();
			HttpPost httpPost = new HttpPost(proxy.getUrl());
			proxy.getHeader().forEach((key, value) -> httpPost.addHeader(value, value));
			StringEntity input = new StringEntity(proxy.getData().toString(), ContentType.APPLICATION_JSON);
			input.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
			httpPost.setEntity(input);

			HttpResponse responseData = client.execute(httpPost);
			HttpEntity entity = responseData.getEntity();
			response = EntityUtils.toString(entity, "UTF-8");
			proxylogger.info("{} - {} - {} - {}", proxy.getUrl(), proxy.getHeader(), proxy.getData().toString(), response);
		}
		catch (Exception e) {
			proxylogger.error("{} - {} - {}", proxy.getUrl(), proxy.getHeader(), proxy.getData().toString());
			e.printStackTrace();
		}
		return response;
	}

}
