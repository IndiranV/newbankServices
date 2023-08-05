package org.in.com.utils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;

/**
 * This HTTP client supports GET and POST protocols. Socket and SO timeout
 * values can be passed to this class.
 * Default timeout value is 60 seconds.
 * 
 * 
 */
public class HttpServiceClient {

	/**
	 * Default socket, so timeout value
	 */
	private final int DEFAULT_CONNECTION_TIMEOUT_SECS = 5;
	private final int DEFAULT_SO_TIMEOUT_SECS = 5;

	private int connectionTimeout;
	private int soTimeout;

	public HttpServiceClient() {
		this.connectionTimeout = DEFAULT_CONNECTION_TIMEOUT_SECS;
		this.soTimeout = DEFAULT_SO_TIMEOUT_SECS;
	}

	public HttpServiceClient(int connectionTimeout, int soTimeout) {
		this.connectionTimeout = connectionTimeout;
		this.soTimeout = soTimeout;
	}

	/**
	 * HTTP GET client method.
	 * 
	 * @param uri
	 *            - Server url
	 * @return Response string
	 * @throws IOException
	 */
	public String get(String url) throws IOException {
		return get(url, null);
	}

	/**
	 * HTTP GET client method with request parameters.
	 * 
	 * @param uri
	 *            - Server url
	 * @param params
	 *            - Request parameters as Map
	 * @return Response string
	 * @throws IOException
	 */
	public String get(String uri, Map<String, String> params) throws IOException {
		String responseStr = null;
		HttpGet request = null;
		String finalUri = uri;

		if (params != null && !params.isEmpty()) {
			finalUri += buildParams(params);
		}

		HttpClient client = getHttpClient();
		try {
			request = new HttpGet(finalUri);
			HttpResponse response = client.execute(request);
			if (response.getStatusLine().getStatusCode() == 200) {
				InputStream contentStream = response.getEntity().getContent();
				responseStr = IOUtils.toString(contentStream, "UTF-8");
			}
			else {
				throw new IOException("Invalid response code from server: " + response.getStatusLine().getStatusCode());
			}
		}
		catch (IOException ioe) {
			throw new IOException(ioe);
		}
		return responseStr;
	}

	public String get(String uri, Map<String, String> params, Map<String, String> header) throws IOException {
		String responseStr = null;
		HttpGet request = null;
		String finalUri = uri;

		if (params != null && !params.isEmpty()) {
			finalUri += buildParams(params);
		}

		HttpClient client = getHttpClient();
		try {
			request = new HttpGet(finalUri);
		    for (Map.Entry<String, String> entry : header.entrySet()) {
		    	request.addHeader(entry.getKey(), entry.getValue());
		    }
			HttpResponse response = client.execute(request);
			if (response.getStatusLine().getStatusCode() == 200) {
				InputStream contentStream = response.getEntity().getContent();
				responseStr = IOUtils.toString(contentStream, "UTF-8");
			}
			else {
				throw new IOException("Invalid response code from server: " + response.getStatusLine().getStatusCode());
			}
		}
		catch (IOException ioe) {
			throw new IOException(ioe);
		}
		return responseStr;
	}

	/**
	 * HTTP POST client method. This method will set body content as
	 * application/xml.
	 * 
	 * @param uri
	 *            - Server url
	 * @param bodyContent
	 *            - POST method body as XML
	 * @return Response string
	 * @throws IOException
	 */
	public String post(String uri, String bodyContent) throws IOException {
		return post(uri, bodyContent, "application/xml");
	}

	public String getSSL(String url) throws IOException {
		return getSSL(url, null);
	}

	/**
	 * HTTP POST client method. This method will accept body content type.
	 * 
	 * @param uri
	 *            - Server url
	 * @param bodyContent
	 *            - POST method body
	 * @param contentType
	 *            - Body content type
	 * @return Response string
	 * @throws IOException
	 */
	public String post(String uri, String bodyContent, String contentType) throws IOException {
		String responseStr = null;

		HttpPost request = null;
		HttpClient client = getHttpClient();
		try {
			request = new HttpPost(uri);
			StringEntity entity = new StringEntity(bodyContent, "UTF-8");
			entity.setContentType(contentType);
			request.setEntity(entity);
			HttpResponse response = client.execute(request);
			if (response.getStatusLine().getStatusCode() == 200) {
				InputStream contentStream = response.getEntity().getContent();
				responseStr = IOUtils.toString(contentStream, "UTF-8");
			}
			else {
				throw new IOException("Invalid response code from server: " + response.getStatusLine().getStatusCode());
			}
		}
		catch (IOException ioe) {
			throw new IOException(ioe);
		}
		return responseStr;
	}

	public String post(String uri, String bodyContent, String contentType, String auth) throws IOException {
		String responseStr = null;

		HttpPost request = null;
		HttpClient client = getHttpClient();
		try {
			request = new HttpPost(uri);
			if (StringUtil.isNotNull(auth)) {
				request.setHeader("Authorization", auth);
			}
			StringEntity entity = new StringEntity(bodyContent, "UTF-8");
			entity.setContentType(contentType);
			request.setEntity(entity);
			HttpResponse response = client.execute(request);
			if (response.getStatusLine().getStatusCode() == 200) {
				InputStream contentStream = response.getEntity().getContent();
				responseStr = IOUtils.toString(contentStream, "UTF-8");
			}
			else {
				throw new IOException("Invalid response code from server: " + response.getStatusLine().getStatusCode());
			}
		}
		catch (IOException ioe) {
			throw new IOException(ioe);
		}
		return responseStr;
	}

	/**
	 * Accepts request parameters and values as Map and returns query string
	 * 
	 * @param params
	 *            - Request parameters and values
	 * @return Request parameter query string value
	 */
	private String buildParams(Map<String, String> params) {
		URIBuilder uriBuilder = new URIBuilder();
		for (Entry<String, String> param : params.entrySet()) {
			uriBuilder.addParameter(param.getKey(), param.getValue());
		}
		return uriBuilder.toString();
	}

	/**
	 * Returns HttpClient object with socket, so time out values applied.
	 * 
	 * @return HttpClient
	 */
	public HttpClient getHttpClient() {
		ConnectionConfig connConfig = ConnectionConfig.custom()
		        .setBufferSize(10 * 1024) // 10kb
		        .build();
		RequestConfig config = RequestConfig.custom()
				.setConnectTimeout(connectionTimeout * 1000)
				.setConnectionRequestTimeout(connectionTimeout * 1000)
				.setSocketTimeout(soTimeout * 1000)
				.setCookieSpec(CookieSpecs.STANDARD).build();
		CloseableHttpClient client = HttpClientBuilder.create()
	            .setDefaultConnectionConfig(connConfig)
				.setDefaultRequestConfig(config).build();
		return client;
	}

	public HttpClient getHttpClient(int connectionTimeout, int soTimeout) {
		ConnectionConfig connConfig = ConnectionConfig.custom()
				.setBufferSize(10 * 1024) // 10kb
				.build();
		RequestConfig config = RequestConfig.custom()
				.setConnectTimeout(connectionTimeout * 1000)
				.setConnectionRequestTimeout(connectionTimeout * 1000)
				.setSocketTimeout(soTimeout * 1000)
				.setCookieSpec(CookieSpecs.STANDARD).build();
		CloseableHttpClient client = HttpClientBuilder.create()
				.setDefaultConnectionConfig(connConfig)
				.setDefaultRequestConfig(config).build();
		return client;
	}

	/**
	 * Returns map of values obtained as query string
	 * 
	 * @return Map<String,String>
	 */
	public Map<String, String> getQueryMap(String query) {
		Map<String, String> queryMap = new HashMap<String, String>();
		String[] params = query.split("&");

		if (params == null || params.length < 0) {
			return queryMap;
		}

		for (String param : params) {
			String[] keyValueArray = param.split("=");

			if (keyValueArray == null || keyValueArray.length < 1) {
				continue;
			}
			String name = keyValueArray[0];
			String value = keyValueArray[1];
			queryMap.put(name, value);
		}
		return queryMap;
	}

	/**
	 * HTTP post client method
	 * 
	 * @param uri
	 *            - Request URL
	 * @param params
	 *            - Input parameters pass them as Key and value
	 * @param auth
	 *            - any authorization key has to be passed.
	 * 
	 * */
	public String post(String uri, List<NameValuePair> params, String auth) throws IOException {
		String responseStr = null;
		HttpPost request = null;
		HttpClient client = getHttpClient();
		try {
			request = new HttpPost(uri);
			if (auth != null) {
				auth = new String(Base64.encodeBase64(auth.getBytes()));
				request.setHeader("Authorization", "Basic " + auth);
			}
			request.setEntity(new UrlEncodedFormEntity(params));
			HttpResponse response = client.execute(request);
			if (response.getStatusLine().getStatusCode() == 200) {
				InputStream contentStream = response.getEntity().getContent();
				responseStr = IOUtils.toString(contentStream, "UTF-8");
			}
			else {
				throw new IOException("Invalid response code from server: " + response.getStatusLine().getStatusCode());
			}
		}
		catch (IOException ioe) {
			throw new IOException(ioe);
		}
		return responseStr;
	}

	public String postSSL(String data, String sslUrl) throws Exception {
		URL vUrl = null;
		URLConnection vHttpUrlConnection = null;
		DataOutputStream vPrintout = null;
		DataInputStream vInput = null;
		StringBuffer vStringBuffer = null;
		vUrl = new URL(sslUrl);
		javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(new javax.net.ssl.HostnameVerifier() {

			public boolean verify(String hostname, javax.net.ssl.SSLSession sslSession) {
				System.out.println(hostname);
				return true;
			}
		});
		if (vUrl.openConnection() instanceof HttpsURLConnection) {
			vHttpUrlConnection = (HttpsURLConnection) vUrl.openConnection();
		}
		else if (vUrl.openConnection() instanceof com.sun.net.ssl.HttpsURLConnection) {
			vHttpUrlConnection = (com.sun.net.ssl.HttpsURLConnection) vUrl.openConnection();
		}
		else {
			vHttpUrlConnection = (URLConnection) vUrl.openConnection();
		}
		System.setProperty("jsse.enableSNIExtension", "false");
		vHttpUrlConnection.setDoInput(true);
		vHttpUrlConnection.setDoOutput(true);
		vHttpUrlConnection.setUseCaches(false);
		vHttpUrlConnection.connect();
		vPrintout = new DataOutputStream(vHttpUrlConnection.getOutputStream());
		vPrintout.writeBytes(data);
		vPrintout.flush();
		vPrintout.close();
		try {
			BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(vHttpUrlConnection.getInputStream()));
			vStringBuffer = new StringBuffer();
			String vRespData;
			while ((vRespData = bufferedreader.readLine()) != null) {
				if (vRespData.length() != 0) {
					vStringBuffer.append(vRespData.trim());
				}
			}
			bufferedreader.close();
			bufferedreader = null;
		}
		finally {
			if (vInput != null)
				vInput.close();
			if (vHttpUrlConnection != null)
				vHttpUrlConnection = null;
		}
		return vStringBuffer.toString();
	}

	public String getSSL(String uri, Map<String, String> params) throws IOException {
		String responseStr = null;
		HttpGet request = null;
		String finalUri = uri;

		if (params != null && !params.isEmpty()) {
			finalUri += buildParams(params);
		}

		HttpClient client = getHttpClientSSL();
		try {
			request = new HttpGet(finalUri);
			HttpResponse response = client.execute(request);
			if (response.getStatusLine().getStatusCode() == 200) {
				InputStream contentStream = response.getEntity().getContent();
				responseStr = IOUtils.toString(contentStream, "UTF-8");
			}
			else {
				throw new IOException("Invalid response code from server: " + response.getStatusLine().getStatusCode());
			}
		}
		catch (IOException ioe) {
			throw new IOException(ioe);
		}
		return responseStr;
	}
	
	// POST SSL with Dynamic Header
	public String postSSLV2(String uri, String bodyContent, Header[] headers) throws IOException {
		String responseStr = null;

		HttpPost request = null;
		HttpClient client = getHttpClientSSL();
		try {
			request = new HttpPost(uri);
			if (headers != null) {
				request.setHeaders(headers);
			}
			StringEntity entity = new StringEntity(bodyContent, "UTF-8");
			request.setEntity(entity);
			HttpResponse response = client.execute(request);
			if (response.getStatusLine().getStatusCode() == 200) {
				InputStream contentStream = response.getEntity().getContent();
				responseStr = IOUtils.toString(contentStream, "UTF-8");
			}
			else {
				throw new IOException("Invalid response code from server: " + response.getStatusLine().getStatusCode());
			}
		}
		catch (IOException ioe) {
			throw new IOException(ioe);
		}
		return responseStr;
	}

	public HttpClient getHttpClientSSL() {
		ConnectionConfig connConfig = ConnectionConfig.custom()
		        .setBufferSize(10 * 1024) // 10kb
				.build();
		RequestConfig config = RequestConfig.custom()
				.setConnectTimeout(connectionTimeout * 1000)
				.setConnectionRequestTimeout(connectionTimeout * 1000)
				.setSocketTimeout(soTimeout * 1000)
				.setCookieSpec(CookieSpecs.STANDARD).build();
		SSLContext sslContext = null;
		try {
			sslContext = SSLContexts.custom().loadTrustMaterial((chain, authType) -> true).build();
		}
		catch (KeyManagementException e) {
			e.printStackTrace();
		}
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		catch (KeyStoreException e) {
			e.printStackTrace();
		}
		SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext);
		CloseableHttpClient httpclient = HttpClients.custom()
				.setSSLSocketFactory(sslConnectionSocketFactory)
				.setDefaultConnectionConfig(connConfig)
				.setDefaultRequestConfig(config).build();

		return httpclient;
	}

}
