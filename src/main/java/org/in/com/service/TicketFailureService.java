package org.in.com.service;

import org.in.com.dto.AuthDTO;

public interface TicketFailureService {

	public void saveFailureLog(AuthDTO authDTO, String errorCode, String event, String extras, String request);
}
