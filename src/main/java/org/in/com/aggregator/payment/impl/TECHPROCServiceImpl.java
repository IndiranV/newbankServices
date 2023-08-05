package org.in.com.aggregator.payment.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.in.com.aggregator.payment.PGInterface;
import org.in.com.config.GatewayConfig;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.OrderDTO;
import org.in.com.dto.RefundDTO;
import org.in.com.dto.TransactionEnquiryDTO;
import org.in.com.exception.ErrorCode;

import com.CheckSumRequestBean;
import com.TPSLUtil;

public class TECHPROCServiceImpl implements PGInterface {

	private static final String TECHPROCESS_ACCOUNT_NUM = "1";
	private static String SUCCESS_CODE = "0300";

	public void packPaymentRequest(AuthDTO authDTO, OrderDTO order) throws Exception {
		try {

			CheckSumRequestBean objTranDetails = new CheckSumRequestBean();
			objTranDetails.setStrMerchantTranId(order.getTransactionCode());
			objTranDetails.setStrMarketCode(order.getGatewayCredentials().getAccessCode());
			objTranDetails.setStrAccountNo(TECHPROCESS_ACCOUNT_NUM);
			objTranDetails.setStrAmt(order.getAmount().toBigInteger().toString());
			objTranDetails.setStrBankCode(order.getGatewayPartnerCode());
			objTranDetails.setStrPropertyPath(order.getGatewayCredentials().getPropertiesFileName());

			TPSLUtil util = new TPSLUtil();
			String checkSum = util.transactionRequestMessage(objTranDetails);

			Map<String, String> gatewayFormParam = new HashMap<String, String>();

			gatewayFormParam.put("CRN", "INR");
			gatewayFormParam.put("SRCSITEID", order.getGatewayCredentials().getAccessCode());
			gatewayFormParam.put("PRN", order.getTransactionCode());
			gatewayFormParam.put("msg", checkSum);
			gatewayFormParam.put("AMT", String.valueOf(order.getAmount()));

			order.setGatewayFormParam(gatewayFormParam);
			order.setGatewayUrl(GatewayConfig.TECHPROCESS_REQUEST_URL);
		}
		catch (Exception e) {
			throw e;
		}
	}

	public void internalVerfication(AuthDTO authDTO, TransactionEnquiryDTO enquiryStatus) throws Exception {

	}

	public void transactionVerify(AuthDTO authDTO, TransactionEnquiryDTO enquiry) throws Exception {
		try {
			Map<String, String> parameter = enquiry.getGatewayReponse();
			String msg = parameter.get("msg");
			if (!StringUtils.isBlank(msg) && msg.contains(SUCCESS_CODE)) {
				String checksum = msg.split("\\|")[25];
				com.CheckSumResponseBean objResTranDetails = new com.CheckSumResponseBean();
				objResTranDetails.setStrMSG(msg);
				objResTranDetails.setStrPropertyPath(enquiry.getGatewayCredentials().getPropertiesFileName());
				com.TPSLUtil util = new com.TPSLUtil();
				/**
				 * This Jar Again hit TechProcess To get The Checksum Value
				 * Again
				 * */
				String newCheckSum = util.transactionResponseMessage(objResTranDetails);

				if (newCheckSum.equals(checksum)) {
					enquiry.setStatus(ErrorCode.SUCCESS);
					enquiry.setAmount(new BigDecimal(msg.split("\\|")[4]));
					enquiry.setGatewayTransactionCode(checksum + "-" + newCheckSum);
				}
				else {
					enquiry.setStatus(ErrorCode.PAYMENT_DECLINED);
				}
			}
			else {
				enquiry.setStatus(ErrorCode.PAYMENT_DECLINED);
			}
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			enquiry.setResponseRecevied(enquiry.getGatewayReponse().get("msg"));
		}

	}

	public void refund(AuthDTO authDTO, RefundDTO refund) {
		// TODO need to implement this

	}

	@Override
	public Map<String, String> verifyPaymentOrder(AuthDTO authDTO, OrderDTO orderDTO) {
		// TODO Auto-generated method stub
		return null;
	}

}
