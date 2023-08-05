package org.in.com.service;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.UserProfileNotificationDTO;

public interface UserProfileNotificationService extends BaseService<UserProfileNotificationDTO>{
	public UserProfileNotificationDTO updateLike(AuthDTO authDTO, UserProfileNotificationDTO dto);
}
