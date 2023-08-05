package org.in.com.dto;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.in.com.cache.UserCache;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.enumeration.AuthenticationTypeEM;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.utils.StringUtil;

@Data
@EqualsAndHashCode(callSuper = true)
public class AuthDTO extends UserCache {
	private String authToken;
	private String apiToken;
	private String userCode;
	private String nativeNamespaceCode;
	private String namespaceCode;

	// load on-demand cache
	private GroupDTO group;
	private UserDTO user;
	private UserCustomerDTO userCustomer;
	private NamespaceDTO namespace;

	private DeviceMediumEM deviceMedium;
	private AuthenticationTypeEM authenticationType;
	private List<MenuDTO> privileges;
	private Map<String, String> additionalAttribute = new HashMap<String, String>();
	private BigDecimal currnetBalance = BigDecimal.ZERO;

	public String getDomainUrl() {
		return getNamespace().getProfile() != null ? getNamespace().getProfile().getDomainURL() : null;
	}

	public NamespaceDTO getNamespace() {
		if (namespace == null) {
			namespace = getNamespaceDTO(namespaceCode);
		}
		return namespace;
	}

	public NamespaceDTO getNativeNamespaceDTO() {
		return getNamespaceDTO(nativeNamespaceCode);
	}

	public UserDTO getUser() {
		if (user == null && StringUtil.isNotNull(userCode)) {
			user = getUserDTO(nativeNamespaceCode, userCode);
			if (user == null) {
				System.out.printf("\nERR00A1: %s-%s-%s-%s-%s-%s", namespaceCode, userCode, authToken, apiToken, nativeNamespaceCode, deviceMedium.getCode());
			}
		}
		return user;
	}

	public GroupDTO getGroup() {
		if (group == null) {
			UserDTO userDTO = getUser();
			if (userDTO != null) {
				group = getGroupDTOById(this, userDTO.getGroup());
				if (group == null) {
					System.out.printf("\nERR00A2: %s-%s-%s-%s-%s-%s", namespaceCode, userCode, authToken, apiToken, nativeNamespaceCode, deviceMedium.getCode());
				}
			}
			// Due to non Native Namespace, Null point bug fix
			if (group != null && group.getCode() == null) {
				group.setCode(Text.NA);
			}
		}
		return group;
	}

	public boolean isSectorEnabled() {
		boolean isPermissionEnabled = false;
		if (additionalAttribute != null && additionalAttribute.containsKey(Text.SECTOR)) {
			isPermissionEnabled = additionalAttribute.get(Text.SECTOR).equals(Numeric.ONE) ? true : false;
		}
		return isPermissionEnabled;
	}

	public String getAliasNamespaceCode() {
		String aliasNamespaceCode = Text.EMPTY;
		if (additionalAttribute != null && additionalAttribute.containsKey(Text.ALIAS_NAMESPACE_CODE)) {
			aliasNamespaceCode = additionalAttribute.get(Text.ALIAS_NAMESPACE_CODE);
		}
		return aliasNamespaceCode;
	}

	public boolean isMultipleRescheduleEnabled() {
		boolean isPermissionEnabled = false;
		if (additionalAttribute != null && additionalAttribute.containsKey(Text.RESCHEDULE)) {
			isPermissionEnabled = additionalAttribute.get(Text.RESCHEDULE).equals(Numeric.ONE) ? true : false;
		}
		return isPermissionEnabled;
	}
}
