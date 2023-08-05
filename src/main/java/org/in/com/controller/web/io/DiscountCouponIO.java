package org.in.com.controller.web.io;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DiscountCouponIO extends BaseIO {

	private String coupon;
	private String activeDesription;
	private String errorDescription;
	private DiscountCategoryIO discountCategory;
	private String lookupCode;
	private int usedCount;
	private List<DiscountCouponIO> overrideList;
	private UserCustomerIO userCustomer;

}
