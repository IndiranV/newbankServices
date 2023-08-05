package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.NamespaceDTO;
import org.in.com.dto.NamespaceProfileDTO;
import org.in.com.dto.NamespaceTabletSettingsDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.NotificationSubscriptionTypeEM;
import org.in.com.dto.enumeration.NotificationTypeEM;

public interface NamespaceService extends BaseService<NamespaceDTO> {
	public NamespaceDTO getNamespace(NamespaceDTO namespace);

	public NamespaceDTO getNamespace(String namespaceCode);

	public NamespaceProfileDTO getProfile(AuthDTO authDTO);

	public boolean updateProfile(AuthDTO authDTO, NamespaceProfileDTO profileDTO);

	public List<NamespaceDTO> getAllUserNamespaceMap(AuthDTO authDTO, UserDTO userDTO);

	public void updateUserNamespaceMap(AuthDTO authDTO, NamespaceDTO namespaceDTO, UserDTO userDTO, String action);

	public void checkUserNamespaceMapping(AuthDTO authDTO, NamespaceDTO namespaceDTO);

	public void updateNotificationSubscriptionType(AuthDTO authDTO, List<NotificationSubscriptionTypeEM> subscriptionTypeList);

	public List<NamespaceDTO> getNotificationEnabledNamespace(AuthDTO authDTO, NotificationTypeEM notificationType);

	public void updateNamespaceWhatsapp(AuthDTO authDTO, NamespaceProfileDTO profileDTO, int status);

	public NamespaceDTO getNamespaceByContextToken(String contextToken);

	public NamespaceTabletSettingsDTO getNamespaceTabletSettings(AuthDTO authDTO);

	public void putNamespaceTabletSettings(AuthDTO authDTO, NamespaceTabletSettingsDTO namespaceTabletSettingsDTO);

	public void removeNamespaceTabletSettingsCache(AuthDTO authDTO);

}
