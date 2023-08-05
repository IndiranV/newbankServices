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
import org.in.com.utils.StringUtil;
import org.in.com.utils.TokenGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import net.sf.json.JSONObject;

public class PayUBizServiceImpl implements PGInterface {
	private static final Logger responselogger = LoggerFactory.getLogger("org.in.com.controller.pgtrace");
	// Base URL
	private static String PAY_U_MONEY_BASE_URL_SECURE = "https://secure.payu.in";
	private static String PAY_U_MONEY_BASE_URL = "https://info.payu.in";
	private static String VERIFY_PAYMENT = "/merchant/postservice.php";
	private static String REFUND_PAYMENT = "/merchant/postservice.php";
	private static String PAYMENT = "/_payment";

	public void packPaymentRequest(AuthDTO authDTO, OrderDTO order) {
		StringBuilder payuMoneyHashRequest = new StringBuilder();
		try {
			payuMoneyHashRequest.append(order.getGatewayCredentials().getAccessCode());
			payuMoneyHashRequest.append("|");
			payuMoneyHashRequest.append(order.getOrderCode());
			payuMoneyHashRequest.append("|");
			payuMoneyHashRequest.append(order.getAmount());
			payuMoneyHashRequest.append("|");
			payuMoneyHashRequest.append(order.getOrderType().getName());
			payuMoneyHashRequest.append("|");
			payuMoneyHashRequest.append(order.getFirstName());
			payuMoneyHashRequest.append("|");
			payuMoneyHashRequest.append(order.getEmail());
			payuMoneyHashRequest.append("|");
			payuMoneyHashRequest.append(order.getUdf1());
			payuMoneyHashRequest.append("|");
			payuMoneyHashRequest.append(order.getUdf2());
			payuMoneyHashRequest.append("|");
			payuMoneyHashRequest.append(order.getUdf3());
			payuMoneyHashRequest.append("|");
			payuMoneyHashRequest.append(order.getUdf4());
			payuMoneyHashRequest.append("|");
			payuMoneyHashRequest.append(order.getUdf5());
			payuMoneyHashRequest.append("|");
			payuMoneyHashRequest.append("|");
			payuMoneyHashRequest.append("|");
			payuMoneyHashRequest.append("|");
			payuMoneyHashRequest.append("|");
			payuMoneyHashRequest.append("|");
			payuMoneyHashRequest.append(order.getGatewayCredentials().getAccessKey());

			Map<String, String> requestMap = new HashMap<String, String>();
			requestMap.put("key", order.getGatewayCredentials().getAccessCode());
			requestMap.put("txnid", order.getOrderCode());
			requestMap.put("amount", String.valueOf(order.getAmount()));
			requestMap.put("productinfo", order.getOrderType().getName());
			requestMap.put("firstname", order.getFirstName());
			requestMap.put("email", order.getEmail());
			requestMap.put("udf1", order.getUdf1());
			requestMap.put("udf2", order.getUdf2());
			requestMap.put("udf3", order.getUdf3());
			requestMap.put("udf4", order.getUdf4());
			requestMap.put("udf5", order.getUdf5());
			requestMap.put("phone", order.getMobile());
			requestMap.put("surl", order.getReturnUrl());
			requestMap.put("furl", order.getReturnUrl());

			String hash = getHash("SHA-512", payuMoneyHashRequest.toString());
			requestMap.put("hash", hash);
			requestMap.put("action", PAY_U_MONEY_BASE_URL_SECURE + PAYMENT);

			order.setGatewayFormParam(requestMap);
			order.setGatewayUrl(PAY_U_MONEY_BASE_URL_SECURE + PAYMENT);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			responselogger.info("packPaymentRequest PNR: " + order.getTransactionCode() + Text.SINGLE_SPACE + order.getGatewayFormParam() + Text.SINGLE_SPACE + payuMoneyHashRequest);
		}
	}

	@Override
	public void transactionVerify(AuthDTO authDTO, TransactionEnquiryDTO enquiryStatus) {
		String response = "";
		Map<String, String> fields = null;
		BigDecimal amount = new BigDecimal(0);
		try {
			String salt = enquiryStatus.getGatewayCredentials().getAccessKey();
			fields = enquiryStatus.getGatewayReponse();
			String responseHash = fields.get("hash");
			boolean transactionStatus = "success".equals(fields.get("status"));
			String generatedHash = getPaymentResponseHashKey(fields, salt);
			if (transactionStatus && responseHash != null && generatedHash != null && responseHash.equals(generatedHash)) {
				enquiryStatus.setStatus(ErrorCode.SUCCESS);
			}
			else {
				responselogger.error("{} generatedHash - {} fields - {} {}", enquiryStatus.getOrderCode(), generatedHash, fields, ErrorCode.PAYMENT_DECLINED.toString());
				enquiryStatus.setStatus(ErrorCode.PAYMENT_DECLINED);
				if ("usercancelled".equals(fields.get("unmappedstatus"))) {
					System.out.println("usercancelled");
				}
			}
			amount = new BigDecimal(fields.get("amount"));
		}
		catch (Exception e) {
			response += e;
			throw e;
		}
		finally {
			if (fields != null) {
				response += fields.get("Error") + "," + fields.get("status") + "," + fields.get("amount") + "," + fields.get("mihpayid") + "," + fields.get("mode") + ",";
				enquiryStatus.setResponseRecevied(response);
				enquiryStatus.setGatewayTransactionCode(fields.get("mihpayid"));
				enquiryStatus.setAmount(amount);
			}
			responselogger.info(enquiryStatus.getOrderCode() + " XactionCheck: " + enquiryStatus.getGatewayReponse() + " Status:" + enquiryStatus.getStatus().getCode());
		}

	}

	public Map<String, String> verifyPaymentOrder(AuthDTO authDTO, OrderDTO orderDTO) {
		Map<String, String> responseMap = new HashMap<String, String>();
		try {
			StringBuilder request = new StringBuilder();
			request.append(orderDTO.getGatewayCredentials().getAccessCode());
			request.append("|");
			request.append("verify_payment");
			request.append("|");
			request.append(orderDTO.getOrderCode());
			request.append("|");
			request.append(orderDTO.getGatewayCredentials().getAccessKey());

			Client client = Client.create(getTLSConfig(new DefaultClientConfig()));

			WebResource webResource = client.resource(PAY_U_MONEY_BASE_URL + VERIFY_PAYMENT);
			MultivaluedMapImpl values = new MultivaluedMapImpl();
			values.add("key", orderDTO.getGatewayCredentials().getAccessCode());
			values.add("command", "verify_payment");
			values.add("hash", getHash("SHA-512", request.toString()));
			values.add("var1", orderDTO.getOrderCode());
			values.add("form", "2");
			ClientResponse response = webResource.type(MediaType.APPLICATION_FORM_URLENCODED).post(ClientResponse.class, values);

			String json = (String) response.getEntity(String.class);
			if (StringUtil.isNotNull(json)) {
				JSONObject jsonResponse = JSONObject.fromObject(json);

				if (jsonResponse.getInt("status") == 1) {
					JSONObject responseJSON = jsonResponse.getJSONObject("transaction_details").getJSONObject(orderDTO.getOrderCode());
					responseMap.put("status", responseJSON.has("status") ? responseJSON.getString("status") : "");
					responseMap.put("referenceNumber", responseJSON.has("mihpayid") ? responseJSON.getString("mihpayid") : "");
					responseMap.put("orderNumber", responseJSON.has("txnid") ? responseJSON.getString("txnid") : "");
					responseMap.put("orderAmount", responseJSON.has("amt") ? responseJSON.getString("amt") : "");
					responseMap.put("dateTime", responseJSON.has("addedon") ? responseJSON.getString("addedon") : "");
					responseMap.put("additionalCharges", responseJSON.has("additional_charges") ? responseJSON.getString("additional_charges") : "");
					responseMap.put("name", responseJSON.has("firstname") ? responseJSON.getString("firstname") : "");
					responseMap.put("orderCapturedAmount", responseJSON.has("transaction_amount") ? responseJSON.getString("transaction_amount") : "");
					responseMap.put("bankCode", responseJSON.has("bankcode") ? responseJSON.getString("bankcode") : "");
					responseMap.put("orderStatus", responseJSON.has("unmappedstatus") ? responseJSON.getString("unmappedstatus") : "");
					responseMap.put("settledAt", responseJSON.has("Settled_At") ? responseJSON.getString("Settled_At") : "");
					responseMap.put("netDebitAmount", responseJSON.has("net_amount_debit") ? responseJSON.getString("net_amount_debit") : "");
					responseMap.put("cardName", responseJSON.has("name_on_card") ? responseJSON.getString("name_on_card") : "");
					responseMap.put("discount", responseJSON.has("disc") ? responseJSON.getString("disc") : "");
					responseMap.put("bankReferenceNumber", responseJSON.has("bank_ref_num") ? responseJSON.getString("bank_ref_num") : "");
					responseMap.put("requestRefferenceNumber", responseJSON.has("request_id") ? responseJSON.getString("request_id") : "");

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
			responseMap.put("Gateway Provider", "PayUBiz");
		}
		return responseMap;

	}

	public void refund(AuthDTO authDTO, RefundDTO refund) {
		JSONObject jsonResponse = null;
		try {
			// Hash Parameter
			String uniqueToken = TokenGenerator.generateToken(5);
			StringBuilder hashParam = new StringBuilder();
			hashParam.append(refund.getGatewayCredentials().getAccessCode());
			hashParam.append("|");
			hashParam.append("cancel_refund_transaction");
			hashParam.append("|");
			hashParam.append(refund.getGatewayTransactionCode().trim());
			hashParam.append("|");
			hashParam.append(refund.getGatewayCredentials().getAccessKey());

			Client client = Client.create(getTLSConfig(new DefaultClientConfig()));

			String mihpayId = getMihpayId(authDTO, refund);

			WebResource webResource = client.resource(PAY_U_MONEY_BASE_URL + REFUND_PAYMENT);
			MultivaluedMapImpl values = new MultivaluedMapImpl();
			values.add("key", refund.getGatewayCredentials().getAccessCode());
			values.add("command", "cancel_refund_transaction");
			values.add("hash", getHash("SHA-512", hashParam.toString()));
			values.add("var1", mihpayId);
			values.add("var2", refund.getTransactionCode() + uniqueToken);
			values.add("var3", String.valueOf(refund.getAmount()));
			values.add("form", "2");
			responselogger.info("Refund 01:" + refund.getGatewayTransactionCode() + Text.SINGLE_SPACE + refund.getAmount() + Text.SINGLE_SPACE + refund.getTransactionCode() + " Request :" + hashParam + values);
			ClientResponse response = webResource.type(MediaType.APPLICATION_FORM_URLENCODED).post(ClientResponse.class, values);

			String json = (String) response.getEntity(String.class);
			if (StringUtil.isNotNull(json)) {
				jsonResponse = JSONObject.fromObject(json);
				if (jsonResponse.getInt("status") == 1) {
					refund.setStatus(PaymentGatewayStatusEM.SUCCESS);
				}
				else {
					refund.setStatus(PaymentGatewayStatusEM.FAILURE);
				}
			}
			else {
				refund.setStatus(PaymentGatewayStatusEM.FAILURE);
			}
			refund.setResponseRecevied(" Request : " + values + " Response: " + jsonResponse.toString());
		}
		catch (Exception e) {
			e.printStackTrace();
			refund.setResponseRecevied(e.getMessage());
		}
		finally {
			responselogger.info("Refund 02: " + refund.getGatewayTransactionCode() + Text.SINGLE_SPACE + refund.getAmount() + Text.SINGLE_SPACE + refund.getTransactionCode() + " Response :" + jsonResponse);
		}
	}

	@Override
	public void internalVerfication(AuthDTO authDTO, TransactionEnquiryDTO enquiryStatus) throws PaymentResponseException, Exception {

	}

	private String getMihpayId(AuthDTO authDTO, RefundDTO refund) {
		OrderDTO orderDTO = new OrderDTO();
		orderDTO.setOrderCode(refund.getOrderCode());
		orderDTO.setGatewayCredentials(refund.getGatewayCredentials());
		Map<String, String> responseMap = verifyPaymentOrder(authDTO, orderDTO);

		String mihpayId = "";
		if (responseMap != null && responseMap.containsKey("referenceNumber")) {
			mihpayId = responseMap.get("referenceNumber");
		}
		return mihpayId;
	}

	public String getHash(String type, String param) {
		byte[] hashseq = param.getBytes();
		StringBuffer hexString = new StringBuffer();
		try {
			MessageDigest algorithm = MessageDigest.getInstance(type);
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

		}
		catch (NoSuchAlgorithmException nsae) {
		}
		return hexString.toString();
	}

	private String getPaymentResponseHashKey(Map<String, String> data, String salt) {
		String hashString = "";
		String hash = null;
		try {
			String hashSequence = "status||||||udf5|udf4|udf3|udf2|udf1|email|firstname|productinfo|amount|txnid|key";
			String[] hashVarSeq = hashSequence.split("\\|");
			hashString = hashString.concat(salt);
			for (String part : hashVarSeq) {
				hashString = hashString.concat("|");
				hashString = hashString.concat(StringUtil.isNull(data.get(part)) ? "" : data.get(part).trim());
			}

			hash = getHash("SHA-512", hashString);
		}
		catch (Exception e) {
		}
		return hash;
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
