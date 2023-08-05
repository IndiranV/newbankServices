package org.in.com.service;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.TicketDTO;

public interface AuditService {

	public void addTicketAuditLog(AuthDTO authDTO, TicketDTO ticketDTO);

	public void addTicketAuditFullLog(AuthDTO authDTO, TicketDTO ticketDTO);

	public void addAuditLog(AuthDTO authDTO, String code, String tableName, String actionName, String logData);

}
