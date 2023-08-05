package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Cleanup;

import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.MenuDTO;
import org.in.com.dto.MenuEventDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.ProductTypeEM;
import org.in.com.dto.enumeration.SeverityPermissionTypeEM;
import org.in.com.exception.ServiceException;
import org.in.com.utils.StringUtil;

public class MenuDAO {
	public List<MenuDTO> getAllMenu(AuthDTO authDTO) {
		List<MenuDTO> list = new ArrayList<MenuDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectEventPS = connection.prepareStatement("SELECT id,code,name,operation_code,attr1_value,action_type,severity,active_flag FROM menu_events where menu_id = ? AND active_flag = 1");
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement(" SELECT id,code,name,link,action_code,tag,lookup_id,severity,display_flag,product_type_id,active_flag FROM menu WHERE active_flag < 2");
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				MenuDTO dto = new MenuDTO();
				dto.setId(selectRS.getInt("id"));
				dto.setCode(selectRS.getString("code"));
				dto.setName(selectRS.getString("name"));
				dto.setLink(selectRS.getString("link"));
				dto.setActionCode(selectRS.getString("action_code"));
				List<String> tagList = new ArrayList<>();
				String tag = StringUtil.isNull(selectRS.getString("tag"), Text.EMPTY);
				String tagArr[] = tag.split(",");
				for (String str : tagArr) {
					tagList.add(str);
				}
				dto.setTagList(tagList);
				dto.setSeverity(SeverityPermissionTypeEM.getSeverityPermissionTypeEM(selectRS.getInt("severity")));
				dto.setDisplayFlag(selectRS.getInt("display_flag"));
				dto.setProductType(ProductTypeEM.getProductTypeEM(selectRS.getInt("product_type_id")));
				MenuDTO lookupdto = new MenuDTO();
				lookupdto.setId(selectRS.getInt("lookup_id"));
				dto.setLookup(lookupdto);
				dto.setActiveFlag(selectRS.getInt("active_flag"));

				selectEventPS.setInt(1, dto.getId());
				@Cleanup
				ResultSet selectEventRS = selectEventPS.executeQuery();
				List<MenuEventDTO> eventDTOList = new ArrayList<MenuEventDTO>();
				while (selectEventRS.next()) {
					MenuEventDTO eventDTO = new MenuEventDTO();
					eventDTO.setId(selectEventRS.getInt("id"));
					eventDTO.setCode(selectEventRS.getString("code"));
					eventDTO.setName(selectEventRS.getString("name"));
					eventDTO.setOperationCode(selectEventRS.getString("operation_code"));
					eventDTO.setAttr1Value(selectEventRS.getString("attr1_value"));
					eventDTO.setPermissionFlag(selectEventRS.getInt("action_type"));
					eventDTO.setSeverity(SeverityPermissionTypeEM.getSeverityPermissionTypeEM(selectEventRS.getInt("severity")));
					eventDTO.setActiveFlag(selectEventRS.getInt("active_flag"));
					eventDTOList.add(eventDTO);
				}
				selectEventPS.clearParameters();
				MenuEventDTO eventDTO = new MenuEventDTO();
				eventDTO.setList(eventDTOList);
				dto.setMenuEvent(eventDTO);
				list.add(dto);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public List<MenuDTO> getRoleMenu(AuthDTO authDTO, MenuDTO dto, String roleCode) {
		List<MenuDTO> list = new ArrayList<MenuDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			if ("UR".equals(roleCode) && dto.getUser() != null && dto.getUser().getId() != 0) {
				@Cleanup
				PreparedStatement selectPS = connection.prepareStatement("SELECT menu_id,refference_id, active_menu_event_codes, menu_exception_flag,active_flag FROM user_menu um WHERE um.namespace_id = ? AND um.refference_id = ? AND refference_type = ? AND um.active_flag = 1");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setInt(2, dto.getUser().getId());
				selectPS.setString(3, roleCode);
				@Cleanup
				ResultSet selectRS = selectPS.executeQuery();
				while (selectRS.next()) {
					MenuDTO menuDTO = new MenuDTO();
					menuDTO.setId(selectRS.getInt("menu_id"));
					String activeEventCodesPlus = selectRS.getString("active_menu_event_codes");
					String activeCodes[] = activeEventCodesPlus.split(",");
					menuDTO.setUser(dto.getUser());
					List<MenuEventDTO> menuEventList = new ArrayList<MenuEventDTO>();
					for (String code : activeCodes) {
						if (StringUtil.isNotNull(code)) {
							MenuEventDTO menuEventDTO = new MenuEventDTO();
							menuEventDTO.setCode(code);
							menuEventList.add(menuEventDTO);
						}
					}
					MenuEventDTO menuEventDTO = new MenuEventDTO();
					menuEventDTO.setList(menuEventList);
					menuDTO.setMenuEvent(menuEventDTO);
					menuDTO.setExceptionFlag(selectRS.getInt("menu_exception_flag"));
					list.add(menuDTO);
				}
			}
			else if ("GR".equals(roleCode) && dto.getGroup() != null && dto.getGroup().getId() != 0) {
				@Cleanup
				PreparedStatement selectPS = connection.prepareStatement("  SELECT menu_id,refference_id, active_menu_event_codes, menu_exception_flag,active_flag FROM user_menu um WHERE um.namespace_id = ? AND refference_id = ? AND refference_type = ? AND um.active_flag  = 1");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setInt(2, dto.getGroup().getId());
				selectPS.setString(3, roleCode);
				@Cleanup
				ResultSet selectRS = selectPS.executeQuery();
				while (selectRS.next()) {
					MenuDTO menuDTO = new MenuDTO();
					menuDTO.setId(selectRS.getInt("menu_id"));
					String activeEventCodesPlus = selectRS.getString("active_menu_event_codes");
					String activeCodes[] = activeEventCodesPlus.split(",");
					menuDTO.setGroup(dto.getGroup());
					List<MenuEventDTO> menuEventList = new ArrayList<MenuEventDTO>();
					for (String code : activeCodes) {
						if (StringUtil.isNotNull(code)) {
							MenuEventDTO menuEventDTO = new MenuEventDTO();
							menuEventDTO.setCode(code);
							menuEventList.add(menuEventDTO);
						}
					}
					MenuEventDTO menuEventDTO = new MenuEventDTO();
					menuEventDTO.setList(menuEventList);
					menuDTO.setMenuEvent(menuEventDTO);
					menuDTO.setExceptionFlag(selectRS.getInt("menu_exception_flag"));
					list.add(menuDTO);
				}
			}
			else if ("NS".equals(roleCode) && dto.getGroup() == null && dto.getUser() == null) {
				@Cleanup
				PreparedStatement selectPS = connection.prepareStatement("  SELECT menu_id,refference_id,active_menu_event_codes, menu_exception_flag,active_flag FROM user_menu um WHERE um.namespace_id = ? AND refference_id = ? AND refference_type = ? AND um.active_flag = 1");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setInt(2, authDTO.getNamespace().getId());
				selectPS.setString(3, roleCode);
				@Cleanup
				ResultSet selectRS = selectPS.executeQuery();
				while (selectRS.next()) {
					MenuDTO menuDTO = new MenuDTO();
					menuDTO.setId(selectRS.getInt("menu_id"));
					String activeEventCodesPlus = selectRS.getString("active_menu_event_codes");
					String activeCodes[] = activeEventCodesPlus.split(",");
					List<MenuEventDTO> menuEventList = new ArrayList<MenuEventDTO>();
					for (String code : activeCodes) {
						if (StringUtil.isNotNull(code)) {
							MenuEventDTO menuEventDTO = new MenuEventDTO();
							menuEventDTO.setCode(code);
							menuEventList.add(menuEventDTO);
						}
					}

					MenuEventDTO menuEventDTO = new MenuEventDTO();
					menuEventDTO.setList(menuEventList);
					menuDTO.setMenuEvent(menuEventDTO);
					menuDTO.setExceptionFlag(selectRS.getInt("menu_exception_flag"));
					list.add(menuDTO);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public void MenuUID(AuthDTO authDTO, MenuDTO dto) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			StringBuilder tagBuilder = new StringBuilder();
			for (String tag : dto.getTagList()) {
				tagBuilder.append(tag);
				tagBuilder.append(",");
			}
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{call  EZEE_SP_MENU_IUD(?,?,?,?,? ,?,?,?,?,? ,?,?,?)}");
			callableStatement.setString(++pindex, dto.getCode());
			callableStatement.setString(++pindex, dto.getName());
			callableStatement.setString(++pindex, dto.getLink());
			callableStatement.setString(++pindex, dto.getActionCode());
			callableStatement.setString(++pindex, tagBuilder.toString());
			callableStatement.setInt(++pindex, dto.getLookup() != null ? dto.getLookup().getId() : 0);
			callableStatement.setInt(++pindex, dto.getSeverity().getId());
			callableStatement.setInt(++pindex, dto.getDisplayFlag());
			callableStatement.setInt(++pindex, dto.getProductType().getId());
			callableStatement.setInt(++pindex, dto.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				dto.setCode(callableStatement.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}

	}

	public MenuDTO getMenuEventsUID(AuthDTO authDTO, MenuDTO dto) {
		try {
			if (dto.getMenuEvent() != null && dto.getMenuEvent().getList() != null && !dto.getMenuEvent().getList().isEmpty()) {
				@Cleanup
				Connection connection = ConnectDAO.getConnection();
				@Cleanup
				CallableStatement callableStatement = connection.prepareCall("{call  EZEE_SP_MENU_EVENTS_IUD(?,?,?,?,? ,?,?,?,?,? ,?)}");
				for (MenuEventDTO eventDTO : dto.getMenuEvent().getList()) {
					int pindex = 0;
					if (StringUtil.isNull(eventDTO.getName())) {
						continue;
					}
					callableStatement.setString(++pindex, eventDTO.getCode());
					callableStatement.setString(++pindex, eventDTO.getName());
					callableStatement.setString(++pindex, dto.getCode());
					callableStatement.setString(++pindex, eventDTO.getOperationCode());
					callableStatement.setString(++pindex, eventDTO.getAttr1Value());
					callableStatement.setInt(++pindex, eventDTO.getPermissionFlag());
					callableStatement.setInt(++pindex, eventDTO.getSeverity().getId());
					callableStatement.setInt(++pindex, eventDTO.getActiveFlag());
					callableStatement.setInt(++pindex, authDTO.getUser().getId());
					callableStatement.setInt(++pindex, 0);
					callableStatement.registerOutParameter(++pindex, Types.INTEGER);
					callableStatement.execute();
					if (callableStatement.getInt("pitRowCount") > 0) {
						eventDTO.setCode(callableStatement.getString("pcrCode"));
					}
					callableStatement.clearParameters();
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}

		return dto;
	}

	public static synchronized MenuDTO getUserMenuUID(AuthDTO authDTO, MenuDTO dto) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			StringBuilder activeEventCodes = new StringBuilder();
			for (MenuEventDTO eventDTO : dto.getMenuEvent().getList()) {
				activeEventCodes.append(",");
				activeEventCodes.append(eventDTO.getCode());
			}
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{call  EZEE_SP_USER_MENU_IUD(?,?,?,?,? ,?,?,?,?,? ,?)}");
			callableStatement.setString(++pindex, null);
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setInt(++pindex, dto.getId());
			callableStatement.setString(++pindex, dto.getGroup() != null ? dto.getGroup().getCode() : dto.getUser() != null ? dto.getUser().getCode() : authDTO.getNamespace().getCode());
			callableStatement.setString(++pindex, dto.getGroup() != null ? "GR" : dto.getUser() != null ? "UR" : "NS");
			callableStatement.setString(++pindex, activeEventCodes.toString());
			callableStatement.setInt(++pindex, dto.getExceptionFlag());
			callableStatement.setInt(++pindex, dto.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				dto.setCode(callableStatement.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}

		return dto;
	}

	public List<MenuDTO> getNamespacePrivileges(AuthDTO authDTO) {
		List<MenuDTO> list = new ArrayList<MenuDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT menu_id,refference_id,refference_type, active_menu_event_codes, menu_exception_flag,active_flag FROM user_menu um WHERE um.namespace_id = ? AND refference_id = ? AND refference_type = 'NS' AND um.active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				MenuDTO roleMenuDTO = new MenuDTO();
				roleMenuDTO.setId(selectRS.getInt("menu_id"));
				String activeEventCodesPlus = selectRS.getString("active_menu_event_codes");
				String activeCodes[] = activeEventCodesPlus.split(",");

				List<MenuEventDTO> menuEventList = new ArrayList<MenuEventDTO>();
				for (String code : activeCodes) {
					if (StringUtil.isNotNull(code)) {
						MenuEventDTO menuEventDTO = new MenuEventDTO();
						menuEventDTO.setCode(code);
						menuEventList.add(menuEventDTO);
					}
				}
				MenuEventDTO menuEventDTO = new MenuEventDTO();
				menuEventDTO.setList(menuEventList);
				roleMenuDTO.setMenuEvent(menuEventDTO);
				roleMenuDTO.setExceptionFlag(selectRS.getInt("menu_exception_flag"));
				list.add(roleMenuDTO);
			}

		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public List<MenuDTO> getUserPrivileges(AuthDTO authDTO, UserDTO userDTO) {
		List<MenuDTO> list = new ArrayList<MenuDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT menu_id,refference_id,refference_type, active_menu_event_codes, menu_exception_flag,active_flag FROM user_menu um WHERE um.namespace_id = ? AND refference_id = ? AND refference_type = 'UR' AND um.active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, userDTO.getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				MenuDTO roleMenuDTO = new MenuDTO();
				roleMenuDTO.setId(selectRS.getInt("menu_id"));
				String activeEventCodesPlus = selectRS.getString("active_menu_event_codes");
				String activeCodes[] = activeEventCodesPlus.split(",");

				List<MenuEventDTO> menuEventList = new ArrayList<MenuEventDTO>();
				for (String code : activeCodes) {
					if (StringUtil.isNotNull(code)) {
						MenuEventDTO menuEventDTO = new MenuEventDTO();
						menuEventDTO.setCode(code);
						menuEventList.add(menuEventDTO);
					}
				}
				MenuEventDTO menuEventDTO = new MenuEventDTO();
				menuEventDTO.setList(menuEventList);
				roleMenuDTO.setMenuEvent(menuEventDTO);
				roleMenuDTO.setExceptionFlag(selectRS.getInt("menu_exception_flag"));
				list.add(roleMenuDTO);
			}

		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public List<MenuDTO> getGroupPrivileges(AuthDTO authDTO, GroupDTO groupDTO) {
		List<MenuDTO> list = new ArrayList<MenuDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT menu_id,refference_id,refference_type, active_menu_event_codes, menu_exception_flag,active_flag FROM user_menu um WHERE um.namespace_id = ? AND refference_id = ? AND refference_type = 'GR' AND um.active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, groupDTO.getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				MenuDTO roleMenuDTO = new MenuDTO();
				roleMenuDTO.setId(selectRS.getInt("menu_id"));
				String activeEventCodesPlus = selectRS.getString("active_menu_event_codes");
				String activeCodes[] = activeEventCodesPlus.split(",");

				List<MenuEventDTO> menuEventList = new ArrayList<MenuEventDTO>();
				for (String code : activeCodes) {
					if (StringUtil.isNotNull(code)) {
						MenuEventDTO menuEventDTO = new MenuEventDTO();
						menuEventDTO.setCode(code);
						menuEventList.add(menuEventDTO);
					}
				}

				MenuEventDTO menuEventDTO = new MenuEventDTO();
				menuEventDTO.setList(menuEventList);
				roleMenuDTO.setMenuEvent(menuEventDTO);
				roleMenuDTO.setExceptionFlag(selectRS.getInt("menu_exception_flag"));
				list.add(roleMenuDTO);
			}

		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public MenuDTO getMenuDTO(MenuDTO menuDTO) {
		MenuDTO menu = new MenuDTO();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectEventPS = connection.prepareStatement("SELECT id,code,name,operation_code,action_type,attr1_value,severity,active_flag FROM menu_events where menu_id = ? AND active_flag = 1");
			@Cleanup
			PreparedStatement selectPS = null;
			if (menuDTO.getId() != 0) {
				selectPS = connection.prepareStatement(" SELECT id, code, name, link, action_code, tag, lookup_id, severity, display_flag, product_type_id, active_flag FROM menu WHERE id = ? AND active_flag = 1");
				selectPS.setInt(1, menuDTO.getId());
			}
			else {
				selectPS = connection.prepareStatement(" SELECT id, code, name, link, action_code, tag, lookup_id, severity, display_flag, product_type_id, active_flag FROM menu WHERE code = ? AND active_flag = 1");
				selectPS.setString(1, menuDTO.getCode());
			}
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				menu.setId(selectRS.getInt("id"));
				menu.setCode(selectRS.getString("code"));
				menu.setName(selectRS.getString("name"));
				menu.setLink(selectRS.getString("link"));
				menu.setSeverity(SeverityPermissionTypeEM.getSeverityPermissionTypeEM(selectRS.getInt("severity")));
				menu.setDisplayFlag(selectRS.getInt("display_flag"));
				menu.setActionCode(selectRS.getString("action_code"));
				List<String> tagList = new ArrayList<>();
				String tag = StringUtil.isNull(selectRS.getString("tag"), Text.EMPTY);
				String tagArr[] = tag.split(",");
				for (String str : tagArr) {
					tagList.add(str);
				}
				menu.setTagList(tagList);
				menu.setProductType(ProductTypeEM.getProductTypeEM(selectRS.getInt("product_type_id")));
				MenuDTO lookupdto = new MenuDTO();
				lookupdto.setId(selectRS.getInt("lookup_id"));
				menu.setLookup(lookupdto);
				menu.setActiveFlag(selectRS.getInt("active_flag"));

				selectEventPS.setInt(1, menu.getId());
				@Cleanup
				ResultSet selectEventRS = selectEventPS.executeQuery();
				List<MenuEventDTO> eventDTOList = new ArrayList<MenuEventDTO>();
				while (selectEventRS.next()) {
					MenuEventDTO eventDTO = new MenuEventDTO();
					eventDTO.setId(selectEventRS.getInt("id"));
					eventDTO.setCode(selectEventRS.getString("code"));
					eventDTO.setName(selectEventRS.getString("name"));
					eventDTO.setOperationCode(selectEventRS.getString("operation_code"));
					eventDTO.setAttr1Value(selectEventRS.getString("attr1_value"));
					eventDTO.setPermissionFlag(selectEventRS.getInt("action_type"));
					eventDTO.setSeverity(SeverityPermissionTypeEM.getSeverityPermissionTypeEM(selectEventRS.getInt("severity")));
					eventDTO.setActiveFlag(selectEventRS.getInt("active_flag"));
					eventDTOList.add(eventDTO);
				}
				MenuEventDTO eventDTO = new MenuEventDTO();
				eventDTO.setList(eventDTOList);
				menu.setMenuEvent(eventDTO);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return menu;
	}

	public List<MenuDTO> getAllforZoneSync(AuthDTO authDTO, String syncDate) {
		List<MenuDTO> list = new ArrayList<MenuDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectEventPS = connection.prepareStatement("SELECT id,code,name,operation_code,attr1_value,action_type,severity,active_flag FROM menu_events where menu_id = ?");
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement(" SELECT id,code,name,link,action_code,tag,lookup_id,severity,display_flag,product_type_id,active_flag FROM menu ");
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				MenuDTO dto = new MenuDTO();
				dto.setId(selectRS.getInt("id"));
				dto.setCode(selectRS.getString("code"));
				dto.setName(selectRS.getString("name"));
				dto.setLink(selectRS.getString("link"));
				dto.setActionCode(selectRS.getString("action_code"));
				String tag = StringUtil.isNull(selectRS.getString("tag"), Text.EMPTY);
				List<String> tagList = new ArrayList<>();
				String tagArr[] = tag.split(",");
				for (String str : tagArr) {
					tagList.add(str);
				}
				dto.setTagList(tagList);
				dto.setSeverity(SeverityPermissionTypeEM.getSeverityPermissionTypeEM(selectRS.getInt("severity")));
				dto.setDisplayFlag(selectRS.getInt("display_flag"));
				dto.setProductType(ProductTypeEM.getProductTypeEM(selectRS.getInt("product_type_id")));
				MenuDTO lookupdto = new MenuDTO();
				lookupdto.setId(selectRS.getInt("lookup_id"));
				dto.setLookup(lookupdto);
				dto.setActiveFlag(selectRS.getInt("active_flag"));

				selectEventPS.setInt(1, dto.getId());
				@Cleanup
				ResultSet selectEventRS = selectEventPS.executeQuery();
				List<MenuEventDTO> eventDTOList = new ArrayList<MenuEventDTO>();
				while (selectEventRS.next()) {
					MenuEventDTO eventDTO = new MenuEventDTO();
					eventDTO.setId(selectEventRS.getInt("id"));
					eventDTO.setCode(selectEventRS.getString("code"));
					eventDTO.setName(selectEventRS.getString("name"));
					eventDTO.setOperationCode(selectEventRS.getString("operation_code"));
					eventDTO.setAttr1Value(selectEventRS.getString("attr1_value"));
					eventDTO.setPermissionFlag(selectEventRS.getInt("action_type"));
					eventDTO.setSeverity(SeverityPermissionTypeEM.getSeverityPermissionTypeEM(selectEventRS.getInt("severity")));
					eventDTO.setActiveFlag(selectEventRS.getInt("active_flag"));
					eventDTOList.add(eventDTO);
				}
				selectEventPS.clearParameters();
				MenuEventDTO eventDTO = new MenuEventDTO();
				eventDTO.setList(eventDTOList);
				dto.setMenuEvent(eventDTO);
				list.add(dto);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public String getMenuZoneSyncDate(AuthDTO authDTO) {
		String zoneSyncDate = null;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT MAX(updated_at) as zoneSyncDate FROM menu");
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				zoneSyncDate = selectRS.getString("zoneSyncDate");
			}
			if (StringUtil.isNull(zoneSyncDate)) {
				zoneSyncDate = "2014-02-12 03:49:03";
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return zoneSyncDate;
	}

	public String getMenuEventZoneSyncDate(AuthDTO authDTO) {
		String zoneSyncDate = null;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT MAX(updated_at) as zoneSyncDate FROM menu_events");
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				zoneSyncDate = selectRS.getString("zoneSyncDate");
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return zoneSyncDate;
	}

	public List<MenuDTO> updateMenuZoneSync(AuthDTO authDTO, List<MenuDTO> list) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			for (MenuDTO menuDTO : list) {
				StringBuilder tagBuilder = new StringBuilder();
				for (String tag : menuDTO.getTagList()) {
					tagBuilder.append(tag);
					tagBuilder.append(",");
				}
				int pindex = 0;
				@Cleanup
				CallableStatement callableStatement = connection.prepareCall("{call  EZEE_SP_MENU_ZONESYNC(?,?,?,?,? ,?,?,?,?,? ,?)}");
				callableStatement.setString(++pindex, menuDTO.getCode());
				callableStatement.setString(++pindex, menuDTO.getName());
				callableStatement.setString(++pindex, menuDTO.getLink());
				callableStatement.setString(++pindex, menuDTO.getActionCode());
				callableStatement.setString(++pindex, tagBuilder.toString());
				callableStatement.setString(++pindex, menuDTO.getLookup() != null ? menuDTO.getLookup().getCode() : null);
				callableStatement.setInt(++pindex, menuDTO.getSeverity().getId());
				callableStatement.setInt(++pindex, menuDTO.getDisplayFlag());
				callableStatement.setInt(++pindex, menuDTO.getProductType().getId());
				callableStatement.setInt(++pindex, menuDTO.getActiveFlag());
				callableStatement.setInt(++pindex, authDTO.getUser().getId());
				callableStatement.execute();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public List<MenuEventDTO> updateMenuEventZoneSync(AuthDTO authDTO, MenuDTO menuDTO, List<MenuEventDTO> list) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{call  EZEE_SP_MENU_EVENTS_ZONESYNC(?,?,?,?,? ,?,?,?,? ,?)}");
			for (MenuEventDTO eventDTO : list) {
				int pindex = 0;
				callableStatement.setString(++pindex, eventDTO.getCode());
				callableStatement.setString(++pindex, eventDTO.getName());
				callableStatement.setString(++pindex, menuDTO.getCode());
				callableStatement.setString(++pindex, eventDTO.getOperationCode());
				callableStatement.setString(++pindex, eventDTO.getAttr1Value());
				callableStatement.setInt(++pindex, eventDTO.getPermissionFlag());
				callableStatement.setInt(++pindex, eventDTO.getSeverity().getId());
				callableStatement.setInt(++pindex, eventDTO.getActiveFlag());
				callableStatement.setInt(++pindex, authDTO.getUser().getId());
				callableStatement.registerOutParameter(++pindex, Types.INTEGER);
				callableStatement.execute();
				callableStatement.clearParameters();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public void restoreDefault(AuthDTO authDTO, UserDTO userDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("UPDATE user_menu SET active_flag = 0 WHERE refference_id = ? AND refference_type = 'UR' AND active_flag = 1");
			ps.setInt(1, userDTO.getId());
			ps.executeUpdate();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
	}

	public List<MenuDTO> getRoleMenu(AuthDTO authDTO, MenuDTO dto) {
		List<MenuDTO> list = new ArrayList<MenuDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT mu.code, mu.name, mu.product_type_id, mu.active_flag, um.refference_id, um.refference_type FROM user_menu um, menu mu WHERE um.namespace_id = ? AND um.menu_id = mu.id AND mu.code = ? AND um.refference_type IN ('UR', 'GR') AND um.active_flag = 1 AND mu.active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setString(2, dto.getCode());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				MenuDTO menuDTO = new MenuDTO();
				menuDTO.setCode(selectRS.getString("mu.code"));
				menuDTO.setName(selectRS.getString("mu.name"));
				menuDTO.setProductType(ProductTypeEM.getProductTypeEM(selectRS.getInt("mu.product_type_id")));
				menuDTO.setActiveFlag(selectRS.getInt("mu.active_flag"));

				if ("UR".equals(selectRS.getString("um.refference_type"))) {
					UserDTO user = new UserDTO();
					user.setId(selectRS.getInt("um.refference_id"));
					menuDTO.setUser(user);
				}
				else if ("GR".equals(selectRS.getString("um.refference_type"))) {
					GroupDTO group = new GroupDTO();
					group.setId(selectRS.getInt("um.refference_id"));
					menuDTO.setGroup(group);
				}

				list.add(menuDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public Map<String, List<MenuDTO>> getRoleMenuPremission(AuthDTO authDTO, String userCodes) {
		Map<String, List<MenuDTO>> privilegeMap = new HashMap<>();
		List<MenuDTO> userMenuList = new ArrayList<MenuDTO>();
		List<MenuDTO> groupMenuList = new ArrayList<MenuDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{call  EZEE_SP_RPT_USER_PERMISSION( ?,?,?)}");
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, userCodes);
			callableStatement.setString(++pindex, "UR");
			@Cleanup
			ResultSet selectRS = callableStatement.executeQuery();
			while (selectRS.next()) {
				MenuDTO menuDTO = new MenuDTO();
				menuDTO.setCode(selectRS.getString("menu_code"));
				menuDTO.setName(selectRS.getString("menu_name"));
				menuDTO.setActiveFlag(1);
				menuDTO.setExceptionFlag(selectRS.getInt("menu_exception_flag"));

				String eventCodes = selectRS.getString("menu_event_codes");
				String activeCodes[] = StringUtil.isNotNull(eventCodes) ? eventCodes.split(",") : new String[0];

				List<MenuEventDTO> menuEventList = new ArrayList<MenuEventDTO>();
				for (String code : activeCodes) {
					if (StringUtil.isNotNull(code)) {
						MenuEventDTO menuEventDTO = new MenuEventDTO();
						menuEventDTO.setCode(code);
						menuEventList.add(menuEventDTO);
					}
				}
				MenuEventDTO menuEventDTO = new MenuEventDTO();
				menuEventDTO.setList(menuEventList);
				menuDTO.setMenuEvent(menuEventDTO);

				UserDTO user = new UserDTO();
				GroupDTO groupDTO = new GroupDTO();
				groupDTO.setId(selectRS.getInt("group_id"));
				user.setGroup(groupDTO);
				user.setId(selectRS.getInt("user_id"));
				user.setCode(selectRS.getString("user_code"));
				user.setName(selectRS.getString("user_name"));
				menuDTO.setUser(user);

				userMenuList.add(menuDTO);
			}
			callableStatement.clearParameters();

			pindex = 0;
			callableStatement = connection.prepareCall("{call  EZEE_SP_RPT_USER_PERMISSION( ?,?,?)}");
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, userCodes);
			callableStatement.setString(++pindex, "GR");
			@Cleanup
			ResultSet selectGroupRS = callableStatement.executeQuery();
			while (selectGroupRS.next()) {
				MenuDTO menuDTO = new MenuDTO();
				menuDTO.setCode(selectGroupRS.getString("menu_code"));
				menuDTO.setName(selectGroupRS.getString("menu_name"));
				menuDTO.setActiveFlag(1);
				menuDTO.setExceptionFlag(selectGroupRS.getInt("menu_exception_flag"));

				String eventCodes = selectGroupRS.getString("menu_event_codes");
				String activeCodes[] = StringUtil.isNotNull(eventCodes) ? eventCodes.split(",") : new String[0];

				List<MenuEventDTO> menuEventList = new ArrayList<MenuEventDTO>();
				for (String code : activeCodes) {
					if (StringUtil.isNotNull(code)) {
						MenuEventDTO menuEventDTO = new MenuEventDTO();
						menuEventDTO.setCode(code);
						menuEventList.add(menuEventDTO);
					}
				}
				MenuEventDTO menuEventDTO = new MenuEventDTO();
				menuEventDTO.setList(menuEventList);
				menuDTO.setMenuEvent(menuEventDTO);

				GroupDTO groupDTO = new GroupDTO();
				groupDTO.setId(selectGroupRS.getInt("group_id"));
				groupDTO.setCode(selectGroupRS.getString("group_code"));
				groupDTO.setName(selectGroupRS.getString("group_name"));
				menuDTO.setGroup(groupDTO);
				groupMenuList.add(menuDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}

		privilegeMap.put("UR", userMenuList);
		privilegeMap.put("GR", groupMenuList);

		return privilegeMap;
	}

	public MenuDTO getUserMenu(AuthDTO authDTO, MenuDTO dto) {
		MenuDTO menuDTO = new MenuDTO();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT menu_id, active_menu_event_codes, menu_exception_flag FROM user_menu WHERE namespace_id = ? AND menu_id = ? AND refference_type = 'NS' AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, dto.getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				menuDTO.setId(selectRS.getInt("menu_id"));
				String activeEventCodesPlus = selectRS.getString("active_menu_event_codes");
				String activeCodes[] = activeEventCodesPlus.split(",");
				List<MenuEventDTO> menuEventList = new ArrayList<MenuEventDTO>();
				for (String code : activeCodes) {
					if (StringUtil.isNotNull(code)) {
						MenuEventDTO menuEventDTO = new MenuEventDTO();
						menuEventDTO.setCode(code);
						menuEventList.add(menuEventDTO);
					}
				}

				MenuEventDTO menuEventDTO = new MenuEventDTO();
				menuEventDTO.setList(menuEventList);
				menuDTO.setMenuEvent(menuEventDTO);
				menuDTO.setExceptionFlag(selectRS.getInt("menu_exception_flag"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return menuDTO;

	}

}
