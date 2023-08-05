package org.in.com.service.impl;

import org.in.com.cache.EhcacheManager;
import org.in.com.cache.dto.AuthCacheDTO;
import org.in.com.constants.Constants;
import org.in.com.constants.Numeric;
import org.in.com.dao.PrivilegeDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.enumeration.UserRoleEM;
import org.in.com.service.UserLoginHistoryService;
import org.in.com.utils.StringUtil;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import net.sf.ehcache.Element;

@Service
public class UserLoginServiceHistoryImpl implements UserLoginHistoryService {

	@Async
	public void saveLoginEntry(AuthDTO authDTO) {
		int duplicateFlag = Numeric.ZERO_INT;
		if (authDTO.getUser().getUserRole().getId() == UserRoleEM.USER_ROLE.getId()) {
			for (Object cahceKey : EhcacheManager.getAuthTokenEhCache().getKeys()) {
				Element element = EhcacheManager.getAuthTokenEhCache().get(cahceKey);
				if (element == null) {
					continue;
				}
				AuthCacheDTO authCacheDTO = (AuthCacheDTO) element.getObjectValue();
				if (authCacheDTO != null && authCacheDTO.getNativeNamespaceCode().equals(authDTO.getNamespaceCode()) && StringUtil.isNotNull(authCacheDTO.getUserCacheCode()) && authCacheDTO.getUserCacheCode().equals(authDTO.getUserCode()) && !authCacheDTO.getAuthToken().equals(authDTO.getAuthToken())) {
					duplicateFlag = Numeric.ONE_INT;
					break;
				}
			}
		}
		authDTO.getAdditionalAttribute().put(Constants.DUPLICATE_SESSION_FLAG, String.valueOf(duplicateFlag));
		
		PrivilegeDAO dao = new PrivilegeDAO();
		dao.loginEntry(authDTO, duplicateFlag);
	}

}
