package org.in.com.service.impl;

import org.in.com.dao.AuditDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.service.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AuditImpl implements AuditService {
	private final Logger TICKET_AUDIT_LOG = LoggerFactory.getLogger("org.in.com.service.impl.AuditImpl");

	@Async
	public void addTicketAuditLog(AuthDTO authDTO, TicketDTO ticketDTO) {
		try {
			AuditDAO auditDAO = new AuditDAO();
			auditDAO.insertTicketAudit(authDTO, ticketDTO);
		}
		catch (Exception e) {
			TICKET_AUDIT_LOG.error("ERROR {} Ticket Audit Log : {}", authDTO.getNamespaceCode(), ticketDTO.toJSON());
			e.printStackTrace();
		}
	}

	@Async
	public void addTicketAuditFullLog(AuthDTO authDTO, TicketDTO ticketDTO) {
		try {
			AuditDAO auditDAO = new AuditDAO();
			auditDAO.addTicketJsonLog(authDTO, ticketDTO);
		}
		catch (Exception e) {
			TICKET_AUDIT_LOG.error("ERROR {} Ticket Audit Log JSON : {}", authDTO.getNamespaceCode(), ticketDTO.toJSON());
			e.printStackTrace();
		}
	}

	public void addAuditLog(AuthDTO authDTO, String code, String tableName, String actionName, String logData) {
		try {
			AuditDAO auditDAO = new AuditDAO();
			auditDAO.addAuditLog(authDTO, code, tableName, actionName, logData);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
