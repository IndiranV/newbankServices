package org.in.com.service.impl;

import java.util.Iterator;
import java.util.List;

import org.in.com.cache.CacheCentral;
import org.in.com.dao.NamespaceDeviceAuthDAO;
import org.in.com.dao.NamespaceDeviceDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.NamespaceDeviceAuthDTO;
import org.in.com.dto.NamespaceDeviceDTO;
import org.in.com.dto.UserDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.GroupService;
import org.in.com.service.NamespaceDeviceAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NamespaceDeviceAuthImpl extends CacheCentral implements NamespaceDeviceAuthService {
	@Autowired
	GroupService groupService;

	public List<NamespaceDeviceAuthDTO> get(AuthDTO authDTO, NamespaceDeviceAuthDTO dto) {
		return null;
	}

	public List<NamespaceDeviceAuthDTO> getAll(AuthDTO authDTO, NamespaceDeviceDTO namespaceDeviceDTO) {
		NamespaceDeviceAuthDAO dao = new NamespaceDeviceAuthDAO();
		List<NamespaceDeviceAuthDTO> list = dao.getDeviceAllAuth(authDTO, namespaceDeviceDTO);
		for (NamespaceDeviceAuthDTO dto : list) {
			if (dto.getGroup() != null) {
				dto.setGroup(groupService.getGroup(authDTO, dto.getGroup()));
			}
			if (dto.getUser() != null) {
				dto.setUser(getUserDTOById(authDTO, dto.getUser()));
			}
		}
		return list;
	}

	public NamespaceDeviceAuthDTO Update(AuthDTO authDTO, NamespaceDeviceAuthDTO dto) {
		NamespaceDeviceAuthDAO dao = new NamespaceDeviceAuthDAO();
		return dao.updateNamespaceDeviceAuth(authDTO, dto);
	}

	public List<NamespaceDeviceAuthDTO> getAll(AuthDTO authDTO) {
		return null;
	}

	public boolean checkDeviceUserAuthendtication(AuthDTO authDTO, NamespaceDeviceDTO deviceDTO, UserDTO userDTO) {
		boolean status = false;
		NamespaceDeviceDAO deviceDAO = new NamespaceDeviceDAO();
		NamespaceDeviceAuthDAO deviceAuthDAO = new NamespaceDeviceAuthDAO();
		NamespaceDeviceDTO namespaceDeviceDTO = deviceDAO.getNamespaceDevices(authDTO, deviceDTO.getCode());
		// Check device
		if (namespaceDeviceDTO.getId() != 0 && namespaceDeviceDTO.getToken().equals(deviceDTO.getToken())) {
			List<NamespaceDeviceAuthDTO> authList = deviceAuthDAO.getAll(authDTO);
			NamespaceDeviceAuthDTO userDeviceAuthDTO = null;
			boolean userDeviceAuthFound = false;
			// filter unwanted data
			for (Iterator<NamespaceDeviceAuthDTO> iterator = authList.iterator(); iterator.hasNext();) {
				NamespaceDeviceAuthDTO deviceAuthDTO = iterator.next();
				if (deviceAuthDTO.getRefferenceType().equals("UR") && deviceAuthDTO.getUser().getId() == userDTO.getId()) {
					userDeviceAuthFound = true;
				}
				if (deviceAuthDTO.getRefferenceType().equals("UR") && deviceAuthDTO.getUser().getId() != userDTO.getId()) {
					iterator.remove();
					continue;
				}
				if (deviceAuthDTO.getRefferenceType().equals("GR") && deviceAuthDTO.getGroup().getId() != userDTO.getGroup().getId()) {
					iterator.remove();
					continue;
				}
				if (deviceAuthDTO.getNamespaceDevice().getId() != namespaceDeviceDTO.getId()) {
					iterator.remove();
					continue;
				}
			}
			for (NamespaceDeviceAuthDTO deviceAuthDTO : authList) {
				if (userDeviceAuthFound && deviceAuthDTO.getRefferenceType().equals("UR")) {
					userDeviceAuthDTO = deviceAuthDTO;
				}
				if (!userDeviceAuthFound && deviceAuthDTO.getRefferenceType().equals("GR")) {
					userDeviceAuthDTO = deviceAuthDTO;
				}
			}
			// Check device and user mapping
			if (userDeviceAuthDTO == null || userDeviceAuthDTO.getNamespaceDevice().getId() != namespaceDeviceDTO.getId()) {
				throw new ServiceException(ErrorCode.USER_INVALID_DEVICE_AUTH_TOKEN);
			}
			status = true;
		}
		else {
			throw new ServiceException(ErrorCode.USER_INVALID_DEVICE_AUTH_TOKEN);
		}
		return status;
	}
}
