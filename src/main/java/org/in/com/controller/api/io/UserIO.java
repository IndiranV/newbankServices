package org.in.com.controller.api.io;

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
	private BaseIO paymentType;
	private String nativeNamespaceCode;
	private BaseIO integrationType;
}
