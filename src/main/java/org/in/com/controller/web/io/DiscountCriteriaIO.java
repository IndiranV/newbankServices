package org.in.com.controller.web.io;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DiscountCriteriaIO extends BaseIO {
	private boolean percentageFlag;
	private boolean travelDateFlag;
	private boolean registeredUserFlag;
	private boolean roundTripFlag;
	private boolean showOfferPageFlag;
	private int maxUsageLimitPerUser;
	private int maxDiscountAmount;
	private int minTicketFare;
	private int minSeatCount;
	private int afterBookingMinitues;
	private int beforeBookingMinitues;
	private float value;
	private String age;
	private String mobileNumber;
	private String serviceTiming;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private List<BaseIO> seatGender;
	private List<GroupIO> userGroup;
	private List<String> deviceMedium;
	private List<String> scheduleCode;
	private List<String> routeCode;
	private DiscountCouponIO discountCoupon;
	private List<DiscountCriteriaSlabIO> discountSlab;
}
