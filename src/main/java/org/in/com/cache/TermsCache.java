package org.in.com.cache;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.TermDTO;

public class TermsCache {
	private static String TERMS_KEY = "ALL_TERMS_KEY";

	protected void removeTermDTO(AuthDTO authDTO, TermDTO termDTO) {
		EhcacheManager.getTermEhCache().remove(termDTO.getCode());
		EhcacheManager.getTermEhCache().remove(TERMS_KEY);
	}

}
