package org.in.com.aggregator.orbit;

import java.net.URLEncoder;

import org.in.com.dto.AuthDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.utils.HttpServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.json.JSONObject;

public class OrbitCommunicator {
	private static final Logger LOGGER = LoggerFactory.getLogger("org.in.com.aggregator.orbit.OrbitCommunicator");

	private static String API_URL = "http://localhost:8081/busservices";

	private String getZoneURL() {
		return API_URL;
	}

	public JSONObject getStationAreas(AuthDTO authDTO, String syncDate) {
		JSONObject json = null;
		try {
			String url = getZoneURL() + "/ezeeinfo/" + authDTO.getNamespaceCode() + "/area/sync?syncDate=" + URLEncoder.encode(syncDate, "UTF-8");
			LOGGER.info(url);
			HttpServiceClient httpClient = new HttpServiceClient();
			String jsonData = httpClient.get(url);
			json = JSONObject.fromObject(jsonData);
			LOGGER.info(json.toString());
		}
		catch (Exception e) {
			LOGGER.error("", e);
			throw new ServiceException(ErrorCode.INVALID_STATION);
		}
		return json;
	}
}