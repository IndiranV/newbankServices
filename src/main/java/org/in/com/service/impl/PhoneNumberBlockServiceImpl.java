package org.in.com.service.impl;

import java.util.List;

import org.in.com.dao.PhoneNumberBlockDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.PhoneNumberBlockDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.PhoneNumberBlockService;
import org.springframework.stereotype.Service;

@Service
public class PhoneNumberBlockServiceImpl implements PhoneNumberBlockService {

	public List<PhoneNumberBlockDTO> get(AuthDTO authDTO, PhoneNumberBlockDTO phoneNumberBlockDTO) {
		return null;
	}

	@Override
	public PhoneNumberBlockDTO validatePhoneNumberBlock(AuthDTO authDTO, PhoneNumberBlockDTO phoneNumberBlockDTO) {
		PhoneNumberBlockDAO dao = new PhoneNumberBlockDAO();
		phoneNumberBlockDTO = dao.getPhoneNumberBlock(authDTO, phoneNumberBlockDTO);
		if (phoneNumberBlockDTO != null) {
			throw new ServiceException(ErrorCode.PHONE_BOOK_TICKET_NOT_ALLOW, phoneNumberBlockDTO.getRemarks());
		}
		return phoneNumberBlockDTO;
	}

	@Override
	public List<PhoneNumberBlockDTO> getAll(AuthDTO authDTO) {
		PhoneNumberBlockDAO dao = new PhoneNumberBlockDAO();
		return dao.getAllPhoneNumberBlock(authDTO);
	}

	@Override
	public PhoneNumberBlockDTO Update(AuthDTO authDTO, PhoneNumberBlockDTO phoneNumberBlockDTO) {
		PhoneNumberBlockDAO dao = new PhoneNumberBlockDAO();
		dao.updatePhoneNumberBlock(authDTO, phoneNumberBlockDTO);
		return null;
	}

	@Override
	public void isExist(AuthDTO authDTO, String mobile) {
		PhoneNumberBlockDAO dao = new PhoneNumberBlockDAO();
		PhoneNumberBlockDTO phoneNumberBlockDTO = new PhoneNumberBlockDTO();
		phoneNumberBlockDTO.setMobile(mobile);
		boolean isFound = dao.isExist(authDTO, phoneNumberBlockDTO);
		if (isFound) {
			throw new ServiceException(ErrorCode.PHONE_BOOK_TICKET_NOT_ALLOW);
		}
	}

}
