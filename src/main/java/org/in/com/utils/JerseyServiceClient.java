package org.in.com.utils;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.core.MediaType;

import org.in.com.constants.Text;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.client.urlconnection.HTTPSProperties;

public class JerseyServiceClient {

	public String get(String url) throws IOException {
		return get(url, null);
	}

	public String get(String url, Map<String, String> headers) throws IOException {
		String responseStr = null;
		try {

			Client client = Client.create(getTLSConfig(new DefaultClientConfig()));
			WebResource request = client.resource(url);
			WebResource.Builder builder = request.getRequestBuilder();
			addHeaders(builder, headers);

			ClientResponse response = builder.get(ClientResponse.class);
			if (response.getStatus() == 200) {
				responseStr = response.getEntity(String.class);
			}
			else {
				throw new IOException("Invalid response code from server: " + response.getStatus());
			}
		}
		catch (IOException ioe) {
			throw new IOException(ioe);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
		return responseStr;
	}

	public String post(String url, Map<String, String> headers) throws IOException {
		return post(url, headers, MediaType.APPLICATION_JSON, Text.EMPTY, false);
	}

	private String post(String url, Map<String, String> headers, String type, Object requestEntity, boolean filter) throws IOException {
		String responseStr = null;
		try {
			Client client = Client.create(getTLSConfig(new DefaultClientConfig()));

			if (filter) {
				client.addFilter(new LoggingFilter(System.out));
			}
			WebResource request = client.resource(url);
			WebResource.Builder builder = request.getRequestBuilder();
			builder.accept(new String[] { MediaType.APPLICATION_JSON });
			builder.type(type);
			addHeaders(builder, headers);

			ClientResponse response = builder.post(ClientResponse.class, requestEntity);
			if (response.getStatus() == 200) {
				responseStr = response.getEntity(String.class);
			}
			else {
				throw new IOException("Invalid response code from server: " + response.getStatus());
			}
		}
		catch (IOException ioe) {
			throw new IOException(ioe);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
		return responseStr;
	}

	public String post(String url, String accept) throws IOException {
		return postV2(url, accept);
	}

	private String postV2(String url, String accept) throws IOException {
		String responseStr = null;
		WebResource request = null;
		try {
			Client client = getClient();
			request = client.resource(url);

			WebResource.Builder builder = request.getRequestBuilder();
			builder.accept(new String[] { accept });
			ClientResponse response = builder.post(ClientResponse.class);

			if (response.getStatus() == 200) {
				responseStr = response.getEntity(String.class);
			}
			else {
				throw new IOException("Invalid response code from server: " + response.getStatus());
			}
		}
		catch (Exception ioe) {
			throw new IOException(ioe);
		}
		return responseStr;
	}

	/**
	 * Accepts headers Map and returns query string
	 * 
	 * @param params
	 *            - Request parameters and values
	 * @return Request parameter query string value
	 */
	private void addHeaders(Builder builder, Map<String, String> headers) {
		for (Entry<String, String> param : headers.entrySet()) {
			builder.header(param.getKey(), param.getValue());
		}
	}

	private static ClientConfig getTLSConfig(ClientConfig config) throws Exception {
		try {
			TrustManager[] trustAllCerts = { new X509TrustManager() {
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(X509Certificate[] certs, String authType) {
				}

				public void checkServerTrusted(X509Certificate[] certs, String authType) {
				}
			} };
			SSLContext sc = SSLContext.getInstance("TLSv1.2");
			sc.init(null, trustAllCerts, new SecureRandom());
			config.getProperties().put("com.sun.jersey.client.impl.urlconnection.httpsProperties", new HTTPSProperties(new HostnameVerifier() {

				public boolean verify(String s, SSLSession sslSession) {
					return true;
				}
			}, sc));
		}
		catch (Exception e) {
			throw e;
		}
		return config;
	}

	public Client getClient() {
		ClientConfig config = new DefaultClientConfig();
		config.getProperties().put(ClientConfig.PROPERTY_CONNECT_TIMEOUT, 10000);
		config.getProperties().put(ClientConfig.PROPERTY_READ_TIMEOUT, 10000);
		Client client = Client.create(config);
		return client;

	}
}
