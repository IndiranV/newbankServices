package org.in.com.service;

import java.util.List;

import org.in.com.dto.AppStoreDetailsDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.UserCustomerAuthDTO;
import org.in.com.dto.UserCustomerDTO;

public interface UserCustomerService {
	public UserCustomerDTO getUserCustomer(AuthDTO authDTO, UserCustomerDTO userCustomerDTO);

	public UserCustomerDTO updateUserCustomer(AuthDTO authDTO, UserCustomerDTO userCustomerDTO);

	public UserCustomerDTO saveUserCustomer(AuthDTO authDTO, UserCustomerDTO userCustomerDTO);

	public UserCustomerDTO checkUserCustomer(AuthDTO authDTO, String mobileNumber);

	public void appStoreUpdate(AuthDTO authDTO, UserCustomerDTO userCustomer);

	public List<AppStoreDetailsDTO> getAppStoreDetails(AuthDTO authDTO, UserCustomerDTO userCustomerDTO);

	public void generateUserCustomerAuth(AuthDTO authDTO, UserCustomerDTO userCustomerDTO);

	public UserCustomerDTO checkUserCustomerAuthBySessionToken(AuthDTO authDTO, UserCustomerDTO userCustomer);

	public void updateUserCustomerAuth(AuthDTO authDTO, UserCustomerAuthDTO userCustomerAuthDTO);

}
