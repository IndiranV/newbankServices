package org.in.com.aggregator.payment.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import net.sf.json.JSONObject;

import org.in.com.aggregator.payment.PGInterface;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.OrderDTO;
import org.in.com.dto.PaymentGatewayCredentialsDTO;
import org.in.com.dto.RefundDTO;
import org.in.com.dto.TransactionEnquiryDTO;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.PaymentGatewayStatusEM;
import org.in.com.dto.enumeration.PaymentOrderEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.PaymentResponseException;
import org.in.com.utils.NumericGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.paytm.pg.merchant.CheckSumServiceHelper;

public class PAYTMServiceImpl implements PGInterface {

	private static final Logger logger = LoggerFactory.getLogger("org.in.com.controller.pgtrace");
	private final String STATUS_SUCESS = "TXN_SUCCESS";
	private final String STATUS_FAILURE = "TXN_FAILURE";
	private final String PROCESS_ORDER_URL = "https://securegw.paytm.in/order/process";
	private final String VERIFY_ORDER_URL = "https://securegw.paytm.in/order/status";
	private final String REFUND_ORDER_URL = "https://securegw.paytm.in/refund/HANDLER_INTERNAL/REFUND?JsonData=";

	@Override
	public void packPaymentRequest(AuthDTO authDTO, OrderDTO order) throws Exception {
		TreeMap<String, String> paytmReqParam = new TreeMap<String, String>();
		try {
			CheckSumServiceHelper checkSumServiceHelper = CheckSumServiceHelper.getCheckSumServiceHelper();

			paytmReqParam.put("REQUEST_TYPE", "DEFAULT");
			paytmReqParam.put("MID", order.getGatewayCredentials().getAccessCode());
			paytmReqParam.put("ORDER_ID", order.getTransactionCode());
			paytmReqParam.put("CUST_ID", order.getEmail());
			paytmReqParam.put("TXN_AMOUNT", String.valueOf(order.getAmount()));
			paytmReqParam.put("CHANNEL_ID", authDTO.getDeviceMedium() != null && (authDTO.getDeviceMedium().getId() == DeviceMediumEM.MOB_USER.getId() || authDTO.getDeviceMedium().getId() == DeviceMediumEM.APP_AND.getId() || authDTO.getDeviceMedium().getId() == DeviceMediumEM.APP_IOS.getId()) ? "WAP" : "WEB");
			paytmReqParam.put("INDUSTRY_TYPE_ID", order.getGatewayCredentials().getAttr1());
			paytmReqParam.put("WEBSITE", authDTO.getNamespaceCode());
			paytmReqParam.put("CALLBACK_URL", order.getReturnUrl());
			String checksum = checkSumServiceHelper.genrateCheckSum(order.getGatewayCredentials().getAccessKey(), paytmReqParam);
			paytmReqParam.put("CHECKSUMHASH", checksum);

			order.setGatewayFormParam(paytmReqParam);
			order.setGatewayUrl(PROCESS_ORDER_URL);
		}
		catch (Exception e) {
			logger.error("Exception occurred :" + order.getOrderCode() + e.getMessage());
			throw e;
		}
		finally {
			logger.info("packPaymentRequest: " + order.getOrderCode() + " - " + paytmReqParam.toString());
		}

	}

	@Override
	public void internalVerfication(AuthDTO authDTO, TransactionEnquiryDTO enquiryStatus) throws PaymentResponseException, Exception {
		try {
			Map<String, String> avenueResponse = enquiryStatus.getGatewayReponse();
			String status = avenueResponse.get("STATUS");
			String amount = avenueResponse.get("TXNAMOUNT");
			enquiryStatus.setAmount(new BigDecimal(amount));
			enquiryStatus.setGatewayTransactionCode(avenueResponse.get("TXNID"));
			enquiryStatus.setResponseRecevied(avenueResponse.toString());
			if (status.equals(STATUS_SUCESS)) {
				enquiryStatus.setStatus(ErrorCode.SUCCESS);
			}
			else {
				enquiryStatus.setStatus(ErrorCode.PAYMENT_DECLINED);
			}
		}
		catch (Exception e) {
			logger.error("Exception occurred for txn_id: " + enquiryStatus.getTransactionCode() + e.getMessage());
			throw e;
		}
		finally {
			logger.info("internalVerfication: " + enquiryStatus.getTransactionCode() + " - " + enquiryStatus.getGatewayReponse() + " - " + enquiryStatus.getStatus().getMessage());
		}
	}

	@Override
	public void transactionVerify(AuthDTO authDTO, TransactionEnquiryDTO enquiryStatus) throws PaymentResponseException, Exception {
		String response = null;
		JSONObject obj = null, jsonObject = null;
		try {
			TreeMap<String, String> paytmParams = new TreeMap<String, String>();
			paytmParams.put("MID", enquiryStatus.getGatewayCredentials().getAccessCode());
			paytmParams.put("ORDER_ID", enquiryStatus.getTransactionCode());
			String checksum = CheckSumServiceHelper.getCheckSumServiceHelper().genrateCheckSum(enquiryStatus.getGatewayCredentials().getAccessKey(), paytmParams);
			paytmParams.put("CHECKSUMHASH", checksum);

			obj = JSONObject.fromObject(paytmParams);
			String post_data = obj.toString();

			response = getConnection(VERIFY_ORDER_URL, post_data);
			jsonObject = JSONObject.fromObject(response);

			if (jsonObject != null && jsonObject.has("STATUS") && jsonObject.getString("STATUS").equals(STATUS_SUCESS)) {
				enquiryStatus.setStatus(ErrorCode.SUCCESS);
				enquiryStatus.setAmount(new BigDecimal(jsonObject.getString("TXNAMOUNT")));
			}
			else if (jsonObject != null && jsonObject.has("STATUS") && jsonObject.getString("STATUS").equals(STATUS_FAILURE)) {
				enquiryStatus.setAmount(new BigDecimal(jsonObject.getString("TXNAMOUNT")));
				enquiryStatus.setStatus(ErrorCode.PAYMENT_DECLINED);
			}
			else {
				enquiryStatus.setAmount(BigDecimal.ZERO);
				enquiryStatus.setStatus(ErrorCode.PAYMENT_DECLINED);
			}
			enquiryStatus.setGatewayTransactionCode(jsonObject != null && jsonObject.has("TXNID") ? jsonObject.getString("TXNID") : Text.NA);
		}
		catch (Exception e) {
			logger.error("Exception occurred for txn_id: " + enquiryStatus.getTransactionCode() + e.getMessage());
			response += e;
			throw e;
		}
		finally {
			enquiryStatus.setResponseRecevied(response);
			logger.info("enquiryStatus.getTransactionCode() " + enquiryStatus.getTransactionCode() + obj.toString() + " - " + jsonObject.toString());
		}
	}

	@Override
	public void refund(AuthDTO authDTO, RefundDTO refund) throws Exception {
		JSONObject body = new JSONObject();
		JSONObject jsonObject = null;
		TreeMap<String, String> paramMap = new TreeMap<String, String>();
		try {
			paramMap.put("MID", refund.getGatewayCredentials().getAccessCode());
			paramMap.put("ORDERID", refund.getOrderCode());
			paramMap.put("TXNID", refund.getGatewayTransactionCode());
			paramMap.put("REFUNDAMOUNT", String.valueOf(refund.getAmount()));
			paramMap.put("TXNTYPE", "REFUND");

			String checksum = CheckSumServiceHelper.getCheckSumServiceHelper().genrateRefundCheckSum(refund.getGatewayCredentials().getAccessKey(), paramMap);

			body.put("MID", refund.getGatewayCredentials().getAccessCode());
			body.put("ORDERID", refund.getOrderCode());
			body.put("TXNID", refund.getGatewayTransactionCode());
			body.put("REFUNDAMOUNT", String.valueOf(refund.getAmount()));
			body.put("TXNTYPE", "REFUND");
			body.put("CHECKSUM", URLEncoder.encode(checksum, "UTF-8"));
			body.put("REFID", NumericGenerator.randInt());

			String response = getConnection(REFUND_ORDER_URL + body.toString(), new JSONObject().toString());
			jsonObject = JSONObject.fromObject(response);

			if (jsonObject != null && jsonObject.has("STATUS") && jsonObject.getString("STATUS").equals(STATUS_SUCESS)) {
				refund.setStatus(PaymentGatewayStatusEM.SUCCESS);
			}
			else {
				refund.setStatus(PaymentGatewayStatusEM.FAILURE);
			}
		}
		catch (Exception exception) {
			exception.printStackTrace();
		}
		finally {
			logger.info("refund: request: paramMap-" + paramMap + " Body: " + body + " - response:" + jsonObject + " - " + refund.getStatus().getName());
		}
	}

	@Override
	public Map<String, String> verifyPaymentOrder(AuthDTO authDTO, OrderDTO orderDTO) {
		Map<String, String> responseMap = new HashMap<String, String>();
		try {
			TreeMap<String, String> paytmParams = new TreeMap<String, String>();
			paytmParams.put("MID", orderDTO.getGatewayCredentials().getAccessCode());
			paytmParams.put("ORDER_ID", orderDTO.getOrderCode());
			String checksum = CheckSumServiceHelper.getCheckSumServiceHelper().genrateCheckSum(orderDTO.getGatewayCredentials().getAccessKey(), paytmParams);
			paytmParams.put("CHECKSUMHASH", checksum);

			JSONObject obj = JSONObject.fromObject(paytmParams);
			String post_data = obj.toString();

			String response = getConnection(VERIFY_ORDER_URL, post_data);
			JSONObject jsonObject = JSONObject.fromObject(response);

			responseMap.put("transactionNumber", jsonObject.getString("TXNID"));
			responseMap.put("referenceNumber", jsonObject.getString("TXNID"));
			responseMap.put("bankReferenceNumber", jsonObject.getString("BANKTXNID"));
			responseMap.put("orderNumber", jsonObject.getString("ORDERID"));
			responseMap.put("orderAmount", jsonObject.getString("TXNAMOUNT"));
			responseMap.put("status", jsonObject.getString("STATUS"));
			responseMap.put("transactionType", jsonObject.getString("TXNTYPE"));
			responseMap.put("gatewayName", jsonObject.has("GATEWAYNAME") ? jsonObject.getString("GATEWAYNAME") : Text.NA);
			responseMap.put("responseCode", jsonObject.getString("RESPCODE"));
			responseMap.put("responseMessage", jsonObject.getString("RESPMSG"));
			responseMap.put("bankName", jsonObject.has("BANKNAME") ? jsonObject.getString("BANKNAME") : Text.NA);
			responseMap.put("paytmMode", jsonObject.has("PAYMENTMODE") ? jsonObject.getString("PAYMENTMODE") : Text.NA);
			responseMap.put("refundAmount", jsonObject.getString("REFUNDAMT"));
			responseMap.put("transactionDate", jsonObject.getString("TXNDATE"));
			if (jsonObject != null && jsonObject.has("STATUS") && jsonObject.getString("STATUS").equals(STATUS_SUCESS)) {
				responseMap.put("paymentOrderStatus", PaymentOrderEM.SUCCESS.getCode());
			}
			else {
				responseMap.put("paymentOrderStatus", PaymentOrderEM.PAYMENT_DECLINED.getCode());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return responseMap;
	}

	private String getConnection(String requestURL, String requestParam) throws Exception {
		// TODO change to spring rest template
		// RestTemplate rest = new RestTemplate();
		// HttpEntity<String> requestEntity = new
		// HttpEntity<String>(requestParam);
		// ResponseEntity<String> responseEntity = rest.postForEntity(new
		// URI(requestURL), requestEntity, String.class);
		// return null;

		String response = Text.EMPTY;
		URL url = new URL(requestURL);
		URLConnection conn = url.openConnection();
		conn.setDoOutput(true);
		OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		wr.write(requestParam);
		wr.flush();

		// Get the response
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line;
		while ((line = rd.readLine()) != null) {
			response += line;
		}
		return response;
	}

	public static void main(String[] args) throws PaymentResponseException, Exception {
		TransactionEnquiryDTO enquiryStatus = new TransactionEnquiryDTO();
		enquiryStatus.setGatewayCredentials(new PaymentGatewayCredentialsDTO());

		enquiryStatus.getGatewayCredentials().setAccessCode("PARVEE25827447456315");
		enquiryStatus.getGatewayCredentials().setAccessKey("xYS7VdrC!WFnJv&B");

		enquiryStatus.setTransactionCode("DEMJAHJ4520");

		// PAYTMServiceImpl paytmServiceImpl = new PAYTMServiceImpl();
		// paytmServiceImpl.transactionVerify(null, enquiryStatus);

		RefundDTO refund = new RefundDTO();
		refund.setGatewayCredentials(new PaymentGatewayCredentialsDTO());
		refund.getGatewayCredentials().setAccessCode("PARVEE25827447456315");
		refund.getGatewayCredentials().setAccessKey("xYS7VdrC!WFnJv&B");

		refund.setGatewayTransactionCode("20190923111212800110168523988914283");
		refund.setOrderCode("DEMJ9NF44401");
		refund.setAmount(Numeric.ONE_HUNDRED);
		// paytmServiceImpl.refund(null, refund);

		OrderDTO orderDTO = new OrderDTO();
		orderDTO.setGatewayCredentials(new PaymentGatewayCredentialsDTO());
		orderDTO.getGatewayCredentials().setAccessCode("PARVEE25827447456315");
		orderDTO.getGatewayCredentials().setAccessKey("xYS7VdrC!WFnJv&B");
		orderDTO.setOrderCode("DEMJADC55482");
		// paytmServiceImpl.verifyPaymentOrder(null, orderDTO);

	}

	public Map<String, String> getOrderStatus(AuthDTO authDTO, OrderDTO orderDTO) {
		Map<String, String> responseMap = new HashMap<String, String>();
		try {
			TreeMap<String, String> paytmParams = new TreeMap<String, String>();
			paytmParams.put("MID", orderDTO.getGatewayCredentials().getAccessCode());
			paytmParams.put("ORDER_ID", orderDTO.getOrderCode());
			String checksum = CheckSumServiceHelper.getCheckSumServiceHelper().genrateCheckSum(orderDTO.getGatewayCredentials().getAccessKey(), paytmParams);
			paytmParams.put("CHECKSUMHASH", checksum);

			JSONObject obj = JSONObject.fromObject(paytmParams);
			String post_data = obj.toString();

			String response = getConnection(VERIFY_ORDER_URL, post_data);
			JSONObject jsonObject = JSONObject.fromObject(response);

			if (jsonObject != null) {
				responseMap.put("transactionNumber", jsonObject.getString("TXNID"));
				responseMap.put("bankReferenceNumber", jsonObject.getString("BANKTXNID"));
				responseMap.put("orderNumber", jsonObject.getString("ORDERID"));
				responseMap.put("orderAmount", jsonObject.getString("TXNAMOUNT"));
				responseMap.put("status", jsonObject.getString("STATUS"));
				responseMap.put("transactionType", jsonObject.getString("TXNTYPE"));
				responseMap.put("gatewayName", jsonObject.getString("GATEWAYNAME"));
				responseMap.put("responseCode", jsonObject.getString("RESPCODE"));
				responseMap.put("responseMessage", jsonObject.getString("RESPMSG"));
				responseMap.put("bankName", jsonObject.getString("BANKNAME"));
				responseMap.put("paytmMode", jsonObject.getString("PAYMENTMODE"));
				responseMap.put("refundAmount", jsonObject.getString("REFUNDAMT"));
				responseMap.put("transactionDate", jsonObject.getString("TXNDATE"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			responseMap.put("Gateway Provider", "PAY TM");
		}
		return responseMap;
	}
}
