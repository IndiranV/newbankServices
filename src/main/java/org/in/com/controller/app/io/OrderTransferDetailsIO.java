package org.in.com.controller.app.io;

import java.util.List;

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
	private Boolean captureTransferCharge;
	private Boolean notificationFlag;
}
