package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.AuditDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.CommissionDTO;
import org.in.com.dto.ExtraCommissionDTO;
import org.in.com.dto.ExtraCommissionSlabDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.RouteDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.CommissionTypeEM;
import org.in.com.dto.enumeration.DateTypeEM;
import org.in.com.dto.enumeration.FareTypeEM;
import org.in.com.dto.enumeration.SlabCalenderModeEM;
import org.in.com.dto.enumeration.SlabCalenderTypeEM;
import org.in.com.dto.enumeration.SlabModeEM;
import org.in.com.exception.ServiceException;
import org.in.com.utils.StringUtil;

import lombok.Cleanup;

public class CommissionDAO {
	public List<CommissionDTO> getUserCommission(AuthDTO authDTO, UserDTO user) {
		List<CommissionDTO> list = new ArrayList<>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			if (user.getId() != 0) {
				selectPS = connection.prepareStatement("SELECT comm.code, comm.commission_value, comm.commission_value_type_id, comm.credit_limit, comm.commission_type, comm.service_tax, comm.active_flag, comm.updated_by, DATE_FORMAT(comm.updated_at,'%Y-%m-%d %H:%i:%S') as updated_at FROM user_commission comm,user usr WHERE usr.namespace_id = ? AND usr.namespace_id = comm.namespace_id AND comm.user_id = usr.id AND usr.id = ?  AND comm.active_flag < 2 ORDER BY updated_at DESC LIMIT 10");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setInt(2, user.getId());
			}
			else {
				selectPS = connection.prepareStatement("SELECT comm.code, comm.commission_value, comm.commission_value_type_id, comm.credit_limit, comm.commission_type, comm.service_tax, comm.active_flag, comm.updated_by,  DATE_FORMAT(comm.updated_at,'%Y-%m-%d %H:%i:%S') as updated_at  FROM user_commission comm,user usr WHERE usr.namespace_id = ? AND usr.namespace_id = comm.namespace_id AND comm.user_id = usr.id AND usr.code = ?  AND comm.active_flag < 2  ORDER BY comm.updated_at DESC LIMIT 10");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setString(2, user.getCode());
			}

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				CommissionDTO commission = new CommissionDTO();
				commission.setCode(selectRS.getString("code"));
				commission.setCommissionValue(selectRS.getBigDecimal("commission_value"));
				commission.setActiveFlag(selectRS.getInt("active_flag"));
				commission.setCommissionValueType(FareTypeEM.getFareTypeEM(selectRS.getInt("commission_value_type_id")));
				commission.setCommissionType(CommissionTypeEM.getCommissionTypeEM(selectRS.getString("comm.commission_type")));
				commission.setCreditlimit(selectRS.getBigDecimal("credit_limit"));
				commission.setServiceTax(selectRS.getBigDecimal("comm.service_tax"));
				commission.setCreatedDateTime(selectRS.getString("updated_at"));
				
				UserDTO updatedBy = new UserDTO();
				updatedBy.setId(selectRS.getInt("updated_by"));
				AuditDTO auditDTO = new AuditDTO();
				auditDTO.setUser(updatedBy);
				commission.setAudit(auditDTO);
				
				list.add(commission);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;

	}

	public CommissionDTO getTransactionCommissionDetails(AuthDTO authDTO, UserDTO userDTO, CommissionTypeEM typeDTO) {
		CommissionDTO commissionDTO = null;
		int userId = 0;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			if (userDTO.getId() != 0) {
				selectPS = connection.prepareStatement("SELECT usr.id, comm.code,comm.commission_value,comm.commission_value_type_id,comm.credit_limit,comm.service_tax,comm.active_flag FROM user_commission comm,user usr WHERE usr.namespace_id = ? AND usr.namespace_id = comm.namespace_id AND comm.user_id = usr.id AND usr.id = ?  AND comm.commission_type = ? AND comm.active_flag = 1");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setInt(2, userDTO.getId());
				selectPS.setString(3, typeDTO.getCode());
			}
			else {
				selectPS = connection.prepareStatement("SELECT usr.id, comm.code,comm.commission_value,comm.commission_value_type_id,comm.credit_limit,comm.service_tax,comm.active_flag FROM user_commission comm,user usr WHERE usr.namespace_id = ? AND usr.namespace_id = comm.namespace_id AND comm.user_id = usr.id AND usr.code = ? AND comm.commission_type = ? AND comm.active_flag = 1");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setString(2, userDTO.getCode());
				selectPS.setString(3, typeDTO.getCode());
			}

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				commissionDTO = new CommissionDTO();
				commissionDTO.setCode(selectRS.getString("code"));
				commissionDTO.setCommissionValue(selectRS.getBigDecimal("commission_value"));
				commissionDTO.setActiveFlag(selectRS.getInt("active_flag"));
				commissionDTO.setCommissionValueType(FareTypeEM.getFareTypeEM(selectRS.getInt("commission_value_type_id")));
				commissionDTO.setCreditlimit(selectRS.getBigDecimal("credit_limit"));
				commissionDTO.setServiceTax(selectRS.getBigDecimal("comm.service_tax"));
				commissionDTO.setCommissionType(typeDTO);
				userId = selectRS.getInt("usr.id");
			}
			if (commissionDTO != null) {
				@Cleanup
				PreparedStatement selectTaxPS = connection.prepareStatement("SELECT tds_tax_value FROM user_tax_details WHERE namespace_id = ? AND user_id = ? AND active_flag = 1");
				selectTaxPS.setInt(1, authDTO.getNamespace().getId());
				selectTaxPS.setInt(2, userId);
				@Cleanup
				ResultSet selectTaxRS = selectTaxPS.executeQuery();
				if (selectTaxRS.next()) {
					commissionDTO.setTdsTaxValue(selectTaxRS.getBigDecimal("tds_tax_value"));
				}
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return commissionDTO;

	}

	public List<CommissionDTO> getTransactionCommissionDetailsV2(AuthDTO authDTO, UserDTO userDTO) {
		List<CommissionDTO> list = new ArrayList<CommissionDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT usr.id, comm.code,comm.commission_value,comm.commission_value_type_id,comm.commission_type,comm.credit_limit,comm.service_tax,comm.active_flag FROM user_commission comm,user usr WHERE usr.namespace_id = ? AND usr.namespace_id = comm.namespace_id AND comm.user_id = usr.id AND usr.id = ? AND comm.active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, userDTO.getId());

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();

			while (selectRS.next()) {
				CommissionDTO commissionDTO = new CommissionDTO();
				commissionDTO.setCode(selectRS.getString("code"));
				commissionDTO.setCommissionValue(selectRS.getBigDecimal("commission_value"));
				commissionDTO.setActiveFlag(selectRS.getInt("active_flag"));
				commissionDTO.setCommissionValueType(FareTypeEM.getFareTypeEM(selectRS.getInt("commission_value_type_id")));
				commissionDTO.setCreditlimit(selectRS.getBigDecimal("credit_limit"));
				commissionDTO.setServiceTax(selectRS.getBigDecimal("comm.service_tax"));
				commissionDTO.setCommissionType(CommissionTypeEM.getCommissionTypeEM(selectRS.getString("comm.commission_type")));
				list.add(commissionDTO);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;

	}

	public CommissionDTO Update(AuthDTO authDTO, UserDTO user, CommissionDTO commissionDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_USER_COMMISSION_IUD(?,?,?,?,? ,?,?,?,?,? ,?,?)}");
			callableStatement.setString(++pindex, commissionDTO.getCode());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, user.getCode());
			callableStatement.setBigDecimal(++pindex, commissionDTO.getCommissionValue());
			callableStatement.setBigDecimal(++pindex, commissionDTO.getServiceTax());
			callableStatement.setInt(++pindex, commissionDTO.getCommissionValueType().getId());
			callableStatement.setBigDecimal(++pindex, commissionDTO.getCreditlimit());

			callableStatement.setString(++pindex, commissionDTO.getCommissionType().getCode());
			callableStatement.setInt(++pindex, commissionDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				commissionDTO.setCode(callableStatement.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return commissionDTO;
	}

	public void UpdateExtraCommission(AuthDTO authDTO, ExtraCommissionDTO commissionDTO) {
		String reffernceCode = getReferecenIds(commissionDTO);
		
		StringBuilder scheduleCodes = new StringBuilder();
		if (commissionDTO.getScheduleList() != null && !commissionDTO.getScheduleList().isEmpty()) {
			for (ScheduleDTO scheduleDTO : commissionDTO.getScheduleList()) {
				if (scheduleCodes.length() > 0) {
					scheduleCodes.append(",");
				}
				scheduleCodes.append(scheduleDTO.getCode());
			}
		}
		StringBuilder routesCodes = new StringBuilder();
		if (commissionDTO.getRouteList() != null && !commissionDTO.getRouteList().isEmpty()) {
			for (RouteDTO route : commissionDTO.getRouteList()) {
				if (StringUtil.isNotNull(route.getCode())) {
					routesCodes.append(",");
				}
				routesCodes.append(route.getCode());
			}
		}

		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_USER_EXTRA_COMMISSION_IUD(?,?,?,?,? ,?,?,?,?,? ,?,?,?,?,? ,?,?,?,?,? ,?,?,?,?)}");
			callableStatement.setString(++pindex, commissionDTO.getCode());
			callableStatement.setString(++pindex, commissionDTO.getName());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setBigDecimal(++pindex, commissionDTO.getCommissionValue());
			callableStatement.setInt(++pindex, commissionDTO.getCommissionValueType() != null ? commissionDTO.getCommissionValueType().getId() : 0);
			callableStatement.setInt(++pindex, commissionDTO.getOverrideCommissionFlag());
			callableStatement.setString(++pindex, reffernceCode);
			callableStatement.setString(++pindex, commissionDTO.getRefferenceType());
			callableStatement.setString(++pindex, commissionDTO.getActiveFrom());
			callableStatement.setString(++pindex, commissionDTO.getActiveTo());
			callableStatement.setString(++pindex, commissionDTO.getDayOfWeek());

			callableStatement.setString(++pindex, commissionDTO.getDateType() != null ? commissionDTO.getDateType().getCode() : null);
			callableStatement.setString(++pindex, scheduleCodes.toString());
			callableStatement.setString(++pindex, routesCodes.toString());
			callableStatement.setString(++pindex, commissionDTO.getCommissionSlab() != null ? commissionDTO.getCommissionSlab().getCode() : null);
			callableStatement.setBigDecimal(++pindex, commissionDTO.getMaxCommissionLimit());
			callableStatement.setBigDecimal(++pindex, commissionDTO.getMinTicketFare());
			callableStatement.setBigDecimal(++pindex, commissionDTO.getMaxExtraCommissionAmount());
			callableStatement.setInt(++pindex, commissionDTO.getMinSeatCount());

			callableStatement.setString(++pindex, commissionDTO.getLookupCode());

			callableStatement.setInt(++pindex, commissionDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				commissionDTO.setCode(callableStatement.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public ExtraCommissionDTO getExtraCommission(AuthDTO authDTO, ExtraCommissionDTO commissionDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id, name, code, commission_value, commission_value_type_id, override_commission_flag, active_from, active_to, day_of_week, refference_id, refference_type, schedule_code, route_code, slab_code, min_increment_commission, max_commission_limit, min_ticket_fare, max_extra_commission_amount, min_seat_count, date_type, lookup_id, active_flag FROM user_extra_commission  WHERE  namespace_id = ?  AND code = ? AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setString(2, commissionDTO.getCode());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				commissionDTO.setId(selectRS.getInt("id"));
				commissionDTO.setCode(selectRS.getString("code"));
				commissionDTO.setName(selectRS.getString("name"));
				commissionDTO.setActiveFlag(selectRS.getInt("active_flag"));
				commissionDTO.setLookupCode(selectRS.getString("lookup_id"));
				commissionDTO.setActiveFrom(selectRS.getString("active_from"));
				commissionDTO.setActiveTo(selectRS.getString("active_to"));
				commissionDTO.setDayOfWeek(selectRS.getString("day_of_week"));
				commissionDTO.setOverrideCommissionFlag(selectRS.getInt("override_commission_flag"));
				commissionDTO.setCommissionValueType(FareTypeEM.getFareTypeEM(selectRS.getInt("commission_value_type_id")));

				String scheduleCode = selectRS.getString("schedule_code");
				String routeCode = selectRS.getString("route_code");

				ExtraCommissionSlabDTO commissionSlabDTO = new ExtraCommissionSlabDTO();
				commissionSlabDTO.setCode(selectRS.getString("slab_code"));
				commissionDTO.setCommissionSlab(commissionSlabDTO);

				List<ScheduleDTO> schedulelist = new ArrayList<ScheduleDTO>();
				if (StringUtil.isNotNull(scheduleCode)) {
					String[] scheduleCodes = scheduleCode.split(",");
					for (String seatCode : scheduleCodes) {
						if (StringUtil.isNotNull(seatCode)) {
							ScheduleDTO scheduleDTO = new ScheduleDTO();
							scheduleDTO.setCode(seatCode);
							schedulelist.add(scheduleDTO);
						}
					}
				}
				commissionDTO.setScheduleList(schedulelist);
				List<RouteDTO> routelist = new ArrayList<RouteDTO>();
				if (StringUtil.isNotNull(routeCode)) {
					List<String> routeCodeList = new ArrayList<>(Arrays.asList(routeCode.split(",")));
					for (String code : routeCodeList) {
						RouteDTO routeDTO = new RouteDTO();
						routeDTO.setCode(code);
						routelist.add(routeDTO);
					}
					commissionDTO.setRouteList(routelist);
				}

				commissionDTO.setCommissionValue(selectRS.getBigDecimal("commission_value"));
				commissionDTO.setMaxCommissionLimit(selectRS.getBigDecimal("max_commission_limit"));
				commissionDTO.setMinTicketFare(selectRS.getBigDecimal("min_ticket_fare"));
				commissionDTO.setMaxExtraCommissionAmount(selectRS.getBigDecimal("max_extra_commission_amount"));
				commissionDTO.setMinSeatCount(selectRS.getInt("min_seat_count"));
				commissionDTO.setRefferenceType(selectRS.getString("refference_type"));
				commissionDTO.setDateType(DateTypeEM.getDateTypeEM(selectRS.getString("date_type")));
			
				String reffernceId = selectRS.getString("refference_id");
				if (StringUtil.isNotNull(reffernceId) && commissionDTO.getRefferenceType().equals("GR")) {
					List<GroupDTO> groupList = convertGroup(reffernceId);
					commissionDTO.setGroup(groupList);
				}
				else if (StringUtil.isNotNull(reffernceId) && commissionDTO.getRefferenceType().equals("UR")) {
					List<UserDTO> userList = convertUser(reffernceId);
					commissionDTO.setUser(userList);
				}
				
				List<ExtraCommissionDTO> overrideList = new ArrayList<ExtraCommissionDTO>();
				@Cleanup
				PreparedStatement selectOrverridePS = connection.prepareStatement("SELECT id,name,code,active_from,active_to,day_of_week, active_flag FROM user_extra_commission  WHERE  namespace_id = ?  AND lookup_id = ? AND active_flag = 1");
				selectOrverridePS.setInt(1, authDTO.getNamespace().getId());
				selectOrverridePS.setInt(2, commissionDTO.getId());
				@Cleanup
				ResultSet selectorverrideRS = selectOrverridePS.executeQuery();
				while (selectorverrideRS.next()) {
					ExtraCommissionDTO overridedto = new ExtraCommissionDTO();
					overridedto.setCode(selectorverrideRS.getString("code"));
					overridedto.setName(selectorverrideRS.getString("name"));
					overridedto.setActiveFlag(selectorverrideRS.getInt("active_flag"));
					overridedto.setActiveFrom(selectorverrideRS.getString("active_from"));
					overridedto.setActiveTo(selectorverrideRS.getString("active_to"));
					overridedto.setDayOfWeek(selectorverrideRS.getString("day_of_week"));
					overrideList.add(overridedto);
				}
				commissionDTO.setOverrideList(overrideList);
			}

		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return commissionDTO;
	}

	public List<ExtraCommissionDTO> getAllExtraCommission(AuthDTO authDTO) {
		List<ExtraCommissionDTO> overrideList = new ArrayList<ExtraCommissionDTO>();
		Map<Integer, ExtraCommissionDTO> scheduleMap = new HashMap<Integer, ExtraCommissionDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectslabPS = connection.prepareStatement("SELECT  code,  name, slab_calendar_type_id, slab_calendar_mode_id, slab_mode_id, slab_from_value, slab_to_value, active_flag FROM user_extra_commision_slab  WHERE  namespace_id = ? AND code = ? AND active_flag = 1");
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id, name, code, commission_value, commission_value_type_id, override_commission_flag, active_from, active_to, day_of_week, refference_id, refference_type, schedule_code, route_code, slab_code, max_commission_limit, min_ticket_fare, max_extra_commission_amount, min_seat_count, date_type, lookup_id, active_flag FROM user_extra_commission  WHERE  namespace_id = ?  AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ExtraCommissionDTO commissionDTO = new ExtraCommissionDTO();
				commissionDTO.setId(selectRS.getInt("id"));
				commissionDTO.setCode(selectRS.getString("code"));
				commissionDTO.setName(selectRS.getString("name"));
				commissionDTO.setActiveFlag(selectRS.getInt("active_flag"));
				commissionDTO.setLookupCode(selectRS.getString("lookup_id"));
				commissionDTO.setActiveFrom(selectRS.getString("active_from"));
				commissionDTO.setActiveTo(selectRS.getString("active_to"));
				commissionDTO.setDayOfWeek(selectRS.getString("day_of_week"));
				commissionDTO.setCommissionValue(selectRS.getBigDecimal("commission_value"));
				commissionDTO.setCommissionValueType(FareTypeEM.getFareTypeEM(selectRS.getInt("commission_value_type_id")));
				commissionDTO.setLookupCode(selectRS.getString("lookup_id"));
				commissionDTO.setOverrideCommissionFlag(selectRS.getInt("override_commission_flag"));

				String scheduleCode = selectRS.getString("schedule_code");
				String routeCode = selectRS.getString("route_code");

				String slabCode = selectRS.getString("slab_code");
				if (StringUtil.isNotNull(slabCode)) {
					ExtraCommissionSlabDTO commissionSlabDTO = new ExtraCommissionSlabDTO();

					selectslabPS.setInt(1, authDTO.getNamespace().getId());
					selectslabPS.setString(2, slabCode);

					@Cleanup
					ResultSet selectSlabRS = selectslabPS.executeQuery();
					if (selectSlabRS.next()) {
						commissionSlabDTO.setCode(selectSlabRS.getString("code"));
						commissionSlabDTO.setName(selectSlabRS.getString("name"));
						commissionSlabDTO.setSlabCalenderType(SlabCalenderTypeEM.getSlabCalenderTypeEM(selectSlabRS.getInt("slab_calendar_type_id")));
						commissionSlabDTO.setSlabCalenderMode(SlabCalenderModeEM.getSlabCalenderModeEM(selectSlabRS.getInt("slab_calendar_mode_id")));
						commissionSlabDTO.setSlabMode(SlabModeEM.getSlabModeEM(selectSlabRS.getInt("slab_mode_id")));
						commissionSlabDTO.setSlabFromValue(selectSlabRS.getInt("slab_from_value"));
						commissionSlabDTO.setSlabToValue(selectSlabRS.getInt("slab_to_value"));
						commissionSlabDTO.setActiveFlag(selectSlabRS.getInt("active_flag"));
						commissionDTO.setCommissionSlab(commissionSlabDTO);
					}
				}
				List<ScheduleDTO> schedulelist = new ArrayList<ScheduleDTO>();
				if (StringUtil.isNotNull(scheduleCode)) {
					String[] scheduleCodes = scheduleCode.split(",");
					for (String seatCode : scheduleCodes) {
						if (StringUtil.isNotNull(seatCode)) {
							ScheduleDTO scheduleDTO = new ScheduleDTO();
							scheduleDTO.setCode(seatCode);
							schedulelist.add(scheduleDTO);
						}
					}
				}
				commissionDTO.setScheduleList(schedulelist);
				List<RouteDTO> routelist = new ArrayList<RouteDTO>();
				if (StringUtil.isNotNull(routeCode)) {
					List<String> routeCodeList = new ArrayList<>(Arrays.asList(routeCode.split(",")));
					for (String code : routeCodeList) {
						RouteDTO routeDTO = new RouteDTO();
						routeDTO.setCode(code);
						routelist.add(routeDTO);
					}
				}
				commissionDTO.setRouteList(routelist);
				commissionDTO.setMaxCommissionLimit(selectRS.getBigDecimal("max_commission_limit"));
				commissionDTO.setMinTicketFare(selectRS.getBigDecimal("min_ticket_fare"));
				commissionDTO.setMaxExtraCommissionAmount(selectRS.getBigDecimal("max_extra_commission_amount"));
				commissionDTO.setMinSeatCount(selectRS.getInt("min_seat_count"));
				commissionDTO.setRefferenceType(selectRS.getString("refference_type"));
				commissionDTO.setDateType(DateTypeEM.getDateTypeEM(selectRS.getString("date_type")));
				
				String reffernceId = selectRS.getString("refference_id");
				if (StringUtil.isNotNull(reffernceId) && commissionDTO.getRefferenceType().equals("GR")) {
					List<GroupDTO> groupList = convertGroup(reffernceId);
					commissionDTO.setGroup(groupList);
				}
				else if (StringUtil.isNotNull(reffernceId) && commissionDTO.getRefferenceType().equals("UR")) {
					List<UserDTO> userList = convertUser(reffernceId);
					commissionDTO.setUser(userList);
				}
				
				if (commissionDTO.getLookupCode().equals("0")) {
					scheduleMap.put(commissionDTO.getId(), commissionDTO);
				}
				else {
					overrideList.add(commissionDTO);
				}
			}
			for (ExtraCommissionDTO overrideCommissionDTO : overrideList) {
				if (scheduleMap.get(Integer.parseInt(overrideCommissionDTO.getLookupCode())) != null) {
					ExtraCommissionDTO dto = scheduleMap.get(Integer.parseInt(overrideCommissionDTO.getLookupCode()));
					dto.getOverrideList().add(overrideCommissionDTO);
					scheduleMap.put(dto.getId(), dto);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return new ArrayList<ExtraCommissionDTO>(scheduleMap.values());
	}

	public List<ExtraCommissionSlabDTO> getAllCommissionSlab(AuthDTO authDTO) {
		List<ExtraCommissionSlabDTO> list = new ArrayList<ExtraCommissionSlabDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT  code,  name, slab_calendar_type_id, slab_calendar_mode_id, slab_mode_id, slab_from_value, slab_to_value, active_flag FROM user_extra_commision_slab  WHERE  namespace_id = ? AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ExtraCommissionSlabDTO detailsDTO = new ExtraCommissionSlabDTO();
				detailsDTO.setCode(selectRS.getString("code"));
				detailsDTO.setName(selectRS.getString("name"));
				detailsDTO.setSlabCalenderType(SlabCalenderTypeEM.getSlabCalenderTypeEM(selectRS.getInt("slab_calendar_type_id")));
				detailsDTO.setSlabCalenderMode(SlabCalenderModeEM.getSlabCalenderModeEM(selectRS.getInt("slab_calendar_mode_id")));
				detailsDTO.setSlabMode(SlabModeEM.getSlabModeEM(selectRS.getInt("slab_mode_id")));
				detailsDTO.setSlabFromValue(selectRS.getInt("slab_from_value"));
				detailsDTO.setSlabToValue(selectRS.getInt("slab_to_value"));
				detailsDTO.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(detailsDTO);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public ExtraCommissionSlabDTO updateExtraCommissionSlab(AuthDTO authDTO, ExtraCommissionSlabDTO dto) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_USER_EXTRA_COMMISSION_SLAB(?,?,?,?,?, ?,?,?,?,? ,?,?)}");
			callableStatement.setString(++pindex, dto.getCode());
			callableStatement.setString(++pindex, dto.getName());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setInt(++pindex, dto.getSlabCalenderType() != null ? dto.getSlabCalenderType().getId() : 0);
			callableStatement.setInt(++pindex, dto.getSlabCalenderMode() != null ? dto.getSlabCalenderMode().getId() : 0);
			callableStatement.setInt(++pindex, dto.getSlabMode() != null ? dto.getSlabMode().getId() : 0);
			callableStatement.setInt(++pindex, dto.getSlabFromValue());
			callableStatement.setInt(++pindex, dto.getSlabToValue());
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
		}
		return dto;
	}

	public CommissionDTO getUserTaxDetails(AuthDTO authDTO, UserDTO userDTO) {
		CommissionDTO commissionDTO = new CommissionDTO();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectTaxPS = connection.prepareStatement("SELECT tds_tax_value FROM user_tax_details WHERE namespace_id = ? AND user_id = ? AND active_flag = 1");
			selectTaxPS.setInt(1, authDTO.getNamespace().getId());
			selectTaxPS.setInt(2, userDTO.getId());
			ResultSet selectTaxRS = selectTaxPS.executeQuery();
			if (selectTaxRS.next()) {
				commissionDTO.setTdsTaxValue(selectTaxRS.getBigDecimal("tds_tax_value"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return commissionDTO;

	}
	
	private String getReferecenIds(ExtraCommissionDTO extraCommissionDTO) {
		StringBuilder referenceIds = new StringBuilder();
		if (extraCommissionDTO.getRefferenceType() != null && extraCommissionDTO.getRefferenceType().equals("UR") && extraCommissionDTO.getUser() != null) {
			for (UserDTO userDTO : extraCommissionDTO.getUser()) {
				if (userDTO.getId() == 0) {
					continue;
				}
				referenceIds.append(userDTO.getId());
				referenceIds.append(Text.COMMA);
			}
		}
		else if (extraCommissionDTO.getRefferenceType() != null && extraCommissionDTO.getRefferenceType().equals("GR") && extraCommissionDTO.getGroup() != null) {
			for (GroupDTO groupDTO : extraCommissionDTO.getGroup()) {
				if (groupDTO.getId() == 0) {
					continue;
				}
				referenceIds.append(groupDTO.getId());
				referenceIds.append(Text.COMMA);
			}
		}
		return StringUtil.isNotNull(referenceIds.toString()) ? referenceIds.toString() : Text.NA;
	}

	public List<UserDTO> convertUser(String referenceIds) {
		List<UserDTO> userList = new ArrayList<>();
		if (StringUtil.isNotNull(referenceIds)) {
			String[] referenceId = referenceIds.split(Text.COMMA);

			for (String userId : referenceId) {
				if (StringUtil.isNull(userId) || Numeric.ZERO.equals(userId)) {
					continue;
				}
				UserDTO user = new UserDTO();
				user.setId(Integer.valueOf(userId));
				userList.add(user);
			}
		}
		return userList;
	}

	public List<GroupDTO> convertGroup(String referenceIds) {
		List<GroupDTO> groupList = new ArrayList<GroupDTO>();
		if (StringUtil.isNotNull(referenceIds)) {
			String[] referenceId = referenceIds.split(Text.COMMA);

			for (String groupId : referenceId) {
				if (StringUtil.isNull(groupId) || Numeric.ZERO.equals(groupId)) {
					continue;
				}
				GroupDTO group = new GroupDTO();
				group.setId(Integer.valueOf(groupId));
				groupList.add(group);
			}
		}
		return groupList;
	}
}
