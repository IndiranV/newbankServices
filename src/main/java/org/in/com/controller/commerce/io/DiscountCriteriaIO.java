package org.in.com.controller.commerce.io;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.DiscountCouponIO;
import org.in.com.controller.web.io.GroupIO;

@Data
@EqualsAndHashCode(callSuper = true)
public class DiscountCriteriaIO extends BaseIO {

	private float value;
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
	private String age;
	private String mobileNumbers;
	private String serviceTimings;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private List<String> scheduleCode;
	private List<GroupIO> userGroup;

	private List<String> deviceMedium;
	private List<String> routeCode;
	private DiscountCouponIO discountCoupon;
	private List<BaseIO> gender;

}
