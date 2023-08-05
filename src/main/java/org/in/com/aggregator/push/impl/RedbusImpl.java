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
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.TripChartDTO;
import org.in.com.utils.DateUtil;
import org.in.com.utils.HttpServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
public class RedbusImpl implements PushService {
	public static Logger TRIP_INFO_LOGGER = LoggerFactory.getLogger("org.in.com.service.impl.PushTripInfoImpl");
	private final static String API_URL = "http://reports.yourbus.in:2130/serviceJourney/assignVehicle";

	// Staging url
	// private final static String API_URL =
	// "http://report2.yourbus.in:2130/serviceJourney/assignVehicle";

	@Override
	public void pushTripDetails(AuthDTO authDTO, TripChartDTO tripChartDTO, JSONObject jsonObject) {
		JSONObject requestJson = new JSONObject();
		try {
			JSONArray tripInfoArray = new JSONArray();
			requestJson.put("gdsOperatorID", authDTO.getNamespaceCode());
			requestJson.put("doj", DateUtil.convertDate(tripChartDTO.getTrip().getTripDate()));
			requestJson.put("serviceID", tripChartDTO.getTrip().getSchedule().getCode());
			requestJson.put("vehicleRegnNo", tripChartDTO.getTrip().getTripInfo().getBusVehicle().getRegistationNumber());

			JSONArray driverDetailsArray = new JSONArray();

			JSONObject driverDetailsJson = new JSONObject();
			driverDetailsJson.put("driverName1", tripChartDTO.getTrip().getTripInfo().getDriverName());
			driverDetailsJson.put("driverMob1", tripChartDTO.getTrip().getTripInfo().getDriverMobile());
			driverDetailsArray.add(driverDetailsJson);

			driverDetailsJson = new JSONObject();
			driverDetailsJson.put("driverName2", tripChartDTO.getTrip().getTripInfo().getDriverName2());
			driverDetailsJson.put("driverMob2", tripChartDTO.getTrip().getTripInfo().getDriverMobile2());
			driverDetailsArray.add(driverDetailsJson);

			driverDetailsJson = new JSONObject();
			driverDetailsJson.put("attenderName1", tripChartDTO.getTrip().getTripInfo().getAttenderName());
			driverDetailsJson.put("attenderMob1", tripChartDTO.getTrip().getTripInfo().getAttenderMobile());
			driverDetailsArray.add(driverDetailsJson);

			driverDetailsJson = new JSONObject();
			driverDetailsJson.put("attenderName2", Text.EMPTY);
			driverDetailsJson.put("attenderMob2", Text.EMPTY);
			driverDetailsArray.add(driverDetailsJson);

			requestJson.put("driverDetails", driverDetailsArray);
			tripInfoArray.add(requestJson);

			HttpClient client = new HttpServiceClient().getHttpClient();
			HttpPost httpPost = new HttpPost(API_URL);
			httpPost.addHeader("vendortoken", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1lIjoiZXplZSIsImlhdCI6MTY3NzY0OTY4NX0.kbaGO1pRvz9FUCKr-Pzf33KEYSP4J9eiBqQKItA8oZg");
			httpPost.addHeader("accept", MediaType.APPLICATION_JSON_VALUE);
			httpPost.addHeader("content-type", MediaType.APPLICATION_JSON_VALUE);
			StringEntity input = new StringEntity(tripInfoArray.toString(), ContentType.APPLICATION_JSON);
			input.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
			httpPost.setEntity(input);

			HttpResponse responseData = client.execute(httpPost);
			HttpEntity entity = responseData.getEntity();
			String response = EntityUtils.toString(entity, "UTF-8");
			TRIP_INFO_LOGGER.info("{} \n{}", requestJson, response);
		}
		catch (Exception e) {
			TRIP_INFO_LOGGER.error("{} \n{}", requestJson, e.getMessage());
			e.printStackTrace();
		}
	}

}
