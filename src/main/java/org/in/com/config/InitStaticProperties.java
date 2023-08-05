package org.in.com.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "classpath:gatewayconfig.properties")
public class InitStaticProperties {

	private @Value("${base.key.location}")
	void setBaseKeyLocation(String baseKeyLocation) {
		GatewayConfig.BASE_KEY_LOCATION = baseKeyLocation;
	}

	private @Value("${base.return.url}")
	void setBaseReturnUrl(String baseReturnUrl) {
		GatewayConfig.BASE_RETURN_URL = baseReturnUrl;
	}

	private @Value("${common.return.path}")
	void setCommonReturnUrl(String commonReturnUrl) {
		GatewayConfig.COMMON_RETURN_PATH = commonReturnUrl;
	}

	private @Value("${hdfc.request.url}")
	void setHDFCRequestUrl(String hdfcRequestUrl) {
		GatewayConfig.HDFC_REQUEST_URL = hdfcRequestUrl;
	}

	private @Value("${hdfc.enquiry.url}")
	void setHDFCTransactionUrl(String hdfcTransactionEnquiryUrl) {
		GatewayConfig.HDFC_TRANSACTION_ENQUIRY_URL = hdfcTransactionEnquiryUrl;
	}

	private @Value("${dev.env}")
	void setHDFCPostParam(boolean devEnv) {
		GatewayConfig.DEV_ENIORNMENT = devEnv;
	}

	private @Value("${hdfc.post.payment.handshake.url}")
	void setPostPaymentUrl(String hdfcPostPaymentHandShakeUrl) {
		GatewayConfig.HDFC_POST_PAYMENT_HANDSHAKE_URL = hdfcPostPaymentHandShakeUrl;
	}

	private @Value("${citrus.request.url}")
	void setCitrusRequestUrl(String citrusRequestUrl) {
		GatewayConfig.CITRUS_REQUEST_URL = citrusRequestUrl;
	}

	private @Value("${cca.netbanking.request.url}")
	void setCCANBRequestUrl(String ccavenueNBRequestUrl) {
		GatewayConfig.CCAVEUNE_NETBANKING_URL = ccavenueNBRequestUrl;
	}

	private @Value("${cca.card.request.url}")
	void setCCACardRequestUrl(String ccavenueCardRequestUrl) {
		GatewayConfig.CCAVEUNE_CARD_URL = ccavenueCardRequestUrl;
	}

	private @Value("${techprocess.request.url}")
	void setTechprocessRequestUrl(String techprocessRequestUrl) {
		GatewayConfig.TECHPROCESS_REQUEST_URL = techprocessRequestUrl;
	}

	private @Value("${cca.v2.request.url}")
	void setCCAVEUNERequestUrl(String ccaveuneRequestUrl) {
		GatewayConfig.CCAVEUNE_V2_URL = ccaveuneRequestUrl;
	}

	private @Value("${cca.v2.server.call.url}")
	void setCCAVEUNEServerCallUrl(String ccaveuneServerCallUrl) {
		GatewayConfig.CCAVEUNE_V2_SERVER_URL = ccaveuneServerCallUrl;
	}

}
