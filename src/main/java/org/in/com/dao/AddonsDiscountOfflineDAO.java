package org.in.com.dao;

import java.math.RoundingMode;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.Cleanup;

import org.in.com.dto.AddonsDiscountOfflineDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.exception.ServiceException;
import org.in.com.utils.StringUtil;

public class AddonsDiscountOfflineDAO {
	public AddonsDiscountOfflineDTO updateOfflineDiscount(AuthDTO authDTO, AddonsDiscountOfflineDTO offlineDiscountDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{call  EZEE_SP_DISCOUNT_OFFLINE_IUD(?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?)}");
			callableStatement.setString(++pindex, offlineDiscountDTO.getCode());
			callableStatement.setString(++pindex, offlineDiscountDTO.getName());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, offlineDiscountDTO.getGroupCode().toString().replace("[", "").replace("]", ""));
			callableStatement.setString(++pindex, offlineDiscountDTO.getActiveFrom());
			callableStatement.setString(++pindex, offlineDiscountDTO.getActiveTo());
			callableStatement.setString(++pindex, offlineDiscountDTO.getDayOfWeek());
			callableStatement.setBigDecimal(++pindex, offlineDiscountDTO.getValue());
			callableStatement.setInt(++pindex, offlineDiscountDTO.isPercentageFlag() ? 1 : 0);
			callableStatement.setInt(++pindex, offlineDiscountDTO.isTravelDateFlag() ? 1 : 0);
			callableStatement.setBigDecimal(++pindex, offlineDiscountDTO.getMaxDiscountAmount().setScale(2, RoundingMode.HALF_UP));
			callableStatement.setInt(++pindex, offlineDiscountDTO.getMinSeatCount());
			callableStatement.setInt(++pindex, offlineDiscountDTO.getMinTicketFare());
			callableStatement.setString(++pindex, offlineDiscountDTO.getScheduleCode().toString().replace("[", "").replace("]", ""));
			callableStatement.setString(++pindex, offlineDiscountDTO.getRouteCode().toString().replace("[", "").replace("]", ""));
			callableStatement.setInt(++pindex, offlineDiscountDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				offlineDiscountDTO.setCode(callableStatement.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return offlineDiscountDTO;
	}

	public List<AddonsDiscountOfflineDTO> getAllOfflineDiscount(AuthDTO authDTO) {
		List<AddonsDiscountOfflineDTO> list = new ArrayList<AddonsDiscountOfflineDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code, name, user_group_code, active_from_date, active_to_date, day_of_week, discount_value, is_percentage, is_travel_date, max_discount_amount, min_seat_count, min_seat_fare, schedule_code, route_code, active_flag FROM addons_discount_offline WHERE namespace_id = ? AND active_flag < 2");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				AddonsDiscountOfflineDTO offlineDiscountDTO = new AddonsDiscountOfflineDTO();
				offlineDiscountDTO.setCode(selectRS.getString("code"));
				offlineDiscountDTO.setName(selectRS.getString("name"));

				offlineDiscountDTO.setActiveFrom(selectRS.getString("active_from_date"));
				offlineDiscountDTO.setActiveTo(selectRS.getString("active_to_date"));
				offlineDiscountDTO.setDayOfWeek(selectRS.getString("day_of_week"));
				offlineDiscountDTO.setValue(selectRS.getBigDecimal("discount_value"));
				offlineDiscountDTO.setPercentageFlag(selectRS.getInt("is_percentage") == 1 ? true : false);
				offlineDiscountDTO.setTravelDateFlag(selectRS.getInt("is_travel_date") == 1 ? true : false);
				offlineDiscountDTO.setMaxDiscountAmount(selectRS.getBigDecimal("max_discount_amount"));
				offlineDiscountDTO.setMinSeatCount(selectRS.getInt("min_seat_count"));
				offlineDiscountDTO.setMinTicketFare(selectRS.getInt("min_seat_fare"));

				String groupCode = selectRS.getString("user_group_code");
				String scheduleCodes = selectRS.getString("schedule_code");
				String routeCodes = selectRS.getString("route_code");
				if (StringUtil.isNotNull(groupCode)) {
					offlineDiscountDTO.setGroupCode(Arrays.asList(groupCode.split("\\s*,\\s*")));
				}
				else {
					offlineDiscountDTO.setGroupCode(new ArrayList<String>());
				}
				if (StringUtil.isNotNull(scheduleCodes)) {
					offlineDiscountDTO.setScheduleCode(Arrays.asList(scheduleCodes.split("\\s*,\\s*")));
				}
				else {
					offlineDiscountDTO.setScheduleCode(new ArrayList<String>());
				}
				if (StringUtil.isNotNull(routeCodes)) {
					offlineDiscountDTO.setRouteCode(Arrays.asList(routeCodes.split("\\s*,\\s*")));
 				}
				else {
					offlineDiscountDTO.setRouteCode(new ArrayList<String>());
				}
				offlineDiscountDTO.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(offlineDiscountDTO);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public AddonsDiscountOfflineDTO getOfflineDiscount(AuthDTO authDTO, String offlineDiscountCode) {
		AddonsDiscountOfflineDTO offlineDiscountDTO = null;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code, name, user_group_code, active_from_date, active_to_date, day_of_week, discount_value, is_percentage, is_travel_date, max_discount_amount, min_seat_count, min_seat_fare, schedule_code, route_code, active_flag FROM addons_discount_offline WHERE namespace_id = ? AND code = ? AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setString(2, offlineDiscountCode);
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				offlineDiscountDTO = new AddonsDiscountOfflineDTO();
				offlineDiscountDTO.setCode(selectRS.getString("code"));
				offlineDiscountDTO.setActiveFrom(selectRS.getString("active_from_date"));
				offlineDiscountDTO.setActiveTo(selectRS.getString("active_to_date"));
				offlineDiscountDTO.setDayOfWeek(selectRS.getString("day_of_week"));
				offlineDiscountDTO.setValue(selectRS.getBigDecimal("discount_value"));
				offlineDiscountDTO.setPercentageFlag(selectRS.getInt("is_percentage") == 1 ? true : false);
				offlineDiscountDTO.setTravelDateFlag(selectRS.getInt("is_travel_date") == 1 ? true : false);
				offlineDiscountDTO.setMaxDiscountAmount(selectRS.getBigDecimal("max_discount_amount"));
				offlineDiscountDTO.setMinSeatCount(selectRS.getInt("min_seat_count"));
				offlineDiscountDTO.setMinTicketFare(selectRS.getInt("min_seat_fare"));

				String groupCode = selectRS.getString("user_group_code");
				if (StringUtil.isNotNull(groupCode)) {
					offlineDiscountDTO.setGroupCode(Arrays.asList(groupCode.split(",")));
				}
				String scheduleCodes = selectRS.getString("schedule_code");
				String routeCodes = selectRS.getString("route_code");
				if (StringUtil.isNotNull(scheduleCodes)) {
					offlineDiscountDTO.setScheduleCode(Arrays.asList(scheduleCodes.split(",")));
				}
				if (StringUtil.isNotNull(routeCodes)) {
					offlineDiscountDTO.setRouteCode(Arrays.asList(routeCodes.split(",")));
				}
				offlineDiscountDTO.setActiveFlag(selectRS.getInt("active_flag"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return offlineDiscountDTO;
	}
}
