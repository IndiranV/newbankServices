package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import lombok.Cleanup;

import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.FareRuleDTO;
import org.in.com.dto.FareRuleDetailsDTO;
import org.in.com.dto.ScheduleStageDTO;
import org.in.com.dto.StateDTO;
import org.in.com.dto.StationDTO;
import org.in.com.exception.ServiceException;
import org.in.com.utils.StringUtil;

public class FareRuleDAO {
	public void getFareRule(AuthDTO authDTO, FareRuleDTO fareRuleDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			if (StringUtil.isNotNull(fareRuleDTO.getCode())) {
				selectPS = connection.prepareStatement("SELECT id, code, name, state_id, active_flag FROM fare_rule WHERE code = ? AND active_flag = 1");
				selectPS.setString(1, fareRuleDTO.getCode());
			}
			else if (fareRuleDTO.getId() != 0) {
				selectPS = connection.prepareStatement("SELECT id, code, name, state_id, active_flag FROM fare_rule WHERE id = ? AND active_flag = 1");
				selectPS.setInt(1, fareRuleDTO.getId());
			}
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				fareRuleDTO.setId(selectRS.getInt("id"));
				fareRuleDTO.setCode(selectRS.getString("code"));
				fareRuleDTO.setName(selectRS.getString("name"));

				StateDTO state = new StateDTO();
				state.setId(selectRS.getInt("state_id"));
				fareRuleDTO.setState(state);

				fareRuleDTO.setActiveFlag(selectRS.getInt("active_flag"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}

	}

	public void updateFareRule(AuthDTO authDTO, FareRuleDTO fareRuleDTO) {

		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_FARE_RULE_IUD(?,?,?,?,?, ?,?)}");
			callableStatement.setString(++pindex, fareRuleDTO.getCode());
			callableStatement.setString(++pindex, fareRuleDTO.getName());
			callableStatement.setInt(++pindex, fareRuleDTO.getState().getId());
			callableStatement.setInt(++pindex, fareRuleDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				fareRuleDTO.setCode(callableStatement.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public List<FareRuleDTO> getAllFareRule(AuthDTO authDTO) {
		List<FareRuleDTO> list = new ArrayList<FareRuleDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code, name, state_id, active_flag FROM fare_rule WHERE active_flag = 1");
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				FareRuleDTO fareRuleDTO = new FareRuleDTO();
				fareRuleDTO.setCode(selectRS.getString("code"));
				fareRuleDTO.setName(selectRS.getString("name"));

				StateDTO state = new StateDTO();
				state.setId(selectRS.getInt("state_id"));
				fareRuleDTO.setState(state);

				fareRuleDTO.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(fareRuleDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public List<FareRuleDetailsDTO> getFareRuleDetails(AuthDTO authDTO, FareRuleDTO fareRule, StationDTO fromStationDTO, StationDTO toStationDTO) {
		List<FareRuleDetailsDTO> list = new ArrayList<FareRuleDetailsDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			if (fareRule.getId() != 0 && fromStationDTO.getId() != 0 && toStationDTO.getId() != 0) {
				selectPS = connection.prepareStatement("SELECT from_station_id, to_station_id, distance, non_ac_seater_min_fare, non_ac_seater_max_fare, ac_seater_min_fare, ac_seater_max_fare, multi_axle_seater_min_fare, multi_axle_seater_max_fare, non_ac_sleeper_lower_min_fare, non_ac_sleeper_lower_max_fare, non_ac_sleeper_upper_min_fare, non_ac_sleeper_upper_max_fare, ac_sleeper_lower_min_fare, ac_sleeper_lower_max_fare, ac_sleeper_upper_min_fare, ac_sleeper_upper_max_fare, branded_ac_sleeper_min_fare, branded_ac_sleeper_max_fare, single_axle_ac_seater_min_fare, single_axle_ac_seater_max_fare, multi_axle_ac_sleeper_min_fare, multi_axle_ac_sleeper_max_fare, active_flag FROM fare_rule_details WHERE fare_rule_id = ? AND from_station_id = ? AND to_station_id = ? AND active_flag = 1");
				selectPS.setInt(1, fareRule.getId());
				selectPS.setInt(2, fromStationDTO.getId());
				selectPS.setInt(3, toStationDTO.getId());
			}
			else if (fareRule.getId() != 0 && fromStationDTO.getId() != 0 && toStationDTO.getId() == 0) {
				selectPS = connection.prepareStatement("SELECT from_station_id, to_station_id, distance, non_ac_seater_min_fare, non_ac_seater_max_fare, ac_seater_min_fare, ac_seater_max_fare, multi_axle_seater_min_fare, multi_axle_seater_max_fare, non_ac_sleeper_lower_min_fare, non_ac_sleeper_lower_max_fare, non_ac_sleeper_upper_min_fare, non_ac_sleeper_upper_max_fare, ac_sleeper_lower_min_fare, ac_sleeper_lower_max_fare, ac_sleeper_upper_min_fare, ac_sleeper_upper_max_fare, branded_ac_sleeper_min_fare, branded_ac_sleeper_max_fare, single_axle_ac_seater_min_fare, single_axle_ac_seater_max_fare, multi_axle_ac_sleeper_min_fare, multi_axle_ac_sleeper_max_fare, active_flag FROM fare_rule_details WHERE fare_rule_id = ? AND from_station_id = ? AND active_flag = 1");
				selectPS.setInt(1, fareRule.getId());
				selectPS.setInt(2, fromStationDTO.getId());
			}
			else if (fareRule.getId() != 0 && fromStationDTO.getId() == 0 && toStationDTO.getId() != 0) {
				selectPS = connection.prepareStatement("SELECT from_station_id, to_station_id, distance, non_ac_seater_min_fare, non_ac_seater_max_fare, ac_seater_min_fare, ac_seater_max_fare, multi_axle_seater_min_fare, multi_axle_seater_max_fare, non_ac_sleeper_lower_min_fare, non_ac_sleeper_lower_max_fare, non_ac_sleeper_upper_min_fare, non_ac_sleeper_upper_max_fare, ac_sleeper_lower_min_fare, ac_sleeper_lower_max_fare, ac_sleeper_upper_min_fare, ac_sleeper_upper_max_fare, branded_ac_sleeper_min_fare, branded_ac_sleeper_max_fare, single_axle_ac_seater_min_fare, single_axle_ac_seater_max_fare, multi_axle_ac_sleeper_min_fare, multi_axle_ac_sleeper_max_fare, active_flag FROM fare_rule_details WHERE fare_rule_id = ? AND to_station_id = ? AND active_flag = 1");
				selectPS.setInt(1, fareRule.getId());
				selectPS.setInt(2, toStationDTO.getId());
			}

			if (selectPS != null) {
				@Cleanup
				ResultSet selectRS = selectPS.executeQuery();
				while (selectRS.next()) {
					FareRuleDetailsDTO fareRuleDetailsDTO = new FareRuleDetailsDTO();

					StationDTO fromStation = new StationDTO();
					fromStation.setId(selectRS.getInt("from_station_id"));
					fareRuleDetailsDTO.setFromStation(fromStation);

					StationDTO toStation = new StationDTO();
					toStation.setId(selectRS.getInt("to_station_id"));
					fareRuleDetailsDTO.setToStation(toStation);

					fareRuleDetailsDTO.setDistance(selectRS.getInt("distance"));
					fareRuleDetailsDTO.setNonAcSeaterMinFare(selectRS.getBigDecimal("non_ac_seater_min_fare"));
					fareRuleDetailsDTO.setNonAcSeaterMaxFare(selectRS.getBigDecimal("non_ac_seater_max_fare"));
					fareRuleDetailsDTO.setAcSeaterMinFare(selectRS.getBigDecimal("ac_seater_min_fare"));
					fareRuleDetailsDTO.setAcSeaterMaxFare(selectRS.getBigDecimal("ac_seater_max_fare"));
					fareRuleDetailsDTO.setMultiAxleSeaterMinFare(selectRS.getBigDecimal("multi_axle_seater_min_fare"));
					fareRuleDetailsDTO.setMultiAxleSeaterMaxFare(selectRS.getBigDecimal("multi_axle_seater_max_fare"));
					fareRuleDetailsDTO.setNonAcSleeperLowerMinFare(selectRS.getBigDecimal("non_ac_sleeper_lower_min_fare"));
					fareRuleDetailsDTO.setNonAcSleeperLowerMaxFare(selectRS.getBigDecimal("non_ac_sleeper_lower_max_fare"));
					fareRuleDetailsDTO.setNonAcSleeperUpperMinFare(selectRS.getBigDecimal("non_ac_sleeper_upper_min_fare"));
					fareRuleDetailsDTO.setNonAcSleeperUpperMaxFare(selectRS.getBigDecimal("non_ac_sleeper_upper_max_fare"));
					fareRuleDetailsDTO.setAcSleeperLowerMinFare(selectRS.getBigDecimal("ac_sleeper_lower_min_fare"));
					fareRuleDetailsDTO.setAcSleeperLowerMaxFare(selectRS.getBigDecimal("ac_sleeper_lower_max_fare"));
					fareRuleDetailsDTO.setAcSleeperUpperMinFare(selectRS.getBigDecimal("ac_sleeper_upper_min_fare"));
					fareRuleDetailsDTO.setAcSleeperUpperMaxFare(selectRS.getBigDecimal("ac_sleeper_upper_max_fare"));
					fareRuleDetailsDTO.setBrandedAcSleeperMinFare(selectRS.getBigDecimal("branded_ac_sleeper_min_fare"));
					fareRuleDetailsDTO.setBrandedAcSleeperMaxFare(selectRS.getBigDecimal("branded_ac_sleeper_max_fare"));
					fareRuleDetailsDTO.setSingleAxleAcSeaterMinFare(selectRS.getBigDecimal("single_axle_ac_seater_min_fare"));
					fareRuleDetailsDTO.setSingleAxleAcSeaterMaxFare(selectRS.getBigDecimal("single_axle_ac_seater_max_fare"));
					fareRuleDetailsDTO.setMultiAxleAcSleeperMinFare(selectRS.getBigDecimal("multi_axle_ac_sleeper_min_fare"));
					fareRuleDetailsDTO.setMultiAxleAcSleeperMaxFare(selectRS.getBigDecimal("multi_axle_ac_sleeper_max_fare"));
					fareRuleDetailsDTO.setActiveFlag(selectRS.getInt("active_flag"));
					list.add(fareRuleDetailsDTO);

				}
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public void updateFareRuleDetails(AuthDTO authDTO, FareRuleDTO fareRule) {
		try {
			int syncFlag = 0;
			if (authDTO.getAdditionalAttribute() != null && authDTO.getAdditionalAttribute().containsKey(Text.FARE_RULE_SYNC_FLAG)) {
				syncFlag = 1;
			}
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_FARE_RULE_DETAILS_IUD(?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?)}");
			for (FareRuleDetailsDTO fareRuleDetailsDTO : fareRule.getFareRuleDetails()) {
				int pindex = 0;
				if (fareRuleDetailsDTO.getFromStation() == null || fareRuleDetailsDTO.getToStation() == null || fareRuleDetailsDTO.getFromStation().getId() == 0 || fareRuleDetailsDTO.getToStation().getId() == 0) {
					continue;
				}
				callableStatement.setString(++pindex, fareRule.getCode());
				callableStatement.setInt(++pindex, fareRuleDetailsDTO.getFromStation().getId());
				callableStatement.setInt(++pindex, fareRuleDetailsDTO.getToStation().getId());
				callableStatement.setFloat(++pindex, fareRuleDetailsDTO.getDistance());
				callableStatement.setBigDecimal(++pindex, fareRuleDetailsDTO.getNonAcSeaterMinFare());
				callableStatement.setBigDecimal(++pindex, fareRuleDetailsDTO.getNonAcSeaterMaxFare());
				callableStatement.setBigDecimal(++pindex, fareRuleDetailsDTO.getAcSeaterMinFare());
				callableStatement.setBigDecimal(++pindex, fareRuleDetailsDTO.getAcSeaterMaxFare());
				callableStatement.setBigDecimal(++pindex, fareRuleDetailsDTO.getMultiAxleSeaterMinFare());
				callableStatement.setBigDecimal(++pindex, fareRuleDetailsDTO.getMultiAxleSeaterMaxFare());
				callableStatement.setBigDecimal(++pindex, fareRuleDetailsDTO.getNonAcSleeperLowerMinFare());
				callableStatement.setBigDecimal(++pindex, fareRuleDetailsDTO.getNonAcSleeperLowerMaxFare());
				callableStatement.setBigDecimal(++pindex, fareRuleDetailsDTO.getNonAcSleeperUpperMinFare());
				callableStatement.setBigDecimal(++pindex, fareRuleDetailsDTO.getNonAcSleeperUpperMaxFare());
				callableStatement.setBigDecimal(++pindex, fareRuleDetailsDTO.getAcSleeperLowerMinFare());
				callableStatement.setBigDecimal(++pindex, fareRuleDetailsDTO.getAcSleeperLowerMaxFare());
				callableStatement.setBigDecimal(++pindex, fareRuleDetailsDTO.getAcSleeperUpperMinFare());
				callableStatement.setBigDecimal(++pindex, fareRuleDetailsDTO.getAcSleeperUpperMaxFare());
				callableStatement.setBigDecimal(++pindex, fareRuleDetailsDTO.getBrandedAcSleeperMinFare());
				callableStatement.setBigDecimal(++pindex, fareRuleDetailsDTO.getBrandedAcSleeperMaxFare());
				callableStatement.setBigDecimal(++pindex, fareRuleDetailsDTO.getSingleAxleAcSeaterMinFare());
				callableStatement.setBigDecimal(++pindex, fareRuleDetailsDTO.getSingleAxleAcSeaterMaxFare());
				callableStatement.setBigDecimal(++pindex, fareRuleDetailsDTO.getMultiAxleAcSleeperMinFare());
				callableStatement.setBigDecimal(++pindex, fareRuleDetailsDTO.getMultiAxleAcSleeperMaxFare());
				callableStatement.setInt(++pindex, syncFlag);
				callableStatement.setInt(++pindex, fareRuleDetailsDTO.getActiveFlag());
				callableStatement.setInt(++pindex, authDTO.getUser().getId());
				callableStatement.setInt(++pindex, 0);
				callableStatement.addBatch();
			}
			callableStatement.executeBatch();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public List<FareRuleDetailsDTO> getFareRuleDetailsBySchedule(AuthDTO authDTO, FareRuleDTO fareRule, List<ScheduleStageDTO> scheduleStageList) {
		List<FareRuleDetailsDTO> fareRuleDetailsList = new ArrayList<FareRuleDetailsDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT from_station_id, to_station_id, distance, non_ac_seater_min_fare, non_ac_seater_max_fare, ac_seater_min_fare, ac_seater_max_fare, multi_axle_seater_min_fare, multi_axle_seater_max_fare, non_ac_sleeper_lower_min_fare, non_ac_sleeper_lower_max_fare, non_ac_sleeper_upper_min_fare, non_ac_sleeper_upper_max_fare, ac_sleeper_lower_min_fare, ac_sleeper_lower_max_fare, ac_sleeper_upper_min_fare, ac_sleeper_upper_max_fare, branded_ac_sleeper_min_fare, branded_ac_sleeper_max_fare, single_axle_ac_seater_min_fare, single_axle_ac_seater_max_fare, multi_axle_ac_sleeper_min_fare, multi_axle_ac_sleeper_max_fare, active_flag FROM fare_rule_details WHERE fare_rule_id = ? AND from_station_id = ? AND to_station_id = ? AND active_flag = 1");
			for (ScheduleStageDTO stageDTO : scheduleStageList) {
				selectPS.setInt(1, fareRule.getId());
				selectPS.setInt(2, stageDTO.getFromStation().getId());
				selectPS.setInt(3, stageDTO.getToStation().getId());
				@Cleanup
				ResultSet selectRS = selectPS.executeQuery();

				while (selectRS.next()) {
					FareRuleDetailsDTO fareRuleDetailsDTO = new FareRuleDetailsDTO();

					StationDTO fromStationDTO = new StationDTO();
					fromStationDTO.setId(selectRS.getInt("from_station_id"));
					fareRuleDetailsDTO.setFromStation(fromStationDTO);

					StationDTO toStationDTO = new StationDTO();
					toStationDTO.setId(selectRS.getInt("to_station_id"));
					fareRuleDetailsDTO.setToStation(toStationDTO);

					fareRuleDetailsDTO.setDistance(selectRS.getInt("distance"));
					fareRuleDetailsDTO.setNonAcSeaterMinFare(selectRS.getBigDecimal("non_ac_seater_min_fare"));
					fareRuleDetailsDTO.setNonAcSeaterMaxFare(selectRS.getBigDecimal("non_ac_seater_max_fare"));
					fareRuleDetailsDTO.setAcSeaterMinFare(selectRS.getBigDecimal("ac_seater_min_fare"));
					fareRuleDetailsDTO.setAcSeaterMaxFare(selectRS.getBigDecimal("ac_seater_max_fare"));
					fareRuleDetailsDTO.setMultiAxleSeaterMinFare(selectRS.getBigDecimal("multi_axle_seater_min_fare"));
					fareRuleDetailsDTO.setMultiAxleSeaterMaxFare(selectRS.getBigDecimal("multi_axle_seater_max_fare"));
					fareRuleDetailsDTO.setNonAcSleeperLowerMinFare(selectRS.getBigDecimal("non_ac_sleeper_lower_min_fare"));
					fareRuleDetailsDTO.setNonAcSleeperLowerMaxFare(selectRS.getBigDecimal("non_ac_sleeper_lower_max_fare"));
					fareRuleDetailsDTO.setNonAcSleeperUpperMinFare(selectRS.getBigDecimal("non_ac_sleeper_upper_min_fare"));
					fareRuleDetailsDTO.setNonAcSleeperUpperMaxFare(selectRS.getBigDecimal("non_ac_sleeper_upper_max_fare"));
					fareRuleDetailsDTO.setAcSleeperLowerMinFare(selectRS.getBigDecimal("ac_sleeper_lower_min_fare"));
					fareRuleDetailsDTO.setAcSleeperLowerMaxFare(selectRS.getBigDecimal("ac_sleeper_lower_max_fare"));
					fareRuleDetailsDTO.setAcSleeperUpperMinFare(selectRS.getBigDecimal("ac_sleeper_upper_min_fare"));
					fareRuleDetailsDTO.setAcSleeperUpperMaxFare(selectRS.getBigDecimal("ac_sleeper_upper_max_fare"));
					fareRuleDetailsDTO.setBrandedAcSleeperMinFare(selectRS.getBigDecimal("branded_ac_sleeper_min_fare"));
					fareRuleDetailsDTO.setBrandedAcSleeperMaxFare(selectRS.getBigDecimal("branded_ac_sleeper_max_fare"));
					fareRuleDetailsDTO.setSingleAxleAcSeaterMinFare(selectRS.getBigDecimal("single_axle_ac_seater_min_fare"));
					fareRuleDetailsDTO.setSingleAxleAcSeaterMaxFare(selectRS.getBigDecimal("single_axle_ac_seater_max_fare"));
					fareRuleDetailsDTO.setMultiAxleAcSleeperMinFare(selectRS.getBigDecimal("multi_axle_ac_sleeper_min_fare"));
					fareRuleDetailsDTO.setMultiAxleAcSleeperMaxFare(selectRS.getBigDecimal("multi_axle_ac_sleeper_max_fare"));
					fareRuleDetailsDTO.setActiveFlag(selectRS.getInt("active_flag"));

					fareRuleDetailsList.add(fareRuleDetailsDTO);
				}
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return fareRuleDetailsList;
	}

	public FareRuleDTO getFareRuleDetailsByFareRule(AuthDTO authDTO, FareRuleDTO fareRule, StationDTO fromStationDTO, StationDTO toStationDTO) {
		FareRuleDTO fareRuleDTO = new FareRuleDTO();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id, code, name FROM fare_rule WHERE code = ? AND active_flag = 1");
			selectPS.setString(1, fareRule.getCode());

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS != null) {
				if (selectRS.next()) {
					fareRuleDTO.setId(selectRS.getInt("id"));
					fareRuleDTO.setCode(selectRS.getString("code"));
					fareRuleDTO.setName(selectRS.getString("name"));

					List<FareRuleDetailsDTO> fareRuleDetailsList = new ArrayList<FareRuleDetailsDTO>();
					@Cleanup
					PreparedStatement selectDetailsPS = null;
					if (fareRuleDTO.getId() != 0 && fromStationDTO.getId() == 0 && toStationDTO.getId() == 0) {
						selectDetailsPS = connection.prepareStatement("SELECT from_station_id, to_station_id, distance, non_ac_seater_min_fare, non_ac_seater_max_fare, ac_seater_min_fare, ac_seater_max_fare, multi_axle_seater_min_fare, multi_axle_seater_max_fare, non_ac_sleeper_lower_min_fare, non_ac_sleeper_lower_max_fare, non_ac_sleeper_upper_min_fare, non_ac_sleeper_upper_max_fare, ac_sleeper_lower_min_fare, ac_sleeper_lower_max_fare, ac_sleeper_upper_min_fare, ac_sleeper_upper_max_fare, branded_ac_sleeper_min_fare, branded_ac_sleeper_max_fare, single_axle_ac_seater_min_fare, single_axle_ac_seater_max_fare, multi_axle_ac_sleeper_min_fare, multi_axle_ac_sleeper_max_fare, active_flag, updated_at FROM fare_rule_details WHERE fare_rule_id = ? AND active_flag = 1");
						selectDetailsPS.setInt(1, fareRuleDTO.getId());
					}
					else if (fareRuleDTO.getId() != 0 && fromStationDTO.getId() != 0 && toStationDTO.getId() != 0) {
						selectDetailsPS = connection.prepareStatement("SELECT from_station_id, to_station_id, distance, non_ac_seater_min_fare, non_ac_seater_max_fare, ac_seater_min_fare, ac_seater_max_fare, multi_axle_seater_min_fare, multi_axle_seater_max_fare, non_ac_sleeper_lower_min_fare, non_ac_sleeper_lower_max_fare, non_ac_sleeper_upper_min_fare, non_ac_sleeper_upper_max_fare, ac_sleeper_lower_min_fare, ac_sleeper_lower_max_fare, ac_sleeper_upper_min_fare, ac_sleeper_upper_max_fare, branded_ac_sleeper_min_fare, branded_ac_sleeper_max_fare, single_axle_ac_seater_min_fare, single_axle_ac_seater_max_fare, multi_axle_ac_sleeper_min_fare, multi_axle_ac_sleeper_max_fare, active_flag, updated_at FROM fare_rule_details WHERE fare_rule_id = ? AND from_station_id = ? AND to_station_id = ? AND active_flag = 1");
						selectDetailsPS.setInt(1, fareRuleDTO.getId());
						selectDetailsPS.setInt(2, fromStationDTO.getId());
						selectDetailsPS.setInt(3, toStationDTO.getId());
					}
					else if (fareRuleDTO.getId() != 0 && fromStationDTO.getId() != 0 && toStationDTO.getId() == 0) {
						selectDetailsPS = connection.prepareStatement("SELECT from_station_id, to_station_id, distance, non_ac_seater_min_fare, non_ac_seater_max_fare, ac_seater_min_fare, ac_seater_max_fare, multi_axle_seater_min_fare, multi_axle_seater_max_fare, non_ac_sleeper_lower_min_fare, non_ac_sleeper_lower_max_fare, non_ac_sleeper_upper_min_fare, non_ac_sleeper_upper_max_fare, ac_sleeper_lower_min_fare, ac_sleeper_lower_max_fare, ac_sleeper_upper_min_fare, ac_sleeper_upper_max_fare, branded_ac_sleeper_min_fare, branded_ac_sleeper_max_fare, single_axle_ac_seater_min_fare, single_axle_ac_seater_max_fare, multi_axle_ac_sleeper_min_fare, multi_axle_ac_sleeper_max_fare, active_flag, updated_at FROM fare_rule_details WHERE fare_rule_id = ? AND from_station_id = ? AND active_flag = 1");
						selectDetailsPS.setInt(1, fareRuleDTO.getId());
						selectDetailsPS.setInt(2, fromStationDTO.getId());
					}
					else if (fareRuleDTO.getId() != 0 && toStationDTO.getId() != 0 && fromStationDTO.getId() == 0) {
						selectDetailsPS = connection.prepareStatement("SELECT from_station_id, to_station_id, distance, non_ac_seater_min_fare, non_ac_seater_max_fare, ac_seater_min_fare, ac_seater_max_fare, multi_axle_seater_min_fare, multi_axle_seater_max_fare, non_ac_sleeper_lower_min_fare, non_ac_sleeper_lower_max_fare, non_ac_sleeper_upper_min_fare, non_ac_sleeper_upper_max_fare, ac_sleeper_lower_min_fare, ac_sleeper_lower_max_fare, ac_sleeper_upper_min_fare, ac_sleeper_upper_max_fare, branded_ac_sleeper_min_fare, branded_ac_sleeper_max_fare, single_axle_ac_seater_min_fare, single_axle_ac_seater_max_fare, multi_axle_ac_sleeper_min_fare, multi_axle_ac_sleeper_max_fare, active_flag, updated_at FROM fare_rule_details WHERE fare_rule_id = ? AND to_station_id = ? AND active_flag = 1");
						selectDetailsPS.setInt(1, fareRuleDTO.getId());
						selectDetailsPS.setInt(2, toStationDTO.getId());
					}
					@Cleanup
					ResultSet selectDetailsRS = selectDetailsPS.executeQuery();
					if (selectDetailsRS != null) {
						while (selectDetailsRS.next()) {
							FareRuleDetailsDTO fareRuleDetailsDTO = new FareRuleDetailsDTO();

							StationDTO fromStation = new StationDTO();
							fromStation.setId(selectDetailsRS.getInt("from_station_id"));
							fareRuleDetailsDTO.setFromStation(fromStation);

							StationDTO toStation = new StationDTO();
							toStation.setId(selectDetailsRS.getInt("to_station_id"));
							fareRuleDetailsDTO.setToStation(toStation);

							fareRuleDetailsDTO.setDistance(selectDetailsRS.getInt("distance"));
							fareRuleDetailsDTO.setNonAcSeaterMinFare(selectDetailsRS.getBigDecimal("non_ac_seater_min_fare"));
							fareRuleDetailsDTO.setNonAcSeaterMaxFare(selectDetailsRS.getBigDecimal("non_ac_seater_max_fare"));
							fareRuleDetailsDTO.setAcSeaterMinFare(selectDetailsRS.getBigDecimal("ac_seater_min_fare"));
							fareRuleDetailsDTO.setAcSeaterMaxFare(selectDetailsRS.getBigDecimal("ac_seater_max_fare"));
							fareRuleDetailsDTO.setMultiAxleSeaterMinFare(selectDetailsRS.getBigDecimal("multi_axle_seater_min_fare"));
							fareRuleDetailsDTO.setMultiAxleSeaterMaxFare(selectDetailsRS.getBigDecimal("multi_axle_seater_max_fare"));
							fareRuleDetailsDTO.setNonAcSleeperLowerMinFare(selectDetailsRS.getBigDecimal("non_ac_sleeper_lower_min_fare"));
							fareRuleDetailsDTO.setNonAcSleeperLowerMaxFare(selectDetailsRS.getBigDecimal("non_ac_sleeper_lower_max_fare"));
							fareRuleDetailsDTO.setNonAcSleeperUpperMinFare(selectDetailsRS.getBigDecimal("non_ac_sleeper_upper_min_fare"));
							fareRuleDetailsDTO.setNonAcSleeperUpperMaxFare(selectDetailsRS.getBigDecimal("non_ac_sleeper_upper_max_fare"));
							fareRuleDetailsDTO.setAcSleeperLowerMinFare(selectDetailsRS.getBigDecimal("ac_sleeper_lower_min_fare"));
							fareRuleDetailsDTO.setAcSleeperLowerMaxFare(selectDetailsRS.getBigDecimal("ac_sleeper_lower_max_fare"));
							fareRuleDetailsDTO.setAcSleeperUpperMinFare(selectDetailsRS.getBigDecimal("ac_sleeper_upper_min_fare"));
							fareRuleDetailsDTO.setAcSleeperUpperMaxFare(selectDetailsRS.getBigDecimal("ac_sleeper_upper_max_fare"));
							fareRuleDetailsDTO.setBrandedAcSleeperMinFare(selectDetailsRS.getBigDecimal("branded_ac_sleeper_min_fare"));
							fareRuleDetailsDTO.setBrandedAcSleeperMaxFare(selectDetailsRS.getBigDecimal("branded_ac_sleeper_max_fare"));
							fareRuleDetailsDTO.setSingleAxleAcSeaterMinFare(selectDetailsRS.getBigDecimal("single_axle_ac_seater_min_fare"));
							fareRuleDetailsDTO.setSingleAxleAcSeaterMaxFare(selectDetailsRS.getBigDecimal("single_axle_ac_seater_max_fare"));
							fareRuleDetailsDTO.setMultiAxleAcSleeperMinFare(selectDetailsRS.getBigDecimal("multi_axle_ac_sleeper_min_fare"));
							fareRuleDetailsDTO.setMultiAxleAcSleeperMaxFare(selectDetailsRS.getBigDecimal("multi_axle_ac_sleeper_max_fare"));
							fareRuleDetailsDTO.setActiveFlag(selectDetailsRS.getInt("active_flag"));
							fareRuleDetailsDTO.setUpdatedAt(selectDetailsRS.getString("updated_at"));
							fareRuleDetailsList.add(fareRuleDetailsDTO);
						}
					}
					fareRuleDTO.setFareRuleDetails(fareRuleDetailsList);
				}
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return fareRuleDTO;
	}

	public FareRuleDetailsDTO getFareRuleDetailsV2(AuthDTO authDTO, FareRuleDTO fareRule, StationDTO fromStationDTO, StationDTO toStationDTO) {
		FareRuleDetailsDTO fareRuleDetailsDTO = new FareRuleDetailsDTO();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id, from_station_id, to_station_id, distance, non_ac_seater_min_fare, non_ac_seater_max_fare, ac_seater_min_fare, ac_seater_max_fare, multi_axle_seater_min_fare, multi_axle_seater_max_fare, non_ac_sleeper_lower_min_fare, non_ac_sleeper_lower_max_fare, non_ac_sleeper_upper_min_fare, non_ac_sleeper_upper_max_fare, ac_sleeper_lower_min_fare, ac_sleeper_lower_max_fare, ac_sleeper_upper_min_fare, ac_sleeper_upper_max_fare, branded_ac_sleeper_min_fare, branded_ac_sleeper_max_fare, single_axle_ac_seater_min_fare, single_axle_ac_seater_max_fare, multi_axle_ac_sleeper_min_fare, multi_axle_ac_sleeper_max_fare, active_flag FROM fare_rule_details WHERE fare_rule_id = ? AND from_station_id = ? AND to_station_id = ? AND active_flag = 1");
			selectPS.setInt(1, fareRule.getId());
			selectPS.setInt(2, fromStationDTO.getId());
			selectPS.setInt(3, toStationDTO.getId());

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				fareRuleDetailsDTO.setId(selectRS.getInt("id"));

				StationDTO fromStation = new StationDTO();
				fromStation.setId(selectRS.getInt("from_station_id"));
				fareRuleDetailsDTO.setFromStation(fromStation);

				StationDTO toStation = new StationDTO();
				toStation.setId(selectRS.getInt("to_station_id"));
				fareRuleDetailsDTO.setToStation(toStation);

				fareRuleDetailsDTO.setDistance(selectRS.getInt("distance"));
				fareRuleDetailsDTO.setNonAcSeaterMinFare(selectRS.getBigDecimal("non_ac_seater_min_fare"));
				fareRuleDetailsDTO.setNonAcSeaterMaxFare(selectRS.getBigDecimal("non_ac_seater_max_fare"));
				fareRuleDetailsDTO.setAcSeaterMinFare(selectRS.getBigDecimal("ac_seater_min_fare"));
				fareRuleDetailsDTO.setAcSeaterMaxFare(selectRS.getBigDecimal("ac_seater_max_fare"));
				fareRuleDetailsDTO.setMultiAxleSeaterMinFare(selectRS.getBigDecimal("multi_axle_seater_min_fare"));
				fareRuleDetailsDTO.setMultiAxleSeaterMaxFare(selectRS.getBigDecimal("multi_axle_seater_max_fare"));
				fareRuleDetailsDTO.setNonAcSleeperLowerMinFare(selectRS.getBigDecimal("non_ac_sleeper_lower_min_fare"));
				fareRuleDetailsDTO.setNonAcSleeperLowerMaxFare(selectRS.getBigDecimal("non_ac_sleeper_lower_max_fare"));
				fareRuleDetailsDTO.setNonAcSleeperUpperMinFare(selectRS.getBigDecimal("non_ac_sleeper_upper_min_fare"));
				fareRuleDetailsDTO.setNonAcSleeperUpperMaxFare(selectRS.getBigDecimal("non_ac_sleeper_upper_max_fare"));
				fareRuleDetailsDTO.setAcSleeperLowerMinFare(selectRS.getBigDecimal("ac_sleeper_lower_min_fare"));
				fareRuleDetailsDTO.setAcSleeperLowerMaxFare(selectRS.getBigDecimal("ac_sleeper_lower_max_fare"));
				fareRuleDetailsDTO.setAcSleeperUpperMinFare(selectRS.getBigDecimal("ac_sleeper_upper_min_fare"));
				fareRuleDetailsDTO.setAcSleeperUpperMaxFare(selectRS.getBigDecimal("ac_sleeper_upper_max_fare"));
				fareRuleDetailsDTO.setBrandedAcSleeperMinFare(selectRS.getBigDecimal("branded_ac_sleeper_min_fare"));
				fareRuleDetailsDTO.setBrandedAcSleeperMaxFare(selectRS.getBigDecimal("branded_ac_sleeper_max_fare"));
				fareRuleDetailsDTO.setSingleAxleAcSeaterMinFare(selectRS.getBigDecimal("single_axle_ac_seater_min_fare"));
				fareRuleDetailsDTO.setSingleAxleAcSeaterMaxFare(selectRS.getBigDecimal("single_axle_ac_seater_max_fare"));
				fareRuleDetailsDTO.setMultiAxleAcSleeperMinFare(selectRS.getBigDecimal("multi_axle_ac_sleeper_min_fare"));
				fareRuleDetailsDTO.setMultiAxleAcSleeperMaxFare(selectRS.getBigDecimal("multi_axle_ac_sleeper_max_fare"));
				fareRuleDetailsDTO.setActiveFlag(selectRS.getInt("active_flag"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return fareRuleDetailsDTO;
	}

	public List<FareRuleDetailsDTO> getZoneSyncFareRuleDetails(AuthDTO authDTO, FareRuleDTO fareRule, String syncDate) {
		List<FareRuleDetailsDTO> list = new ArrayList<FareRuleDetailsDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT from_station_id, to_station_id, distance, non_ac_seater_min_fare, non_ac_seater_max_fare, ac_seater_min_fare, ac_seater_max_fare, multi_axle_seater_min_fare, multi_axle_seater_max_fare, non_ac_sleeper_lower_min_fare, non_ac_sleeper_lower_max_fare, non_ac_sleeper_upper_min_fare, non_ac_sleeper_upper_max_fare, ac_sleeper_lower_min_fare, ac_sleeper_lower_max_fare, ac_sleeper_upper_min_fare, ac_sleeper_upper_max_fare, branded_ac_sleeper_min_fare, branded_ac_sleeper_max_fare, single_axle_ac_seater_min_fare, single_axle_ac_seater_max_fare, multi_axle_ac_sleeper_min_fare, multi_axle_ac_sleeper_max_fare, frd.active_flag FROM fare_rule fr, fare_rule_details frd WHERE fr.code = ? AND fr.id = frd.fare_rule_id AND DATE(frd.updated_at) >= ?");
			selectPS.setString(1, fareRule.getCode());
			selectPS.setString(2, syncDate);

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				FareRuleDetailsDTO fareRuleDetailsDTO = new FareRuleDetailsDTO();

				StationDTO fromStation = new StationDTO();
				fromStation.setId(selectRS.getInt("from_station_id"));
				fareRuleDetailsDTO.setFromStation(fromStation);

				StationDTO toStation = new StationDTO();
				toStation.setId(selectRS.getInt("to_station_id"));
				fareRuleDetailsDTO.setToStation(toStation);

				fareRuleDetailsDTO.setDistance(selectRS.getInt("distance"));
				fareRuleDetailsDTO.setNonAcSeaterMinFare(selectRS.getBigDecimal("non_ac_seater_min_fare"));
				fareRuleDetailsDTO.setNonAcSeaterMaxFare(selectRS.getBigDecimal("non_ac_seater_max_fare"));
				fareRuleDetailsDTO.setAcSeaterMinFare(selectRS.getBigDecimal("ac_seater_min_fare"));
				fareRuleDetailsDTO.setAcSeaterMaxFare(selectRS.getBigDecimal("ac_seater_max_fare"));
				fareRuleDetailsDTO.setMultiAxleSeaterMinFare(selectRS.getBigDecimal("multi_axle_seater_min_fare"));
				fareRuleDetailsDTO.setMultiAxleSeaterMaxFare(selectRS.getBigDecimal("multi_axle_seater_max_fare"));
				fareRuleDetailsDTO.setNonAcSleeperLowerMinFare(selectRS.getBigDecimal("non_ac_sleeper_lower_min_fare"));
				fareRuleDetailsDTO.setNonAcSleeperLowerMaxFare(selectRS.getBigDecimal("non_ac_sleeper_lower_max_fare"));
				fareRuleDetailsDTO.setNonAcSleeperUpperMinFare(selectRS.getBigDecimal("non_ac_sleeper_upper_min_fare"));
				fareRuleDetailsDTO.setNonAcSleeperUpperMaxFare(selectRS.getBigDecimal("non_ac_sleeper_upper_max_fare"));
				fareRuleDetailsDTO.setAcSleeperLowerMinFare(selectRS.getBigDecimal("ac_sleeper_lower_min_fare"));
				fareRuleDetailsDTO.setAcSleeperLowerMaxFare(selectRS.getBigDecimal("ac_sleeper_lower_max_fare"));
				fareRuleDetailsDTO.setAcSleeperUpperMinFare(selectRS.getBigDecimal("ac_sleeper_upper_min_fare"));
				fareRuleDetailsDTO.setAcSleeperUpperMaxFare(selectRS.getBigDecimal("ac_sleeper_upper_max_fare"));
				fareRuleDetailsDTO.setBrandedAcSleeperMinFare(selectRS.getBigDecimal("branded_ac_sleeper_min_fare"));
				fareRuleDetailsDTO.setBrandedAcSleeperMaxFare(selectRS.getBigDecimal("branded_ac_sleeper_max_fare"));
				fareRuleDetailsDTO.setSingleAxleAcSeaterMinFare(selectRS.getBigDecimal("single_axle_ac_seater_min_fare"));
				fareRuleDetailsDTO.setSingleAxleAcSeaterMaxFare(selectRS.getBigDecimal("single_axle_ac_seater_max_fare"));
				fareRuleDetailsDTO.setMultiAxleAcSleeperMinFare(selectRS.getBigDecimal("multi_axle_ac_sleeper_min_fare"));
				fareRuleDetailsDTO.setMultiAxleAcSleeperMaxFare(selectRS.getBigDecimal("multi_axle_ac_sleeper_max_fare"));
				fareRuleDetailsDTO.setActiveFlag(selectRS.getInt("frd.active_flag"));
				list.add(fareRuleDetailsDTO);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public String getZoneSyncDate(AuthDTO authDTO, FareRuleDTO fareRule) {
		String zoneSyncDate = null;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT DATE(MAX(frd.updated_at)) as zoneSyncDate FROM fare_rule fr, fare_rule_details frd WHERE fr.code = ? AND fr.id = frd.fare_rule_id");
			selectPS.setString(1, fareRule.getCode());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				zoneSyncDate = selectRS.getString("zoneSyncDate");
			}
			if (StringUtil.isNull(zoneSyncDate)) {
				zoneSyncDate = "2019-12-30";
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return zoneSyncDate;
	}

}
