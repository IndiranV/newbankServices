package org.in.com.service.impl;

import java.util.List;

import org.in.com.dao.UserDeviceDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.UserDeviceDTO;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.service.UserDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserDeviceServiceImpl implements UserDeviceService {

	@Autowired
	UserDeviceDAO deviceDAO;

	@Override
	public List<UserDeviceDTO> getUserDevice(AuthDTO authDTO, DeviceMediumEM deviceMedium) {
		List<UserDeviceDTO> list = deviceDAO.get(authDTO, deviceMedium);
		return list;
	}

	@Override
	public void registerUserDevice(AuthDTO authDTO, UserDeviceDTO userAppInfoDTO) {
		deviceDAO.registerUserDevice(authDTO, userAppInfoDTO);
	}

}
