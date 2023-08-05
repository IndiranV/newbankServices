package org.in.com.dto;

import java.util.List;

import org.in.com.constants.Text;
import org.in.com.utils.StringUtil;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PaymentGatewayPartnerDTO extends BaseDTO<PaymentGatewayProviderDTO> {
	private String apiProviderCode;
	private String offerNotes;
	private List<String> offerTerms;
	private PaymentModeDTO paymentMode;
	private PaymentGatewayProviderDTO gatewayProvider;
	
	public String getOfferTerm() {
		StringBuilder terms = new StringBuilder(); 
		for(String term : offerTerms) {
			if(StringUtil.isNull(term)) {
				continue;
			}
			terms.append(term + Text.VERTICAL_BAR);
		}
		return terms.toString();
	}
}
