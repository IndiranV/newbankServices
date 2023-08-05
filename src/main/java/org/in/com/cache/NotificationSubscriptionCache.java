package org.in.com.cache;

import java.util.ArrayList;
import java.util.List;

import org.in.com.cache.dto.NotificationSubscriptionCacheDTO;
import org.in.com.constants.Text;
import org.in.com.dao.NotificationDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.NotificationSubscriptionDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.NotificationMediumEM;
import org.in.com.dto.enumeration.NotificationSubscriptionTypeEM;
import org.in.com.utils.StringUtil;

import net.sf.ehcache.Element;

public class NotificationSubscriptionCache extends AppStoreDetailsCache {
	private static String CACHEKEY = "SUBS_";

	protected List<NotificationSubscriptionDTO> getSubscriptionByType(AuthDTO authDTO, NotificationSubscriptionTypeEM subscriptionType) {
		String cacheKey = CACHEKEY + authDTO.getNamespace().getId() + Text.UNDER_SCORE + subscriptionType.getCode();

		List<NotificationSubscriptionDTO> subscriptionList = new ArrayList<>();
		Element element = EhcacheManager.getNotificationSubscriptionEhCache().get(cacheKey);
		if (element != null) {
			List<NotificationSubscriptionCacheDTO> cacheList = (List<NotificationSubscriptionCacheDTO>) element.getObjectValue();
			bindSubscriptionFromCache(cacheList, subscriptionList);
		}
		else {
			NotificationDAO notificationDAO = new NotificationDAO();
			subscriptionList = notificationDAO.getSubscriptionsByType(authDTO, subscriptionType);
			if (!subscriptionList.isEmpty() && StringUtil.isNotNull(subscriptionType.getCode())) {
				List<NotificationSubscriptionCacheDTO> cacheList = bindSubscriptionToCache(subscriptionList);
				element = new Element(cacheKey, cacheList);
				EhcacheManager.getNotificationSubscriptionEhCache().put(element);
			}
		}
		return subscriptionList;
	}

	private List<NotificationSubscriptionCacheDTO> bindSubscriptionToCache(List<NotificationSubscriptionDTO> subscriptionList) {
		List<NotificationSubscriptionCacheDTO> cacheList = new ArrayList<NotificationSubscriptionCacheDTO>();
		for (NotificationSubscriptionDTO subscriptionDTO : subscriptionList) {
			NotificationSubscriptionCacheDTO cahceDTO = new NotificationSubscriptionCacheDTO();
			cahceDTO.setCode(subscriptionDTO.getCode());
			cahceDTO.setActiveFlag(subscriptionDTO.getActiveFlag());

			List<String> mediumList = new ArrayList<>();
			List<Integer> userList = new ArrayList<>();
			List<Integer> groupList = new ArrayList<>();

			for (NotificationMediumEM notificationMedium : subscriptionDTO.getNotificationMediumList()) {
				mediumList.add(notificationMedium.getCode());
			}
			for (GroupDTO groupDTO : subscriptionDTO.getGroupList()) {
				if (groupDTO.getId() == 0) {
					continue;
				}
				groupList.add(groupDTO.getId());
			}
			for (UserDTO userDTO : subscriptionDTO.getUserList()) {
				if (userDTO.getId() == 0) {
					continue;
				}
				userList.add(userDTO.getId());
			}

			cahceDTO.setNotificationMedium(mediumList);
			cahceDTO.setGroup(groupList);
			cahceDTO.setUser(userList);
			cacheList.add(cahceDTO);
		}
		return cacheList;
	}

	private void bindSubscriptionFromCache(List<NotificationSubscriptionCacheDTO> cacheList, List<NotificationSubscriptionDTO> subscriptionList) {
		for (NotificationSubscriptionCacheDTO cacheDTO : cacheList) {
			NotificationSubscriptionDTO subscriptionDTO = new NotificationSubscriptionDTO();
			subscriptionDTO.setCode(cacheDTO.getCode());
			subscriptionDTO.setActiveFlag(cacheDTO.getActiveFlag());

			List<GroupDTO> groupList = new ArrayList<>();
			List<UserDTO> userList = new ArrayList<>();

			List<NotificationMediumEM> notificationMediumList = new ArrayList<>();
			for (String notificationMedium : cacheDTO.getNotificationMedium()) {
				NotificationMediumEM notificationMediumEM = NotificationMediumEM.getNotificationMediumEM(notificationMedium);
				if (notificationMediumEM == null) {
					continue;
				}
				notificationMediumList.add(notificationMediumEM);
			}

			for (int groupId : cacheDTO.getGroup()) {
				if (groupId == 0) {
					continue;
				}
				GroupDTO grouDTO = new GroupDTO();
				grouDTO.setId(groupId);
				groupList.add(grouDTO);
			}

			for (int userId : cacheDTO.getUser()) {
				if (userId == 0) {
					continue;
				}
				UserDTO userDTO = new UserDTO();
				userDTO.setId(userId);
				userList.add(userDTO);
			}

			subscriptionDTO.setNotificationMediumList(notificationMediumList);
			subscriptionDTO.setGroupList(groupList);
			subscriptionDTO.setUserList(userList);
			subscriptionList.add(subscriptionDTO);
		}
	}

	protected void removeSubscriptionCache(AuthDTO authDTO, NotificationSubscriptionTypeEM subscriptionType) {
		String cacheKey = CACHEKEY + authDTO.getNamespace().getId() + Text.UNDER_SCORE + subscriptionType.getCode();
		EhcacheManager.getNotificationSubscriptionEhCache().remove(cacheKey);
	}
}
