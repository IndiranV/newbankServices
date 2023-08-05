package org.in.com.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.in.com.constants.Text;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.FareTypeEM;
import org.in.com.dto.enumeration.JourneyTypeEM;
import org.in.com.dto.enumeration.SeatGendarEM;
import org.in.com.utils.StringUtil;

@Data
@EqualsAndHashCode(callSuper = true)
public class DiscountCriteriaDTO extends BaseDTO<DiscountCriteriaDTO> {
	private boolean percentageFlag;
	private boolean travelDateFlag;
	private boolean registeredUserFlag;
	private boolean roundTripFlag;
	private boolean showOfferPageFlag = true;
	private int maxUsageLimitPerUser;
	private int maxDiscountAmount;
	private int minTicketFare;
	private int minSeatCount;
	private int afterBookingMinitues;
	private int beforeBookingMinitues;
	private float value;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private String age;
	private String mobileNumber;
	private String serviceTiming;
	private JourneyTypeEM journeyType;
	private List<DeviceMediumEM> deviceMedium;
	private List<GroupDTO> groupList;
	private List<SeatGendarEM> seatGender;
	private List<String> scheduleCode;
	private List<String> routeCode;
	private DiscountCouponDTO discountCoupon;
	private List<DiscountCriteriaSlabDTO> slabList;

	public String getSlabDetails() {
		StringBuilder builder = new StringBuilder();
		if (slabList != null && !slabList.isEmpty()) {
			for (DiscountCriteriaSlabDTO slab : slabList) {
				builder.append(slab.getSlabFromValue());
				builder.append(Text.HYPHEN);
				builder.append(slab.getSlabToValue());
				builder.append(Text.COLON);
				builder.append(slab.getSlabValue());
				builder.append(Text.UNDER_SCORE);
				builder.append(slab.getSlabValueType().getId());
				builder.append(Text.COMMA);
			}
		}
		else if (slabList == null || slabList.isEmpty()) {
			builder.append(Text.NA);
		}
		return builder.toString();
	}

	public List<DiscountCriteriaSlabDTO> getSlabDetails(String slab) {
		List<DiscountCriteriaSlabDTO> slabList = new ArrayList<DiscountCriteriaSlabDTO>();
		if (StringUtil.isNotNull(slab)) {
			for (String entity : slab.split(Text.COMMA)) {
				if (StringUtil.isNull(entity)) {
					continue;
				}
				String slabDetails[] = entity.split("[\\-]|[\\:]|[\\_]");
				if (slabDetails.length != 4) {
					continue;
				}
				DiscountCriteriaSlabDTO slabDTO = new DiscountCriteriaSlabDTO();
				slabDTO.setSlabFromValue(Integer.parseInt(slabDetails[0]));
				slabDTO.setSlabToValue(Integer.parseInt(slabDetails[1]));
				slabDTO.setSlabValue(Integer.parseInt(slabDetails[2]));
				slabDTO.setSlabValueType(FareTypeEM.getFareTypeEM(Integer.parseInt(slabDetails[3])));
				slabList.add(slabDTO);
			}
		}
		return slabList;
	}

	public String getDeviceMediums() {
		StringBuilder medium = new StringBuilder();
		if (deviceMedium != null) {
			for (DeviceMediumEM deviceMediumEM : deviceMedium) {
				medium.append(deviceMediumEM.getCode());
				medium.append(Text.COMMA);
			}
		}
		return StringUtil.isNotNull(medium.toString()) ? medium.toString() : Text.NA;
	}

	public List<String> getDeviceMediumCodes() {
		List<String> deviceMediumCodes = new ArrayList<>();
		if (deviceMedium != null) {
			for (DeviceMediumEM deviceMediumEM : deviceMedium) {
				deviceMediumCodes.add(deviceMediumEM.getCode());
			}
		}
		return deviceMediumCodes;
	}

	public List<String> getMobileNumberList() {
		List<String> mobileNumberList = new ArrayList<>();
		if (StringUtil.isNotNull(mobileNumber)) {
			mobileNumberList = Arrays.asList(mobileNumber.split(Text.COMMA));
		}
		return mobileNumberList;
	}
}
