package org.in.com.service;

import org.in.com.dto.AuthDTO;

public interface UserLoginHistoryService {

	public void saveLoginEntry(AuthDTO authDTO);
}
