package org.in.com.service;

import org.in.com.dto.ProxyDTO;

import com.google.gson.JsonObject;

public interface ProxyService {

	public String processRequest(ProxyDTO proxy);

}