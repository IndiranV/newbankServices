package org.in.com.aggregator.payment.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.Adler32;

import org.in.com.aggregator.payment.PGInterface;
import org.in.com.config.GatewayConfig;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.OrderDTO;
import org.in.com.dto.RefundDTO;
import org.in.com.dto.TransactionEnquiryDTO;
import org.in.com.dto.enumeration.PaymentGatewayStatusEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.PaymentResponseException;
import org.in.com.exception.RefundException;
import org.in.com.utils.HttpServiceClient;
import org.in.com.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CCAServiceImpl implements PGInterface {
	private static Logger logger = LoggerFactory.getLogger(CCAServiceImpl.class);

	private static final String TRANSACTION_SUCCESS_CODE = "Y";
	private static final String REFUND_SUCCESS_CODE = "0";

	@Override
	public void packPaymentRequest(AuthDTO authDTO, OrderDTO order) {
		try {

			String str = order.getGatewayCredentials().getAccessCode() + "|" + order.getTransactionCode() + "|" + String.valueOf(order.getAmount()) + "|" + order.getReturnUrl() + "|" + order.getGatewayCredentials().getAccessKey();

			Adler32 adl = new Adler32();
			adl.update(str.getBytes());
			String checkSum = String.valueOf(adl.getValue());
			Map<String, String> gatewayInputData = new HashMap<String, String>();

			gatewayInputData.put("Checksum", checkSum);
			gatewayInputData.put("Merchant_Id", order.getGatewayCredentials().getAccessCode());
			gatewayInputData.put("Order_Id", order.getTransactionCode());
			gatewayInputData.put("vpc_OrderInfo", order.getTransactionCode());
			gatewayInputData.put("Amount", String.valueOf(order.getAmount()));
			gatewayInputData.put("Redirect_Url", order.getReturnUrl());

			gatewayInputData.put("billing_cust_name", StringUtil.removeSymbolWithSpace(order.getFirstName() + " " + order.getLastName()));
			gatewayInputData.put("billing_cust_tel", order.getMobile());
			gatewayInputData.put("billing_cust_country", "India");
			gatewayInputData.put("billing_cust_address", order.getAddress1() + " " + order.getAddress2());
			gatewayInputData.put("billing_cust_city", order.getCity());
			gatewayInputData.put("billing_cust_state", order.getState());
			gatewayInputData.put("billing_zip_code", order.getZipCode());
			gatewayInputData.put("billing_cust_email", order.getEmail());
			gatewayInputData.put("billing_cust_notes", order.getUdf1());

			if (order.getGatewayPartnerCode() == null) {
				order.setGatewayUrl(GatewayConfig.CCAVEUNE_CARD_URL);
			}
			else {
				gatewayInputData.put("cardOption", "netBanking");
				gatewayInputData.put("netBankingCards", order.getGatewayPartnerCode());
				order.setGatewayUrl(GatewayConfig.CCAVEUNE_NETBANKING_URL);
			}
			order.setGatewayFormParam(gatewayInputData);
		}
		catch (Exception e) {
			logger.error("", e);
		}
	}

	@Override
	public void internalVerfication(AuthDTO authDTO, TransactionEnquiryDTO enquiryStatus) throws PaymentResponseException {
		Map<String, String> avenueResponse = enquiryStatus.getGatewayReponse();
		String amount = avenueResponse.get("Amount");
		String gatewayTransactionId = avenueResponse.get("nb_order_no");
		try {
			/**
			 * 1.Get parameters from CCAvenue request parameter
			 * 2.Concatenate with working key
			 * 3.Using adler32 generate checksum
			 * 4.Compare new checksum(adler32) is equal with checksum sent by
			 * ccavenue in request
			 * 5.Then transaction is success else failures
			 * 
			 */

			String checksum = avenueResponse.get("Checksum");
			String merchantId = avenueResponse.get("Merchant_Id");
			String orderId = avenueResponse.get("Order_Id");
			String authDesc = avenueResponse.get("AuthDesc");
			String str = merchantId + "|" + orderId + "|" + amount + "|" + authDesc + "|" + enquiryStatus.getGatewayCredentials().getAccessKey();
			Adler32 adl = new Adler32();
			adl.update(str.getBytes());
			String newChecksum = String.valueOf(adl.getValue());

			if ((newChecksum.equals(checksum)) && (TRANSACTION_SUCCESS_CODE.equals(authDesc))) {
				enquiryStatus.setStatus(ErrorCode.SUCCESS);
			}
			else {
				enquiryStatus.setStatus(ErrorCode.PAYMENT_DECLINED);
			}
		}
		catch (Exception e) {
			// TODO need to test for the exceptional case whether error message
			// is binding for loggin purpose
			avenueResponse.put("Excception", e.getMessage() + e.getCause() + e);
			throw e;
		}
		finally {
			enquiryStatus.setResponseRecevied(avenueResponse.toString());
			enquiryStatus.setAmount(new BigDecimal(amount));
			enquiryStatus.setGatewayTransactionCode(gatewayTransactionId);
		}
	}

	@Override
	public void transactionVerify(AuthDTO authDTO, TransactionEnquiryDTO enquiryStatus) throws PaymentResponseException {
		throw new PaymentResponseException(ErrorCode.TRANSACTION_ENQUIRY_NOT_AVAILABLE);
	}

	@Override
	public void refund(AuthDTO authDTO, RefundDTO refund) throws Exception {
		try {
			String str = refund.getGatewayCredentials().getAccessCode() + "|" + refund.getAmount() + "|" + refund.getTransactionCode() + "|" + refund.getGatewayCredentials().getAccessKey();
			Adler32 adl = new Adler32();
			adl.update(str.getBytes());
			String checksum = String.valueOf(adl.getValue());
			String url = GatewayConfig.CCAVEUNE_REFUND_URL;
			HttpServiceClient client = new HttpServiceClient();
			Map<String, String> param = new HashMap<String, String>();
			param.put("user_id", refund.getGatewayCredentials().getAccessCode());
			param.put("order_no", refund.getTransactionCode());
			param.put("amount", String.valueOf(refund.getAmount()));
			param.put("chk_sum", checksum);
			try {
				String paymentGatewayResponse = client.get(url, param);
				logger.info("CCAvenue Refund response for order id " + refund.getGatewayCredentials().getAccessCode() + " - " + paymentGatewayResponse);
				if (paymentGatewayResponse != null) {
					String[] splitResponseString = paymentGatewayResponse.split("\\|");
					String responseState = splitResponseString[1].split("=")[1];
					if (REFUND_SUCCESS_CODE.equalsIgnoreCase(responseState)) {
						refund.setStatus(PaymentGatewayStatusEM.SUCCESS);
					}
					else {
						refund.setStatus(PaymentGatewayStatusEM.FAILURE);
					}
				}
				System.out.println("test" + paymentGatewayResponse);
			}
			catch (Exception e) {
				logger.error("", e);
				throw new RefundException(ErrorCode.AUTO_REFUND_IMPLEMENTATION_NOT_AVAILABLE);
			}
		}
		catch (Exception e) {
			logger.error("", e);
			throw new RefundException(ErrorCode.AUTO_REFUND_IMPLEMENTATION_NOT_AVAILABLE);
		}
	}

	@Override
	public Map<String, String> verifyPaymentOrder(AuthDTO authDTO, OrderDTO orderDTO) {
		// TODO Auto-generated method stub
		return null;
	}
}
