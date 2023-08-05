package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.DiscountSpecialCriteriaDTO;
import org.in.com.exception.ServiceException;
import org.in.com.utils.StringUtil;
import org.springframework.stereotype.Component;

import lombok.Cleanup;

@Component
public class DiscountSpecialCriteriaDAO {

	public List<DiscountSpecialCriteriaDTO> getAllSpecialDiscountCriteria(AuthDTO authDTO) {
		List<DiscountSpecialCriteriaDTO> list = new ArrayList<>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("SELECT id, code, user_group_ids, schedule_ids, max_amount, percentage_flag, active_flag FROM discount_special_criteria WHERE namespace_id = ? AND active_flag = 1");
			ps.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				DiscountSpecialCriteriaDTO discountDTO = new DiscountSpecialCriteriaDTO();
				discountDTO.setId(rs.getInt("id"));
				discountDTO.setCode(rs.getString("code"));
				String group = rs.getString("user_group_ids");
				String userGroup[] = group.split(",");
				List<GroupDTO> groupList = new ArrayList<>();
				for (String id : userGroup) {
					if (StringUtil.isNotNull(id)) {
						GroupDTO groupDTO = new GroupDTO();
						groupDTO.setId(Integer.valueOf(id));
						groupList.add(groupDTO);
					}

				}
				discountDTO.setUserGroups(groupList);
				List<ScheduleDTO> scheduleList = new ArrayList<>();
				String schedule = rs.getString("schedule_ids");
				String scheduleIds[] = schedule.split(",");
				for (String id : scheduleIds) {
					if (StringUtil.isNotNull(id)) {
						ScheduleDTO scheduleDTO = new ScheduleDTO();
						scheduleDTO.setId(Integer.valueOf(id));
						scheduleList.add(scheduleDTO);
					}
				}
				discountDTO.setSchedules(scheduleList);
				discountDTO.setMaxAmount(rs.getBigDecimal("max_amount"));
				discountDTO.setPercentageFlag(rs.getBoolean("percentage_flag"));
				discountDTO.setActiveFlag(rs.getInt("active_flag"));
				list.add(discountDTO);
			}

		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;

	}

	public void updateSpecialDiscountCriteria(AuthDTO authDTO, DiscountSpecialCriteriaDTO discount) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			StringBuilder userGroupBuilder = new StringBuilder();
			StringBuilder scheduleBuilder = new StringBuilder();
			for (GroupDTO group : discount.getUserGroups()) {
				userGroupBuilder.append(group.getId());
				userGroupBuilder.append(",");
			}
			for (ScheduleDTO schedule : discount.getSchedules()) {
				scheduleBuilder.append(schedule.getId());
				scheduleBuilder.append(",");
			}
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{call  EZEE_SP_DISCOUNT_SPECIAL_CRITERIA_IUD(?,?,?,?,? ,?,?,?,?,?)}");
			callableStatement.setString(++pindex, discount.getCode());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, userGroupBuilder.toString());
			callableStatement.setString(++pindex, scheduleBuilder.toString());
			callableStatement.setBigDecimal(++pindex, discount.getMaxAmount());
			callableStatement.setBoolean(++pindex, discount.isPercentageFlag());
			callableStatement.setInt(++pindex, discount.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				discount.setCode(callableStatement.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public DiscountSpecialCriteriaDTO getSpecialDiscountCriteriaByCode(AuthDTO authDTO, DiscountSpecialCriteriaDTO specialDiscountCriteria) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("SELECT id, code, user_group_ids, schedule_ids, max_amount, percentage_flag, active_flag FROM discount_special_criteria WHERE namespace_id = ? AND active_flag = 1 AND code = ?");
			ps.setInt(1, authDTO.getNamespace().getId());
			ps.setString(2, specialDiscountCriteria.getCode());
			@Cleanup
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				specialDiscountCriteria.setId(rs.getInt("id"));
				specialDiscountCriteria.setCode(rs.getString("code"));
				String group = rs.getString("user_group_ids");
				String userGroup[] = group.split(",");
				List<GroupDTO> userGroupList = new ArrayList<>();
				for (String id : userGroup) {
					if (StringUtil.isNotNull(id)) {
						GroupDTO groupDTO = new GroupDTO();
						groupDTO.setId(Integer.valueOf(id));
						userGroupList.add(groupDTO);
					}
				}
				specialDiscountCriteria.setUserGroups(userGroupList);
				String schedule = rs.getString("schedule_ids");
				String scheduleIds[] = schedule.split(",");
				List<ScheduleDTO> scheduleList = new ArrayList<>();
				for (String id : scheduleIds) {
					if (StringUtil.isNotNull(id)) {
						ScheduleDTO scheduleDTO = new ScheduleDTO();
						scheduleDTO.setId(Integer.valueOf(id));
						scheduleList.add(scheduleDTO);
					}
				}
				specialDiscountCriteria.setSchedules(scheduleList);
				specialDiscountCriteria.setMaxAmount(rs.getBigDecimal("max_amount"));
				specialDiscountCriteria.setPercentageFlag(rs.getBoolean("percentage_flag"));
				specialDiscountCriteria.setActiveFlag(rs.getInt("active_flag"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return specialDiscountCriteria;

	}

}
