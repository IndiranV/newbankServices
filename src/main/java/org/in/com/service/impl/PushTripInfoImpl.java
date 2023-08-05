
package org.in.com.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.in.com.aggregator.costiv.CostivService;
import org.in.com.aggregator.gps.TrackBusService;
import org.in.com.aggregator.push.PartnerEM;
import org.in.com.aggregator.push.PushService;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleStationDTO;
import org.in.com.dto.TripChartDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.GPSDeviceVendorEM;
import org.in.com.dto.enumeration.NotificationTypeEM;
import org.in.com.dto.enumeration.UserTagEM;
import org.in.com.service.NotificationService;
import org.in.com.service.PushTripInfoService;
import org.in.com.service.UserService;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import net.sf.json.JSONObject;

@Service
public class PushTripInfoImpl implements PushTripInfoService {
	public static Logger TRIP_INFO_LOGGER = LoggerFactory.getLogger("org.in.com.service.impl.TripImpl");
	private static final String API_PARTNER = "APIEZ,APIRB,APIPT,APIAB";

	@Autowired
	NotificationService notificationService;
	@Autowired
	CostivService costivService;
	@Autowired
	UserService userService;
	@Autowired
	TrackBusService trackbusService;
	
	@Async
	public void updateTripInfo(AuthDTO authDTO, TripChartDTO tripChartDTO) {
		TripDTO tripDTO = tripChartDTO.getTrip();
		// fire TrackBus SMS
		if (NotificationTypeEM.isNotificationEnabled(authDTO.getNamespace().getProfile().getSmsNotificationFlagCode(), NotificationTypeEM.GPS_TRACKING)) {
			TRIP_INFO_LOGGER.info(" Push to tracking SMS - {}", tripDTO.getCode());
			notificationService.sendTripJourneyTrackingSMS(authDTO, tripDTO);
		}
		// Save Costiv Trip
		costivService.saveTrip(authDTO, tripDTO);
		
		Map<String, String> uniquePartnerMap = new HashMap<String, String>();
		for (UserDTO userDTO : authDTO.getNamespace().getProfile().getAllowApiTripInfo()) {
			userDTO = userService.getUser(authDTO, userDTO);
			for (UserTagEM userTag : userDTO.getUserTags()) {
				uniquePartnerMap.put(userTag.getCode(), userTag.getCode());
			}
		}
		
		JSONObject jsonObject = getTripDetailsJSON(authDTO, tripChartDTO);
		
		List<String> vendorList = new ArrayList<String>(uniquePartnerMap.values());
		tripChartDTO.setVendorList(vendorList);
		
		for (Entry<String, String> otaPartner : uniquePartnerMap.entrySet()) {
			if (!API_PARTNER.contains(otaPartner.getKey())) {
				continue;
			}
			PartnerEM apiPartner = PartnerEM.getPartnerEM(otaPartner.getKey());
			PushService pushService = getPushImplFactory(apiPartner);
			if (pushService != null) {
				pushService.pushTripDetails(authDTO, tripChartDTO, jsonObject);
			}
		}		
	}

	@Async
	public void removeTripInfo(AuthDTO authDTO, TripDTO tripDTO) {
		// remove Trip details in geo
		trackbusService.removeGeoTripDetails(authDTO, GPSDeviceVendorEM.EZEEGPS, tripDTO);
		
	}

	private PushService getPushImplFactory(PartnerEM partnerEm) {
		PushService pushService = null;
		if (partnerEm != null) {
			String className = "org.in.com.aggregator.push.impl." + partnerEm.getImpl();
			try {
				Class<?> gatewayClass = Class.forName(className);
				pushService = (PushService) gatewayClass.newInstance();
			}
			catch (ClassNotFoundException e) {
				System.out.println("Requested impl not found");
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return pushService;
	}
	
	private JSONObject getTripDetailsJSON(AuthDTO authDTO, TripChartDTO tripChartDTO) {
		JSONObject tripDetails = new JSONObject();
		TripDTO tripDTO = tripChartDTO.getTrip();
		if (tripDTO.getTripInfo() != null) {
			tripDetails.put("tripCode", tripDTO.getCode());
			
			JSONObject operatorJson = new JSONObject();
			operatorJson.put("code", authDTO.getNamespaceCode());
			operatorJson.put("name", authDTO.getNamespace().getName());
			tripDetails.put("operator", operatorJson);
			
			ScheduleStationDTO fromStationDTO =  BitsUtil.getOriginStation(tripDTO.getStationList());
			ScheduleStationDTO toStationDTO = BitsUtil.getDestinationStation(tripDTO.getStationList());
			
			JSONObject fromStationJson = new JSONObject();
			fromStationJson.put("code", fromStationDTO.getStation().getCode());
			fromStationJson.put("name", fromStationDTO.getStation().getName());
			fromStationJson.put("dateTime", DateUtil.convertDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), fromStationDTO.getMinitues())));
			tripDetails.put("fromStation", fromStationJson);
			
			JSONObject toStationJson = new JSONObject();
			toStationJson.put("code", toStationDTO.getStation().getCode());
			toStationJson.put("name", toStationDTO.getStation().getName());
			toStationJson.put("dateTime", DateUtil.convertDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), toStationDTO.getMinitues())));
			tripDetails.put("toStation", toStationJson);
			
			JSONObject scheduleJson = new JSONObject();
			scheduleJson.put("code", tripDTO.getSchedule().getCode());
			scheduleJson.put("name", tripDTO.getSchedule().getName());
			scheduleJson.put("serviceNumber", tripDTO.getSchedule().getServiceNumber());
			tripDetails.put("schedule", scheduleJson);

			JSONObject tripInfoJson = new JSONObject();
			tripInfoJson.put("driverName", tripDTO.getTripInfo().getDriverName());
			tripInfoJson.put("driverMobile", tripDTO.getTripInfo().getDriverMobile());
			tripInfoJson.put("vehicleRegistationNumber", tripDTO.getTripInfo().getBusVehicle().getRegistationNumber());
			tripInfoJson.put("vehicleDeviceCode", tripDTO.getTripInfo().getBusVehicle().getGpsDeviceCode());
			tripDetails.put("tripInfo", tripInfoJson);
		}
		return tripDetails;
	}

}
