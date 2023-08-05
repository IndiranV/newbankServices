package org.in.com.controller.web.io;

import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserIO extends BaseIO {

	private String username;
	private String email;
	private String mobile;
	private String apiToken;
	private String lastname;
	private OrganizationIO organization;
	private GroupIO group;
	private PaymentTypeIO paymentType;
	private UserPaymentPreferencesIO paymentPreferences;
	private RoleIO role;
	private NamespaceIO namespace;
	private String nativeNamespaceCode;
	private double currnetBalance;
	private double creditLimit;
	private List<CommissionIO> commission;
	private List<BaseIO> userTags;
	private BaseIO integrationType;
	private int mobileVerifiedFlag;
	private String passwordUpdateAt;
	private UserDetailsIO userDetails;
	private Map<String, String> additionalAttribute;
}
