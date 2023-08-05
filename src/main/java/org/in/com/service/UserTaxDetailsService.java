package org.in.com.service;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.UserTaxDetailsDTO;

public interface UserTaxDetailsService extends BaseService<UserTaxDetailsDTO> {
	public UserTaxDetailsDTO getUserTaxDetails(AuthDTO authDTO, UserDTO userDTO);

}
