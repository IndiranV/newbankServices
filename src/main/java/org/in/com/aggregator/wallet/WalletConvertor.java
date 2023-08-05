package org.in.com.aggregator.wallet;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.UserCustomerDTO;
import org.in.com.dto.UserTransactionDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.utils.StringUtil;

public class WalletConvertor {

	public void getJSONData(JSONObject responseJSON, UserCustomerDTO userCustomer) {
		try {
			if (responseJSON != null && Numeric.ONE_INT == responseJSON.getInt("status") && responseJSON.has("data")) {
				JSONObject jsonObject = responseJSON.getJSONObject("data");
				userCustomer.setWalletCode(jsonObject.getString("code"));
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			throw new ServiceException(ErrorCode.UNABLE_PROCESS);
		}
	}

	public Map<String, Object> validateWalletCouponCode(JSONObject responseJSON) {
		Map<String, Object> addonDetailMap = new HashMap<>();
		try {
			if (responseJSON != null && Numeric.ONE_INT == responseJSON.getInt("status") && responseJSON.has("data")) {
				JSONObject jsonObject = responseJSON.getJSONObject("data");
				if (jsonObject == null || !jsonObject.has("code")) {
					throw new ServiceException(ErrorCode.INVALID_CODE);
				}
				addonDetailMap.put("code", jsonObject.getString("code"));
				addonDetailMap.put("redeemAmount", jsonObject.has("redeemAmount") ? new BigDecimal(jsonObject.getString("redeemAmount")) : BigDecimal.ZERO);
				addonDetailMap.put("discountAmount", jsonObject.has("discountAmount") ? new BigDecimal(jsonObject.getString("discountAmount")) : BigDecimal.ZERO);
				addonDetailMap.put("currentBalance", jsonObject.has("currentBalance") ? new BigDecimal(jsonObject.getString("currentBalance")) : BigDecimal.ZERO);
			}
			else if (Numeric.ZERO_INT == responseJSON.getInt("status") || !responseJSON.has("data")) {
				throw new ServiceException(responseJSON.has("errorDesc") ? responseJSON.getString("errorDesc") : ErrorCode.INVALID_DISCOUNT_CODE.getMessage());
			}
		}
		catch (ServiceException e) {
			throw e;
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			throw new ServiceException(ErrorCode.UNABLE_PROCESS);
		}
		return addonDetailMap;
	}

	public Map<String, Object> processWalletTransaction(JSONObject responseJSON) {
		Map<String, Object> addonDetailMap = new HashMap<>();
		try {
			if (responseJSON != null && Numeric.ONE_INT == responseJSON.getInt("status") && responseJSON.has("data")) {
				JSONObject jsonObject = responseJSON.getJSONObject("data");
				addonDetailMap.put("redeemAmount", jsonObject.has("redeemAmount") ? new BigDecimal(jsonObject.getString("redeemAmount")) : BigDecimal.ZERO);
			}
			else if (Numeric.ZERO_INT == responseJSON.getInt("status") || !responseJSON.has("data")) {
				throw new ServiceException(ErrorCode.UNABLE_PROCESS, responseJSON.has("errorDesc") ? responseJSON.getString("errorDesc") : Text.EMPTY);
			}
		}
		catch (ServiceException e) {
			System.out.println(e.getMessage());
			throw e;
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			throw new ServiceException(ErrorCode.UNABLE_PROCESS);
		}
		return addonDetailMap;
	}

	public Map<String, Object> getCurrentCreditBalace(JSONObject responseJSON) {
		Map<String, Object> addonDetailMap = new HashMap<>();
		try {
			if (responseJSON != null && Numeric.ONE_INT == responseJSON.getInt("status") && responseJSON.has("data")) {
				JSONObject jsonObject = responseJSON.getJSONObject("data");
				addonDetailMap.put("currentBalance", jsonObject.has("currentBalance") ? new BigDecimal(jsonObject.getString("currentBalance")) : BigDecimal.ZERO);
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return addonDetailMap;
	}

	public Map<String, String> getWalletRedeemDetails(JSONObject responseJSON) {
		Map<String, String> addonDetailMap = new HashMap<>();
		try {
			if (responseJSON != null && Numeric.ONE_INT == responseJSON.getInt("status") && responseJSON.has("data")) {
				JSONObject jsonObject = responseJSON.getJSONObject("data");
				if (jsonObject == null || !jsonObject.has("code")) {
					throw new ServiceException(ErrorCode.INVALID_DISCOUNT_CODE);
				}
				addonDetailMap.put("code", jsonObject.getString("code"));
				addonDetailMap.put("currentBalance", jsonObject.has("currentBalance") ? jsonObject.getString("currentBalance") : Numeric.ZERO);
				addonDetailMap.put("redeemAmount", jsonObject.has("redeemAmount") ? jsonObject.getString("redeemAmount") : Numeric.ZERO);
				addonDetailMap.put("discountAmount", jsonObject.has("discountAmount") ? jsonObject.getString("discountAmount") : Numeric.ZERO);
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			throw new ServiceException(ErrorCode.UNABLE_PROCESS);
		}
		return addonDetailMap;
	}

	public List<Map<String, Object>> getUserCachCoupons(JSONObject responseJSON) {
		List<Map<String, Object>> couponList = new ArrayList<Map<String, Object>>();
		try {
			if (responseJSON != null && Numeric.ONE_INT == responseJSON.getInt("status") && responseJSON.has("data")) {
				JSONArray jsonArray = responseJSON.getJSONArray("data");
				for (Object couponObj : jsonArray) {
					JSONObject jsonObject = (JSONObject) couponObj;

					Map<String, Object> couponDetailMap = new HashMap<>();
					couponDetailMap.put("couponCode", jsonObject.get("code"));
					couponDetailMap.put("mobileNumber", jsonObject.get("mobileNumber"));
					couponDetailMap.put("expiryDate", jsonObject.get("expiryDate"));
					couponDetailMap.put("amount", jsonObject.get("amount"));
					couponDetailMap.put("activeFlag", jsonObject.get("activeFlag"));
					couponList.add(couponDetailMap);
				}
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			throw new ServiceException(ErrorCode.UNABLE_PROCESS);
		}
		return couponList;
	}

	public JSONArray userTransaction(JSONObject responseJSON) {
		JSONArray jsonArray = new JSONArray();
		try {
			if (responseJSON != null && Numeric.ONE_INT == responseJSON.getInt("status") && responseJSON.has("data")) {
				jsonArray = responseJSON.getJSONArray("data");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return jsonArray;

	}

	public List<Map<String, String>> getUserBalanceDetails(JSONObject responseJSON) {
		List<Map<String, String>> userBalanceList = new ArrayList<Map<String, String>>();
		try {
			if (responseJSON != null && Numeric.ONE_INT == responseJSON.getInt("status") && responseJSON.has("data")) {
				JSONArray dataArray = responseJSON.getJSONArray("data");

				for (Object object : dataArray) {
					JSONObject userJSON = (JSONObject) object;

					Map<String, String> userBalance = new HashMap<String, String>();
					userBalance.put("name", userJSON.has("name") ? userJSON.getString("name") : Text.EMPTY);
					userBalance.put("mobile", userJSON.getString("mobile"));
					userBalance.put("currentBalance", userJSON.getString("currentBalance"));
					userBalance.put("activeFlag", userJSON.getString("activeFlag"));
					userBalanceList.add(userBalance);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(ErrorCode.UNABLE_PROCESS);
		}
		return userBalanceList;

	}

	public void getAfterTravelTransaction(JSONObject responseJSON, UserTransactionDTO userTransactionDTO) {
		try {
			if (responseJSON != null && Numeric.ONE_INT == responseJSON.getInt("status") && responseJSON.has("data")) {
				JSONObject jsonObject = responseJSON.getJSONObject("data");
				if (jsonObject != null) {
					userTransactionDTO.setCode(jsonObject.has("code") ? jsonObject.getString("code") : Text.NA);
					userTransactionDTO.setTransactionAmount(jsonObject.has("transactionAmount") ? new BigDecimal(jsonObject.getString("transactionAmount")) : BigDecimal.ZERO);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(ErrorCode.UNABLE_PROCESS);
		}
	}

	public Map<String, String> getUserReferral(JSONObject responseJSON) {
		Map<String, String> userReferralMap = new HashMap<>();
		try {
			if (responseJSON != null && Numeric.ONE_INT == responseJSON.getInt("status") && responseJSON.has("data")) {
				JSONObject jsonObject = responseJSON.getJSONObject("data");
				if (jsonObject != null && jsonObject.has("referralCode") && StringUtil.isNotNull(jsonObject.getString("referralCode"))) {
					userReferralMap.put("referralCode", jsonObject.getString("referralCode"));
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(ErrorCode.UNABLE_PROCESS);
		}
		return userReferralMap;
	}

	public Map<String, String> applyUserReferral(JSONObject responseJSON) {
		Map<String, String> userReferralMap = new HashMap<>();
		try {
			if (responseJSON != null && Numeric.ONE_INT == responseJSON.getInt("status") && responseJSON.has("data")) {
				JSONObject jsonObject = responseJSON.getJSONObject("data");
				if (jsonObject != null) {
					userReferralMap.put("referralCode", jsonObject.getString("referralCode"));
					userReferralMap.put("mobileNumber", jsonObject.getString("mobileNumber"));
					userReferralMap.put("referralUserMobile", jsonObject.getJSONObject("user") != null ? jsonObject.getJSONObject("user").getString("mobile") : Text.EMPTY);
					userReferralMap.put("referralUserWalletCode", jsonObject.getJSONObject("user") != null ? jsonObject.getJSONObject("user").getString("code") : Text.EMPTY);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(ErrorCode.UNABLE_PROCESS);
		}
		return userReferralMap;
	}
}
