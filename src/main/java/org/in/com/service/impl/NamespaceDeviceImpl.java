package org.in.com.service.impl;

import java.util.List;

import org.in.com.aggregator.mail.EmailService;
import org.in.com.dao.NamespaceDeviceDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.NamespaceDTO;
import org.in.com.dto.NamespaceDeviceDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.NamespaceDeviceService;
import org.in.com.utils.BitsEnDecrypt;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NamespaceDeviceImpl implements NamespaceDeviceService {
	@Autowired
	EmailService emailService;

	@Override
	public List<NamespaceDeviceDTO> get(AuthDTO authDTO, NamespaceDeviceDTO dto) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<NamespaceDeviceDTO> getAll(AuthDTO authDTO) {
		NamespaceDeviceDAO dao = new NamespaceDeviceDAO();
		return dao.getAllNamespaceDevices(authDTO);
	}

	@Override
	public NamespaceDeviceDTO Update(AuthDTO authDTO, NamespaceDeviceDTO dto) {
		NamespaceDeviceDAO dao = new NamespaceDeviceDAO();
		return dao.updateNamespaceDevice(authDTO, dto);
	}

	@Override
	public NamespaceDeviceDTO registerDevice(AuthDTO authDTO, NamespaceDTO namespaceDTO, NamespaceDeviceDTO deviceDTO) {
		NamespaceDeviceDAO dao = new NamespaceDeviceDAO();
		NamespaceDeviceDTO namespaceDeviceDTO = dao.getRegisterDevice(namespaceDTO, deviceDTO);
		try {
			if (namespaceDeviceDTO.getId() != 0) {
				BitsEnDecrypt myEncryptor = new BitsEnDecrypt();
				if (StringUtil.isNull(namespaceDeviceDTO.getToken())) {
					namespaceDeviceDTO.setToken(myEncryptor.getDecrypParams(deviceDTO.getToken())[0]);
					dao.updateNamespaceDevice(authDTO, namespaceDeviceDTO);
				}
				emailService.sendDeviceRegistration(authDTO, namespaceDeviceDTO);
				String params[] = { namespaceDeviceDTO.getToken() };
				namespaceDeviceDTO.setToken(myEncryptor.getEncrypParams(params));
			}
			else {
				throw new ServiceException(ErrorCode.INVALID_CODE);
			}
		}
		catch (ServiceException se) {
			throw se;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return namespaceDeviceDTO;
	}

}
