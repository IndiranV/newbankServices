package org.in.com.service.impl;

import java.util.Arrays;
import java.util.List;

import org.in.com.dao.AuditEventDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.AuditEventDTO;
import org.in.com.dto.enumeration.AuditEventTypeEM;
import org.in.com.service.AuditEventService;
import org.springframework.stereotype.Service;

@Service
public class AuditEventImpl implements AuditEventService {

	@Override
	public List<AuditEventDTO> get(AuthDTO authDTO, AuditEventDTO namespaceEvent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AuditEventDTO> getAll(AuthDTO authDTO) {
		AuditEventDAO eventDAO = new AuditEventDAO();
		return eventDAO.getAll(authDTO);
	}

	@Override
	public AuditEventDTO Update(AuthDTO authDTO, AuditEventDTO namespaceEvent) {
		AuditEventDAO eventDAO = new AuditEventDAO();
		return eventDAO.getNamespaceEventUpdate(authDTO, namespaceEvent);
	}

	@Override
	public List<AuditEventTypeEM> getAllNamespaceEvent(AuthDTO authDTO) {
		return Arrays.asList(AuditEventTypeEM.values());
	}

	@Override
	public AuditEventDTO getNamespaceEvent(AuthDTO authDTO, AuditEventDTO namespaceEventType) {
		AuditEventDAO eventDAO = new AuditEventDAO();
		return eventDAO.getNamespaceEvent(authDTO, namespaceEventType);
	}

}
