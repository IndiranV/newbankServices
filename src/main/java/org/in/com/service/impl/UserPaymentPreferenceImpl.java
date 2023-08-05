package org.in.com.service.impl;

import java.util.List;

import org.in.com.dao.UserPaymentPreferencesDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.UserPaymentPreferencesDTO;
import org.in.com.service.UserPaymentPreferenceService;
import org.springframework.stereotype.Service;

@Service
public class UserPaymentPreferenceImpl implements UserPaymentPreferenceService {

	@Override
	public List<UserPaymentPreferencesDTO> get(AuthDTO authDTO, UserPaymentPreferencesDTO preferencesDTO) {
		UserPaymentPreferencesDAO preferencesDAO = new UserPaymentPreferencesDAO();
		preferencesDAO.getUserPaymentPreferencesDTO(authDTO, preferencesDTO);
		return null;
	}

	@Override
	public List<UserPaymentPreferencesDTO> getAll(AuthDTO authDTO) {
		return null;
	}

	@Override
	public UserPaymentPreferencesDTO Update(AuthDTO authDTO, UserPaymentPreferencesDTO preferencesDTO) {
		UserPaymentPreferencesDAO preferencesDAO = new UserPaymentPreferencesDAO();
		preferencesDAO.Update(authDTO, preferencesDTO);
		return preferencesDTO;
	}

}
