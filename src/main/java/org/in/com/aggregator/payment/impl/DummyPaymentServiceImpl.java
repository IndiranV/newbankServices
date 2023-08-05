package org.in.com.aggregator.payment.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.Adler32;

import org.in.com.aggregator.payment.PGInterface;
import org.in.com.config.ApplicationConfig;
import org.in.com.config.GatewayConfig;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.OrderDTO;
import org.in.com.dto.RefundDTO;
import org.in.com.dto.TransactionEnquiryDTO;
import org.in.com.dto.enumeration.PaymentGatewayStatusEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.PaymentResponseException;

public class DummyPaymentServiceImpl implements PGInterface {

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
			gatewayInputData.put("vpc_OrderInfo", order.getOrderCode());
			gatewayInputData.put("amount", String.valueOf(order.getAmount()));
			gatewayInputData.put("Redirect_Url", order.getReturnUrl());
			gatewayInputData.put("billing_cust_name", order.getFirstName());
			gatewayInputData.put("billing_cust_tel", order.getMobile());
			gatewayInputData.put("billing_cust_email", order.getEmail());
			if (order.getGatewayPartnerCode() == null) {
				order.setGatewayUrl("http://" + ApplicationConfig.getServerZoneUrl() + GatewayConfig.DUMMY_CARD_URL);
			}
			else {
				gatewayInputData.put("cardOption", "netBanking");
				gatewayInputData.put("netBankingCards", order.getGatewayPartnerCode());
				order.setGatewayUrl("http://" + ApplicationConfig.getServerZoneUrl() + GatewayConfig.DUMMY_NETBANKING_URL);
			}
			order.setGatewayFormParam(gatewayInputData);
		}
		catch (Exception e) {
			throw e;
		}
	}

	@Override
	public void internalVerfication(AuthDTO authDTO, TransactionEnquiryDTO enquiryStatus) throws PaymentResponseException {
		Map<String, String> avenueResponse = enquiryStatus.getGatewayReponse();
		// String amount = "1.00";//
		// PaymentResponseParamUtil.getParamValue(avenueResponse, "Amount");
		String amount = avenueResponse.get("amount");
		String status = avenueResponse.get("status");
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

			// String status = "Success";//
			// PaymentResponseParamUtil.getParamValue(avenueResponse, "status");
			if (("Success".equalsIgnoreCase(status))) {
				enquiryStatus.setStatus(ErrorCode.SUCCESS);
			}
			else {
				enquiryStatus.setStatus(ErrorCode.PAYMENT_DECLINED);
			}
		}
		catch (Exception e) {
			// TODO need to test for the exceptional case whether error message
			// is binding for loggin purpose
			// avenueResponse.put("Exception", new String[]
			// {e.getMessage()+e.getCause()+e});
			throw e;
		}
		finally {
			enquiryStatus.setResponseRecevied(avenueResponse.toString());
			enquiryStatus.setAmount(new BigDecimal(amount));
			enquiryStatus.setGatewayTransactionCode("NA");
		}
	}

	@Override
	public void transactionVerify(AuthDTO authDTO, TransactionEnquiryDTO enquiryStatus) throws PaymentResponseException {
		throw new PaymentResponseException(ErrorCode.TRANSACTION_ENQUIRY_NOT_AVAILABLE);
	}

	@Override
	public void refund(AuthDTO authDTO, RefundDTO refund) throws Exception {
		refund.setStatus(PaymentGatewayStatusEM.SUCCESS);
	}

	@Override
	public Map<String, String> verifyPaymentOrder(AuthDTO authDTO, OrderDTO orderDTO) {
		// TODO Auto-generated method stub
		return null;
	}
}
