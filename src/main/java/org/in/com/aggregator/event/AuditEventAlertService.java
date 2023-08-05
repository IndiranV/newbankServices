package org.in.com.aggregator.event;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.enumeration.AuditEventTypeEM;

public interface AuditEventAlertService {
	public void getNamespaceEventAlertImpl(AuthDTO authDTO, Object object, AuditEventTypeEM namespaceEvent);
}
