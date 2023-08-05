package org.in.com.aggregator.payment.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;

import net.sf.json.JSONObject;

import org.apache.commons.lang.ArrayUtils;
import org.in.com.aggregator.payment.PGInterface;
import org.in.com.config.ApplicationConfig;
import org.in.com.config.GatewayConfig;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.OrderDTO;
import org.in.com.dto.RefundDTO;
import org.in.com.dto.TransactionEnquiryDTO;
import org.in.com.dto.enumeration.PaymentGatewayStatusEM;
import org.in.com.dto.enumeration.PaymentOrderEM;
import org.in.com.exception.ErrorCode;
import org.in.com.utils.HttpServiceClient;
import org.in.com.utils.StringUtil;
import org.in.com.utils.TokenGenerator;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ccavenue.security.AesCryptUtil;

public class CCAvenueV2ServiceImpl implements PGInterface {
	private static String CCAVENUE_ORDER_STATUS_API = "https://api.ccavenue.com/apis/servlet/DoWebTrans";
	protected static ModelMapper modelMapper = new ModelMapper();

	private static final Logger LOGGER = LoggerFactory.getLogger("org.in.com.controller.pgtrace");

	private static final String[] SUCCESS_CODE = { "Success", "Successful", "Shipped" };
	private static final String[] FAILURE_CODE = { "Cancelled", "Unsuccessful", "Aborted", "Awaited" };

	private static final String REFUND_SUCCESS_CODE = "0";

	@Override
	public void packPaymentRequest(AuthDTO authDTO, OrderDTO order) {
		Map<String, String> fields = new HashMap<String, String>();
		try {

			StringBuilder ccaRequest = new StringBuilder();
			ccaRequest.append("merchant_id=");
			ccaRequest.append(order.getGatewayCredentials().getAccessCode());
			ccaRequest.append("&order_id=");
			ccaRequest.append(order.getTransactionCode());
			ccaRequest.append("&amount=");
			ccaRequest.append(String.valueOf(order.getAmount()));
			ccaRequest.append("&currency=INR");
			ccaRequest.append("&language=EN");
			ccaRequest.append("&redirect_url=");
			ccaRequest.append(order.getReturnUrl());
			ccaRequest.append("&cancel_url=");
			ccaRequest.append(order.getReturnUrl());
			ccaRequest.append("&billing_name=");
			ccaRequest.append(StringUtil.removeSymbolWithSpace(order.getFirstName()));
			ccaRequest.append("&billing_address=");
			ccaRequest.append(StringUtil.removeSymbolWithSpace(order.getAddress1()));
			ccaRequest.append("&billing_city=");
			ccaRequest.append(order.getCity());
			ccaRequest.append("&billing_state=");
			ccaRequest.append(order.getState());
			ccaRequest.append("&billing_zip=");
			ccaRequest.append("600107");
			ccaRequest.append("&billing_country=India");
			ccaRequest.append("&billing_tel=");
			ccaRequest.append(order.getMobile());
			ccaRequest.append("&billing_email=");
			ccaRequest.append(order.getEmail());
			ccaRequest.append("&merchant_param1=");
			ccaRequest.append(StringUtil.removeSymbolWithSpace(order.getUdf1()));
			ccaRequest.append("&merchant_param2=");
			ccaRequest.append(StringUtil.removeSymbolWithSpace(order.getUdf2()));
			ccaRequest.append("&merchant_param3=");
			ccaRequest.append(StringUtil.removeSymbolWithSpace(order.getUdf3()));
			ccaRequest.append("&merchant_param4=");
			ccaRequest.append(StringUtil.removeSymbolWithSpace(order.getUdf4()));
			AesCryptUtil aesUtil = new AesCryptUtil(order.getGatewayCredentials().getAttr1());
			String encRequest = aesUtil.encrypt(ccaRequest.toString());
			Map<String, String> gatewayInputData = new HashMap<String, String>();
			gatewayInputData.put("encRequest", encRequest);
			gatewayInputData.put("access_code", order.getGatewayCredentials().getAccessKey());
			gatewayInputData.put("request_type", "STRING");
			gatewayInputData.put("Command", "confirmOrder");
			gatewayInputData.put("reference_no", order.getTransactionCode());
			gatewayInputData.put("amount", String.valueOf(order.getAmount()));
			order.setGatewayFormParam(gatewayInputData);
			String logData = gatewayInputData.toString();
			fields.put("CCAV_Request sent = ", ccaRequest.toString());
			fields.put("CCAV_Request sent = ", logData);
			order.setResponseRecevied(fields.toString());
			order.setGatewayUrl(GatewayConfig.CCAVEUNE_V2_URL);
			LOGGER.info(order.getTransactionCode() + "- CCA Gateway Request parameters--" + ccaRequest.toString() + " - " + gatewayInputData);
		}
		catch (Exception e) {
			e.printStackTrace();
			order.setResponseRecevied(fields.toString() + "ERROR -" + e.getMessage());

		}
	}

	@Override
	public void internalVerfication(AuthDTO authDTO, TransactionEnquiryDTO enquiryStatus) {
	}

	@Override
	public void transactionVerify(AuthDTO authDTO, TransactionEnquiryDTO enquiryStatus) {
		Map<String, String> avenueResponse = enquiryStatus.getGatewayReponse();
		String amount = null;
		String gatewayTransactionId = null;
		String orderId = null;
		String status = null;
		StringBuilder response = new StringBuilder();
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

			String encResp = avenueResponse.get("encResp");
			AesCryptUtil aesUtil = new AesCryptUtil(enquiryStatus.getGatewayCredentials().getAttr1());
			String decResp = aesUtil.decrypt(encResp);
			StringTokenizer tokenizer = new StringTokenizer(decResp, "&");
			Hashtable<String, String> hs = new Hashtable<String, String>();
			String pair = null, pname = null, pvalue = null;
			while (tokenizer.hasMoreTokens()) {
				pair = (String) tokenizer.nextToken();
				if (pair != null) {
					StringTokenizer strTok = new StringTokenizer(pair, "=");
					pname = "";
					pvalue = "";
					if (strTok.hasMoreTokens()) {
						pname = (String) strTok.nextToken();
						if (strTok.hasMoreTokens())
							pvalue = (String) strTok.nextToken();
						hs.put(pname, pvalue);
					}
				}
			}

			amount = hs.get("amount");
			gatewayTransactionId = hs.get("tracking_id");
			orderId = hs.get("order_id");
			status = hs.get("order_status");
			response.append("Gateway transaction Id : " + gatewayTransactionId + " Status : " + status + " Msg : " + hs.get("failure_message") + " Response code :" + hs.get("response_code"));

			String encrypt_data1 = gatewayTransactionId + "|" + orderId + "|";

			AesCryptUtil aesServerUtil = new AesCryptUtil(ApplicationConfig.getCCASecretKey(enquiryStatus.getGatewayCredentials().getAccessCode()));
			String encRefundRequest = aesServerUtil.encrypt(encrypt_data1);
			StringBuffer data1 = new StringBuffer();
			data1.append("enc_request=" + encRefundRequest + "&access_code=" + ApplicationConfig.getCCAAccessCode(enquiryStatus.getGatewayCredentials().getAccessCode()) + "&reference_no=" + gatewayTransactionId + "&order_no=" + orderId + "&command=orderStatusTracker" + "&response_type=JSON" + "&request_type=STRING" + "&version=1.1");

			HttpServiceClient client = new HttpServiceClient();
			String responseData = client.postSSL(data1.toString(), GatewayConfig.CCAVEUNE_V2_SERVER_URL);
			StringTokenizer tokenizer1 = new StringTokenizer(responseData, "&");
			Hashtable<String, String> hs1 = new Hashtable<String, String>();
			while (tokenizer1.hasMoreTokens()) {
				String pair1 = (String) tokenizer1.nextToken();
				if (pair1 != null) {
					StringTokenizer strTok = new StringTokenizer(pair1, "=");
					if (strTok.hasMoreTokens()) {
						String pname1 = (String) strTok.nextToken();
						String pvalue1 = (String) strTok.nextToken();
						hs1.put(pname1, pvalue1);
					}
				}
			}
			String encResp1 = hs1.get("enc_response");
			String decResp1 = aesServerUtil.decrypt(encResp1);

			JSONObject responseJSON = JSONObject.fromObject(decResp1);
			status = responseJSON.getString("order_status");

			enquiryStatus.setResponseRecevied(responseJSON.toString());

			LOGGER.info(orderId + "- CCA V2 Gateway Response --" + status);
			if (StringUtil.isNotNull(status) && ArrayUtils.contains(SUCCESS_CODE, status)) {
				enquiryStatus.setStatus(ErrorCode.SUCCESS);
			}
			else if (ArrayUtils.contains(FAILURE_CODE, status)) {
				enquiryStatus.setStatus(ErrorCode.PAYMENT_DECLINED);
			}
			else {
				System.out.println("New CCA Status: " + status);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			enquiryStatus.setResponseRecevied(e.getMessage());
			enquiryStatus.setStatus(ErrorCode.PAYMENT_DECLINED);
		}
		finally {
			enquiryStatus.setAmount(new BigDecimal(amount));
			enquiryStatus.setGatewayTransactionCode(gatewayTransactionId);
		}

	}

	public Map<String, String> getOrderStatus(AuthDTO authDTO, OrderDTO order) {
		Map<String, String> response = new HashMap<String, String>();
		try {
			String encrypt_data = "|" + order.getOrderCode() + "|";

			AesCryptUtil aesUtil = new AesCryptUtil(ApplicationConfig.getCCASecretKey(order.getGatewayCredentials().getAccessCode()));
			String encRefundRequest = aesUtil.encrypt(encrypt_data);

			StringBuffer data = new StringBuffer();
			data.append("enc_request=" + encRefundRequest + "&access_code=" + ApplicationConfig.getCCAAccessCode(order.getGatewayCredentials().getAccessCode()) + "&command=" + "orderStatusTracker" + "&request_type=STRING" + "&response_type=JSON" + "&version=1.1");

			HttpServiceClient client = new HttpServiceClient();
			String responseData = client.postSSL(data.toString(), CCAVENUE_ORDER_STATUS_API);
			StringTokenizer tokenizer = new StringTokenizer(responseData, "&");
			Hashtable<String, String> hsTable = new Hashtable<String, String>();
			while (tokenizer.hasMoreTokens()) {
				String pair = (String) tokenizer.nextToken();
				if (pair != null) {
					StringTokenizer strTok = new StringTokenizer(pair, "=");
					if (strTok.hasMoreTokens()) {
						String pname = (String) strTok.nextToken();
						String pvalue = (String) strTok.nextToken();
						hsTable.put(pname, pvalue);
					}
				}
			}

			String encResp = hsTable.get("enc_response");
			String decResp = aesUtil.decrypt(encResp);

			JSONObject responseJSON = JSONObject.fromObject(decResp);
			if (responseJSON != null) {
				response.put("status", responseJSON.has("status") ? responseJSON.getString("status") : Text.NA);
				response.put("referenceNumber", responseJSON.has("responseJSON") ? responseJSON.getString("reference_no") : Text.NA);
				response.put("orderNumber", responseJSON.has("order_no") ? responseJSON.getString("order_no") : Text.NA);
				response.put("orderAmount", responseJSON.has("order_amt") ? responseJSON.getString("order_amt") : Text.NA);
				response.put("dateTime", responseJSON.has("order_date_time") ? responseJSON.getString("order_date_time") : Text.NA);
				response.put("billName", responseJSON.has("order_bill_name") ? responseJSON.getString("order_bill_name") : Text.NA);
				response.put("billEmail", responseJSON.has("order_bill_email") ? responseJSON.getString("order_bill_email") : Text.NA);
				response.put("orderCapturedAmount", responseJSON.has("order_capt_amt") ? responseJSON.getString("order_capt_amt") : Text.NA);
				response.put("mobileNumber", responseJSON.has("order_bill_tel") ? responseJSON.getString("order_bill_tel") : Text.NA);
				response.put("orderStatus", responseJSON.has("order_status") ? responseJSON.getString("order_status") : Text.NA);
				response.put("statusDatetime", responseJSON.has("order_status_date_time") ? responseJSON.getString("order_status_date_time") : Text.NA);
				response.put("cardName", responseJSON.has("order_card_name") ? responseJSON.getString("order_card_name") : Text.NA);
				response.put("orderFeePercentage", responseJSON.has("order_fee_perc") ? responseJSON.getString("order_fee_perc") : Text.NA);
				response.put("orderFeeValue", responseJSON.has("order_fee_perc_value") ? responseJSON.getString("order_fee_perc_value") : Text.NA);
				response.put("orderFeeFlat", responseJSON.has("order_fee_flat") ? responseJSON.getString("order_fee_flat") : Text.NA);
				response.put("grossAmount", responseJSON.has("order_gross_amt") ? responseJSON.getString("order_gross_amt") : Text.NA);
				response.put("discount", responseJSON.has("order_discount") ? responseJSON.getString("order_discount") : Text.NA);
				response.put("tax", responseJSON.has("order_tax") ? responseJSON.getString("order_tax") : Text.NA);
				response.put("bankReferenceNumber", responseJSON.has("order_bank_ref_no") ? responseJSON.getString("order_bank_ref_no") : Text.NA);
				response.put("bankResponse", responseJSON.has("order_bank_response") ? responseJSON.getString("order_bank_response") : Text.NA);
				response.put("paymentType", responseJSON.has("order_option_type") ? responseJSON.getString("order_option_type") : Text.NA);
				response.put("tds", responseJSON.has("order_TDS") ? responseJSON.getString("order_TDS") : Text.NA);
				response.put("deviceType", responseJSON.has("order_device_type") ? responseJSON.getString("order_device_type") : Text.NA);

				if (ArrayUtils.contains(SUCCESS_CODE, response.get("orderStatus"))) {
					response.put("paymentOrderStatus", PaymentOrderEM.SUCCESS.getCode());
				}
				else if (ArrayUtils.contains(FAILURE_CODE, response.get("orderStatus"))) {
					response.put("paymentOrderStatus", PaymentOrderEM.PAYMENT_DECLINED.getCode());
				}
				else {
					System.out.println("New CCA Status: " + response.get("orderStatus"));
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			response.put("Gateway Provider", "CCAvenue V2");
		}
		return response;
	}

	public Map<String, String> verifyPaymentOrder(AuthDTO authDTO, OrderDTO orderDTO) {
		Map<String, String> response = new HashMap<String, String>();
		try {
			String encrypt_data = "|" + orderDTO.getOrderCode() + "|";

			AesCryptUtil aesUtil = new AesCryptUtil(ApplicationConfig.getCCASecretKey(orderDTO.getGatewayCredentials().getAccessCode()));
			String encRefundRequest = aesUtil.encrypt(encrypt_data);

			StringBuffer data = new StringBuffer();
			data.append("enc_request=" + encRefundRequest + "&access_code=" + ApplicationConfig.getCCAAccessCode(orderDTO.getGatewayCredentials().getAccessCode()) + "&command=" + "orderStatusTracker" + "&request_type=STRING" + "&response_type=JSON" + "&version=1.1");

			HttpServiceClient client = new HttpServiceClient();
			String responseData = client.postSSL(data.toString(), CCAVENUE_ORDER_STATUS_API);

			StringTokenizer tokenizer = new StringTokenizer(responseData, "&");
			Hashtable<String, String> hsTable = new Hashtable<String, String>();
			while (tokenizer.hasMoreTokens()) {
				String pair = (String) tokenizer.nextToken();
				if (pair != null) {
					StringTokenizer strTok = new StringTokenizer(pair, "=");
					if (strTok.hasMoreTokens()) {
						String pname = (String) strTok.nextToken();
						String pvalue = (String) strTok.nextToken();
						hsTable.put(pname, pvalue);
					}
				}
			}

			String encResp = hsTable.get("enc_response");
			AesCryptUtil decryptUtil = new AesCryptUtil(ApplicationConfig.getCCASecretKey(orderDTO.getGatewayCredentials().getAccessCode()));
			String decResp = decryptUtil.decrypt(encResp);

			JSONObject responseJSON = JSONObject.fromObject(decResp);
			if (responseJSON != null) {
				response.put("status", responseJSON.has("status") ? responseJSON.getString("status") : Text.NA);
				response.put("referenceNumber", responseJSON.has("responseJSON") ? responseJSON.getString("reference_no") : Text.NA);
				response.put("orderNumber", responseJSON.has("order_no") ? responseJSON.getString("order_no") : Text.NA);
				response.put("orderAmount", responseJSON.has("order_amt") ? responseJSON.getString("order_amt") : Text.NA);
				response.put("dateTime", responseJSON.has("order_date_time") ? responseJSON.getString("order_date_time") : Text.NA);
				response.put("billName", responseJSON.has("order_bill_name") ? responseJSON.getString("order_bill_name") : Text.NA);
				response.put("billEmail", responseJSON.has("order_bill_email") ? responseJSON.getString("order_bill_email") : Text.NA);
				response.put("orderCapturedAmount", responseJSON.has("order_capt_amt") ? responseJSON.getString("order_capt_amt") : Text.NA);
				response.put("mobileNumber", responseJSON.has("order_bill_tel") ? responseJSON.getString("order_bill_tel") : Text.NA);
				response.put("orderStatus", responseJSON.has("order_status") ? responseJSON.getString("order_status") : Text.NA);
				response.put("statusDatetime", responseJSON.has("order_status_date_time") ? responseJSON.getString("order_status_date_time") : Text.NA);
				response.put("cardName", responseJSON.has("order_card_name") ? responseJSON.getString("order_card_name") : Text.NA);
				response.put("orderFeePercentage", responseJSON.has("order_fee_perc") ? responseJSON.getString("order_fee_perc") : Text.NA);
				response.put("orderFeeValue", responseJSON.has("order_fee_perc_value") ? responseJSON.getString("order_fee_perc_value") : Text.NA);
				response.put("orderFeeFlat", responseJSON.has("order_fee_flat") ? responseJSON.getString("order_fee_flat") : Text.NA);
				response.put("grossAmount", responseJSON.has("order_gross_amt") ? responseJSON.getString("order_gross_amt") : Text.NA);
				response.put("discount", responseJSON.has("order_discount") ? responseJSON.getString("order_discount") : Text.NA);
				response.put("tax", responseJSON.has("order_tax") ? responseJSON.getString("order_tax") : Text.NA);
				response.put("bankReferenceNumber", responseJSON.has("order_bank_ref_no") ? responseJSON.getString("order_bank_ref_no") : Text.NA);
				response.put("bankResponse", responseJSON.has("order_bank_response") ? responseJSON.getString("order_bank_response") : Text.NA);
				response.put("paymentType", responseJSON.has("order_option_type") ? responseJSON.getString("order_option_type") : Text.NA);
				response.put("tds", responseJSON.has("order_TDS") ? responseJSON.getString("order_TDS") : Text.NA);
				response.put("deviceType", responseJSON.has("order_device_type") ? responseJSON.getString("order_device_type") : Text.NA);

				if (ArrayUtils.contains(SUCCESS_CODE, response.get("orderStatus"))) {
					response.put("paymentOrderStatus", PaymentOrderEM.SUCCESS.getCode());
				}
				else if (ArrayUtils.contains(FAILURE_CODE, response.get("orderStatus"))) {
					response.put("paymentOrderStatus", PaymentOrderEM.PAYMENT_DECLINED.getCode());
				}
				else {
					response.put("paymentOrderStatus (New Status Code)", response.get("orderStatus"));
					System.out.println("New CCA Status: " + response.get("orderStatus"));
				}
			}
		}
		catch (IllegalArgumentException e) {

		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			response.put("Gateway Provider", "CCAvenue V2");
		}
		return response;
	}

	@Override
	public void refund(AuthDTO authDTO, RefundDTO refund) throws Exception {
		try {
			String uniqueToken = TokenGenerator.generateToken(5);
			String encrypt_data1 = refund.getGatewayTransactionCode() + "|" + refund.getAmount().setScale(2, RoundingMode.CEILING) + "|" + uniqueToken + "|";

			LOGGER.info(refund.getOrderCode() + " - " + refund.getTransactionCode() + "- CCA Gateway Refund Request --" + refund.getGatewayTransactionCode() + " - " + encrypt_data1);

			AesCryptUtil aesUtil = new AesCryptUtil(ApplicationConfig.getCCASecretKey(refund.getGatewayCredentials().getAccessCode()));
			String encRefundRequest = aesUtil.encrypt(encrypt_data1);
			StringBuffer data = new StringBuffer();
			data.append("enc_request=" + encRefundRequest + "&access_code=" + ApplicationConfig.getCCAAccessCode(refund.getGatewayCredentials().getAccessCode()) + "&reference_no=" + refund.getGatewayTransactionCode() + "&refund_amount=" + refund.getAmount() + "&refund_ref_no=" + uniqueToken + "&command=refundOrder" + "&response_type=JSON" + "&request_type=STRING" + "&version=1.1");

			LOGGER.info("CCA Gateway Refund URL --" + GatewayConfig.CCAVEUNE_V2_SERVER_URL);
			LOGGER.info(refund.getOrderCode() + " - " + refund.getTransactionCode() + "- CCA Gateway Refund Request Parameters --" + data.toString());

			HttpServiceClient client = new HttpServiceClient();
			String responseData = client.postSSL(data.toString(), GatewayConfig.CCAVEUNE_V2_SERVER_URL);

			StringTokenizer tokenizer = new StringTokenizer(responseData, "&");
			Hashtable<String, String> hs = new Hashtable<String, String>();
			while (tokenizer.hasMoreTokens()) {
				String pair = (String) tokenizer.nextToken();
				if (pair != null) {
					StringTokenizer strTok = new StringTokenizer(pair, "=");
					if (strTok.hasMoreTokens()) {
						String pname = (String) strTok.nextToken();
						String pvalue = (String) strTok.nextToken();
						hs.put(pname, pvalue);
					}
				}
			}
			LOGGER.info(refund.getOrderCode() + " - " + "CCA Gateway Refund Response Raw --" + responseData);

			String encResp = hs.get("enc_response");
			AesCryptUtil decryptUtil = new AesCryptUtil(ApplicationConfig.getCCASecretKey(refund.getGatewayCredentials().getAccessCode()));
			String decResp = decryptUtil.decrypt(encResp);
			LOGGER.info(refund.getOrderCode() + " - " + "CCA Gateway Refund Response --" + decResp);

			JSONObject responseJSON = JSONObject.fromObject(decResp);

			String refundStatusCode = responseJSON.getString("refund_status");

			if (StringUtil.isNotNull(refundStatusCode) && REFUND_SUCCESS_CODE.equals(refundStatusCode)) {
				refund.setStatus(PaymentGatewayStatusEM.SUCCESS);
			}
			else {
				refund.setStatus(PaymentGatewayStatusEM.FAILURE);
			}

			refund.setResponseRecevied(" Request: " + data.toString() + " Response: " + responseJSON.toString());
		}
		catch (Exception e) {
			e.printStackTrace();
			refund.setResponseRecevied(e.getMessage());
		}
	}

	public static void main(String a[]) {
		AesCryptUtil aesUtil = new AesCryptUtil("2BE5FA81C41A62B9AC39B11453CBFCB3");
		String decResp = aesUtil.decrypt("93ba465c16404021fa0ee35466aadff00819cf29e4820941fd672c349f9a733f60224e43f4a5cff68e6adf5d8c99c128ff1e7472aacafc264db8f992abceee665a6d853e1a6909d5bf9c0ce13da6a973d6c77ff919da8dc3c74e81d36310b9446e1d6a2d5ea0f60357fabd828c0182683c1817d882876648c39cc4a7ac82a55af7eade3902f619071c7a9793724ec76a300a3af7f579e75b4165a6bf2311fd5544f9a0c06691390ad904e27172f1e94f5068f6c2abe303819f4f41e3c1d674419ae3e65b49070890e770d55d3ac83d4015264268e568d4fa811eee9c504a2f0fc1acdb501182b734b75821138e0d59d89ed49e821bc394ee23937d7632e0d8cd10807db83099bb4b6612054cc488ea5bcf835044bc222f2922323a46638a0bab8069e4f2f4b814627372dcb5290b67b6da6840554e599169f007a15766eb0edbb7c6e4b3a250dbc6bd0ca4f33a87c3018a5063b90c1a46093b486890c17c61f69edd4e3609d515074fa44ebb8e0609d31a6014cd5c348e0cfd401f2fa35d3d67ce8ae1336b9a0ccd79142268c4996cefcf05235eb42381e4b6c1d882b423e38181473d483780f3579eb6299f16e05f961837269d64507e6e3c87869d7ffad343291c211c6f765c0d55c4ea24c4fe129535c2786d5700065db3ffcb8f5cc122f2e2792e0a9b98691836b9d2f0c13a689391ce6a26041835ae148235e75ad26a60fec1281815084de69fc743dec9f3bcdf342551524e794f5a47e81aeab15922cab8bf7cc75182e35911eb3b4543b64dd79c84649762e1b2d7ae2ceb64c2bfed85924df9afbb30c25d00f54e816006e4fe45ab51ce1b1b3845334c90bc91d28574");
		System.out.println(decResp);
	}
}
