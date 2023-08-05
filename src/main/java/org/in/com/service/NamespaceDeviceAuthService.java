package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.NamespaceDeviceAuthDTO;
import org.in.com.dto.NamespaceDeviceDTO;
import org.in.com.dto.UserDTO;

public interface NamespaceDeviceAuthService extends BaseService<NamespaceDeviceAuthDTO> {

	public List<NamespaceDeviceAuthDTO> getAll(AuthDTO authDTO, NamespaceDeviceDTO namespaceDeviceDTO);

	public boolean checkDeviceUserAuthendtication(AuthDTO authDTO, NamespaceDeviceDTO deviceDTO, UserDTO userDTO);
}
