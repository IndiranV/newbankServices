package org.in.com.controller.api.io;

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
	private String dateTime;
	private List<String> seatList;
}
