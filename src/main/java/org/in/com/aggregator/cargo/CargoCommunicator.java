package org.in.com.aggregator.cargo;

import java.net.URLEncoder;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.IntegrationDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.utils.HttpServiceClient;

import net.sf.json.JSONObject;

public class CargoCommunicator {
	
	public JSONObject getCargoTransitDetails(AuthDTO authDTO, IntegrationDTO integration, String tripDate, String registrationNumber) {
		JSONObject json = null;
		try {
			String url = "/cargo/" + integration.getAccount() + "/transit/details?tripDate=" + URLEncoder.encode(tripDate, "UTF-8") + "&registrationNumber=" + URLEncoder.encode(registrationNumber, "UTF-8");
			HttpServiceClient httpClient = new HttpServiceClient();
			String jsonData = httpClient.get(integration.getAccessUrl() + url);
			json = JSONObject.fromObject(jsonData);
		}
		catch (Exception e) {
			throw new ServiceException(ErrorCode.INVALID_STATION);
		}
		return json;
	}

}
