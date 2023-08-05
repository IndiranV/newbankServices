package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Cleanup;

import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.NotificationDTO;
import org.in.com.dto.NotificationSubscriptionDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.NotificationMediumEM;
import org.in.com.dto.enumeration.NotificationSubscriptionTypeEM;
import org.in.com.exception.ServiceException;
import org.in.com.utils.StringUtil;

import com.google.common.collect.Maps;
import com.google.gson.Gson;

public class NotificationDAO {
	public void insertNotification(AuthDTO authDTO, NotificationDTO dto) throws Exception {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("INSERT INTO notification_log (namespace_id,notification_mode,notification_type_id,participant_address,refference_code,transaction_count,request_log1,request_log2,response_log,active_flag,updated_by,updated_at) VALUES(?,?,?,?,? ,?,?,?,?,1,?, NOW())");
			ps.setInt(++pindex, authDTO.getNamespace().getId());
			ps.setString(++pindex, dto.getNotificationMode().getCode());
			ps.setInt(++pindex, dto.getNotificationType().getId());
			ps.setString(++pindex, StringUtil.substring(dto.getParticipantAddress(), 120));
			ps.setString(++pindex, dto.getRefferenceCode());
			ps.setInt(++pindex, dto.getTransactionCount());
			ps.setString(++pindex, dto.getRequestLog1());
			ps.setString(++pindex, dto.getRequestLog2());
			ps.setString(++pindex, StringUtil.substring(dto.getResponseLog(), 250));
			ps.setInt(++pindex, authDTO.getUser() != null ? authDTO.getUser().getId() : 0);
			ps.executeUpdate();
		}
		catch (Exception e) {
			System.out.println(new Gson().toJson(dto));
			e.printStackTrace();
		}
	}

	public NotificationSubscriptionDTO updateNotificationSubscription(AuthDTO authDTO, NotificationSubscriptionDTO subscriptionsDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement termSt = connection.prepareCall("{CALL EZEE_SP_NOTIFICATION_SUBSCRIPTIONS_IUD(?,?,?,?,?, ?,?,?,?,?)}");
			termSt.setString(++pindex, subscriptionsDTO.getCode());
			termSt.setInt(++pindex, authDTO.getNamespace().getId());
			termSt.setInt(++pindex, subscriptionsDTO.getSubscriptionType() != null ? subscriptionsDTO.getSubscriptionType().getId() : 0);
			termSt.setString(++pindex, subscriptionsDTO.getUsers());
			termSt.setString(++pindex, subscriptionsDTO.getGroups());
			termSt.setString(++pindex, subscriptionsDTO.getNotificationMediums());
			termSt.setInt(++pindex, subscriptionsDTO.getActiveFlag());
			termSt.setInt(++pindex, authDTO.getUser().getId());
			termSt.setInt(++pindex, 0);
			termSt.registerOutParameter(++pindex, Types.INTEGER);
			termSt.execute();
			if (termSt.getInt("pitRowCount") > 0) {
				subscriptionsDTO.setCode(termSt.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return subscriptionsDTO;
	}

	public NotificationSubscriptionDTO updateUserNotificationSubscription(AuthDTO authDTO, List<NotificationSubscriptionDTO> subscriptionsList) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			CallableStatement termSt = connection.prepareCall("{CALL EZEE_SP_NOTIFICATION_SUBSCRIPTIONS_IUD(?,?,?,?,?, ?,?,?,?,?)}");
			for (NotificationSubscriptionDTO subscriptionsDTO : subscriptionsList) {
				int pindex = 0;
				termSt.setString(++pindex, subscriptionsDTO.getCode());
				termSt.setInt(++pindex, authDTO.getNamespace().getId());
				termSt.setInt(++pindex, subscriptionsDTO.getSubscriptionType() != null ? subscriptionsDTO.getSubscriptionType().getId() : 0);
				termSt.setString(++pindex, subscriptionsDTO.getUsers());
				termSt.setString(++pindex, subscriptionsDTO.getGroups());
				termSt.setString(++pindex, subscriptionsDTO.getNotificationMediums());
				termSt.setInt(++pindex, subscriptionsDTO.getActiveFlag());
				termSt.setInt(++pindex, authDTO.getUser().getId());
				termSt.setInt(++pindex, 0);
				termSt.registerOutParameter(++pindex, Types.INTEGER);
				termSt.execute();
				termSt.clearParameters();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return null;
	}

	public List<NotificationSubscriptionDTO> getAllAlertSubscriptions(AuthDTO authDTO) {
		List<NotificationSubscriptionDTO> subscriptionList = new ArrayList<NotificationSubscriptionDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code, subscription_type_id, notification_medium, user_id, group_id, active_flag FROM notification_subscription WHERE namespace_id = ? AND active_flag < 2");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				NotificationSubscriptionDTO alertsubscriptions = new NotificationSubscriptionDTO();
				alertsubscriptions.setCode(selectRS.getString("code"));
				alertsubscriptions.setSubscriptionType(NotificationSubscriptionTypeEM.getSubscriptionTypeEM(selectRS.getInt("subscription_type_id")));

				String notificationMediumCodes = selectRS.getString("notification_medium");
				List<NotificationMediumEM> notificationMediums = convertNotificationMediumList(notificationMediumCodes);
				alertsubscriptions.setNotificationMediumList(notificationMediums);

				String userIds = selectRS.getString("user_id");
				List<UserDTO> userList = convertUserList(userIds);
				alertsubscriptions.setUserList(userList);

				String groupIds = selectRS.getString("group_id");
				List<GroupDTO> groupList = convertGroupList(groupIds);
				alertsubscriptions.setGroupList(groupList);

				alertsubscriptions.setActiveFlag(selectRS.getInt("active_flag"));
				subscriptionList.add(alertsubscriptions);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return subscriptionList;
	}

	public List<NotificationSubscriptionDTO> getSubscriptionsByType(AuthDTO authDTO, NotificationSubscriptionTypeEM subscriptionType) {
		List<NotificationSubscriptionDTO> subscriptionList = new ArrayList<NotificationSubscriptionDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code, subscription_type_id, notification_medium, user_id, group_id, active_flag FROM notification_subscription WHERE namespace_id = ? AND subscription_type_id = ? AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, subscriptionType.getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				NotificationSubscriptionDTO alertsubscriptions = new NotificationSubscriptionDTO();
				alertsubscriptions.setCode(selectRS.getString("code"));
				alertsubscriptions.setSubscriptionType(NotificationSubscriptionTypeEM.getSubscriptionTypeEM(selectRS.getInt("subscription_type_id")));

				String notificationMediumCodes = selectRS.getString("notification_medium");
				List<NotificationMediumEM> notificationMediums = convertNotificationMediumList(notificationMediumCodes);
				alertsubscriptions.setNotificationMediumList(notificationMediums);

				String userIds = selectRS.getString("user_id");
				List<UserDTO> userList = convertUserList(userIds);
				alertsubscriptions.setUserList(userList);

				String groupIds = selectRS.getString("group_id");
				List<GroupDTO> groupList = convertGroupList(groupIds);
				alertsubscriptions.setGroupList(groupList);

				alertsubscriptions.setActiveFlag(selectRS.getInt("active_flag"));
				subscriptionList.add(alertsubscriptions);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return subscriptionList;
	}

	public Map<String, NotificationSubscriptionDTO> getUserSubscriptions(AuthDTO authDTO) {
		Map<String, NotificationSubscriptionDTO> subscriptionMap = Maps.newHashMap();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT subscription_type_id, active_flag FROM notification_subscription WHERE namespace_id = ? AND FIND_IN_SET (?, user_id) AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, authDTO.getUser().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				NotificationSubscriptionTypeEM subscriptionType = NotificationSubscriptionTypeEM.getSubscriptionTypeEM(selectRS.getInt("subscription_type_id"));

				NotificationSubscriptionDTO alertsubscriptions = new NotificationSubscriptionDTO();
				alertsubscriptions.setSubscriptionType(subscriptionType);
				alertsubscriptions.setActiveFlag(selectRS.getInt("active_flag"));

				if (subscriptionType.getLevel() == Numeric.ONE_INT) {
					subscriptionMap.put(subscriptionType.getCode(), alertsubscriptions);
				}
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return subscriptionMap;
	}

	private List<NotificationMediumEM> convertNotificationMediumList(String notificationMediumCodes) {
		List<NotificationMediumEM> notificationMediumList = new ArrayList<NotificationMediumEM>();
		if (StringUtil.isNotNull(notificationMediumCodes)) {
			for (String id : notificationMediumCodes.split(Text.COMMA)) {
				if (id.equals("0")) {
					continue;
				}
				notificationMediumList.add(NotificationMediumEM.getNotificationMediumEM(Integer.valueOf(id)));
			}
		}
		return notificationMediumList;
	}

	private List<UserDTO> convertUserList(String userIds) {
		List<UserDTO> userList = new ArrayList<UserDTO>();
		if (StringUtil.isNotNull(userIds)) {
			for (String id : userIds.split(Text.COMMA)) {
				UserDTO userDTO = new UserDTO();
				userDTO.setId(Integer.valueOf(id));
				userList.add(userDTO);
			}
		}
		return userList;
	}

	private List<GroupDTO> convertGroupList(String groupIds) {
		List<GroupDTO> groupList = new ArrayList<GroupDTO>();
		if (StringUtil.isNotNull(groupIds)) {
			for (String id : groupIds.split(Text.COMMA)) {
				GroupDTO groupDTO = new GroupDTO();
				groupDTO.setId(Integer.valueOf(id));
				groupList.add(groupDTO);
			}
		}
		return groupList;
	}

}
