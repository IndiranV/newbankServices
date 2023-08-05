package org.in.com.aggregator.push.impl;

import java.io.IOException;

import org.in.com.aggregator.push.PushService;
import org.in.com.config.ApplicationConfig;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleStationDTO;
import org.in.com.dto.TripChartDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.enumeration.GPSDeviceVendorEM;
import org.in.com.service.impl.BusImpl;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.DateUtil;
import org.in.com.utils.HttpServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import hirondelle.date4j.DateTime;
import net.sf.json.JSONObject;

@Service
@EnableAsync
public class OrbitImpl implements PushService {
	public static Logger TRIP_INFO_LOGGER = LoggerFactory.getLogger("org.in.com.service.impl.PushTripInfoImpl");

	private final static String API_URL = "http://app.busticketagent.com/orbitservices";

	@Async
	public void pushTripDetails(AuthDTO authDTO, TripChartDTO tripChartDTO, JSONObject json) {
		try {
			JSONObject jsonObject = getTripDetailsJSON(authDTO, tripChartDTO, tripChartDTO.getTrip());

			String url = "/ezeeinfo/" + authDTO.getNamespaceCode() + "/trip/details/update";
			HttpServiceClient httpClient = new HttpServiceClient();
			String response = httpClient.post(API_URL + url, jsonObject.toString(), "application/json");
			TRIP_INFO_LOGGER.info("{} \n{}", jsonObject, response);

		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public JSONObject getTripDetailsJSON(AuthDTO authDTO, TripChartDTO tripChartDTO, TripDTO tripDTO) {
		JSONObject jsonObject = new JSONObject();
		try {
			DateTime dateTime = DateUtil.addMinituesToDate(tripDTO.getTripDate(), tripDTO.getTripMinutes());
			jsonObject.put("tripCode", tripDTO.getCode());
			jsonObject.put("travelDate", DateUtil.convertDate(dateTime));
			jsonObject.put("journeyMinutes", tripDTO.getTripMinutes());

			JSONObject tripStatusObject = new JSONObject();
			tripStatusObject.put("code", tripDTO.getTripStatus().getCode());
			jsonObject.put("tripStatus", tripStatusObject);

			BusImpl busImpl = new BusImpl();
			JSONObject busObject = new JSONObject();
			busObject.put("busType", busImpl.getBusCategoryByCode(tripDTO.getBus().getCategoryCode()));
			jsonObject.put("bus", busObject);

			JSONObject scheduleObject = new JSONObject();
			scheduleObject.put("code", tripDTO.getSchedule().getCode());
			scheduleObject.put("serviceNumber", tripDTO.getSchedule().getServiceNumber());
			scheduleObject.put("name", tripDTO.getSchedule().getName());
			jsonObject.put("schedule", scheduleObject);

			ScheduleStationDTO fromStationDTO = BitsUtil.getOriginStation(tripDTO.getStationList());
			ScheduleStationDTO toStationDTO = BitsUtil.getDestinationStation(tripDTO.getStationList());

			JSONObject fromStation = new JSONObject();
			fromStation.put("code", fromStationDTO.getStation().getCode());
			fromStation.put("name", fromStationDTO.getStation().getName());
			fromStation.put("dateTime", DateUtil.convertDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), fromStationDTO.getMinitues())));
			jsonObject.put("fromStation", fromStation);

			JSONObject toStation = new JSONObject();
			toStation.put("code", toStationDTO.getStation().getCode());
			toStation.put("name", toStationDTO.getStation().getName());
			toStation.put("dateTime", DateUtil.convertDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), toStationDTO.getMinitues())));
			jsonObject.put("toStation", toStation);

			JSONObject tripInfoObject = new JSONObject();
			tripInfoObject.put("driverName", tripDTO.getTripInfo().getDriverName());
			tripInfoObject.put("driverMobile", tripDTO.getTripInfo().getDriverMobile());
			tripInfoObject.put("driverName2", tripDTO.getTripInfo().getDriverName2());
			tripInfoObject.put("driverMobile2", tripDTO.getTripInfo().getDriverMobile2());
			tripInfoObject.put("attendarName", tripDTO.getTripInfo().getAttenderName());
			tripInfoObject.put("attendarMobile", tripDTO.getTripInfo().getAttenderMobile());

			JSONObject vehicleObject = new JSONObject();
			vehicleObject.put("registationNumber", tripDTO.getTripInfo().getBusVehicle().getRegistationNumber());
			vehicleObject.put("gpsDeviceCode", tripDTO.getTripInfo().getBusVehicle().getGpsDeviceCode());

			JSONObject gpsDeviceVendor = new JSONObject();
			gpsDeviceVendor.put("code", tripDTO.getTripInfo().getBusVehicle().getDeviceVendor() != null ? tripDTO.getTripInfo().getBusVehicle().getDeviceVendor().getCode() : Text.NA);
			vehicleObject.put("gpsDeviceVendor", gpsDeviceVendor);

			if (tripChartDTO.getTrip().getTripInfo().getBusVehicle().getDeviceVendor().getId() == GPSDeviceVendorEM.EZEEGPS.getId()) {
				vehicleObject.put("gpsUrl", "https://m.trackbus.in/?t=" + tripChartDTO.getTrip().getCode() + "&z=" + ApplicationConfig.getServerZoneCode() + "&n=" + authDTO.getNamespaceCode());
			}
			tripInfoObject.put("busVehicle", vehicleObject);
			jsonObject.put("tripInfo", tripInfoObject);
			jsonObject.put("vendors", tripChartDTO.getVendorList());

		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return jsonObject;
	}

}
