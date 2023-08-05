package org.in.com.utils;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Custom RestTemplate with timeout support. This class extends Spring
 * RestTemplate and sets
 * connection and read timeout to its request factory.
 * This class provides two constructors,
 * 1. no argument constructor - set the connection timeout 20 secs and read
 * timeout 60 secs
 * 2. second constructor accepts connection and read timeout in seconds
 * 
 * 
 */
public class RestClient extends RestTemplate {

	private final static int CONNECTION_TIMEOUT_SECS = 20;
	private final static int READ_TIMEOUT_SECS = 60;

	public RestClient() {
		this(CONNECTION_TIMEOUT_SECS, READ_TIMEOUT_SECS);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
		MappingJackson2HttpMessageConverter map = new MappingJackson2HttpMessageConverter();
		ObjectMapper newObjectMapper = new ObjectMapper();
		newObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		map.setObjectMapper(newObjectMapper);
		messageConverters.add(map);
		messageConverters.add(new FormHttpMessageConverter());
		setMessageConverters(messageConverters);

	}

	public RestClient(int connectionTimeoutSecs, int readTimeoutSecs) {
		super();
		// Get the request factory from RestTemplate class and set timeout
		// values
		SimpleClientHttpRequestFactory requestFactory = (SimpleClientHttpRequestFactory) getRequestFactory();
		requestFactory.setConnectTimeout(connectionTimeoutSecs * 1000); // milliseconds
		requestFactory.setReadTimeout(readTimeoutSecs * 1000); // milliseconds
	}

}
