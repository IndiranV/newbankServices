package org.in.com.service.impl;

import java.util.List;

import org.in.com.constants.Text;
import org.in.com.dao.DiscountDAO;
import org.in.com.dao.UserCustomerDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.DiscountCouponDTO;
import org.in.com.service.DiscountCouponService;
import org.springframework.stereotype.Service;

@Service
public class DiscountCouponImpl implements DiscountCouponService {

	@Override
	public List<DiscountCouponDTO> get(AuthDTO authDTO, DiscountCouponDTO dto) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<DiscountCouponDTO> getAll(AuthDTO authDTO) {
		DiscountDAO dao = new DiscountDAO();
		List<DiscountCouponDTO> list = dao.getAllDiscountCoupon(authDTO, Text.NA);
		return list;

	}

	@Override
	public List<DiscountCouponDTO> getDiscountCoupons(AuthDTO authDTO, String discountType) {
		DiscountDAO dao = new DiscountDAO();
		List<DiscountCouponDTO> list = dao.getAllDiscountCoupon(authDTO, discountType);

		UserCustomerDAO userCustomerDAO = new UserCustomerDAO();
		for (DiscountCouponDTO discountCouponDTO : list) {
			if (discountCouponDTO.getUserCustomer() != null && discountCouponDTO.getUserCustomer().getId() != 0) {
				userCustomerDAO.getUserCustomer(authDTO, discountCouponDTO.getUserCustomer());
			}
		}
		return list;
	}

	@Override
	public DiscountCouponDTO Update(AuthDTO authDTO, DiscountCouponDTO dto) {
		DiscountDAO dao = new DiscountDAO();
		return dao.updateDiscountCouponCode(authDTO, dto);

	}

	@Override
	public void reloadDiscount() {
		// TODO Auto-generated method stub

	}

}
