package org.in.com.cache;

import net.sf.ehcache.Element;

import java.util.HashMap;

import org.in.com.cache.dto.AuthCacheDTO;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.UserCustomerDTO;
import org.in.com.dto.enumeration.AuthenticationTypeEM;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.utils.StringUtil;

public class AuthCache extends DriverCache {

	public AuthDTO getAuthDTO(String authToken) {
		AuthDTO authDTO = null;
		Element element = EhcacheManager.getAuthTokenEhCache().get(authToken);
		if (element != null) {
			AuthCacheDTO authCacheDTO = (AuthCacheDTO) element.getObjectValue();
			authDTO = bindBusFromCacheObject(authCacheDTO);
			if (StringUtil.isNotNull(authDTO.getAuthToken()) && !authDTO.getAuthToken().equals(authToken)) {
				clearAuthDTO(authToken);
				throw new ServiceException(ErrorCode.INVALID_CREDENTIALS);
			}
			if (StringUtil.isNull(authDTO.getUserCode()) || authDTO.getUser() == null) {
				clearAuthDTO(authToken);
				throw new ServiceException(ErrorCode.INVALID_CREDENTIALS);
			}

		}
		return authDTO;
	}

	public AuthDTO getAPIAuthDTO(String APIToken) {
		AuthDTO authDTO = null;
		Element element = EhcacheManager.getAuthTokenEhCache().get(APIToken);
		if (element != null) {
			AuthCacheDTO authCacheDTO = (AuthCacheDTO) element.getObjectValue();
			authDTO = bindBusFromCacheObject(authCacheDTO);
			if (StringUtil.isNotNull(authDTO.getApiToken()) && !authDTO.getApiToken().equals(APIToken)) {
				throw new ServiceException(ErrorCode.INVALID_CREDENTIALS);
			}
		}
		return authDTO;
	}

	public void putAuthDTO(AuthDTO authDTO) {
		AuthCacheDTO authCacheDTO = bindAuthToCacheObject(authDTO);
		// copy object
		Element element = new Element(authCacheDTO.getAuthToken(), authCacheDTO);
		EhcacheManager.getAuthTokenEhCache().put(element);
	}

	public void putInvalidAPIAuth(String namespaceCode, String username, String errorCode) {
		AuthCacheDTO authCacheDTO = new AuthCacheDTO();
		authCacheDTO.setAuthenticationTypeCode(errorCode);
		Element element = new Element(namespaceCode + Text.UNDER_SCORE + username, authCacheDTO);
		EhcacheManager.getAuthTokenEhCache().put(element);
	}

	public String checkInvalidAPIAuth(String namespaceCode, String username) {
		String errorCode = Text.EMPTY;
		Element element = EhcacheManager.getAuthTokenEhCache().get(namespaceCode + Text.UNDER_SCORE + username);
		if (element != null) {
			AuthCacheDTO authCacheDTO = (AuthCacheDTO) element.getObjectValue();
			errorCode = authCacheDTO.getAuthenticationTypeCode();
		}
		return errorCode;
	}

	public void clearInvalidAPIAuth(String namespaceCode, String username) {
		EhcacheManager.getAuthTokenEhCache().remove(namespaceCode + Text.UNDER_SCORE + username);
	}

	public void clearAuthDTO(String authToken) {
		EhcacheManager.getAuthTokenEhCache().remove(authToken);
	}

	protected AuthCacheDTO bindAuthToCacheObject(AuthDTO authDTO) {
		AuthCacheDTO cacheDTO = new AuthCacheDTO();
		cacheDTO.setApiToken(authDTO.getApiToken());
		cacheDTO.setAuthToken(authDTO.getAuthToken());
		cacheDTO.setAuthenticationTypeCode(authDTO.getAuthenticationType().getCode());
		cacheDTO.setNamespaceCode(authDTO.getNamespaceCode());
		cacheDTO.setUserCacheCode(authDTO.getUserCode());
		cacheDTO.setNativeNamespaceCode(authDTO.getNativeNamespaceCode());
		cacheDTO.setDeviceMediumCode(authDTO.getDeviceMedium().getCode());
		cacheDTO.setPrivilegesDTO(authDTO.getPrivileges());
		cacheDTO.setAdditionalAttribute(authDTO.getAdditionalAttribute() != null ? authDTO.getAdditionalAttribute() : new HashMap<String, String>());
		if (authDTO.getUserCustomer() != null) {
			cacheDTO.setUserCustomerId(authDTO.getUserCustomer().getId());
		}
		return cacheDTO;
	}

	protected AuthDTO bindBusFromCacheObject(AuthCacheDTO cacheDTO) {
		AuthDTO authDTO = new AuthDTO();
		authDTO.setApiToken(cacheDTO.getApiToken());
		authDTO.setAuthToken(cacheDTO.getAuthToken());
		authDTO.setAuthenticationType(AuthenticationTypeEM.getAuthenticationTypeEM(cacheDTO.getAuthenticationTypeCode()));
		authDTO.setDeviceMedium(DeviceMediumEM.getDeviceMediumEM(cacheDTO.getDeviceMediumCode()));
		authDTO.setNamespaceCode(cacheDTO.getNamespaceCode());
		authDTO.setNativeNamespaceCode(cacheDTO.getNativeNamespaceCode());
		authDTO.setUserCode(cacheDTO.getUserCacheCode());
		authDTO.setPrivileges(cacheDTO.getPrivileges());
		authDTO.getAdditionalAttribute().putAll(cacheDTO.getAdditionalAttribute());
		if (cacheDTO.getUserCustomerId() != 0) {
			UserCustomerDTO userCustomerDTO = new UserCustomerDTO();
			userCustomerDTO.setId(cacheDTO.getUserCustomerId());
			authDTO.setUserCustomer(userCustomerDTO);
		}
		return authDTO;
	}

	public AuthDTO bindAuthFromCacheObject(AuthCacheDTO cacheDTO) {
		return bindBusFromCacheObject(cacheDTO);
	}
}
