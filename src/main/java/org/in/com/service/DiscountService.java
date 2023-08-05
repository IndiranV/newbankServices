package org.in.com.service;

import java.util.List;
import java.util.Map;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.BookingDTO;
import org.in.com.dto.DiscountCategoryDTO;
import org.in.com.dto.DiscountCouponDTO;
import org.in.com.dto.DiscountCriteriaDTO;
import org.in.com.dto.TicketAddonsDetailsDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.UserCustomerDTO;

public interface DiscountService extends BaseService<DiscountCriteriaDTO> {

	public List<DiscountCriteriaDTO> getAllDiscountByCoupon(AuthDTO authDTO, DiscountCouponDTO dto);

	public List<DiscountCriteriaDTO> getAllDiscountByCategory(AuthDTO authDTO, DiscountCategoryDTO dto);

	public List<DiscountCriteriaDTO> getAllAvailableDiscountOfferPage(AuthDTO authDTO);

	public DiscountCriteriaDTO validateCouponCode(AuthDTO authDTO, BookingDTO dto);

	public DiscountCriteriaDTO validateCouponCodeV3(AuthDTO authDTO, BookingDTO dto);

	public Map<String, DiscountCriteriaDTO> applyCouponCode(AuthDTO authDTO, BookingDTO bookingDTO, TicketDTO ticketDTO, TicketDTO preTicket);

	public TicketDTO validatePreviousTicketCoupen(AuthDTO authDTO, BookingDTO bookingDTO);

	public void updateDiscountCouponUsage(AuthDTO authDTO, TicketAddonsDetailsDTO ticketAddonsDetailsDTO);

	public DiscountCriteriaDTO getCustomerDiscountCriteria(AuthDTO authDTO, UserCustomerDTO userCustomerDTO);
	
	public DiscountCriteriaDTO getCustomerDiscountCoupon(AuthDTO authDTO, UserCustomerDTO userCustomerDTO);

}
