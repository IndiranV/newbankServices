package org.in.com.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.utils.StringUtil;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleStationPointDTO extends BaseDTO<ScheduleStationPointDTO> {
	private ScheduleDTO schedule;
	private StationDTO station;
	private StationPointDTO stationPoint;
	private int minitues;
	private String creditDebitFlag;
	private String activeFrom;
	private String activeTo;
	private List<String> tripDates;
	private String dayOfWeek;
	private String lookupCode;
	private String boardingDroppingFlag;
	private List<StationPointDTO> stationPointList;
	private List<ScheduleStationPointDTO> overrideList = new ArrayList<ScheduleStationPointDTO>();
	private List<ScheduleDTO> scheduleList;
	private int releaseMinutes;
	private BusVehicleVanPickupDTO busVehicleVanPickup;
	// REG - Regular Station Point 
	// VAN - Van Pickup/Drop Point
	private String stationPointType; 
	private BigDecimal fare = BigDecimal.ZERO;
	private String mobileNumber;
	private String amenities;
	private String address;

	public int getBoardingFlag() {
		return StringUtil.isNotNull(boardingDroppingFlag) && StringUtil.isNotNull(Character.toString(boardingDroppingFlag.charAt(0))) ? Integer.parseInt(Character.toString(boardingDroppingFlag.charAt(0))) : Numeric.ZERO_INT;
	}

	public int getDroppingFlag() {
		return StringUtil.isNotNull(boardingDroppingFlag) && StringUtil.isNotNull(Character.toString(boardingDroppingFlag.charAt(1))) ? Integer.parseInt(Character.toString(boardingDroppingFlag.charAt(1))) : Numeric.ZERO_INT;
	}

	public boolean isActive() {
		return getActiveFlag() == 1 ? true : false;
	}

	public int getVanRouteEnabledFlag() {
		return busVehicleVanPickup != null && busVehicleVanPickup.getId() != 0 ? 1 : 0;
	}
	
	public String getTripDatesToString() {
		StringBuilder tripdates = new StringBuilder();
		if (tripDates != null && !tripDates.isEmpty()) {
			for (String tripDate : tripDates) {
				if (StringUtil.isNotNull(tripdates.toString())) {
					tripdates.append(Text.COMMA);
				}
				tripdates.append(tripDate);
			}
		}
		return tripdates.toString();
	}
}