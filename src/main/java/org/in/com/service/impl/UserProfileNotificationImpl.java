package org.in.com.service.impl;

import java.util.List;

import org.in.com.cache.CacheCentral;
import org.in.com.dao.UserProfileNotificationDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.UserProfileNotificationDTO;
import org.in.com.service.GroupService;
import org.in.com.service.UserProfileNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserProfileNotificationImpl extends CacheCentral implements UserProfileNotificationService {
	@Autowired
	GroupService groupService;

	@Override
	public List<UserProfileNotificationDTO> get(AuthDTO authDTO, UserProfileNotificationDTO dto) {
		UserProfileNotificationDAO dao = new UserProfileNotificationDAO();
		return dao.getNotification(authDTO);
	}

	@Override
	public List<UserProfileNotificationDTO> getAll(AuthDTO authDTO) {
		UserProfileNotificationDAO dao = new UserProfileNotificationDAO();
		List<UserProfileNotificationDTO> list = dao.getAllNotification(authDTO);
		for (UserProfileNotificationDTO notificationDTO : list) {
			if (notificationDTO.getGroup() != null && notificationDTO.getGroup().getId() != 0) {
				notificationDTO.setGroup(groupService.getGroup(authDTO, notificationDTO.getGroup()));
			}
			if (notificationDTO.getUser() != null && notificationDTO.getUser().getId() != 0) {
				notificationDTO.setUser(getUserDTOById(authDTO, notificationDTO.getUser()));
			}
		}
		return list;
	}

	@Override
	public UserProfileNotificationDTO Update(AuthDTO authDTO, UserProfileNotificationDTO dto) {
		UserProfileNotificationDAO dao = new UserProfileNotificationDAO();
		dao.update(authDTO, dto);
		return null;
	}

	@Override
	public UserProfileNotificationDTO updateLike(AuthDTO authDTO, UserProfileNotificationDTO dto) {
		UserProfileNotificationDAO dao = new UserProfileNotificationDAO();
		dao.updateLike(authDTO, dto);
		return null;
	}

}
