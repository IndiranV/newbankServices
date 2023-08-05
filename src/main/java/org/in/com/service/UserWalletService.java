package org.in.com.service;

import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.BookingDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.UserCustomerDTO;

public interface UserWalletService {
	public Map<String, Object> getCurrentCreditBalace(AuthDTO authDTO, UserCustomerDTO userCustomerDTO);

	public Map<String, Object> validateWalletCoupon(AuthDTO authDTO, BookingDTO bookingDTO);

	public Map<String, String> getWalletRedeemDetails(AuthDTO authDTO, String cashCouponCode);

	public void processWalletTransaction(AuthDTO authDTO, TicketDTO ticketDTO, UserCustomerDTO userCustomerDTO);

	public List<Map<String, Object>> getUserCachCoupons(AuthDTO authDTO, String mobileNumber);

	public JSONArray userTransactionHistory(AuthDTO authDTO, String fromDate, String toDate, String mobileNumber);

	public List<Map<String, String>> getUserBalanceDetails(AuthDTO authDTO);

	public void addAfterTravelTransaction(AuthDTO authDTO, TicketDTO ticketDTO, String eventType);

	public Map<String, String> getUserReferral(AuthDTO authDTO, UserCustomerDTO userCustomerDTO);

	public void applyUserReferral(AuthDTO authDTO, String referralCode);

	public void verifyBenificiaryReferral(AuthDTO authDTO, String referralCode);

}
