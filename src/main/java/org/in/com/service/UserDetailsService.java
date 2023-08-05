package org.in.com.service;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.UserDetailsDTO;

public interface UserDetailsService extends BaseService<UserDetailsDTO> {

	public UserDetailsDTO getUserDetails(AuthDTO authDTO, UserDTO userDTO);
	
	public UserDetailsDTO getUserDetailsV2(AuthDTO authDTO, UserDTO userDTO);
}
