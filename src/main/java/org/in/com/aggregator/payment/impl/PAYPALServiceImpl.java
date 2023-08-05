package org.in.com.aggregator.payment.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.in.com.aggregator.payment.PGInterface;
import org.in.com.cache.PaymentCache;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.OrderDTO;
import org.in.com.dto.PaymentGatewayCredentialsDTO;
import org.in.com.dto.RefundDTO;
import org.in.com.dto.TransactionEnquiryDTO;
import org.in.com.dto.enumeration.OrderTypeEM;
import org.in.com.dto.enumeration.PaymentGatewayStatusEM;
import org.in.com.dto.enumeration.PaymentGatewayTransactionTypeEM;
import org.in.com.dto.enumeration.PaymentOrderEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.PaymentResponseException;
import org.in.com.exception.ServiceException;
import org.in.com.utils.DateUtil;
import org.in.com.utils.JsonArrayBuilder;
import org.in.com.utils.JsonBuilder;
import org.in.com.utils.StringUtil;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.client.urlconnection.HTTPSProperties;
import com.sun.jersey.core.util.MultivaluedMapImpl;

@Service
public class PAYPALServiceImpl implements PGInterface {
	private static String PAYPAL_URL = "https://api.paypal.com";
	private static String OAUTH_TOKEN = "/v1/oauth2/token";
	private static String CREATE_ORDER = "/v2/checkout/orders";
	private static String ORDER_STATUS = "/v2/checkout/orders";
	private static String REFUND_ORDER = "/v2/payments/captures";
	private static String PAYPAL_SET_URL = "/v1/risk/transaction-contexts";
	private static String STATUS_SUCESS = "COMPLETED";
	private static String TRANSACTION_ID_SEPARATOR = "__";
	private static final Logger log = LoggerFactory.getLogger("org.in.com.controller.pgtrace");

	public void packPaymentRequest(AuthDTO authDTO, OrderDTO order) throws Exception {
		String authorization = null;
		TreeMap<String, String> paytmReqParam = new TreeMap<String, String>();
		try {
			authorization = getAuthorizationToken(authDTO, order.getGatewayCredentials());

			JsonBuilder transactionJson = new JsonBuilder().add("application_context", 
					new JsonBuilder().add("user_action", "PAY_NOW")
					.add("return_url", order.getReturnUrl())
					.add("cancel_url", order.getReturnUrl())
					.add("brand_name", authDTO.getNamespace().getName())
					.add("shipping_preference", "NO_SHIPPING")
					.add("locale", "en-IN")
					.add("payment_method", new JsonBuilder()
						.add("payer_selected", "PAYPAL")
						.add("payee_preferred", "IMMEDIATE_PAYMENT_REQUIRED")))
					.add("intent", "CAPTURE")
					.add("payer", new JsonBuilder()
							.add("email_address", order.getEmail())
							.add("name", new JsonBuilder()
									.add("given_name", order.getLastName())
									.add("surname", order.getFirstName()))
							.add("phone", new JsonBuilder()
									.add("phone_number", new JsonBuilder()
											.add("national_number", order.getMobile()))))
					.add("purchase_units", new JsonArrayBuilder()
							.add(new JsonBuilder()
									.add("invoice_id", order.getTransactionCode())
									.add("amount", new JsonBuilder()
											.add("currency_code", "INR")
											.add("value", order.getAmount())
											.add("breakdown", new JsonBuilder()
													.add("item_total", new JsonBuilder()
															.add("currency_code", "INR")
															.add("value", order.getAmount()))))
									.add("custom_id", order.getUdf2())
									.add("soft_descriptor", order.getUdf3())
									.add("items", new JsonArrayBuilder()
											.add(new JsonBuilder()
													.add("name", order.getUdf1())
													.add("unit_amount", new JsonBuilder()
															.add("currency_code", "INR")
															.add("value", order.getAmount()))
													.add("quantity", "1")
													.add("description", order.getUdf1())
													.add("sku", order.getOrderCode())
													.add("category", "DIGITAL_GOODS").toJson()).toJson())
									.toJson()).toJson());

			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Authorization", "Bearer " + authorization);
			headers.put("Prefer", "return=representation");
			headers.put("PayPal-Request-Id", order.getOrderCode());
			headers.put("PayPal-Client-Metadata-Id", order.getOrderCode());

			JSONObject responseData = httpPOST(PAYPAL_URL + CREATE_ORDER, headers, transactionJson.toJson());

			order.setResponseRecevied(responseData.toString());
			if (StringUtil.isNotNull(responseData) && responseData.get("status").equals("CREATED")) {
				String gatewayTransactionCode = responseData.has("id") ? responseData.getString("id") : null;
				JSONArray linksArray = responseData.getJSONArray("links");
				order.setResponseRecevied(responseData.toString());
				for (Object object : linksArray) {
					JSONObject js = JSONObject.fromObject(object);
					if (js.get("rel").equals("approve")) {
						order.setGatewayUrl(js.getString("href"));
						order.setGatewayTransactionCode(gatewayTransactionCode);
						order.setGatewayFormParam(paytmReqParam);
						break;
					}
				}
			}
			else if (responseData.has("invalid_token")) {
				throw new Exception();
			}
		}
		catch (Exception e) {
			log.error("transactionEnquiry : " + authDTO.getNamespaceCode() + " " + order.getOrderCode() + " " + order.getGatewayTransactionCode() + " " + paytmReqParam);
			throw e;
		}
		finally {
			updateSTC(authDTO, order, authorization);
		}
	}

	@Override
	public void transactionVerify(AuthDTO authDTO, TransactionEnquiryDTO enquiryStatus) throws PaymentResponseException, Exception {
		try {
			String OrderId = enquiryStatus.getGatewayTransactionCode();
			Map<String, String> headers = new HashMap<String, String>();
			String authorization = getAuthorizationToken(authDTO, enquiryStatus.getGatewayCredentials());
			headers.put("Authorization", "Bearer " + authorization);
			String URL = PAYPAL_URL + CREATE_ORDER + "/" + OrderId + "/capture";

			JSONObject responseData = httpPOST(URL, headers, new JsonObject());
			enquiryStatus.setResponseRecevied(responseData.toString());
			if (responseData.has("status") && responseData.getString("status").equals(STATUS_SUCESS)) {
				JSONArray purchaseUnits = responseData.getJSONArray("purchase_units");
				for (int pu = 0; pu < purchaseUnits.size(); pu++) {
					JSONObject purchaseUnit = purchaseUnits.getJSONObject(pu);
					if (purchaseUnit.has("payments")) {
						JSONArray payments = purchaseUnit.getJSONObject("payments").getJSONArray("captures");
						for (int p = 0; p < payments.size(); p++) {
							JSONObject payment = payments.getJSONObject(p);
							if (payment.getString("status").equals(STATUS_SUCESS)) {
								StringBuilder transactionCode = new StringBuilder();
								transactionCode.append(enquiryStatus.getGatewayTransactionCode().split(TRANSACTION_ID_SEPARATOR)[0]);
								transactionCode.append(TRANSACTION_ID_SEPARATOR);
								transactionCode.append(payment.getString("id"));
								enquiryStatus.setStatus(ErrorCode.SUCCESS);
								enquiryStatus.setGatewayTransactionCode(transactionCode.toString());
								enquiryStatus.setAmount(new BigDecimal(payment.getJSONObject("amount").getString("value")));
							}
						}
					}
				}
			}
			else {
				enquiryStatus.setStatus(ErrorCode.PAYMENT_DECLINED);
				JSONArray purchaseUnits = responseData.getJSONArray("purchase_units");
				JSONObject purchaseUnit = purchaseUnits.getJSONObject(Numeric.ZERO_INT);
				enquiryStatus.setAmount(new BigDecimal(purchaseUnit.getJSONObject("amount").getString("value")));
			}
			log.info("transactionEnquiry : " + authDTO.getNamespaceCode() + " " + enquiryStatus.getOrderCode() + " " + enquiryStatus.getGatewayTransactionCode() + " " + responseData);
		}
		catch (Exception e) {
			log.error("transactionEnquiry : " + authDTO.getNamespaceCode() + "  " + enquiryStatus.getOrderCode() + "  " + enquiryStatus.getGatewayTransactionCode());
			e.printStackTrace();
		}
		finally {
			if (enquiryStatus.getStatus() == null) {
				enquiryStatus.setStatus(ErrorCode.PAYMENT_DECLINED);
			}
		}

	}

	@Override
	public void refund(AuthDTO authDTO, RefundDTO refund) {
		try {
			String paymentCaptureTransactionId = Text.NA;

			if (refund.getGatewayTransactionCode().split(TRANSACTION_ID_SEPARATOR).length == 2) {
				paymentCaptureTransactionId = refund.getGatewayTransactionCode().split(TRANSACTION_ID_SEPARATOR)[1];
			}
			else if (refund.getGatewayTransactionCode().split(TRANSACTION_ID_SEPARATOR).length != 2) {
				OrderDTO orderDTO = new OrderDTO();
				orderDTO.setGatewayCredentials(refund.getGatewayCredentials());
				orderDTO.setOrderCode(refund.getOrderCode());
				orderDTO.setGatewayTransactionCode(refund.getGatewayTransactionCode());
				Map<String, String> captureDetails = verifyPaymentOrder(authDTO, orderDTO);
				paymentCaptureTransactionId = captureDetails.get("paymentCaptureId");
			}
			if (StringUtil.isNull(paymentCaptureTransactionId)) {
				throw new ServiceException(ErrorCode.INVALID_TRANSACTION_ID, "Payment Capture Gateway Id not found");
			}
			JsonBuilder transactionJson = new JsonBuilder().add("amount", new JsonBuilder().add("value", refund.getAmount().setScale(2, RoundingMode.HALF_UP)).add("currency_code", "INR")).add("invoice_id", refund.getOrderCode()).add("note_to_payer", "Bus Ticket has been Cancel");

			Map<String, String> headers = new HashMap<String, String>();
			String authorization = getAuthorizationToken(authDTO, refund.getGatewayCredentials());

			headers.put("Authorization", "Bearer " + authorization);
			headers.put("Prefer", "return=representation");
			String URL = PAYPAL_URL + REFUND_ORDER + "/" + paymentCaptureTransactionId + "/refund";
			JSONObject responseData = httpPOST(URL, headers, transactionJson.toJson());

			if (responseData != null && responseData.has("status") && responseData.getString("status").equals(STATUS_SUCESS)) {
				refund.setStatus(PaymentGatewayStatusEM.SUCCESS);
				refund.setGatewayTransactionCode(responseData.getString("id"));
			}
			else {
				refund.setStatus(PaymentGatewayStatusEM.FAILURE);
			}
			log.info("refund : " + authDTO.getNamespaceCode() + " " + URL + transactionJson + " " + responseData);

		}
		catch (Exception e) {
			log.error("refund : " + authDTO.getNamespaceCode() + " " + refund.getOrderCode() + " " + refund.getGatewayTransactionCode());
			e.printStackTrace();
		}
	}

	@Override
	public Map<String, String> verifyPaymentOrder(AuthDTO authDTO, OrderDTO orderDTO) {
		Map<String, String> responseMap = new HashMap<String, String>();
		JSONObject responseData = null;
		responseMap.put("OrderCode", orderDTO.getOrderCode());
		responseMap.put("gatewayOrderCode", orderDTO.getGatewayTransactionCode());
		try {
			String OrderId = orderDTO.getGatewayTransactionCode().split(TRANSACTION_ID_SEPARATOR)[0];
			Map<String, String> headers = new HashMap<String, String>();
			String authorization = getAuthorizationToken(authDTO, orderDTO.getGatewayCredentials());
			headers.put("Authorization", "Bearer " + authorization);
			String URL = PAYPAL_URL + ORDER_STATUS + "/" + OrderId;
			responseData = httpGET(URL, headers);
			if (responseData != null) {
				responseMap.put("refferenceId", responseData.getString("id"));
				responseMap.put("status", responseData.has("status") ? responseData.getString("status") : "");
				if (responseData.has("purchase_units")) {
					JSONArray purchaseUnits = responseData.getJSONArray("purchase_units");
					for (int pu = 0; pu < purchaseUnits.size(); pu++) {
						JSONObject purchaseUnit = purchaseUnits.getJSONObject(pu);
						if (purchaseUnit.has("payments")) {
							JSONObject payments = purchaseUnit.getJSONObject("payments");
							if (payments.has("captures")) {
								JSONArray paymentsCaptures = payments.getJSONArray("captures");
								for (int p = 0; p < paymentsCaptures.size(); p++) {
									JSONObject captures = paymentsCaptures.getJSONObject(p);
									responseMap.put(p + "status", captures.has("status") ? captures.getString("status") : "");
									responseMap.put("paymentCaptureId", captures.has("id") ? captures.getString("id") : "");
									responseMap.put(p + "orderNumber", purchaseUnit.has("reference_id") ? purchaseUnit.getString("reference_id") : "");
									responseMap.put(p + "orderAmount", captures.has("amount") ? captures.getJSONObject("amount").getString("value") : "");
									responseMap.put(p + "updatedTime2", captures.has("update_time") ? captures.getString("update_time") : "");
									responseMap.put(p + "createTime2", captures.has("create_time") ? captures.getString("create_time") : "");
									responseMap.put(p + "name", captures.has("firstname") ? captures.getString("firstname") : "");
									responseMap.put(p + "finalCapture", captures.has("final_capture") ? captures.getString("final_capture") : "");

									if (captures != null && captures.has("status") && captures.getString("status").equals(STATUS_SUCESS)) {
										responseMap.put(p + "paymentOrderStatus", PaymentOrderEM.SUCCESS.getCode());
										responseMap.put("paymentOrderStatus", PaymentOrderEM.SUCCESS.getCode());
									}
								}
							}
							if (payments.has("refunds")) {
								JSONArray paymentsRefunds = payments.getJSONArray("refunds");
								for (int r = 0; r < paymentsRefunds.size(); r++) {
									JSONObject refund = paymentsRefunds.getJSONObject(r);
									responseMap.put(r + "refundStatus", refund.has("status") ? refund.getString("status") : "");
									responseMap.put(r + "refundReferenceNumber", refund.has("id") ? refund.getString("id") : "");
									responseMap.put(r + "refundorderNumber", purchaseUnit.has("reference_id") ? purchaseUnit.getString("reference_id") : "");
									responseMap.put(r + "refundAmount", refund.has("amount") ? refund.getJSONObject("amount").getString("value") : "");
									responseMap.put(r + "refundUpdatedTime", refund.has("update_time") ? refund.getString("update_time") : "");
									responseMap.put(r + "refundCreateTime", refund.has("create_time") ? refund.getString("create_time") : "");

									if (refund != null && refund.has("status") && refund.getString("status").equals(STATUS_SUCESS)) {
										responseMap.put(r + "refundOrderStatus", PaymentOrderEM.SUCCESS.getCode());
									}
								}
							}
						}
						responseMap.put("description", purchaseUnit.has("description") ? purchaseUnit.getString("description") : "");
						responseMap.put("softDescriptor", purchaseUnit.has("soft_descriptor") ? purchaseUnit.getString("soft_descriptor") : "");
						responseMap.put("referencePNR", purchaseUnit.has("reference_id") ? purchaseUnit.getString("reference_id") : "");
						responseMap.put("orderValue", purchaseUnit.has("amount") ? purchaseUnit.getJSONObject("amount").getString("value") : "");
					}
				}
				if (responseData.has("payer")) {
					JSONObject payer = responseData.getJSONObject("payer");
					responseMap.put("payerName", payer.getJSONObject("name").getString("given_name") + " " + payer.getJSONObject("name").getString("surname"));
					responseMap.put("emailAddress", payer.getString("email_address"));
					responseMap.put("payerId", payer.has("payer_id") ? payer.getString("payer_id") : "");
					responseMap.put("phoneNumber", payer.has("phone") ? payer.getJSONObject("phone").getJSONObject("phone_number").getString("national_number") : "");
				}
				responseMap.put("createTime", responseData.has("create_time") ? responseData.getString("create_time") : "");
				responseMap.put("updatedTime", responseData.has("update_time") ? responseData.getString("update_time") : "");
			}
			else {
				responseMap.put("paymentOrderStatus", PaymentOrderEM.PAYMENT_DECLINED.getCode());
				responseMap.put("paymentOrderStatus", PaymentOrderEM.PAYMENT_DECLINED.getCode());
			}
		}
		catch (Exception e) {
			responseMap.put("paymentOrderStatus", PaymentOrderEM.PAYMENT_DECLINED.getCode());
			e.printStackTrace();
		}
		finally {
			log.info("verify: " + authDTO.getNamespaceCode() + " " + responseMap.toString() + " - responseData:" + responseData);
		}
		return responseMap;
	}

	@Override
	public void internalVerfication(AuthDTO authDTO, TransactionEnquiryDTO enquiryStatus) {
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

	// Paypal Secure Transaction Customer Call(paypalRiskRequest)
	private void updateSTC(AuthDTO authDTO, OrderDTO order, String authorization) {
		try {
			JsonBuilder transactionJson = new JsonBuilder().add("tracking_id", order.getOrderCode()).add("additional_data", new JsonArrayBuilder().add(new JsonBuilder().add("key", "sender_account_id").add("value", "675350").toJson()).add(new JsonBuilder().add("key", "sender_email").add("value", order.getEmail()).toJson()).add(new JsonBuilder().add("key", "sender_phone").add("value", order.getMobile()).toJson()).add(new JsonBuilder().add("key", "sender_first_name").add("value", order.getFirstName()).toJson()).add(new JsonBuilder().add("key", "sender_last_name").add("value", order.getLastName()).toJson()).add(new JsonBuilder().add("key", "sender_create_date").add("value", DateTime.now().toString(DateUtil.DATE_FORMATE_ZONE)).toJson()).add(new JsonBuilder().add("key", "sender_country_code").add("value", "IN").toJson()).toJson());
			String url = PAYPAL_URL + PAYPAL_SET_URL + "/" + order.getGatewayCredentials().getAttr1() + "/" + order.getOrderCode();
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Authorization", "Bearer " + authorization);
			headers.put("PayPal-Client-Metadata-Id", order.getOrderCode());
			httpPUT(url, headers, transactionJson.toJson());
		}
		catch (Exception e) {
			log.error("STC Error: " + authDTO.getNamespaceCode() + " " + order.getOrderCode());
			e.printStackTrace();
		}
	}

	private void httpPUT(String URL, Map<String, String> headers, JsonObject jsonObject2) throws Exception {
		Client client = Client.create(getTLSConfig(new DefaultClientConfig()));
		// client.addFilter(new LoggingFilter(System.out));

		WebResource webResource = client.resource(URL);
		WebResource.Builder builder = (WebResource.Builder) webResource.accept(new String[] { "application/json" }).type("application/json");
		for (Entry<String, String> param : headers.entrySet()) {
			builder.header(param.getKey(), param.getValue());
		}

		ClientResponse response = builder.put(ClientResponse.class, jsonObject2.toString());
		if (response.getStatus() != 200) {
			log.error("STC Error: " + URL + " " + response.getStatus());
		}
	}

	private String getAuthorizationToken(AuthDTO authDTO, PaymentGatewayCredentialsDTO credentials) throws Exception {
		PaymentCache cache = new PaymentCache();
		String authorization = cache.getPayPalAuthorizationToken(authDTO.getNamespaceCode());
		if (StringUtil.isNull(authorization)) {
			log.info("Generate Authorization Token: " + authDTO.getNamespaceCode());
			Client client = Client.create(getTLSConfig(new DefaultClientConfig()));
			client.addFilter(new HTTPBasicAuthFilter(credentials.getAccessCode(), credentials.getAccessKey()));

			MultivaluedMapImpl multivaluedMapImpl = new MultivaluedMapImpl();
			multivaluedMapImpl.add("grant_type", "client_credentials");
			WebResource webResource = client.resource(PAYPAL_URL + OAUTH_TOKEN).queryParams(multivaluedMapImpl);
			webResource.header("accept", "application/x-www-form-urlencoded");
			webResource.header("content-type", "application/x-www-form-urlencoded");

			ClientResponse response = webResource.post(ClientResponse.class);
			String json = response.getEntity(String.class);
			if (response.getStatus() == 200) {
				JSONObject jsonObject = JSONObject.fromObject(json);
				authorization = jsonObject.getString("access_token");
				cache.putPayPalAuthorizationToken(authDTO.getNamespaceCode(), authorization);
				log.info("Generated Authorization Token: " + authDTO.getNamespaceCode());
			}
			else {
				log.error("getAuthorizationToken Not Found: " + json);
			}
		}
		return authorization;
	}

	private JSONObject httpPOST(String URL, Map<String, String> headers, JsonObject data) throws Exception {
		Client client = Client.create(getTLSConfig(new DefaultClientConfig()));
		// client.addFilter(new LoggingFilter(System.out));

		WebResource webResource = client.resource(URL);
		WebResource.Builder builder = (WebResource.Builder) webResource.accept(new String[] { "application/json" }).type("application/json");
		for (Entry<String, String> param : headers.entrySet()) {
			builder.header(param.getKey(), param.getValue());
		}

		ClientResponse response = builder.post(ClientResponse.class, data.toString());
		String json = (String) response.getEntity(String.class);
		JSONObject jsonObject = null;
		if (StringUtil.isNotNull(json)) {
			jsonObject = JSONObject.fromObject(json);
		}
		return jsonObject;
	}

	private JSONObject httpGET(String URL, Map<String, String> headers) throws Exception {
		Client client = Client.create(getTLSConfig(new DefaultClientConfig()));
		// client.addFilter(new LoggingFilter(System.out));

		WebResource webResource = client.resource(URL);
		WebResource.Builder builder = (WebResource.Builder) webResource.accept(new String[] { "application/json" }).type("application/json");
		for (Entry<String, String> param : headers.entrySet()) {
			builder.header(param.getKey(), param.getValue());
		}

		ClientResponse response = builder.get(ClientResponse.class);
		String json = (String) response.getEntity(String.class);
		JSONObject jsonObject = null;
		if (response.getStatus() == 200 && StringUtil.isNotNull(json)) {
			jsonObject = JSONObject.fromObject(json);
		}
		else {
			System.out.println(json);
		}
		return jsonObject;
	}

	public static void main(String a[]) throws Exception {

		OrderDTO order = new OrderDTO();
		AuthDTO authDTO = new AuthDTO();
		authDTO.setNamespaceCode("JAVA");
		PAYPALServiceImpl impl = new PAYPALServiceImpl();
		PaymentGatewayCredentialsDTO credentialsDTO = new PaymentGatewayCredentialsDTO();
		credentialsDTO.setAccessCode("AWDteluTtvdwumZr6wlMN_MY963AHdMg_f2CsrbTIVuKI09gTZfZdGaW5N8oYBsZ-hEYk-pNqTVFezs5");
		credentialsDTO.setAccessKey("EF2FVituclkqZBNuA7cQv_zdxGzHw4JtLpCuPpxN_SBP6VOLFf7lEql7U_tZzTZAp42reh-VN3LiVJQG");
		credentialsDTO.setAttr1("BRVNUQ25TPMAE");
		order.setGatewayCredentials(credentialsDTO);
		order.setOrderCode("DC00006279");
		order.setTransactionCode("DC00006279");
		order.setAddress1("Chennai");
		order.setAddress2("Chennai");
		order.setZipCode("600026");
		order.setAmount(new BigDecimal(100));
		order.setFirstName("Javakar");
		order.setLastName("R");
		order.setOrderType(OrderTypeEM.TICKET);
		order.setTransactionTypeDTO(PaymentGatewayTransactionTypeEM.PAYMENT);
		order.setMobile("9500006279");
		order.setEmail("ramasamy@ezeeinfosolutions.com");
		order.setUdf1("Testing");
		order.setUdf2("Testing1");
		order.setUdf3("Testing2");
		order.setUdf4("Testing3");
		TransactionEnquiryDTO enquiryStatus = new TransactionEnquiryDTO();
		enquiryStatus.setGatewayCredentials(credentialsDTO);
		enquiryStatus.setGatewayTransactionCode("2NP89393CL2742318");
		RefundDTO refundDTO = new RefundDTO();
		refundDTO.setGatewayCredentials(credentialsDTO);
		refundDTO.setAmount(new BigDecimal(788));
		refundDTO.setGatewayTransactionCode("4X217301SP639764U__5B053758PV268525S");
		order.setGatewayTransactionCode("4X217301SP639764U__5B053758PV268525S");
		impl.refund(authDTO, refundDTO);
	}

}