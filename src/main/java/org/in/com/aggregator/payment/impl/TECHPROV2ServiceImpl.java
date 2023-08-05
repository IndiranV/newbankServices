package org.in.com.aggregator.payment.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.in.com.aggregator.payment.PGInterface;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.OrderDTO;
import org.in.com.dto.RefundDTO;
import org.in.com.dto.TransactionEnquiryDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.PaymentResponseException;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;

import com.tp.pg.util.TransactionRequestBean;
import com.tp.pg.util.TransactionResponseBean;

public class TECHPROV2ServiceImpl implements PGInterface {

	private final String TRANS_PAYLOAD = "T";
	private final String REFUND_PAYLOAD = "R";
	private final String VERIFICATION_PAYLOAD = "S";
	private final String WEB_SERVICE_URL = "https://payments.paynimo.com/PaynimoProxy/services/TransactionLiveDetails";

	public void packPaymentRequest(AuthDTO authDTO, OrderDTO order) throws Exception {
		TransactionRequestBean objTransactionRequestBean = new TransactionRequestBean();
		objTransactionRequestBean.setWebServiceLocator(WEB_SERVICE_URL);
		objTransactionRequestBean.setStrRequestType(TRANS_PAYLOAD);
		objTransactionRequestBean.setStrMerchantCode(order.getGatewayCredentials().getAccessCode());
		objTransactionRequestBean.setMerchantTxnRefNumber(order.getTransactionCode());
		objTransactionRequestBean.setStrAmount(String.valueOf(order.getAmount().setScale(2)));
		objTransactionRequestBean.setStrCurrencyCode("INR");
		objTransactionRequestBean.setStrITC("email:" + order.getEmail());
		objTransactionRequestBean.setStrReturnURL(order.getReturnUrl());
		objTransactionRequestBean.setStrShoppingCartDetails(order.getGatewayPartnerCode() + "_" + order.getAmount().setScale(1) + "_0.0");
		objTransactionRequestBean.setTxnDate(DateUtil.NOW().format("DD-MM-YYYY"));
		if (StringUtil.isNotNull(order.getEmail())) {
			objTransactionRequestBean.setStrEmail(order.getEmail());
		}
		objTransactionRequestBean.setStrMobileNumber(order.getMobile());
		if (StringUtil.isNotNull(order.getFirstName())) {
			objTransactionRequestBean.setStrCustomerName(StringUtil.removeSymbolWithSpace(order.getFirstName()));
		}
		objTransactionRequestBean.setStrTPSLTxnID("");
		objTransactionRequestBean.setIv(order.getGatewayCredentials().getAttr1().getBytes());
		objTransactionRequestBean.setKey(order.getGatewayCredentials().getAccessKey().getBytes());
		objTransactionRequestBean.setStrBankCode(order.getGatewayPartnerCode());
		Map<String, String> gatewayFormParam = new HashMap<String, String>();

		gatewayFormParam.put("transactionCode", order.getTransactionCode());
		order.setGatewayFormParam(gatewayFormParam);
		order.setGatewayUrl(objTransactionRequestBean.getTransactionToken());
		System.out.println(order.getGatewayUrl());
	}

	public void internalVerfication(AuthDTO authDTO, TransactionEnquiryDTO enquiryStatus) throws PaymentResponseException, Exception {
		TransactionRequestBean objTransactionRequestBean = new TransactionRequestBean();
		objTransactionRequestBean.setWebServiceLocator(enquiryStatus.getGatewayCredentials().getPropertiesFileName());
		objTransactionRequestBean.setStrRequestType(VERIFICATION_PAYLOAD);
		objTransactionRequestBean.setStrMerchantCode(enquiryStatus.getGatewayCredentials().getAccessCode());
		objTransactionRequestBean.setMerchantTxnRefNumber(enquiryStatus.getTransactionCode());
		objTransactionRequestBean.setStrAmount(String.valueOf(enquiryStatus.getAmount().setScale(2)));
		objTransactionRequestBean.setStrCurrencyCode("INR");
		objTransactionRequestBean.setStrReturnURL(enquiryStatus.getMerchantReturnUrl());
		objTransactionRequestBean.setStrShoppingCartDetails("India" + "_" + enquiryStatus.getAmount().setScale(2) + "_0.0");
		objTransactionRequestBean.setTxnDate(DateUtil.NOW().format("DD-MM-YYYY"));
		objTransactionRequestBean.setIv(enquiryStatus.getGatewayCredentials().getAttr1().getBytes());
		objTransactionRequestBean.setKey(enquiryStatus.getGatewayCredentials().getAccessKey().getBytes());

		String response = objTransactionRequestBean.getTransactionToken();
		System.out.println("Tech Process Internal Vefication Response : " + response);

		HashMap<String, String> gatewayResponsePair = new HashMap<String, String>();
		StringTokenizer gatewayResponseTokenizer = new StringTokenizer(response, "|");

		while (gatewayResponseTokenizer.hasMoreElements()) {
			String strrData = (String) gatewayResponseTokenizer.nextElement();
			StringTokenizer gatewayResponseParams = new StringTokenizer(strrData, "=");
			String strrKey = (String) gatewayResponseParams.nextElement();
			String strValue = (String) gatewayResponseParams.nextElement();
			gatewayResponsePair.put(strrKey, strValue);
		}
		if ("0300".equalsIgnoreCase(gatewayResponsePair.get("txn_status")) && "success".equalsIgnoreCase(gatewayResponsePair.get("txn_msg"))) {
			enquiryStatus.setStatus(ErrorCode.SUCCESS);
		}
		else {
			enquiryStatus.setStatus(ErrorCode.PAYMENT_DECLINED);
		}
		enquiryStatus.setGatewayTransactionCode(gatewayResponsePair.get("tpsl_txn_id"));
		enquiryStatus.setAmount(new BigDecimal(gatewayResponsePair.get("txn_amt")));
	}

	public void transactionVerify(AuthDTO authDTO, TransactionEnquiryDTO enquiryStatus) throws PaymentResponseException, Exception {
		TransactionResponseBean transactionResponseBean = new TransactionResponseBean();
		transactionResponseBean.setResponsePayload(enquiryStatus.getGatewayReponse().get("msg"));
		transactionResponseBean.setIv(enquiryStatus.getGatewayCredentials().getAttr1().getBytes());
		transactionResponseBean.setKey(enquiryStatus.getGatewayCredentials().getAccessKey().getBytes());
		String response = transactionResponseBean.getResponsePayload();
		System.out.println("Tech Process Transaction Enquiry Response : " + response);

		HashMap<String, String> gatewayResponsePair = new HashMap<String, String>();
		StringTokenizer gatewayResponseTokenizer = new StringTokenizer(response, "|");

		while (gatewayResponseTokenizer.hasMoreElements()) {
			String strrData = (String) gatewayResponseTokenizer.nextElement();
			StringTokenizer gatewayResponseParams = new StringTokenizer(strrData, "=");
			String strrKey = (String) gatewayResponseParams.nextElement();
			String strValue = (String) gatewayResponseParams.nextElement();
			gatewayResponsePair.put(strrKey, strValue);
		}
		if ("0300".equalsIgnoreCase(gatewayResponsePair.get("txn_status")) && "success".equalsIgnoreCase(gatewayResponsePair.get("txn_msg"))) {
			enquiryStatus.setStatus(ErrorCode.SUCCESS);
		}
		else {
			throw new PaymentResponseException(ErrorCode.TRANSACTION_ENQUIRY_NOT_AVAILABLE);
		}
		enquiryStatus.setGatewayTransactionCode(gatewayResponsePair.get("tpsl_txn_id"));
		enquiryStatus.setAmount(new BigDecimal(gatewayResponsePair.get("txn_amt")));
	}

	public void refund(AuthDTO authDTO, RefundDTO refund) throws Exception {
		TransactionRequestBean objTransactionRequestBean = new TransactionRequestBean();
		objTransactionRequestBean.setWebServiceLocator(refund.getGatewayCredentials().getPropertiesFileName());
		objTransactionRequestBean.setStrRequestType(REFUND_PAYLOAD);
		objTransactionRequestBean.setStrMerchantCode(refund.getGatewayCredentials().getAccessCode());
		objTransactionRequestBean.setMerchantTxnRefNumber(refund.getTransactionCode());
		objTransactionRequestBean.setStrAmount(String.valueOf(refund.getAmount().setScale(2)));
		objTransactionRequestBean.setStrCurrencyCode("INR");
		objTransactionRequestBean.setStrReturnURL("");
		objTransactionRequestBean.setStrShoppingCartDetails("India" + "_" + refund.getAmount().setScale(2) + "_0.0");
		objTransactionRequestBean.setTxnDate(DateUtil.NOW().format("DD-MM-YYYY"));
		objTransactionRequestBean.setIv(refund.getGatewayCredentials().getAccessKey().getBytes());
		objTransactionRequestBean.setKey(refund.getGatewayCredentials().getAttr1().getBytes());
	}

	@Override
	public Map<String, String> verifyPaymentOrder(AuthDTO authDTO, OrderDTO orderDTO) {
		// TODO Auto-generated method stub
		return null;
	}
}
