package org.in.com.controller.web;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;

import org.in.com.controller.commerce.io.OrderV3IO;
import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.enumeration.WalletAccessEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.UserWalletService;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("{authtoken}/wallet")
public class WalletController extends BaseController {
	@Autowired
	UserWalletService walletService;

	@RequestMapping(value = "/profile/balance", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<Map<String, Object>> getProfileBalance(@PathVariable("authtoken") String authtoken) throws Exception {
		Map<String, Object> currentDetails = new HashMap<>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null && authDTO.getUserCustomer() != null) {
			currentDetails = walletService.getCurrentCreditBalace(authDTO, authDTO.getUserCustomer());
		}
		return ResponseIO.success(currentDetails);
	}

	@RequestMapping(value = "/redeem/{walletCode}/details", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<OrderV3IO> getWalletRedeemDetails(@PathVariable("authtoken") String authtoken, @PathVariable("walletCode") String walletCode) throws Exception {
		OrderV3IO order = new OrderV3IO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {

			Map<String, String> redeemDetails = walletService.getWalletRedeemDetails(authDTO, walletCode);
			if (redeemDetails != null) {
				order.setCode(redeemDetails.get("code"));
				order.setCurrentBalance(new BigDecimal(redeemDetails.get("currentBalance")));
				order.setDiscountAmount(new BigDecimal(redeemDetails.get("redeemAmount")));
			}
			if (redeemDetails == null || StringUtil.isNull(order.getCode())) {
				throw new ServiceException(ErrorCode.INVALID_DISCOUNT_CODE);
			}
		}
		return ResponseIO.success(order);
	}

	@RequestMapping(value = "/coupon/details", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<Map<String, Object>>> getUserCoupons(@PathVariable("authtoken") String authtoken, String mobileNumber) throws Exception {
		List<Map<String, Object>> couponList = new ArrayList<Map<String, Object>>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			try {
				if (!StringUtil.isValidMobileNumber(mobileNumber)) {
					throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
				}
				couponList = walletService.getUserCachCoupons(authDTO, mobileNumber);
			}
			catch (ServiceException e) {
				throw e;
			}
			catch (Exception e) {
				e.printStackTrace();
				throw new ServiceException(e.getMessage());
			}
		}
		return ResponseIO.success(couponList);
	}

	@RequestMapping(value = "/user/transactrion", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<JSONArray> userTransaction(@PathVariable("authtoken") String authToken, String fromDate, String toDate, String mobileNumber) {
		JSONArray userTransactions = new JSONArray();
		AuthDTO authDTO = authService.getAuthDTO(authToken);
		if (authDTO != null) {
			if (!DateUtil.isValidDate(fromDate) || !DateUtil.isValidDate(toDate)) {
				throw new ServiceException(ErrorCode.INVALID_DATE);
			}
			userTransactions = walletService.userTransactionHistory(authDTO, fromDate, toDate, mobileNumber);
		}
		return ResponseIO.success(userTransactions);
	}

	@RequestMapping(value = "/user/balance", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<Map<String, String>>> getUserCurrentBalance(@PathVariable("authtoken") String authToken) {
		List<Map<String, String>> userBalanceList = new ArrayList<>();
		AuthDTO authDTO = authService.getAuthDTO(authToken);
		if (authDTO != null) {
			userBalanceList = walletService.getUserBalanceDetails(authDTO);
		}
		return ResponseIO.success(userBalanceList);
	}

	@RequestMapping(value = "/token", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<Map<String, String>> getWalletToken(@PathVariable("authtoken") String authToken) {
		Map<String, String> wallet = new HashMap<>();
		AuthDTO authDTO = authService.getAuthDTO(authToken);
		if (authDTO != null) {
			WalletAccessEM walletAccessEM = WalletAccessEM.getWalletAccessEM(authDTO.getNamespaceCode());
			if (walletAccessEM == null) {
				throw new ServiceException(ErrorCode.INVALID_NAMESPACE, "Invalid Wallet Account!");
			}
			wallet.put("token", walletAccessEM.getToken());
		}
		return ResponseIO.success(wallet);
	}

	@RequestMapping(value = "/user/referral", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<Map<String, String>> getUserReferral(@PathVariable("authtoken") String authtoken) throws Exception {
		Map<String, String> currentDetails = new HashMap<>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null && authDTO.getUserCustomer() != null) {
			currentDetails = walletService.getUserReferral(authDTO, authDTO.getUserCustomer());
		}
		return ResponseIO.success(currentDetails);
	}

	@RequestMapping(value = "/referral/{referralCode}/apply", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> applyReferralCode(@PathVariable("authtoken") String authtoken, @PathVariable("referralCode") String referralCode) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null && authDTO.getUserCustomer() != null) {
			walletService.applyUserReferral(authDTO, referralCode);
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/referral/{referralCode}/benificiary/verify", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> verifyBenificiaryReferral(@PathVariable("authtoken") String authtoken, @PathVariable("referralCode") String referralCode) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			walletService.verifyBenificiaryReferral(authDTO, referralCode);
		}
		return ResponseIO.success();
	}
}
