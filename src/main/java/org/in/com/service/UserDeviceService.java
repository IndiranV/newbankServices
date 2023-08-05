package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.UserDeviceDTO;
import org.in.com.dto.enumeration.DeviceMediumEM;

public interface UserDeviceService {
	public List<UserDeviceDTO> getUserDevice(AuthDTO authDTO, DeviceMediumEM deviceMedium);

	public void registerUserDevice(AuthDTO authDTO, UserDeviceDTO userAppInfoDTO);

}
