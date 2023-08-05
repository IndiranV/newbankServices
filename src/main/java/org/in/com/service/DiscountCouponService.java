package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.DiscountCouponDTO;

public interface DiscountCouponService extends BaseService<DiscountCouponDTO> {

	public void reloadDiscount();

	public List<DiscountCouponDTO> getDiscountCoupons(AuthDTO authDTO, String discountType);

}
