package org.in.com.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.in.com.dto.enumeration.DeviceMediumEM;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserCustomerAuthDTO extends BaseDTO<UserCustomerAuthDTO> {
	private DeviceMediumEM deviceMedium;
	private String sessionToken;
}
