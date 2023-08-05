package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.AuditEventDTO;
import org.in.com.dto.enumeration.AuditEventTypeEM;

public interface AuditEventService extends BaseService<AuditEventDTO> {
	public List<AuditEventTypeEM> getAllNamespaceEvent(AuthDTO authDTO);

	public AuditEventDTO getNamespaceEvent(AuthDTO authDTO, AuditEventDTO namespaceEventType);
}
