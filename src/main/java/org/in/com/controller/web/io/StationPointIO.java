package org.in.com.controller.web.io;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StationPointIO extends BaseIO {

	private String latitude;
	private String longitude;
	private String address;
	private String landmark;
	private String number;
	private StationIO station;
	private String dateTime;
	private String mapUrl;
	private int seatCount;
	// User Specific Station Point Commission
	private BigDecimal boardingCommission;
	private List<GroupIO> userGroupList;
	private List<BaseIO> amenities;
}
