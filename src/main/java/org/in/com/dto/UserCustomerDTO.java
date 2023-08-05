package org.in.com.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserCustomerDTO extends BaseDTO<UserCustomerDTO> {
	private String email;
	private String mobile;
	private String lastname;
	private String walletCode;
	private AppStoreDetailsDTO appStoreDetails;
	private UserCustomerAuthDTO userCustomerAuth;
}
