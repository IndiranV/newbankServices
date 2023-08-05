package org.in.com.dto;

import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.in.com.constants.Constants;
import org.in.com.constants.Text;
import org.in.com.dto.enumeration.PaymentTypeEM;
import org.in.com.dto.enumeration.UserRoleEM;
import org.in.com.dto.enumeration.UserTagEM;
import org.in.com.utils.StringUtil;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserDTO extends BaseDTO<UserDTO> {
	private String username;
	private String email;
	private String apiToken;
	private String mobile;
	private String lastname;
	private boolean oAuth;
	private String token;
	private String forgetToken;
	private GroupDTO group;
	private UserRoleEM userRole;
	private OrganizationDTO organization;
	private String oldPassword;
	private String newPassword;
	private PaymentTypeEM paymentType;
	private AppStoreDetailsDTO appStoreDetails;
	private List<UserTagEM> userTags;
	private IntegrationDTO integration;
	private String contactVerifiedFlag;
	private String passwordUpdatedAt;
	private UserDetailsDTO userDetails;
	private Map<String, String> additionalAttribute;

	public String getUserTagCodes() {
		StringBuilder tag = new StringBuilder();
		if (userTags != null && !userTags.isEmpty()) {
			for (UserTagEM userTagEM : userTags) {
				if (userTagEM.getId() != 0) {
					tag.append(userTagEM.getCode());
					tag.append(Text.COMMA);
				}
			}
		}
		if (userTags == null || userTags.isEmpty() || StringUtil.isNull(tag)) {
			tag.append(Text.NA);
		}
		return tag.toString();
	}

	public int getMobileVerifiedFlag() {
		int mobileVerifyFlag = 0;
		if (StringUtil.isNotNull(contactVerifiedFlag)) {
			mobileVerifyFlag = contactVerifiedFlag.charAt(0) == '1' ? 1 : 0;
		}
		return mobileVerifyFlag;
	}
	
	public boolean isAliasNamespaceExist(String alisNamespaecCode) {
		boolean isExist = false;
		if (additionalAttribute != null && additionalAttribute.containsKey(Constants.ALIAS_NAMESPACE) && additionalAttribute.get(Constants.ALIAS_NAMESPACE).equals(alisNamespaecCode)) {
			isExist = true;	
		}
		return isExist;
	}
}
