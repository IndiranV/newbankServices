package org.in.com.aggregator.wallet;

import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.BookingDTO;
import org.in.com.dto.UserCustomerDTO;
import org.in.com.dto.UserTransactionDTO;

public interface WalletService {

	public void updateWalletUser(AuthDTO authDTO, UserCustomerDTO userCustomer);

	public Map<String, Object> validateWalletCoupon(AuthDTO authDTO, BookingDTO bookingDTO, UserCustomerDTO userCustomerDTO);

	public Map<String, Object> processWalletTransaction(AuthDTO authDTO, BookingDTO bookingDTO, UserCustomerDTO userCustomerDTO);

	public Map<String, String> getWalletRedeemDetails(AuthDTO authDTO, String cashCoupon);

	public Map<String, Object> getCurrentCreditBalace(AuthDTO authDTO, UserCustomerDTO userCustomerDTO);

	public List<Map<String, Object>> getUserCachCoupons(AuthDTO authDTO, String mobileNumber);

	public JSONArray userTransactionHistory(AuthDTO authDTO, String fromDate, String toDate, String mobileNumber);

	public List<Map<String, String>> getUserBalanceDetails(AuthDTO authDTO);

	public void addAfterTravelTransaction(AuthDTO authDTO, UserTransactionDTO userTransactionDTO, UserCustomerDTO userCustomerDTO, String eventType);

	public Map<String, String> getUserReferral(AuthDTO authDTO, UserCustomerDTO userCustomerDTO);

	public Map<String, String> applyUserReferral(AuthDTO authDTO, String mobileNumber, String referralCode);

	public boolean verifyBenificiaryReferral(AuthDTO authDTO, String referralCode);
}
