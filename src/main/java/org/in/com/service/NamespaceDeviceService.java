package org.in.com.service;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.NamespaceDTO;
import org.in.com.dto.NamespaceDeviceDTO;

public interface NamespaceDeviceService extends BaseService<NamespaceDeviceDTO> {
	public NamespaceDeviceDTO registerDevice(AuthDTO authDTO, NamespaceDTO namespaceDTO, NamespaceDeviceDTO deviceDTO);
}
