package org.in.com.service.impl;

import java.util.List;

import org.in.com.dao.UserTaxDetailsDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.UserTaxDetailsDTO;
import org.in.com.service.UserService;
import org.in.com.service.UserTaxDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserTaxDetailsServiceImpl implements UserTaxDetailsService {

	@Autowired
	UserService userService;

	public List<UserTaxDetailsDTO> get(AuthDTO authDTO, UserTaxDetailsDTO userTaxDetailsDTO) {
		return null;
	}

	public List<UserTaxDetailsDTO> getAll(AuthDTO authDTO) {
		UserTaxDetailsDAO userTaxDAO = new UserTaxDetailsDAO();
		List<UserTaxDetailsDTO> userTaxList = userTaxDAO.getAllUserTaxdetails(authDTO);
	
		for (UserTaxDetailsDTO userTaxDetails : userTaxList) {
			userTaxDetails.setUser(userService.getUser(authDTO, userTaxDetails.getUser()));
		}
		return userTaxList;
	}

	public UserTaxDetailsDTO Update(AuthDTO authDTO, UserTaxDetailsDTO userTaxDetailsDTO) {
		UserTaxDetailsDAO userTaxDAO = new UserTaxDetailsDAO();
		userTaxDetailsDTO.setUser(userService.getUser(authDTO, userTaxDetailsDTO.getUser()));
		userTaxDAO.updateUserTaxDetails(authDTO, userTaxDetailsDTO);
		return null;
	}

	public UserTaxDetailsDTO getUserTaxDetails(AuthDTO authDTO, UserDTO userDTO) {
		UserTaxDetailsDAO userTaxDAO = new UserTaxDetailsDAO();
		UserTaxDetailsDTO userTaxDetails = userTaxDAO.getUsertaxDetails(authDTO, userDTO);
		return userTaxDetails;

	}

}
