package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.UserStationPointDTO;
import org.in.com.exception.ServiceException;
import org.in.com.utils.StringUtil;

import lombok.Cleanup;

public class StationPointDAO {
	public List<StationPointDTO> getAllStationPoints(AuthDTO authDTO) {
		List<StationPointDTO> list = new ArrayList<StationPointDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT poit.code,stat.code,stat.name,poit.name,poit.address,poit.landmark,poit.contact_number,poit.latitude,poit.longitude,poit.map_url,poit.amenities_code,poit.active_flag FROM station_point poit ,station stat WHERE poit.station_id = stat.id AND namespace_id = ? AND poit.active_flag < 2");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				StationPointDTO dto = new StationPointDTO();
				StationDTO stationDTO = new StationDTO();
				stationDTO.setCode(selectRS.getString("stat.code"));
				stationDTO.setName(selectRS.getString("stat.name"));
				dto.setStation(stationDTO);
				dto.setCode(selectRS.getString("poit.code"));
				dto.setName(selectRS.getString("poit.name"));
				dto.setAddress(selectRS.getString("poit.address"));
				dto.setLandmark(selectRS.getString("poit.landmark"));
				dto.setNumber(selectRS.getString("poit.contact_number"));
				dto.setLatitude(selectRS.getString("poit.latitude"));
				dto.setLongitude(selectRS.getString("poit.longitude"));
				dto.setMapUrl(selectRS.getString("poit.map_url"));
				dto.setAmenities(selectRS.getString("poit.amenities_code"));
				dto.setActiveFlag(selectRS.getInt("poit.active_flag"));
				list.add(dto);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public List<StationPointDTO> getStationPoint(AuthDTO authDTO, StationPointDTO stationPointDTO) {
		List<StationPointDTO> list = new ArrayList<StationPointDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT poit.code,poit.name,poit.address,poit.landmark,poit.contact_number,poit.latitude,poit.longitude,poit.map_url,poit.amenities_code,poit.active_flag FROM station_point poit WHERE  namespace_id = ? AND poit.station_id = ? AND poit.active_flag < 2");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, stationPointDTO.getStation().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				StationPointDTO dto = new StationPointDTO();
				dto.setStation(stationPointDTO.getStation());
				dto.setCode(selectRS.getString("poit.code"));
				dto.setName(selectRS.getString("poit.name"));
				dto.setAddress(selectRS.getString("poit.address"));
				dto.setLandmark(selectRS.getString("poit.landmark"));
				dto.setNumber(selectRS.getString("poit.contact_number"));
				dto.setLatitude(selectRS.getString("poit.latitude"));
				dto.setLongitude(selectRS.getString("poit.longitude"));
				dto.setMapUrl(selectRS.getString("poit.map_url"));
				dto.setAmenities(selectRS.getString("poit.amenities_code"));
				dto.setActiveFlag(selectRS.getInt("poit.active_flag"));
				list.add(dto);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public void getStationPointbyId(Connection connection, StationPointDTO stationPointDTO) {
		try {
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT poit.code,poit.name,poit.address,poit.landmark,poit.contact_number,poit.latitude,poit.longitude,poit.map_url,poit.amenities_code,poit.active_flag FROM station_point poit WHERE poit.id = ? AND poit.active_flag < 2");
			selectPS.setInt(1, stationPointDTO.getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				stationPointDTO.setStation(stationPointDTO.getStation());
				stationPointDTO.setCode(selectRS.getString("poit.code"));
				stationPointDTO.setName(selectRS.getString("poit.name"));
				stationPointDTO.setAddress(selectRS.getString("poit.address"));
				stationPointDTO.setLandmark(selectRS.getString("poit.landmark"));
				stationPointDTO.setNumber(selectRS.getString("poit.contact_number"));
				stationPointDTO.setLatitude(selectRS.getString("poit.latitude"));
				stationPointDTO.setLongitude(selectRS.getString("poit.longitude"));
				stationPointDTO.setMapUrl(selectRS.getString("poit.map_url"));
				stationPointDTO.setAmenities(selectRS.getString("poit.amenities_code"));
				stationPointDTO.setActiveFlag(selectRS.getInt("poit.active_flag"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public StationPointDTO getStationPointsUID(AuthDTO authDTO, StationPointDTO stationPointDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{call EZEE_SP_STATION_POINT_IUD(?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?)}");
			callableStatement.setString(++pindex, stationPointDTO.getCode());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setInt(++pindex, stationPointDTO.getStation().getId());
			callableStatement.setString(++pindex, stationPointDTO.getName());
			callableStatement.setString(++pindex, stationPointDTO.getAddress());
			callableStatement.setString(++pindex, stationPointDTO.getLandmark());
			callableStatement.setString(++pindex, stationPointDTO.getNumber());
			callableStatement.setString(++pindex, stationPointDTO.getLatitude());
			callableStatement.setString(++pindex, stationPointDTO.getLongitude());
			callableStatement.setString(++pindex, stationPointDTO.getMapUrl());
			callableStatement.setString(++pindex, stationPointDTO.getAmenities());
			callableStatement.setInt(++pindex, stationPointDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				stationPointDTO.setCode(callableStatement.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return null;
	}

	public StationPointDTO getStationPointbyIdCode(AuthDTO authDTO, StationPointDTO stationPointDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			if (stationPointDTO.getId() != 0) {
				selectPS = connection.prepareStatement("SELECT poit.id,poit.code,poit.name,poit.address,poit.landmark,poit.contact_number,poit.latitude,poit.longitude,poit.map_url,poit.amenities_code,poit.active_flag FROM station_point poit WHERE namespace_id = ? AND poit.id = ? AND poit.active_flag < 2");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setInt(2, stationPointDTO.getId());
			}
			else {
				selectPS = connection.prepareStatement("SELECT poit.id,poit.code,poit.name,poit.address,poit.landmark,poit.contact_number,poit.latitude,poit.longitude,poit.map_url,poit.amenities_code,poit.active_flag FROM station_point poit WHERE namespace_id = ? AND poit.code = ? AND poit.active_flag < 2");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setString(2, stationPointDTO.getCode());

			}
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				stationPointDTO.setStation(stationPointDTO.getStation());
				stationPointDTO.setId(selectRS.getInt("poit.id"));
				stationPointDTO.setCode(selectRS.getString("poit.code"));
				stationPointDTO.setName(selectRS.getString("poit.name"));
				stationPointDTO.setAddress(selectRS.getString("poit.address"));
				stationPointDTO.setLandmark(selectRS.getString("poit.landmark"));
				stationPointDTO.setNumber(selectRS.getString("poit.contact_number"));
				stationPointDTO.setLatitude(selectRS.getString("poit.latitude"));
				stationPointDTO.setLongitude(selectRS.getString("poit.longitude"));
				stationPointDTO.setMapUrl(selectRS.getString("poit.map_url"));
				stationPointDTO.setAmenities(selectRS.getString("poit.amenities_code"));
				stationPointDTO.setActiveFlag(selectRS.getInt("poit.active_flag"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return stationPointDTO;
	}

	public void updateUserSpecificStationPoint(AuthDTO authDTO, UserStationPointDTO userStationPointDTO) {
		try {
			StringBuilder groupList = new StringBuilder();
			for (GroupDTO group : userStationPointDTO.getGroupList()) {
				groupList.append(group.getCode());
				groupList.append(Text.COMMA);
			}
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement termSt = connection.prepareCall("{CALL EZEE_SP_USER_SPECIFIC_STATION_POINT_IUD(?,?,?,?,?,?, ?,?,?,?,?)}");
			termSt.setString(++pindex, userStationPointDTO.getCode());
			termSt.setInt(++pindex, authDTO.getNamespace().getId());
			termSt.setString(++pindex, userStationPointDTO.getUser().getCode());
			termSt.setString(++pindex, userStationPointDTO.getStation() != null ? userStationPointDTO.getStation().getCode() : null);
			termSt.setString(++pindex, userStationPointDTO.getStationPointIds());
			termSt.setString(++pindex, groupList.toString());
			termSt.setBigDecimal(++pindex, userStationPointDTO.getBoardingCommission());
			termSt.setInt(++pindex, userStationPointDTO.getActiveFlag());
			termSt.setInt(++pindex, authDTO.getUser().getId());
			termSt.setInt(++pindex, 0);
			termSt.registerOutParameter(++pindex, Types.INTEGER);
			termSt.execute();
			if (termSt.getInt("pitRowCount") > 0) {
				userStationPointDTO.setCode(termSt.getString("pcrCode"));
				userStationPointDTO.setActiveFlag(termSt.getInt("pitRowCount"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public List<UserStationPointDTO> getUserSpecificStationPoint(AuthDTO authDTO, UserDTO userDTO, StationDTO station) {
		List<UserStationPointDTO> list = new ArrayList<UserStationPointDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			if (station != null && station.getId() != 0) {
				selectPS = connection.prepareStatement("SELECT code, station_id, station_point_id, user_group_codes, boarding_commission_amount, active_flag FROM user_specific_station_point WHERE user_id = ? AND namespace_id = ? AND station_id = ? AND active_flag = 1");
				selectPS.setInt(1, userDTO.getId());
				selectPS.setInt(2, authDTO.getNamespace().getId());
				selectPS.setInt(3, station.getId());
			}
			else {
				selectPS = connection.prepareStatement("SELECT code, station_id, station_point_id, user_group_codes, boarding_commission_amount, active_flag FROM user_specific_station_point WHERE user_id = ? AND namespace_id = ? AND active_flag = 1");
				selectPS.setInt(1, userDTO.getId());
				selectPS.setInt(2, authDTO.getNamespace().getId());
			}
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				UserStationPointDTO userStationPointDTO = new UserStationPointDTO();
				userStationPointDTO.setCode(selectRS.getString("code"));
				StationDTO stationDTO = new StationDTO();
				stationDTO.setId(selectRS.getInt("station_id"));
				
				List<StationPointDTO> statoinPointList = convertStationPointList(selectRS.getString("station_point_id"));
				stationDTO.setStationPoints(statoinPointList);
				userStationPointDTO.setStation(stationDTO);
				
				String codes = selectRS.getString("user_group_codes");
				List<GroupDTO> userGroupList = new ArrayList<GroupDTO>();
				if (StringUtil.isNotNull(codes)) {
					for (String groupCode : codes.split(",")) {
						GroupDTO group = new GroupDTO();
						group.setCode(groupCode);
						userGroupList.add(group);
					}
				}
				userStationPointDTO.setGroupList(userGroupList);
				userStationPointDTO.setBoardingCommission(selectRS.getBigDecimal("boarding_commission_amount"));
				userStationPointDTO.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(userStationPointDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return list;
	}
	
	private List<StationPointDTO> convertStationPointList(String stationPointIds) {
		List<StationPointDTO> statoinPointList = Stream.of(stationPointIds.split(",")).filter(stpId -> StringUtil.isNotNull(stpId)).map(stpId -> {
        	StationPointDTO statinoPointDTO = new StationPointDTO();
        	statinoPointDTO.setId(Integer.valueOf(stpId));
        	return statinoPointDTO;
        }).collect(Collectors.toList());
		
		return statoinPointList;
	}

}
