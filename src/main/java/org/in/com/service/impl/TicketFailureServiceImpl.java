package org.in.com.service.impl;

import org.in.com.cache.UserCache;
import org.in.com.dao.TicketFailureLogDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.service.TicketFailureService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class TicketFailureServiceImpl extends UserCache implements TicketFailureService {

	@Async
	public void saveFailureLog(AuthDTO authDTO, String errorCode, String event, String extras, String request) {
		TicketFailureLogDAO dao = new TicketFailureLogDAO();
		dao.insertFailureLog(authDTO, errorCode, event, extras, request);
	}

}
