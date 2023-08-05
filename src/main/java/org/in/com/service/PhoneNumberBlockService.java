package org.in.com.service;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.PhoneNumberBlockDTO;

public interface PhoneNumberBlockService extends BaseService<PhoneNumberBlockDTO> {

	public PhoneNumberBlockDTO validatePhoneNumberBlock(AuthDTO authDTO, PhoneNumberBlockDTO phoneNumberBlockDTO);

	public void isExist(AuthDTO authDTO, String mobile);

}
