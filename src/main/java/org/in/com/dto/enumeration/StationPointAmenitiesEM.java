package org.in.com.dto.enumeration;

import java.util.ArrayList;
import java.util.List;

import org.in.com.constants.Text;
import org.in.com.utils.StringUtil;

public enum StationPointAmenitiesEM {

	NOT_AVAILABLE("NA", ""),
	WAITING_LOUNGE("WTLONGE", "Waiting lounge"),
	AC_WAITING_LOUNGE("ACWTLONGE", "AC Waiting lounge"),
	WASH_ROOM("WASHRM", "Wash Room"),
	METRO_CONNECTING("MTRCONNT", "Metro Connecting"),
	CCTV_SURVEILLANCE("CCTVSRVLCE", "CCTV Surveillance"),
	TELEVISION_ENTERTAINMENT("TVENTMT", "Television Entertainment"),
	BIKE_PARKING("BIKEPARK", "Bike Parking"),
	CAR_PARKING("CARPARK", "Car Parking"),
	FREE_WIFI("FREEWIFI", "Free Wi-Fi"),
	CHARGING_POINT("CHAGPNT", "Charging point"),
	SUPPORT_EXECUTIVE("SUPEXEC", "Support Executive"),
	BOOKING_COUNTER("BOOKING", "Booking Counter");

	private final String code;
	private final String name;

	private StationPointAmenitiesEM(String code, String name) {
		this.code = code;
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public static StationPointAmenitiesEM getStationPointAmenitiesEM(String Code) {
		StationPointAmenitiesEM[] values = values();
		for (StationPointAmenitiesEM amenities : values) {
			if (amenities.getCode().equalsIgnoreCase(Code)) {
				return amenities;
			}
		}
		return NOT_AVAILABLE;
	}

	public static List<StationPointAmenitiesEM> getStationPointAmenitiesFromCodes(String amenitiesCodes) {
		List<StationPointAmenitiesEM> amenitiesList = new ArrayList<>();
		if (StringUtil.isNotNull(amenitiesCodes)) {
			for (String amenitiesCode : amenitiesCodes.split("\\,")) {
				StationPointAmenitiesEM amenitiesEM = StationPointAmenitiesEM.getStationPointAmenitiesEM(amenitiesCode);
				if (amenitiesEM == null) {
					continue;
				}
				amenitiesList.add(amenitiesEM);
			}
		}
		return amenitiesList;
	}
	
	public static String getStationPointAmenitiesCodes(List<StationPointAmenitiesEM> amenities) {
		StringBuilder amenitiesCodes = new StringBuilder();
		if (amenities != null) {
			for (StationPointAmenitiesEM amenitiesEM : amenities) {
				if (amenitiesEM == null) {
					continue;
				}
				amenitiesCodes.append(amenitiesEM.getCode());
				amenitiesCodes.append(Text.COMMA);
			}
		}
		return amenitiesCodes.toString();
	}
}
