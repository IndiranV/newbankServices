package org.in.com.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StationPointDTO extends BaseDTO<StationPointDTO> {
	private String latitude;
	private String longitude;
	private String address;
	private String landmark;
	private String number;
	private StationDTO station;
	private int minitues;
	private String creditDebitFlag;
	private String mapUrl;

	private List<String> seatList;
	// User Specific Station Point Commission
	private BigDecimal boardingCommission;
	private List<GroupDTO> userGroupList;
	private BigDecimal fare = BigDecimal.ZERO;
	private String amenities;
	/** Van Pickup */
	private BusVehicleVanPickupDTO busVehicleVanPickup;
	private int seatCount;

	public boolean isActive() {
		return getActiveFlag() == 1 ? true : false;
	}
	
	public int getVanRouteEnabledFlag() {
		return busVehicleVanPickup != null && busVehicleVanPickup.getId() != 0 ? 1 : 0;
	}
}
