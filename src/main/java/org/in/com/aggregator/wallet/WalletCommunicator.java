package org.in.com.aggregator.wallet;

import java.math.BigDecimal;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BookingDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.UserCustomerDTO;
import org.in.com.dto.UserTransactionDTO;
import org.in.com.dto.enumeration.JourneyTypeEM;
import org.in.com.dto.enumeration.WalletAccessEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.utils.HttpServiceClient;
import org.in.com.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class WalletCommunicator {
	private final Logger WALLET_LOGGER = LoggerFactory.getLogger("org.in.com.aggregator.wallet.WalletCommunicator");

	private static String API_URL = "http://app.walletservice.ezeebits.in/walletservices";

	public JSONObject updateUser(AuthDTO authDTO, UserCustomerDTO userCustomer) {
		JSONObject userJSON = new JSONObject();
		JSONObject responseJSON = null;
		String url = Text.EMPTY;
		String response = "";
		try {
			url = API_URL + "/" + authDTO.getNamespaceCode() + "/" + getToken(authDTO.getNamespaceCode()) + "/user/update";

			userJSON.put("mobile", userCustomer.getMobile());
			userJSON.put("name", userCustomer.getName());

			HttpServiceClient httpClient = new HttpServiceClient();
			response = httpClient.post(url, userJSON.toString(), "application/json");
			responseJSON = JSONObject.fromObject(response);
		}
		catch (ServiceException e) {
			WALLET_LOGGER.error("NS:{} url:{} errorCode:{}", authDTO.getNamespaceCode(), url, e.getErrorCode().getCode());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			String key = Text.SUCCESS;
			if (responseJSON == null || Numeric.ONE_INT != responseJSON.getInt("status")) {
				key = Text.ERROR;
			}
			WALLET_LOGGER.info("NS:{} Update User {}01: Url {}, Req {} , Resp {}", authDTO.getNamespaceCode(), key, url, userJSON, response);
		}
		return responseJSON;
	}

	public JSONObject validateWalletCouponCode(AuthDTO authDTO, BookingDTO bookingDTO, UserCustomerDTO userCustomerDTO) {
		JSONObject orders = null;
		JSONObject responseJSON = null;
		String url = Text.EMPTY;
		String response = "";
		try {
			url = API_URL + "/" + authDTO.getNamespaceCode() + "/" + getToken(authDTO.getNamespaceCode()) + "/wallet/cash/coupon/validate";
			orders = convertBookingDetails(authDTO, bookingDTO, userCustomerDTO);

			HttpServiceClient httpClient = new HttpServiceClient();
			response = httpClient.post(url, orders.toString(), "application/json");
			responseJSON = JSONObject.fromObject(response);
		}
		catch (ServiceException e) {
			WALLET_LOGGER.error("NS:{} url:{} errorCode:{}", authDTO.getNamespaceCode(), url, e.getErrorCode().getCode());
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(ErrorCode.UNABLE_PROCESS, e.getMessage());
		}
		finally {
			String key = Text.SUCCESS;
			if (responseJSON == null || Numeric.ONE_INT != responseJSON.getInt("status")) {
				key = Text.ERROR;
			}
			WALLET_LOGGER.info("NS:{} Validate Wallet Coupon {}02: Url {}, Req {} , Resp {}", authDTO.getNamespaceCode(), key, url, orders, response);
		}
		return responseJSON;
	}

	public JSONObject convertBookingDetails(AuthDTO authDTO, BookingDTO bookingDTO, UserCustomerDTO userCustomerDTO) {
		JSONObject orders = new JSONObject();
		orders.put("cashCoupon", bookingDTO.getAdditionalAttributes() != null && StringUtil.isNotNull(bookingDTO.getAdditionalAttributes().get(Text.WALLET_COUPON_CODE)) ? bookingDTO.getAdditionalAttributes().get(Text.WALLET_COUPON_CODE) : null);
		orders.put("code", bookingDTO.getAdditionalAttributes() != null && StringUtil.isNotNull(bookingDTO.getAdditionalAttributes().get(Text.WALLET_REDREEM)) ? bookingDTO.getAdditionalAttributes().get(Text.WALLET_REDREEM) : null);
		orders.put("orderCode", bookingDTO.getCode());

		JSONArray orderDetails = new JSONArray();
		for (TicketDTO ticketDTO : bookingDTO.getTicketList()) {
			JSONObject orderDetailJSON = new JSONObject();
			orders.put("mobileNumber", userCustomerDTO.getMobile());

			JSONObject deviceMedium = new JSONObject();
			deviceMedium.put("code", ticketDTO.getDeviceMedium().getCode());
			orderDetailJSON.put("deviceMedium", deviceMedium);

			orderDetailJSON.put("amount", ticketDTO.getTotalSeatFare());
			orderDetailJSON.put("journeyType", ticketDTO.getJourneyType() != null ? ticketDTO.getJourneyType().getCode() : JourneyTypeEM.ONWARD_TRIP.getCode());
			orderDetailJSON.put("seatCount", ticketDTO.getTicketDetails().size());
			orderDetails.add(orderDetailJSON);
		}
		orders.put("orderDetails", orderDetails);
		return orders;
	}

	public JSONObject processWalletTransaction(AuthDTO authDTO, BookingDTO bookingDTO, UserCustomerDTO userCustomerDTO) {
		JSONObject responseJSON = null;
		JSONObject orders = null;
		String url = Text.EMPTY;
		String response = "";
		try {
			url = API_URL + "/" + authDTO.getNamespaceCode() + "/" + getToken(authDTO.getNamespaceCode()) + "/wallet/cash/coupon/apply";
			orders = convertBookingDetails(authDTO, bookingDTO, userCustomerDTO);

			HttpServiceClient httpClient = new HttpServiceClient();
			response = httpClient.post(url, orders.toString(), "application/json");
			responseJSON = JSONObject.fromObject(response);
		}
		catch (ServiceException e) {
			WALLET_LOGGER.error("NS:{} url:{} errorCode:{}", authDTO.getNamespaceCode(), url, e.getErrorCode().getCode());
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(ErrorCode.UNABLE_PROCESS, e.getMessage());
		}
		finally {
			String key = Text.SUCCESS;
			if (responseJSON == null || Numeric.ONE_INT != responseJSON.getInt("status")) {
				key = Text.ERROR;
			}
			WALLET_LOGGER.info("NS:{} Apply Wallet Coupon {}03: Url {}, Req {} , Resp {}", authDTO.getNamespaceCode(), key, url, orders, response);
		}
		return responseJSON;
	}

	public JSONObject getUserBalance(AuthDTO authDTO, UserCustomerDTO userCustomerDTO) {
		JSONObject responseJSON = null;
		String url = Text.EMPTY;
		String response = "";
		try {
			url = API_URL + "/" + authDTO.getNamespaceCode() + "/" + getToken(authDTO.getNamespaceCode()) + "/user/" + userCustomerDTO.getWalletCode() + "/balance";

			HttpServiceClient httpClient = new HttpServiceClient();
			response = httpClient.get(url);
			responseJSON = JSONObject.fromObject(response);
		}
		catch (ServiceException e) {
			WALLET_LOGGER.error("NS:{} url:{} errorCode:{}", authDTO.getNamespaceCode(), url, e.getErrorCode().getCode());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			String key = Text.SUCCESS;
			if (responseJSON == null || Numeric.ONE_INT != responseJSON.getInt("status")) {
				key = Text.ERROR;
			}
			WALLET_LOGGER.info("NS:{} User Balance {}04: Url {}, Req {} , Resp {}", authDTO.getNamespaceCode(), key, url, userCustomerDTO.getWalletCode(), response);
		}
		return responseJSON;
	}

	public JSONObject getWalletRedeemDetails(AuthDTO authDTO, String cashCoupon) {
		JSONObject responseJSON = null;
		String url = Text.EMPTY;
		String response = "";
		try {
			url = API_URL + "/" + authDTO.getNamespaceCode() + "/" + getToken(authDTO.getNamespaceCode()) + "/wallet/redeem/" + cashCoupon + "/details";

			HttpServiceClient httpClient = new HttpServiceClient();
			response = httpClient.get(url);
			responseJSON = JSONObject.fromObject(response);
		}
		catch (ServiceException e) {
			WALLET_LOGGER.error("NS:{} url:{} errorCode:{}", authDTO.getNamespaceCode(), url, e.getErrorCode().getCode());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			String key = Text.SUCCESS;
			if (responseJSON == null || Numeric.ONE_INT != responseJSON.getInt("status")) {
				key = Text.ERROR;
			}
			WALLET_LOGGER.info("NS:{} Wallet Redeem Details {}05: Url {}, Req {} , Resp {}", authDTO.getNamespaceCode(), key, url, cashCoupon, response);
		}
		return responseJSON;
	}

	public JSONObject getUserCachCoupons(AuthDTO authDTO, String mobileNumber) {
		JSONObject responseJSON = null;
		String url = Text.EMPTY;
		String response = "";
		try {
			url = API_URL + "/" + authDTO.getNamespaceCode() + "/" + getToken(authDTO.getNamespaceCode()) + "/campaign/transaction/" + mobileNumber + "/details";

			HttpServiceClient httpClient = new HttpServiceClient();
			response = httpClient.get(url);
			responseJSON = JSONObject.fromObject(response);
		}
		catch (ServiceException e) {
			WALLET_LOGGER.error("NS:{} url:{} errorCode:{}", authDTO.getNamespaceCode(), url, e.getErrorCode().getCode());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			String key = Text.SUCCESS;
			if (responseJSON == null || Numeric.ONE_INT != responseJSON.getInt("status")) {
				key = Text.ERROR;
			}
			WALLET_LOGGER.info("NS:{} User Cach Coupons {}07: Url {}, Req {} , Resp {}", authDTO.getNamespaceCode(), key, url, mobileNumber, response);
		}
		return responseJSON;
	}

	public JSONObject userTransaction(AuthDTO authDTO, String fromDate, String toDate, String mobileNumber) {
		JSONObject responseJSON = null;
		String url = Text.EMPTY;
		String response = "";
		try {
			url = API_URL + "/" + authDTO.getNamespaceCode() + "/" + getToken(authDTO.getNamespaceCode()) + "/user/transaction?fromDate=" + fromDate + "&toDate=" + toDate + "&mobileNumber=" + mobileNumber;
			HttpServiceClient httpClient = new HttpServiceClient();
			response = httpClient.get(url);
			responseJSON = JSONObject.fromObject(response);
		}
		catch (ServiceException e) {
			WALLET_LOGGER.error("NS:{} url:{} errorCode:{}", authDTO.getNamespaceCode(), url, e.getErrorCode().getCode());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			String key = Text.SUCCESS;
			if (responseJSON == null || Numeric.ONE_INT != responseJSON.getInt("status")) {
				key = Text.ERROR;
			}
			WALLET_LOGGER.info("NS:{} User Transaction {}08: Url {}, Req {} , Resp {}", authDTO.getNamespaceCode(), key, url, mobileNumber, response);
		}
		return responseJSON;
	}

	public JSONObject getUserBalanceDetails(AuthDTO authDTO) {
		JSONObject responseJSON = null;
		String url = Text.EMPTY;
		String response = "";
		try {
			url = API_URL + "/" + authDTO.getNamespaceCode() + "/" + getToken(authDTO.getNamespaceCode()) + "/user/balance";
			HttpServiceClient httpClient = new HttpServiceClient();
			response = httpClient.get(url);
			responseJSON = JSONObject.fromObject(response);
		}
		catch (ServiceException e) {
			WALLET_LOGGER.error("NS:{} url:{} errorCode:{}", authDTO.getNamespaceCode(), url, e.getErrorCode().getCode());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			String key = Text.SUCCESS;
			if (responseJSON == null || Numeric.ONE_INT != responseJSON.getInt("status")) {
				key = Text.ERROR;
			}
			WALLET_LOGGER.info("NS:{} User Transaction {}08: Url {}, Req {} , Resp {}", authDTO.getNamespaceCode(), key, url, Text.EMPTY, response);
		}
		return responseJSON;
	}

	public JSONObject addAfterTravelTransaction(AuthDTO authDTO, UserTransactionDTO userTransactionDTO, UserCustomerDTO userCustomerDTO, String eventType) {
		JSONObject userTransactionJSON = new JSONObject();
		JSONObject responseJSON = null;
		String url = Text.EMPTY;
		String response = "";
		try {
			url = API_URL + "/" + authDTO.getNamespaceCode() + "/" + getToken(authDTO.getNamespaceCode()) + "/wallet/after/travel/type/" + eventType;

			userTransactionJSON.put("refferenceCode", userTransactionDTO.getRefferenceCode());
			userTransactionJSON.put("debitAmount", BigDecimal.ZERO);
			userTransactionJSON.put("transactionAmount", userTransactionDTO.getTransactionAmount());
			userTransactionJSON.put("transactionDate", userTransactionDTO.getTransactionDate());
			userTransactionJSON.put("activeFlag", Numeric.ONE_INT);

			JSONObject transactionType = new JSONObject();
			transactionType.put("code", "AFTC");
			userTransactionJSON.put("transactionType", transactionType);

			JSONObject user = new JSONObject();
			user.put("mobile", userCustomerDTO.getMobile());
			user.put("name", userCustomerDTO.getName());
			userTransactionJSON.put("user", user);

			HttpServiceClient httpClient = new HttpServiceClient();
			response = httpClient.post(url, userTransactionJSON.toString(), "application/json");
			responseJSON = JSONObject.fromObject(response);
		}
		catch (ServiceException e) {
			WALLET_LOGGER.error("NS:{} url:{} errorCode:{}", authDTO.getNamespaceCode(), url, e.getErrorCode().getCode());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			String key = Text.SUCCESS;
			if (responseJSON == null || Numeric.ONE_INT != responseJSON.getInt("status")) {
				key = Text.ERROR;
			}
			WALLET_LOGGER.info("NS:{} After Travel {}09: Url {}, Req {} , Resp {}", authDTO.getNamespaceCode(), key, url, userTransactionJSON, response);
		}
		return responseJSON;
	}

	public JSONObject getUserReferral(AuthDTO authDTO, UserCustomerDTO userCustomerDTO) {
		JSONObject responseJSON = null;
		String url = Text.EMPTY;
		String response = "";
		try {
			url = API_URL + "/" + authDTO.getNamespaceCode() + "/" + getToken(authDTO.getNamespaceCode()) + "/user/" + userCustomerDTO.getMobile() + "/referral/generate";
			HttpServiceClient httpClient = new HttpServiceClient();
			response = httpClient.post(url, new JSONObject().toString(), "application/json");
			responseJSON = JSONObject.fromObject(response);
		}
		catch (ServiceException e) {
			WALLET_LOGGER.error("NS:{} url:{} errorCode:{}", authDTO.getNamespaceCode(), url, e.getErrorCode().getCode());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			String key = Text.SUCCESS;
			if (responseJSON == null || Numeric.ONE_INT != responseJSON.getInt("status")) {
				key = Text.ERROR;
			}
			WALLET_LOGGER.info("NS:{} Get User Referral {}10: Url {}, Req {} , Resp {}", authDTO.getNamespaceCode(), key, url, Text.EMPTY, response);
		}
		return responseJSON;
	}

	public JSONObject applyUserReferral(AuthDTO authDTO, String mobileNumber, String referralCode) {
		JSONObject responseJSON = null;
		String url = Text.EMPTY;
		String response = "";
		try {
			url = API_URL + "/" + authDTO.getNamespaceCode() + "/user/" + getToken(authDTO.getNamespaceCode()) + "/" + mobileNumber + "/referral/" + referralCode + "/apply";
			HttpServiceClient httpClient = new HttpServiceClient();
			response = httpClient.post(url, new JSONObject().toString(), "application/json");
			responseJSON = JSONObject.fromObject(response);
		}
		catch (ServiceException e) {
			WALLET_LOGGER.error("NS:{} url:{} errorCode:{}", authDTO.getNamespaceCode(), url, e.getErrorCode().getCode());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			String key = Text.SUCCESS;
			if (responseJSON == null || Numeric.ONE_INT != responseJSON.getInt("status")) {
				key = Text.ERROR;
			}
			WALLET_LOGGER.info("NS:{} Apply User Referral {}11: Url {}, Req {} , Resp {}", authDTO.getNamespaceCode(), key, url, Text.EMPTY, response);
		}
		return responseJSON;
	}

	public JSONObject verifyBenificiaryReferral(AuthDTO authDTO, String referralCode) {
		JSONObject responseJSON = null;
		String url = Text.EMPTY;
		String response = "";
		try {
			url = API_URL + "/" + authDTO.getNamespaceCode() + "/" + getToken(authDTO.getNamespaceCode()) + "/user/referral/" + referralCode + "/benificiary/verify";
			HttpServiceClient httpClient = new HttpServiceClient();
			response = httpClient.get(url);
			responseJSON = JSONObject.fromObject(response);
		}
		catch (ServiceException e) {
			WALLET_LOGGER.error("NS:{} url:{} errorCode:{}", authDTO.getNamespaceCode(), url, e.getErrorCode().getCode());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			String key = Text.SUCCESS;
			if (responseJSON == null || Numeric.ONE_INT != responseJSON.getInt("status")) {
				key = Text.ERROR;
			}
			WALLET_LOGGER.info("NS:{} User Transaction {}08: Url {}, Req {} , Resp {}", authDTO.getNamespaceCode(), key, url, Text.EMPTY, response);
		}
		return responseJSON;
	}

	private String getToken(String namespaceCode) {
		WalletAccessEM walletAccessEM = WalletAccessEM.getWalletAccessEM(namespaceCode);
		if (walletAccessEM == null) {
			throw new ServiceException(ErrorCode.UNAUTHORIZED);
		}
		return walletAccessEM.getToken();
	}

	public static void main(String a[]) {
		String data = "[{\"mobile_number\":\"\"}]";
		JsonParser jsonParser = new JsonParser();
		JsonArray jsonArray = (JsonArray) jsonParser.parse(data);
		String url = API_URL + "/tranzking/7MQEZNHFNKSVLS32/user/transaction/instant/balance/update";
		HttpServiceClient httpClient = new HttpServiceClient();
		for (int i = 0; i < jsonArray.size(); i++) {
			JsonObject jsonObj = jsonArray.get(i).getAsJsonObject();

			try {
				JSONObject userJSON = new JSONObject();
				userJSON.put("creditAmount", 0);
				userJSON.put("transactionAmount", 500);
				userJSON.put("debitAmount", 500);
				userJSON.put("transactionDate", "2019-02-17");
				userJSON.put("refferenceCode", "Credit Adjustment");
				JSONObject TransactionType = new JSONObject();
				TransactionType.put("code", "WARED");
				userJSON.put("transactionType", TransactionType);
				JSONObject CampaignType = new JSONObject();
				CampaignType.put("code", "NUSR");
				userJSON.put("campaignType", CampaignType);
				JSONObject user = new JSONObject();
				user.put("mobile", jsonObj.get("mobile_number").getAsString());
				userJSON.put("user", user);

				String response = httpClient.post(url, userJSON.toString(), "application/json");
				System.out.println(response);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
