package org.in.com.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class DiscountCouponDTO extends BaseDTO<DiscountCouponDTO> {

	private String coupon;
	private String activeDesription;
	private String errorDescription;
	private DiscountCategoryDTO discountCategory;
	private String lookupCode;
	private int usedCount;
	private UserCustomerDTO userCustomer;
	private List<DiscountCouponDTO> overrideList = new ArrayList<DiscountCouponDTO>();
}
