package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.in.com.config.ApplicationConfig;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.NamespaceDTO;
import org.in.com.dto.NotificationConfigDTO;
import org.in.com.dto.NotificationTemplateConfigDTO;
import org.in.com.dto.enumeration.NotificationMediumEM;
import org.in.com.dto.enumeration.NotificationTypeEM;
import org.in.com.exception.ServiceException;
import org.in.com.utils.StringUtil;

import lombok.Cleanup;

public class NotificationConfigDAO {

	public List<NotificationConfigDTO> getAllNotificationConfig(AuthDTO authDTO, NamespaceDTO namespace) {
		List<NotificationConfigDTO> list = new ArrayList<NotificationConfigDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code, entity_code, header_dlt_code, header, notification_mode_id, active_flag FROM notification_config WHERE namespace_id = ? AND active_flag < 2");
			selectPS.setInt(1, namespace.getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				NotificationConfigDTO smsConfig = new NotificationConfigDTO();
				smsConfig.setCode(selectRS.getString("code"));
				smsConfig.setEntityCode(selectRS.getString("entity_code"));
				smsConfig.setHeaderDltCode(selectRS.getString("header_dlt_code"));
				smsConfig.setHeader(selectRS.getString("header"));
				smsConfig.setNotificationMode(NotificationMediumEM.getNotificationMediumEM(selectRS.getInt("notification_mode_id")));
				smsConfig.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(smsConfig);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public NotificationConfigDTO getNotificationConfig(AuthDTO authDTO, NamespaceDTO namespace, NotificationConfigDTO smsConfig) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code, entity_code, header_dlt_code, header, notification_mode_id, active_flag FROM notification_config WHERE namespace_id = ? AND code = ? AND active_flag = 1");
			selectPS.setInt(1, namespace.getId());
			selectPS.setString(2, smsConfig.getCode());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				smsConfig.setCode(selectRS.getString("code"));
				smsConfig.setEntityCode(selectRS.getString("entity_code"));
				smsConfig.setHeaderDltCode(selectRS.getString("header_dlt_code"));
				smsConfig.setHeader(selectRS.getString("header"));
				smsConfig.setNotificationMode(NotificationMediumEM.getNotificationMediumEM(selectRS.getInt("notification_mode_id")));
				smsConfig.setActiveFlag(selectRS.getInt("active_flag"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return smsConfig;
	}

	public NotificationConfigDTO updateNotificationConfig(AuthDTO authDTO, NamespaceDTO namespace, NotificationConfigDTO smsConfigDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_NOTIFICATION_CONFIG_IUD(?,?,?,?,?, ?,?,?,?,?)}");
			callableStatement.setString(++pindex, smsConfigDTO.getCode());
			callableStatement.setInt(++pindex, namespace.getId());
			callableStatement.setString(++pindex, smsConfigDTO.getEntityCode());
			callableStatement.setString(++pindex, smsConfigDTO.getHeaderDltCode());
			callableStatement.setString(++pindex, smsConfigDTO.getHeader());
			callableStatement.setInt(++pindex, smsConfigDTO.getNotificationMode().getId());
			callableStatement.setInt(++pindex, smsConfigDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();

			if (callableStatement.getInt("pitRowCount") > 0) {
				smsConfigDTO.setCode(callableStatement.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return smsConfigDTO;
	}

	public List<NotificationTemplateConfigDTO> getAllNotificationTemplateConfig(AuthDTO authDTO, NamespaceDTO namespace, NotificationConfigDTO smsConfig) {
		List<NotificationTemplateConfigDTO> list = new ArrayList<NotificationTemplateConfigDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			if (StringUtil.isNotNull(smsConfig.getCode())) {
				selectPS = connection.prepareStatement("SELECT tc.code, tc.name, notification_sms_config_id, template_dlt_code, notification_type, content, sc.code, entity_code, header_dlt_code, header, notification_mode_id, tc.active_flag FROM notification_template_config tc, notification_config sc WHERE tc.namespace_id = ? AND tc.namespace_id = sc.namespace_id AND sc.code = ? AND tc.notification_sms_config_id = sc.id AND tc.active_flag = 1 AND sc.active_flag = 1");
				selectPS.setInt(1, namespace.getId());
				selectPS.setString(2, smsConfig.getCode());
			}
			else if (StringUtil.isNotNull(smsConfig.getHeader())) {
				selectPS = connection.prepareStatement("SELECT tc.code, tc.name, notification_sms_config_id, template_dlt_code, notification_type, content, sc.code, entity_code, header_dlt_code, header, notification_mode_id, tc.active_flag FROM notification_template_config tc, notification_config sc WHERE tc.namespace_id = ? AND tc.namespace_id = sc.namespace_id AND sc.header = ? AND tc.notification_sms_config_id = sc.id AND tc.active_flag = 1 AND sc.active_flag = 1");
				selectPS.setInt(1, namespace.getId());
				selectPS.setString(2, smsConfig.getHeader());
			}
			if (selectPS != null) {
				@Cleanup
				ResultSet selectRS = selectPS.executeQuery();
				while (selectRS.next()) {
					NotificationTemplateConfigDTO smsTemplateConfig = new NotificationTemplateConfigDTO();
					smsTemplateConfig.setCode(selectRS.getString("tc.code"));
					smsTemplateConfig.setName(selectRS.getString("tc.name"));
					smsTemplateConfig.setTemplateDltCode(selectRS.getString("template_dlt_code"));
					smsTemplateConfig.setNotificationType(NotificationTypeEM.getNotificationTypeEM(selectRS.getInt("notification_type")));
					smsTemplateConfig.setContent(selectRS.getString("content"));

					NotificationConfigDTO notificationSMSConfig = new NotificationConfigDTO();
					notificationSMSConfig.setCode(selectRS.getString("sc.code"));
					notificationSMSConfig.setEntityCode(selectRS.getString("entity_code"));
					notificationSMSConfig.setHeaderDltCode(selectRS.getString("header_dlt_code"));
					notificationSMSConfig.setHeader(selectRS.getString("header"));
					smsConfig.setNotificationMode(NotificationMediumEM.getNotificationMediumEM(selectRS.getInt("notification_mode_id")));
					smsTemplateConfig.setNotificationSMSConfig(notificationSMSConfig);

					smsTemplateConfig.setActiveFlag(selectRS.getInt("tc.active_flag"));
					list.add(smsTemplateConfig);
				}
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public NotificationTemplateConfigDTO getNotificationTemplateConfig(AuthDTO authDTO, NamespaceDTO namespace, NotificationTemplateConfigDTO smsTemplateConfig) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			if (smsTemplateConfig.getId() != 0) {
				selectPS = connection.prepareStatement("SELECT tc.id, tc.code, tc.name, notification_sms_config_id, template_dlt_code, notification_type, content, sc.code, entity_code, header_dlt_code, header, notification_mode_id, tc.active_flag FROM notification_template_config tc, notification_config sc WHERE tc.namespace_id = ? AND tc.namespace_id = sc.namespace_id AND tc.id = ? AND tc.notification_sms_config_id = sc.id AND tc.active_flag = 1 AND sc.active_flag = 1");
				selectPS.setInt(1, namespace.getId());
				selectPS.setInt(2, smsTemplateConfig.getId());
			}
			else if (StringUtil.isNotNull(smsTemplateConfig.getCode())) {
				selectPS = connection.prepareStatement("SELECT tc.id, tc.code, tc.name, notification_sms_config_id, template_dlt_code, notification_type, content, sc.code, entity_code, header_dlt_code, header, notification_mode_id, tc.active_flag FROM notification_template_config tc, notification_config sc WHERE tc.namespace_id = ? AND tc.namespace_id = sc.namespace_id AND tc.code = ? AND tc.notification_sms_config_id = sc.id AND tc.active_flag = 1 AND sc.active_flag = 1");
				selectPS.setInt(1, namespace.getId());
				selectPS.setString(2, smsTemplateConfig.getCode());
			}

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				smsTemplateConfig.setId(selectRS.getInt("tc.id"));
				smsTemplateConfig.setCode(selectRS.getString("tc.code"));
				smsTemplateConfig.setName(selectRS.getString("tc.name"));
				smsTemplateConfig.setTemplateDltCode(selectRS.getString("template_dlt_code"));
				smsTemplateConfig.setNotificationType(NotificationTypeEM.getNotificationTypeEM(selectRS.getInt("notification_type")));
				smsTemplateConfig.setContent(selectRS.getString("content"));

				NotificationConfigDTO notificationSMSConfig = new NotificationConfigDTO();
				notificationSMSConfig.setCode(selectRS.getString("sc.code"));
				notificationSMSConfig.setEntityCode(selectRS.getString("entity_code"));
				notificationSMSConfig.setHeaderDltCode(selectRS.getString("header_dlt_code"));
				notificationSMSConfig.setHeader(selectRS.getString("header"));
				notificationSMSConfig.setNotificationMode(NotificationMediumEM.getNotificationMediumEM(selectRS.getInt("notification_mode_id")));
				smsTemplateConfig.setNotificationSMSConfig(notificationSMSConfig);

				smsTemplateConfig.setActiveFlag(selectRS.getInt("active_flag"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return smsTemplateConfig;
	}

	public NotificationTemplateConfigDTO getNotificationTemplateConfig(AuthDTO authDTO, NotificationTypeEM notificationType, NotificationMediumEM notificationMode) {
		NotificationTemplateConfigDTO smsTemplateConfig = new NotificationTemplateConfigDTO();
		try {

			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT tc.id, tc.code, tc.name, notification_sms_config_id, template_dlt_code,notification_type, content, sc.code, entity_code, header_dlt_code, header, notification_mode_id, tc.active_flag FROM notification_template_config tc, notification_config sc WHERE tc.namespace_id = ? AND tc.namespace_id = sc.namespace_id AND tc.notification_type = ? AND notification_mode_id = ? AND tc.notification_sms_config_id = sc.id AND tc.active_flag = 1 AND sc.active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, notificationType.getId());
			selectPS.setInt(3, notificationMode.getId());

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				smsTemplateConfig.setId(selectRS.getInt("tc.id"));
				smsTemplateConfig.setCode(selectRS.getString("tc.code"));
				smsTemplateConfig.setName(selectRS.getString("tc.name"));
				smsTemplateConfig.setTemplateDltCode(selectRS.getString("template_dlt_code"));
				smsTemplateConfig.setNotificationType(NotificationTypeEM.getNotificationTypeEM(selectRS.getInt("notification_type")));
				smsTemplateConfig.setContent(selectRS.getString("content"));

				NotificationConfigDTO notificationSMSConfig = new NotificationConfigDTO();
				notificationSMSConfig.setCode(selectRS.getString("sc.code"));
				notificationSMSConfig.setEntityCode(selectRS.getString("entity_code"));
				notificationSMSConfig.setHeaderDltCode(selectRS.getString("header_dlt_code"));
				notificationSMSConfig.setHeader(selectRS.getString("header"));
				notificationSMSConfig.setNotificationMode(NotificationMediumEM.getNotificationMediumEM(selectRS.getInt("notification_mode_id")));
				smsTemplateConfig.setNotificationSMSConfig(notificationSMSConfig);
				smsTemplateConfig.setActiveFlag(selectRS.getInt("active_flag"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return smsTemplateConfig;
	}

	public NotificationTemplateConfigDTO getNotificationTemplateConfigDefault(NotificationTypeEM notificationType, NotificationMediumEM notificationMode) {
		NotificationTemplateConfigDTO smsTemplateConfig = new NotificationTemplateConfigDTO();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT tc.id, tc.code, tc.name, notification_sms_config_id, template_dlt_code,notification_type, content, sc.code, entity_code, header_dlt_code, header, notification_mode_id, tc.active_flag FROM notification_template_config tc, notification_config sc,namespace ns WHERE ns.code = ? AND tc.namespace_id = ns.id AND tc.namespace_id = sc.namespace_id AND tc.notification_type = ? AND notification_mode_id = ? AND tc.notification_sms_config_id = sc.id AND tc.active_flag = 1 AND sc.active_flag = 1");
			selectPS.setString(1, ApplicationConfig.getServerZoneCode());
			selectPS.setInt(2, notificationType.getId());
			selectPS.setInt(3, notificationMode.getId());

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				smsTemplateConfig.setId(selectRS.getInt("tc.id"));
				smsTemplateConfig.setCode(selectRS.getString("tc.code"));
				smsTemplateConfig.setName(selectRS.getString("tc.name"));
				smsTemplateConfig.setTemplateDltCode(selectRS.getString("template_dlt_code"));
				smsTemplateConfig.setNotificationType(NotificationTypeEM.getNotificationTypeEM(selectRS.getInt("notification_type")));
				smsTemplateConfig.setContent(selectRS.getString("content"));

				NotificationConfigDTO notificationSMSConfig = new NotificationConfigDTO();
				notificationSMSConfig.setCode(selectRS.getString("sc.code"));
				notificationSMSConfig.setEntityCode(selectRS.getString("entity_code"));
				notificationSMSConfig.setHeaderDltCode(selectRS.getString("header_dlt_code"));
				notificationSMSConfig.setHeader(selectRS.getString("header"));
				notificationSMSConfig.setNotificationMode(NotificationMediumEM.getNotificationMediumEM(selectRS.getInt("notification_mode_id")));
				smsTemplateConfig.setNotificationSMSConfig(notificationSMSConfig);
				smsTemplateConfig.setActiveFlag(selectRS.getInt("active_flag"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return smsTemplateConfig;
	}
	
	public List<NotificationTemplateConfigDTO> getNotificationTemplateConfigList(AuthDTO authDTO, NotificationTypeEM notificationType) {
		List<NotificationTemplateConfigDTO> list = new ArrayList<>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT tc.code, tc.name, notification_sms_config_id, template_dlt_code,notification_type, content, sc.code, entity_code, header_dlt_code, header, notification_mode_id, tc.active_flag FROM notification_template_config tc, notification_config sc WHERE tc.namespace_id = ? AND tc.namespace_id = sc.namespace_id AND tc.notification_type = ? AND tc.notification_sms_config_id = sc.id AND tc.active_flag = 1 AND sc.active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, notificationType.getId());

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				NotificationTemplateConfigDTO smsTemplateConfig = new NotificationTemplateConfigDTO();
				smsTemplateConfig.setCode(selectRS.getString("tc.code"));
				smsTemplateConfig.setName(selectRS.getString("tc.name"));
				smsTemplateConfig.setTemplateDltCode(selectRS.getString("template_dlt_code"));
				smsTemplateConfig.setNotificationType(NotificationTypeEM.getNotificationTypeEM(selectRS.getInt("notification_type")));
				smsTemplateConfig.setContent(selectRS.getString("content"));

				NotificationConfigDTO notificationSMSConfig = new NotificationConfigDTO();
				notificationSMSConfig.setCode(selectRS.getString("sc.code"));
				notificationSMSConfig.setEntityCode(selectRS.getString("entity_code"));
				notificationSMSConfig.setHeaderDltCode(selectRS.getString("header_dlt_code"));
				notificationSMSConfig.setHeader(selectRS.getString("header"));
				notificationSMSConfig.setNotificationMode(NotificationMediumEM.getNotificationMediumEM(selectRS.getInt("notification_mode_id")));
				smsTemplateConfig.setNotificationSMSConfig(notificationSMSConfig);
				smsTemplateConfig.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(smsTemplateConfig);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}
	
	public List<NotificationTemplateConfigDTO> getNotificationTemplateConfigListDefault(NotificationTypeEM notificationType) {
		List<NotificationTemplateConfigDTO> list = new ArrayList<>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT tc.code, tc.name, notification_sms_config_id, template_dlt_code,notification_type, content, sc.code, entity_code, header_dlt_code, header, notification_mode_id, tc.active_flag FROM notification_template_config tc, notification_config sc,namespace ns WHERE ns.code = ? AND tc.namespace_id = ns.id AND tc.namespace_id = sc.namespace_id AND tc.notification_type = ? AND tc.notification_sms_config_id = sc.id AND tc.active_flag = 1 AND sc.active_flag = 1");
			selectPS.setString(1, ApplicationConfig.getServerZoneCode());
			selectPS.setInt(2, notificationType.getId());

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				NotificationTemplateConfigDTO smsTemplateConfig = new NotificationTemplateConfigDTO();
				smsTemplateConfig.setCode(selectRS.getString("tc.code"));
				smsTemplateConfig.setName(selectRS.getString("tc.name"));
				smsTemplateConfig.setTemplateDltCode(selectRS.getString("template_dlt_code"));
				smsTemplateConfig.setNotificationType(NotificationTypeEM.getNotificationTypeEM(selectRS.getInt("notification_type")));
				smsTemplateConfig.setContent(selectRS.getString("content"));

				NotificationConfigDTO notificationSMSConfig = new NotificationConfigDTO();
				notificationSMSConfig.setCode(selectRS.getString("sc.code"));
				notificationSMSConfig.setEntityCode(selectRS.getString("entity_code"));
				notificationSMSConfig.setHeaderDltCode(selectRS.getString("header_dlt_code"));
				notificationSMSConfig.setHeader(selectRS.getString("header"));
				notificationSMSConfig.setNotificationMode(NotificationMediumEM.getNotificationMediumEM(selectRS.getInt("notification_mode_id")));
				smsTemplateConfig.setNotificationSMSConfig(notificationSMSConfig);
				smsTemplateConfig.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(smsTemplateConfig);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public NotificationTemplateConfigDTO updateNotificationTemplateConfig(AuthDTO authDTO, NamespaceDTO namespace, NotificationTemplateConfigDTO smsTemplateConfig) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_NOTIFICATION_TEMPLATE_CONFIG_IUD( ?,?,?,?,?, ?,?,?,?,?, ?)}");
			callableStatement.setString(++pindex, smsTemplateConfig.getCode());
			callableStatement.setInt(++pindex, namespace.getId());
			callableStatement.setString(++pindex, smsTemplateConfig.getName());
			callableStatement.setString(++pindex, smsTemplateConfig.getNotificationSMSConfig().getCode());
			callableStatement.setString(++pindex, smsTemplateConfig.getTemplateDltCode());
			callableStatement.setInt(++pindex, smsTemplateConfig.getNotificationType() != null ? smsTemplateConfig.getNotificationType().getId() : 0);
			callableStatement.setString(++pindex, smsTemplateConfig.getContent());
			callableStatement.setInt(++pindex, smsTemplateConfig.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();

			if (callableStatement.getInt("pitRowCount") > 0) {
				smsTemplateConfig.setCode(callableStatement.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return smsTemplateConfig;
	}
}
