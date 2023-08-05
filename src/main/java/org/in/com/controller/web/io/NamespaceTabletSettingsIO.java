package org.in.com.controller.web.io;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NamespaceTabletSettingsIO extends BaseIO {
	private List<String> tabs;
	private int tripSyncPeriod;
	
	/* booking */
//	private boolean discountFlag;
	private BigDecimal maxDiscountAmount;
	private int bookingOpenMinutes;
//	private boolean serviceChargeFlag;
	private BigDecimal maxServiceChargePerSeat;
//	private boolean optionGstFlag;
	private String bookingType;
	private String flagCodes;
	private int hideBookedTicketFare; 
	private int tripChartOpenMinutes;
	private int forceReleaseFlag;
	/* boarding */
//	private boolean maskMobileNumberFlag;
//	private boolean dialerEnabledFlag;
//	
//	/* trip start flow*/
//	private boolean tripStartPhotoFlag;
//	private boolean tripStartOdometerFlag;
//	private boolean tripStartCrewPhotoFlag;
//	
//	/* trip end flow*/
//	private boolean tripEndPhotoFlag;
//	private boolean tripEndOdometerFlag;
//	private boolean tripEndCrewPhotoFlag;
}
