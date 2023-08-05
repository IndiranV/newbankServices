package org.in.com.controller.commerce.io;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.google.gson.Gson;

import lombok.Data;

@Data
public class OrderTransferDetailsIO {
	private String ticketCode;
	private String paymentPartnerCode;
	private String responseUrl;
	private List<String> seatCode;

	private StationIO transferFromStation;
	private StationIO transferToStation;
	private String transferTravelDate;

	private String transferTripCode;
	private String transferTripStageCode;
	private StationPointIO transferBoardingPoint;
	private StationPointIO transferDroppingPoint;
	private List<String> transferSeatCode;
	// Phone booking
	private Boolean myAccountFlag;
	private Boolean captureFareDifferece;
	private Boolean captureLowFareDifferece;
	private Boolean captureTransferCharge;
	private Boolean notificationFlag;

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public String toJSON() {
		Gson gson = new Gson();
		if (this != null) {
			gson.toJson(this);
		}
		return gson.toJson(this);
	}
}
