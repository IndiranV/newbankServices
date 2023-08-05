package org.in.com.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.ehcache.Element;

import org.in.com.cache.dto.UserCacheDTO;
import org.in.com.constants.Text;
import org.in.com.dao.UserDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.OrganizationDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.PaymentTypeEM;
import org.in.com.dto.enumeration.UserRoleEM;
import org.in.com.dto.enumeration.UserTagEM;
import org.in.com.utils.StringUtil;

public class UserCache extends GroupCache {
	private static String CACHEKEY = "USUR";

	public UserDTO getUserDTO(String namespaceCode, String userCode) {
		UserDTO userDTO = new UserDTO();
		Element element = EhcacheManager.getUserEhCache().get(userCode);
		if (element != null) {
			UserCacheDTO cacheDTO = (UserCacheDTO) element.getObjectValue();
			// Copy from Cache
			copyUserFromCache(cacheDTO, userDTO);
		}
		else {
			UserDAO userDAO = new UserDAO();
			userDTO = userDAO.getUserDTO(getNamespaceDTO(namespaceCode), userCode);
			// Copy from Cache
			if (userDTO != null && StringUtil.isNotNull(userDTO.getCode()) && userDTO.getId() != 0) {
				UserCacheDTO cacheDTO = copyUserToCache(userDTO);
				element = new Element(cacheDTO.getCode(), cacheDTO);
				EhcacheManager.getUserEhCache().put(element);
			}
		}
		return userDTO;
	}

	protected UserDTO getUserDTO(AuthDTO authDTO, UserDTO userDTO) {
		Element element = EhcacheManager.getUserEhCache().get(userDTO.getCode());
		if (element != null) {
			UserCacheDTO cacheDTO = (UserCacheDTO) element.getObjectValue();
			// Copy from Cache
			copyUserFromCache(cacheDTO, userDTO);
		}
		else {
			UserDAO userDAO = new UserDAO();
			userDAO.getUserDTO(authDTO, userDTO);
			// Copy from Cache
			if (userDTO != null && StringUtil.isNotNull(userDTO.getCode()) && userDTO.getId() != 0) {
				UserCacheDTO cacheDTO = copyUserToCache(userDTO);
				element = new Element(cacheDTO.getCode(), cacheDTO);
				EhcacheManager.getUserEhCache().put(element);
			}
		}

		return userDTO;
	}

	protected UserDTO getUserDTOById(AuthDTO authDTO, UserDTO userDTO) {
		Element elementKey = null;
		String key = CACHEKEY + authDTO.getNamespace().getId() + "_" + userDTO.getId();
		if (userDTO.getId() != 0) {
			elementKey = EhcacheManager.getUserEhCache().get(key);
			if (elementKey != null) {
				String userCode = (String) elementKey.getObjectValue();
				userDTO.setCode(userCode);
			}
		}
		userDTO = getUserDTO(authDTO, userDTO);

		if (elementKey == null && StringUtil.isNotNull(userDTO.getCode()) && userDTO.getId() != 0) {
			key = CACHEKEY + authDTO.getNamespace().getId() + "_" + userDTO.getId();
			elementKey = new Element(key, userDTO.getCode());
			EhcacheManager.getUserEhCache().put(elementKey);
		}
		return userDTO;
	}

	private UserCacheDTO copyUserToCache(UserDTO userDTO) {
		UserCacheDTO cacheDTO = new UserCacheDTO();
		cacheDTO.setActiveFlag(userDTO.getActiveFlag());
		cacheDTO.setId(userDTO.getId());
		cacheDTO.setCode(userDTO.getCode());
		cacheDTO.setUsername(userDTO.getUsername());
		cacheDTO.setEmail(userDTO.getEmail());
		cacheDTO.setMobile(userDTO.getMobile());
		cacheDTO.setName(userDTO.getName());
		cacheDTO.setApiToken(userDTO.getApiToken());
		cacheDTO.setPaymentTypeCode(userDTO.getPaymentType().getCode());
		if (userDTO.getGroup() != null) {
			cacheDTO.setGroupId(userDTO.getGroup().getId());
		}
		cacheDTO.setRoleCode(userDTO.getUserRole().getCode());
		if (userDTO.getOrganization() != null) {
			cacheDTO.setOrganizationId(userDTO.getOrganization().getId());
		}

		List<String> userTags = new ArrayList<String>();
		if (userDTO.getUserTags() != null) {
			for (UserTagEM userTagEM : userDTO.getUserTags()) {
				userTags.add(userTagEM.getCode());
			}
		}
		cacheDTO.setUserTags(userTags);
		cacheDTO.setContactVerifiedFlag(userDTO.getContactVerifiedFlag());
		cacheDTO.setPasswordUpdatedAt(userDTO.getPasswordUpdatedAt());
		cacheDTO.setAdditionalAttribute(getAdditionalAttribute(userDTO.getAdditionalAttribute()));
		return cacheDTO;
	}

	private void copyUserFromCache(UserCacheDTO cacheDTO, UserDTO userDTO) {
		userDTO.setActiveFlag(cacheDTO.getActiveFlag());
		userDTO.setId(cacheDTO.getId());
		userDTO.setCode(cacheDTO.getCode());
		userDTO.setUsername(cacheDTO.getUsername());
		userDTO.setEmail(cacheDTO.getEmail());
		userDTO.setMobile(cacheDTO.getMobile());
		userDTO.setName(cacheDTO.getName());
		userDTO.setApiToken(cacheDTO.getApiToken());

		GroupDTO groupDTO = new GroupDTO();
		groupDTO.setId(cacheDTO.getGroupId());
		groupDTO.setActiveFlag(1);
		userDTO.setGroup(groupDTO);
		userDTO.setPaymentType(PaymentTypeEM.getPaymentTypeEM(cacheDTO.getPaymentTypeCode()));
		userDTO.setUserRole(UserRoleEM.getUserRoleEM(cacheDTO.getRoleCode()));

		OrganizationDTO organizationDTO = new OrganizationDTO();
		organizationDTO.setId(cacheDTO.getOrganizationId());
		userDTO.setOrganization(organizationDTO);

		List<UserTagEM> userTags = new ArrayList<UserTagEM>();
		if (cacheDTO.getUserTags() != null) {
			for (String userTagCode : cacheDTO.getUserTags()) {
				UserTagEM userTagEM = UserTagEM.getUserTagEM(userTagCode);
				if (userTagEM == null) {
					continue;
				}
				userTags.add(userTagEM);
			}
		}
		userDTO.setUserTags(userTags);
		userDTO.setContactVerifiedFlag(cacheDTO.getContactVerifiedFlag());
		userDTO.setPasswordUpdatedAt(cacheDTO.getPasswordUpdatedAt());
		Map<String, String> additionalAttribute = convertAdditionalAttribute(cacheDTO.getAdditionalAttribute());
		userDTO.setAdditionalAttribute(additionalAttribute);
	}

	protected void removeUserDTO(AuthDTO authDTO, UserDTO userDTO) {
		clearInvalidAPIAuth(authDTO.getNamespaceCode(), getUserDTO(authDTO.getNamespaceCode(), userDTO.getCode()).getUsername());
		EhcacheManager.getUserEhCache().remove(userDTO.getCode());
	}

	private String getAdditionalAttribute(Map<String, String> additionalAttribute) {
		StringBuilder details = new StringBuilder();
		if (additionalAttribute != null) {
			for (Map.Entry<String, String> map : additionalAttribute.entrySet()) {
				if (details.length() > 0) {
					details.append(Text.COMMA);
				}
				details.append(map.getKey());
				details.append(Text.COLON);
				details.append(map.getValue());
			}
		}
		
		String additionalDetails = details.toString();
		if (StringUtil.isNull(additionalDetails)) {
			additionalDetails =  Text.NA;
		}
		return additionalDetails;
	}
	
	private Map<String, String> convertAdditionalAttribute(String additionalStr) {
		Map<String, String> additionalAttribute = new HashMap<>();
		if (StringUtil.isNotNull(additionalStr)) {
			String[] additionldetails = additionalStr.split(Text.COMMA);
			for (String details : additionldetails){
				if (details.split("\\:").length != 2) {
					continue;
				}
				additionalAttribute.put(details.split("\\:")[0], details.split("\\:")[1]);
			}
		}
		return additionalAttribute;
	}
}
