package org.in.com.controller.api_v3.io;

import java.util.List;

import lombok.Data;

import org.in.com.controller.commerce.io.StationIO;
import org.in.com.controller.commerce.io.StationPointIO;

import com.google.gson.Gson;

@Data
public class OrderTransferDetailsIO {
	private List<String> seatCode;

	private String tripCode;
	private StationIO fromStation;
	private StationIO toStation;
	private String travelDate;
	private StationPointIO boardingPoint;
	private StationPointIO droppingPoint;
	private List<String> transferSeatCode;

	public String toJSON() {
		Gson gson = new Gson();
		if (this != null) {
			gson.toJson(this);
		}
		return gson.toJson(this);
	}
}
