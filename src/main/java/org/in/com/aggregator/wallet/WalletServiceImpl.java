package org.in.com.aggregator.wallet;

import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BookingDTO;
import org.in.com.dto.UserCustomerDTO;
import org.in.com.dto.UserTransactionDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class WalletServiceImpl implements WalletService {

	@Override
	public void updateWalletUser(AuthDTO authDTO, UserCustomerDTO userCustomer) {
		WalletCommunicator walletCommunicator = new WalletCommunicator();
		JSONObject responseJSON = walletCommunicator.updateUser(authDTO, userCustomer);
		if (responseJSON != null && Numeric.ONE_INT == responseJSON.getInt("status")) {
			WalletConvertor walletConvertor = new WalletConvertor();
			walletConvertor.getJSONData(responseJSON, userCustomer);
		}
	}

	@Override
	public Map<String, Object> validateWalletCoupon(AuthDTO authDTO, BookingDTO bookingDTO, UserCustomerDTO userCustomerDTO) {
		WalletCommunicator walletCommunicator = new WalletCommunicator();
		JSONObject responseJSON = walletCommunicator.validateWalletCouponCode(authDTO, bookingDTO, userCustomerDTO);
		WalletConvertor walletConvertor = new WalletConvertor();
		Map<String, Object> addonDetailMap = walletConvertor.validateWalletCouponCode(responseJSON);
		return addonDetailMap;
	}

	@Override
	public Map<String, Object> processWalletTransaction(AuthDTO authDTO, BookingDTO bookingDTO, UserCustomerDTO userCustomerDTO) {
		WalletCommunicator walletCommunicator = new WalletCommunicator();
		JSONObject responseJSON = walletCommunicator.processWalletTransaction(authDTO, bookingDTO, userCustomerDTO);
		WalletConvertor walletConvertor = new WalletConvertor();
		Map<String, Object> addonDetailMap = walletConvertor.processWalletTransaction(responseJSON);
		return addonDetailMap;
	}

	@Override
	public Map<String, Object> getCurrentCreditBalace(AuthDTO authDTO, UserCustomerDTO userCustomerDTO) {
		WalletCommunicator walletCommunicator = new WalletCommunicator();
		JSONObject responseJSON = walletCommunicator.getUserBalance(authDTO, userCustomerDTO);
		WalletConvertor walletConvertor = new WalletConvertor();
		Map<String, Object> currentBalance = walletConvertor.getCurrentCreditBalace(responseJSON);
		return currentBalance;
	}

	@Override
	public Map<String, String> getWalletRedeemDetails(AuthDTO authDTO, String cashCoupon) {
		WalletCommunicator walletCommunicator = new WalletCommunicator();
		JSONObject responseJSON = walletCommunicator.getWalletRedeemDetails(authDTO, cashCoupon);
		WalletConvertor walletConvertor = new WalletConvertor();
		Map<String, String> addonDetailMap = walletConvertor.getWalletRedeemDetails(responseJSON);
		return addonDetailMap;
	}

	@Override
	public List<Map<String, Object>> getUserCachCoupons(AuthDTO authDTO, String mobileNumber) {
		WalletCommunicator walletCommunicator = new WalletCommunicator();
		JSONObject responseJSON = walletCommunicator.getUserCachCoupons(authDTO, mobileNumber);
		WalletConvertor walletConvertor = new WalletConvertor();
		List<Map<String, Object>> couponList = walletConvertor.getUserCachCoupons(responseJSON);
		return couponList;
	}

	@Override
	public JSONArray userTransactionHistory(AuthDTO authDTO, String fromDate, String toDate, String mobileNumber) {
		WalletCommunicator walletcommunicator = new WalletCommunicator();
		JSONObject jsonData = walletcommunicator.userTransaction(authDTO, fromDate, toDate, mobileNumber);
		WalletConvertor walletDataConverter = new WalletConvertor();
		JSONArray convertotList = walletDataConverter.userTransaction(jsonData);
		return convertotList;

	}

	@Override
	public List<Map<String, String>> getUserBalanceDetails(AuthDTO authDTO) {
		WalletCommunicator walletcommunicator = new WalletCommunicator();
		JSONObject jsonData = walletcommunicator.getUserBalanceDetails(authDTO);
		WalletConvertor walletDataConverter = new WalletConvertor();
		List<Map<String, String>> userBalanceList = walletDataConverter.getUserBalanceDetails(jsonData);
		return userBalanceList;
	}

	@Async
	public void addAfterTravelTransaction(AuthDTO authDTO, UserTransactionDTO userTransactionDTO, UserCustomerDTO userCustomerDTO, String eventType) {
		WalletCommunicator walletcommunicator = new WalletCommunicator();
		JSONObject responseJSON = walletcommunicator.addAfterTravelTransaction(authDTO, userTransactionDTO, userCustomerDTO, eventType);
		WalletConvertor walletDataConverter = new WalletConvertor();
		walletDataConverter.getAfterTravelTransaction(responseJSON, userTransactionDTO);
	}

	@Override
	public Map<String, String> getUserReferral(AuthDTO authDTO, UserCustomerDTO userCustomerDTO) {
		WalletCommunicator walletcommunicator = new WalletCommunicator();
		JSONObject responseJSON = walletcommunicator.getUserReferral(authDTO, userCustomerDTO);
		WalletConvertor walletDataConverter = new WalletConvertor();
		return walletDataConverter.getUserReferral(responseJSON);
	}

	@Override
	public Map<String, String> applyUserReferral(AuthDTO authDTO, String mobileNumber, String referralCode) {
		WalletCommunicator walletcommunicator = new WalletCommunicator();
		JSONObject responseJSON = walletcommunicator.applyUserReferral(authDTO, mobileNumber, referralCode);
		if (responseJSON == null || Numeric.ONE_INT != responseJSON.getInt("status")) {
			throw new ServiceException(ErrorCode.UNABLE_PROCESS);
		}
		WalletConvertor walletDataConverter = new WalletConvertor();
		return walletDataConverter.applyUserReferral(responseJSON);
	}

	@Override
	public boolean verifyBenificiaryReferral(AuthDTO authDTO, String referralCode) {
		boolean isUserExist = Text.FALSE;
		WalletCommunicator walletCommunicator = new WalletCommunicator();
		JSONObject responseJSON = walletCommunicator.verifyBenificiaryReferral(authDTO, referralCode);
		if (responseJSON != null && Numeric.ONE_INT == responseJSON.getInt("status")) {
			isUserExist = Text.TRUE;
		}
		return isUserExist;
	}
}
