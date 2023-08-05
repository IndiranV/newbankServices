package org.in.com.aggregator.push.impl;

import org.in.com.aggregator.push.PushService;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.TripChartDTO;
import org.in.com.utils.HttpServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import net.sf.json.JSONObject;

@Service
public class AbhibusImpl implements PushService {

	private final static String API_URL = "https://tracking.abhibus.com/Devise_services/getEzeeInfoVehiclePush";
	public static Logger TRIP_INFO_LOGGER = LoggerFactory.getLogger("org.in.com.service.impl.PushTripInfoImpl");
	private int connectionTimeout = 3;
	private int soTimeout = 5;

	public void pushTripDetails(AuthDTO authDTO, TripChartDTO tripChartDTO, JSONObject jsonObject) {
		try {
			HttpServiceClient httpClient = new HttpServiceClient(connectionTimeout, soTimeout);
			String response = httpClient.post(API_URL, jsonObject.toString(), "application/json");
			TRIP_INFO_LOGGER.info("{} \n{}", jsonObject, response);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
