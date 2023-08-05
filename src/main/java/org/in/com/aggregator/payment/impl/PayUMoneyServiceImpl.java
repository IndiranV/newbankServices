package org.in.com.aggregator.payment.impl;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.core.MediaType;

import net.sf.json.JSONObject;

import org.in.com.aggregator.payment.PGInterface;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.OrderDTO;
import org.in.com.dto.RefundDTO;
import org.in.com.dto.TransactionEnquiryDTO;
import org.in.com.dto.enumeration.PaymentGatewayStatusEM;
import org.in.com.dto.enumeration.PaymentOrderEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.PaymentResponseException;
import org.in.com.utils.JSONUtil;
import org.in.com.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class PayUMoneyServiceImpl implements PGInterface {
	private static final Logger RESPONSE_LOGGER = LoggerFactory.getLogger("org.in.com.controller.pgtrace");
	private static final String ENCRYPTION_TYPE = "SHA-512";
	// Base URL
	private static String PAY_U_MONEY_PAYMENT_URL = "https://secure.payu.in/_payment";
	private static String PAY_U_MONEY_BASE_URL = "https://www.payumoney.com";
	private static String PAY_U_MONEY_PAYMENT_VERIFY = "/payment/op/getPaymentResponse";
	private static String PAY_U_MONEY_REFUND = "/treasury/merchant/refundPayment";

	private static final String SUCCESS_CODE = "captured";
	private static final String USER_CANCELLED = "usercancelled";

	@Override
	public void packPaymentRequest(AuthDTO authDTO, OrderDTO order) throws Exception {
		StringBuilder payuMoneyHashRequest = new StringBuilder();
		try { // First generate hash - sha-512 using order data
			payuMoneyHashRequest.append(order.getGatewayCredentials().getAccessCode()).append("|");
			payuMoneyHashRequest.append(order.getTransactionCode()).append("|");
			payuMoneyHashRequest.append(order.getAmount()).append("|");
			payuMoneyHashRequest.append(order.getOrderType().getName()).append("|");
			payuMoneyHashRequest.append(order.getFirstName()).append("|");
			payuMoneyHashRequest.append(order.getEmail()).append("|");
			payuMoneyHashRequest.append(order.getUdf1()).append("|");
			payuMoneyHashRequest.append(order.getUdf2()).append("|");
			payuMoneyHashRequest.append(order.getUdf3()).append("||");
			payuMoneyHashRequest.append(order.getUdf5()).append("||||||");
			payuMoneyHashRequest.append(order.getGatewayCredentials().getAccessKey());

			// Prepare payu request form data
			Map<String, String> requestMap = new HashMap<String, String>();
			requestMap.put("key", order.getGatewayCredentials().getAccessCode());
			requestMap.put("txnid", order.getTransactionCode());
			requestMap.put("amount", String.valueOf(order.getAmount()));
			requestMap.put("productinfo", order.getOrderType().getName());
			requestMap.put("firstname", order.getFirstName());
			requestMap.put("email", order.getEmail());
			requestMap.put("udf1", order.getUdf1());
			requestMap.put("udf2", order.getUdf2());
			requestMap.put("udf3", order.getUdf3());
			requestMap.put("udf5", order.getUdf5());
			requestMap.put("phone", order.getMobile());
			requestMap.put("surl", order.getReturnUrl());
			requestMap.put("furl", order.getReturnUrl());

			String hash = getHash(payuMoneyHashRequest.toString());
			requestMap.put("hash", hash);
			requestMap.put("action", PAY_U_MONEY_PAYMENT_URL);
			requestMap.put("hashString", payuMoneyHashRequest.toString());

			order.setGatewayFormParam(requestMap);
			order.setGatewayUrl(PAY_U_MONEY_PAYMENT_URL);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			RESPONSE_LOGGER.info("PPR: " + order.getTransactionCode() + Text.SINGLE_SPACE + order.getGatewayFormParam() + Text.SINGLE_SPACE + payuMoneyHashRequest);
		}
	}

	private String getHash(String param) throws NoSuchAlgorithmException {
		byte[] hashseq = param.getBytes();
		StringBuffer hexString = new StringBuffer();

		MessageDigest algorithm = MessageDigest.getInstance(ENCRYPTION_TYPE);
		algorithm.reset();
		algorithm.update(hashseq);
		byte messageDigest[] = algorithm.digest();
		for (int i = 0; i < messageDigest.length; i++) {
			String hex = Integer.toHexString(0xFF & messageDigest[i]);
			if (hex.length() == 1) {
				hexString.append("0");
			}
			hexString.append(hex);
		}
		return hexString.toString();
	}

	@Override
	public void internalVerfication(AuthDTO authDTO, TransactionEnquiryDTO enquiryStatus) throws PaymentResponseException, Exception {
		BigDecimal amount = new BigDecimal(0);
		String salt = enquiryStatus.getGatewayCredentials().getAccessKey();
		Map<String, String> fields = enquiryStatus.getGatewayReponse();
		String responseHash = fields.get("hash");
		boolean transactionStatus = "success".equals(fields.get("status"));
		String generatedHash = getPaymentResponseHashKey(fields, salt);

		if (transactionStatus && responseHash != null && generatedHash != null && responseHash.equals(generatedHash)) {
			enquiryStatus.setStatus(ErrorCode.SUCCESS);
		}
		else {
			enquiryStatus.setStatus(ErrorCode.PAYMENT_DECLINED);
			if ("usercancelled".equals(fields.get("unmappedstatus"))) {
				enquiryStatus.setStatus(ErrorCode.TRANSACTION_CANCELLED_BY_USER);
			}
		}
		amount = new BigDecimal(fields.get("amount"));

		// TODO set charges and tax values
		enquiryStatus.setGatewayTransactionCode(fields.get("paymentId"));
		enquiryStatus.setAmount(amount);
	}

	private String getPaymentResponseHashKey(Map<String, String> data, String salt) throws NoSuchAlgorithmException {
		String hashString = Text.EMPTY;
		String hash = null;
		String hashSequence = "status||||||udf5|udf4|udf3|udf2|udf1|email|firstname|productinfo|amount|txnid|key";
		String[] hashVarSeq = hashSequence.split("\\|");
		hashString = hashString.concat(salt);
		for (String part : hashVarSeq) {
			hashString = hashString.concat("|");
			hashString = hashString.concat(StringUtil.isNull(data.get(part)) ? Text.EMPTY : data.get(part).trim());
		}

		hash = getHash(hashString);
		data.put("generatedhash", hash);
		return hash;
	}

	@Override
	public void transactionVerify(AuthDTO authDTO, TransactionEnquiryDTO enquiryStatus) throws PaymentResponseException, Exception {
		String errorResponse = "";
		Map<String, String> fields = null;
		try {
			Client client = Client.create(getTLSConfig(new DefaultClientConfig()));

			fields = enquiryStatus.getGatewayReponse();

			WebResource webResource = client.resource(PAY_U_MONEY_BASE_URL + PAY_U_MONEY_PAYMENT_VERIFY + "?merchantKey=" + enquiryStatus.getGatewayCredentials().getAccessCode() + "&merchantTransactionIds=" + enquiryStatus.getOrderCode());

			ClientResponse response = webResource.type(MediaType.APPLICATION_FORM_URLENCODED).header("Authorization", enquiryStatus.getGatewayCredentials().getAttr1()).post(ClientResponse.class, new MultivaluedMapImpl());

			String json = (String) response.getEntity(String.class);
			RESPONSE_LOGGER.info(enquiryStatus.getOrderCode() + " -- " + response + "\n" + json);

			JSONObject jsonResponse = JSONObject.fromObject(json);
			if (jsonResponse.getInt("status") >= 0 || (jsonResponse.has("result") && jsonResponse.getJSONArray("result") != null && jsonResponse.getJSONArray("result").get(0) != null)) {
				JSONObject data = (JSONObject) jsonResponse.getJSONArray("result").get(0);
				JSONObject responseJSON = data.getJSONObject("postBackParam");

				// TODO set charges and tax values
				enquiryStatus.setGatewayTransactionCode(responseJSON.getString("paymentId"));

				if (responseJSON.has("unmappedstatus")) {
					if (SUCCESS_CODE.equalsIgnoreCase(responseJSON.getString("unmappedstatus")) && responseJSON.has("net_amount_debit")) {
						enquiryStatus.setAmount(new BigDecimal(responseJSON.getString("net_amount_debit")));
						enquiryStatus.setGatewayTransactionCode(responseJSON.getString("payuMoneyId"));
						enquiryStatus.setStatus(ErrorCode.SUCCESS);
					}
					else if (USER_CANCELLED.equalsIgnoreCase(responseJSON.getString("unmappedstatus"))) {
						enquiryStatus.setStatus(ErrorCode.TRANSACTION_CANCELLED_BY_USER);
						enquiryStatus.setAmount(new BigDecimal(responseJSON.getString("amount")));
						enquiryStatus.setGatewayTransactionCode(responseJSON.getString("payuMoneyId"));
					}
				}
				else {
					enquiryStatus.setStatus(ErrorCode.PAYMENT_DECLINED);
					enquiryStatus.setAmount(new BigDecimal(responseJSON.getString("amount")));
					enquiryStatus.setGatewayTransactionCode(responseJSON.getString("payuMoneyId"));
				}

				// TODO too much conversion this can be changed to improve
				// performance either use map or JSON
				enquiryStatus.setGatewayReponse(JSONUtil.jsonToMap(data));
			}
		}
		catch (Exception e) {
			errorResponse += e;
			throw e;
		}
		finally {
			if (fields != null) {
				errorResponse += fields.get("Error") + "," + fields.get("status") + "," + fields.get("amount") + "," + fields.get("paymentId") + "," + fields.get("mode") + ",";
				enquiryStatus.setResponseRecevied(errorResponse);
			}
			RESPONSE_LOGGER.info(enquiryStatus.getOrderCode() + " XactionCheck: " + enquiryStatus.getGatewayReponse() + " Status:" + enquiryStatus.getStatus().getCode());
		}
	}

	@Override
	public void refund(AuthDTO authDTO, RefundDTO refund) throws Exception {
		try {
			String requestParam = "?paymentId=" + refund.getGatewayTransactionCode() + "&refundAmount=" + refund.getAmount() + "&merchantKey=" + refund.getGatewayCredentials().getAccessCode();

			Client client = Client.create(getTLSConfig(new DefaultClientConfig()));
			WebResource webResource = client.resource(PAY_U_MONEY_BASE_URL + PAY_U_MONEY_REFUND + requestParam);

			ClientResponse response = webResource.header("Content-Type", "application/json").header("Authorization", refund.getGatewayCredentials().getAttr1()).post(ClientResponse.class);

			String json = (String) response.getEntity(String.class);

			JSONObject fullJsonResponse = JSONObject.fromObject(json);
			RESPONSE_LOGGER.info("Refund:" + requestParam + " - " + json);

			if (fullJsonResponse != null) {
				if (fullJsonResponse.has("status") && fullJsonResponse.getInt("status") >= 0) {
					refund.setStatus(PaymentGatewayStatusEM.SUCCESS);
					refund.setGatewayTransactionCode(fullJsonResponse.getString("result") + "_" + refund.getGatewayTransactionCode());
				}
				else {
					refund.setStatus(PaymentGatewayStatusEM.FAILURE);
				}
			}
			else {
				refund.setStatus(PaymentGatewayStatusEM.FAILURE);
			}

			refund.setResponseRecevied(" Request: " + requestParam + " Response: " + fullJsonResponse.toString());
		}
		catch (Exception e) {
			e.printStackTrace();
			refund.setResponseRecevied(e.getMessage());
		}
	}

	@Override
	public Map<String, String> verifyPaymentOrder(AuthDTO authDTO, OrderDTO orderDTO) {
		Map<String, String> responseMap = new HashMap<String, String>();
		try {
			Client client = Client.create(getTLSConfig(new DefaultClientConfig()));

			String requestParam = "?merchantKey=" + orderDTO.getGatewayCredentials().getAccessCode() + "&merchantTransactionIds=" + orderDTO.getOrderCode();
			WebResource webResource = client.resource(PAY_U_MONEY_BASE_URL + PAY_U_MONEY_PAYMENT_VERIFY + requestParam);

			ClientResponse response = webResource.type(MediaType.APPLICATION_FORM_URLENCODED).header("Authorization", orderDTO.getGatewayCredentials().getAttr1()).post(ClientResponse.class, new MultivaluedMapImpl());

			String json = (String) response.getEntity(String.class);
			if (StringUtil.isNotNull(json)) {
				JSONObject jsonResponse = JSONObject.fromObject(json);

				if (jsonResponse.getInt("status") >= 0 || (jsonResponse.has("result") && jsonResponse.getJSONArray("result") != null && jsonResponse.getJSONArray("result").get(0) != null)) {
					JSONObject data = (JSONObject) jsonResponse.getJSONArray("result").get(0);
					JSONObject responseJSON = data.getJSONObject("postBackParam");
					responseMap.put("status", responseJSON.has("status") ? responseJSON.getString("status") : "");
					responseMap.put("referenceNumber", responseJSON.has("paymentId") ? responseJSON.getString("paymentId") : "");
					responseMap.put("orderNumber", responseJSON.has("txnid") ? responseJSON.getString("txnid") : "");
					responseMap.put("orderAmount", responseJSON.has("amount") ? responseJSON.getString("amount") : "");
					responseMap.put("dateTime", responseJSON.has("addedon") ? responseJSON.getString("addedon") : "");
					responseMap.put("additionalCharges", responseJSON.has("additionalCharges") ? responseJSON.getString("additionalCharges") : "");
					responseMap.put("name", responseJSON.has("firstname") ? responseJSON.getString("firstname") : "");
					responseMap.put("orderCapturedAmount", responseJSON.has("amount") ? responseJSON.getString("amount") : "");
					responseMap.put("bankCode", responseJSON.has("bankcode") ? responseJSON.getString("bankcode") : "");
					responseMap.put("orderStatus", responseJSON.has("unmappedstatus") ? responseJSON.getString("unmappedstatus") : "");
					responseMap.put("netDebitAmount", responseJSON.has("net_amount_debit") ? responseJSON.getString("net_amount_debit") : "");
					responseMap.put("cardName", responseJSON.has("name_on_card") ? responseJSON.getString("name_on_card") : "");
					responseMap.put("discount", responseJSON.has("discount") ? responseJSON.getString("discount") : "");
					responseMap.put("bankReferenceNumber", responseJSON.has("bank_ref_num") ? responseJSON.getString("bank_ref_num") : "");

					if ("success".equalsIgnoreCase(responseMap.get("orderStatus")) || "Captured".equalsIgnoreCase(responseMap.get("orderStatus"))) {
						responseMap.put("paymentOrderStatus", PaymentOrderEM.SUCCESS.getCode());
					}
					else if ("Bounced".equalsIgnoreCase(responseMap.get("orderStatus")) || "cancelledbyuser".equalsIgnoreCase(responseMap.get("orderStatus").replace(Text.SINGLE_SPACE, Text.EMPTY)) || "failedbybank".equalsIgnoreCase(responseMap.get("orderStatus").replace(Text.SINGLE_SPACE, Text.EMPTY)) || "Dropped".equalsIgnoreCase(responseMap.get("orderStatus"))) {
						responseMap.put("paymentOrderStatus", PaymentOrderEM.PAYMENT_DECLINED.getCode());
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			responseMap.put("Gateway Provider", "PayUMoney");
		}
		return responseMap;
	}

	private static ClientConfig getTLSConfig(ClientConfig config) throws Exception {
		try {
			TrustManager[] trustAllCerts = { new X509TrustManager() {
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(X509Certificate[] certs, String authType) {
				}

				public void checkServerTrusted(X509Certificate[] certs, String authType) {
				}
			} };
			SSLContext sc = SSLContext.getInstance("TLSv1.2");
			sc.init(null, trustAllCerts, new SecureRandom());
			config.getProperties().put("com.sun.jersey.client.impl.urlconnection.httpsProperties", new HTTPSProperties(new HostnameVerifier() {

				public boolean verify(String s, SSLSession sslSession) {
					return true;
				}
			}, sc));
		}
		catch (Exception e) {
			throw e;
		}
		return config;
	}
}
