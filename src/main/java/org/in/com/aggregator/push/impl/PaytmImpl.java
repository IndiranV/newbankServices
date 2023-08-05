package org.in.com.aggregator.push.impl;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.in.com.aggregator.push.PushService;
import org.in.com.config.ApplicationConfig;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.TripChartDTO;
import org.in.com.dto.enumeration.GPSDeviceVendorEM;
import org.in.com.utils.DateUtil;
import org.in.com.utils.HttpServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
@EnableAsync
public class PaytmImpl implements PushService {

	private final static String API_URL = "https://travel.paytm.com/bus/boardingpoints/v1/driver/details";
	public static Logger TRIP_INFO_LOGGER = LoggerFactory.getLogger("org.in.com.service.impl.PushTripInfoImpl");

	@Async
	public void pushTripDetails(AuthDTO authDTO, TripChartDTO tripChartDTO, JSONObject jsonObject) {
		try {
			JSONObject requestJson = new JSONObject();
			requestJson.put("providerId", 50);
			requestJson.put("operatorId", authDTO.getNamespaceCode());
			requestJson.put("journeyDate", DateUtil.convertDate(tripChartDTO.getTrip().getTripDate()));
			requestJson.put("tripId", tripChartDTO.getTrip().getCode());
			if (tripChartDTO.getTrip().getTripInfo().getBusVehicle().getDeviceVendor().getId() == GPSDeviceVendorEM.EZEEGPS.getId()) {
				requestJson.put("isGpsAvailable", true);
				requestJson.put("gpsUrl", "https://m.trackbus.in/?t=" + tripChartDTO.getTrip().getCode() + "&z=" + ApplicationConfig.getServerZoneCode() + "&n=" + authDTO.getNamespaceCode());
			}
			else {
				requestJson.put("isGpsAvailable", false);
			}
			JSONArray driverDetailsArray = new JSONArray();
			JSONObject driverDetailsJson = new JSONObject();
			driverDetailsJson.put("driverName", tripChartDTO.getTrip().getTripInfo().getDriverName());

			JSONArray driverMobileArray = new JSONArray();
			driverMobileArray.add(tripChartDTO.getTrip().getTripInfo().getDriverMobile());
			driverDetailsJson.put("phoneNumbers", driverMobileArray);

			driverDetailsArray.add(driverDetailsJson);

			JSONObject tripInfoJson = new JSONObject();
			tripInfoJson.put("driverInfo", driverDetailsArray);
			tripInfoJson.put("vehicleNumber", tripChartDTO.getTrip().getTripInfo().getBusVehicle().getRegistationNumber());
//			tripInfoJson.put("vehicleDeviceCode", tripChartDTO.getTrip().getTripInfo().getBusVehicle().getGpsDeviceCode());
			requestJson.put("info", tripInfoJson);

			HttpClient client = new HttpServiceClient().getHttpClient();
			HttpPost httpPost = new HttpPost(API_URL);
			httpPost.addHeader("bus-ek", "ezeeDirect");
			httpPost.addHeader("bus-es", "ce2219be8f8841c38bac6d5d02c0be73");
			httpPost.addHeader("accept", MediaType.APPLICATION_JSON_VALUE);
			httpPost.addHeader("content-type", MediaType.APPLICATION_JSON_VALUE);
			StringEntity input = new StringEntity(requestJson.toString(), ContentType.APPLICATION_JSON);
			input.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
			httpPost.setEntity(input);

			HttpResponse responseData = client.execute(httpPost);
			HttpEntity entity = responseData.getEntity();
			String response = EntityUtils.toString(entity, "UTF-8");
			TRIP_INFO_LOGGER.info("{} \n{}", requestJson, response);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
