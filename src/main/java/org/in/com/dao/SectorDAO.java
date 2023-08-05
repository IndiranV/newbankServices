package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusVehicleDTO;
import org.in.com.dto.OrganizationDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.SectorDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.UserDTO;
import org.in.com.exception.ServiceException;
import org.in.com.utils.StringUtil;

import lombok.Cleanup;

public class SectorDAO {

	public SectorDTO updateSector(AuthDTO authDTO, SectorDTO sectorDTO) {
		SectorDTO sector = new SectorDTO();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_SECTOR_IUD(?,?,?,?,?, ?,?,?,?,?, ?)}");
			callableStatement.setString(++pindex, sectorDTO.getCode());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, sectorDTO.getName());
			callableStatement.setString(++pindex, sectorDTO.getScheduleIds());
			callableStatement.setString(++pindex, sectorDTO.getVehicleIds());
			callableStatement.setString(++pindex, sectorDTO.getStationIds());
			callableStatement.setString(++pindex, sectorDTO.getOrganizationIds());
			callableStatement.setInt(++pindex, sectorDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				sector.setCode(callableStatement.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return sector;
	}

	public void updateSectorSchedule(AuthDTO authDTO, SectorDTO sectorDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("UPDATE sector SET schedule_id = ?, updated_by = ?, updated_at = NOW() WHERE namespace_id = ? AND code = ? AND active_flag = 1");
			selectPS.setString(1, sectorDTO.getScheduleIds());
			selectPS.setInt(2, authDTO.getUser().getId());
			selectPS.setInt(3, authDTO.getNamespace().getId());
			selectPS.setString(4, sectorDTO.getCode());
			selectPS.executeUpdate();
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public void updateSectorVehicle(AuthDTO authDTO, SectorDTO sectorDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("UPDATE sector SET vehicle_id = ?, updated_by = ?, updated_at = NOW() WHERE namespace_id = ? AND code = ? AND active_flag = 1");
			selectPS.setString(1, sectorDTO.getVehicleIds());
			selectPS.setInt(2, authDTO.getUser().getId());
			selectPS.setInt(3, authDTO.getNamespace().getId());
			selectPS.setString(4, sectorDTO.getCode());
			selectPS.executeUpdate();
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public void updateSectorStation(AuthDTO authDTO, SectorDTO sectorDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("UPDATE sector SET station_id = ?, updated_by = ?, updated_at = NOW() WHERE namespace_id = ? AND code = ? AND active_flag = 1");
			selectPS.setString(1, sectorDTO.getStationIds());
			selectPS.setInt(2, authDTO.getUser().getId());
			selectPS.setInt(3, authDTO.getNamespace().getId());
			selectPS.setString(4, sectorDTO.getCode());
			selectPS.executeUpdate();
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public void updateSectorOrganization(AuthDTO authDTO, SectorDTO sectorDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("UPDATE sector SET organization_id = ?, updated_by = ?, updated_at = NOW() WHERE namespace_id = ? AND code = ? AND active_flag = 1");
			selectPS.setString(1, sectorDTO.getOrganizationIds());
			selectPS.setInt(2, authDTO.getUser().getId());
			selectPS.setInt(3, authDTO.getNamespace().getId());
			selectPS.setString(4, sectorDTO.getCode());
			selectPS.executeUpdate();
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public SectorDTO getSector(AuthDTO authDTO, SectorDTO sectorDTO) {
		SectorDTO sector = new SectorDTO();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			if (sectorDTO.getId() != 0) {
				selectPS = connection.prepareStatement("SELECT id, code, name, schedule_id, vehicle_id, station_id, organization_id, active_flag FROM sector WHERE id = ? AND namespace_id = ? AND active_flag = 1");
				selectPS.setInt(1, sectorDTO.getId());
				selectPS.setInt(2, authDTO.getNamespace().getId());
			}
			else if (StringUtil.isNotNull(sectorDTO.getCode())) {
				selectPS = connection.prepareStatement("SELECT id, code, name, schedule_id, vehicle_id, station_id, organization_id, active_flag FROM sector WHERE code = ? AND namespace_id = ? AND active_flag = 1");
				selectPS.setString(1, sectorDTO.getCode());
				selectPS.setInt(2, authDTO.getNamespace().getId());
			}

			if (selectPS != null) {
				@Cleanup
				ResultSet selectRS = selectPS.executeQuery();
				if (selectRS.next()) {
					sector.setId(selectRS.getInt("id"));
					sector.setCode(selectRS.getString("code"));
					sector.setName(selectRS.getString("name"));
					List<ScheduleDTO> scheduleList = convertScheduleList(selectRS.getString("schedule_id"));
					List<BusVehicleDTO> vehicleList = convertVehicleList(selectRS.getString("vehicle_id"));
					List<StationDTO> stationList = convertStationList(selectRS.getString("station_id"));
					List<OrganizationDTO> organizationList = convertOrganizationList(selectRS.getString("organization_id"));
					sector.setSchedule(scheduleList);
					sector.setVehicle(vehicleList);
					sector.setStation(stationList);
					sector.setOrganization(organizationList);
					sector.setActiveFlag(selectRS.getInt("active_flag"));
				}
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return sector;
	}

	public List<SectorDTO> getAllSector(AuthDTO authDTO) {
		List<SectorDTO> sectorList = new ArrayList<SectorDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			selectPS = connection.prepareStatement("SELECT code, name, schedule_id, vehicle_id, station_id, organization_id, active_flag FROM sector WHERE namespace_id = ? AND active_flag < 2");
			selectPS.setInt(1, authDTO.getNamespace().getId());

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				SectorDTO sector = new SectorDTO();
				sector.setCode(selectRS.getString("code"));
				sector.setName(selectRS.getString("name"));

				List<ScheduleDTO> scheduleList = convertScheduleList(selectRS.getString("schedule_id"));
				List<BusVehicleDTO> vehicleList = convertVehicleList(selectRS.getString("vehicle_id"));
				List<StationDTO> stationList = convertStationList(selectRS.getString("station_id"));
				List<OrganizationDTO> organizationList = convertOrganizationList(selectRS.getString("organization_id"));
				sector.setSchedule(scheduleList);
				sector.setVehicle(vehicleList);
				sector.setStation(stationList);
				sector.setOrganization(organizationList);

				sector.setActiveFlag(selectRS.getInt("active_flag"));
				sectorList.add(sector);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}

		return sectorList;
	}

	public void updateSectorUser(AuthDTO authDTO, SectorDTO sectorDTO, UserDTO userDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_SECTOR_USER_IUD(?,?,?,?,?, ?,?)}");
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, sectorDTO.getCode());
			callableStatement.setString(++pindex, userDTO.getCode());
			callableStatement.setInt(++pindex, sectorDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public List<SectorDTO> getSectorUser(AuthDTO authDTO, UserDTO userDTO) {
		List<SectorDTO> sectorList = new ArrayList<>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			selectPS = connection.prepareStatement("SELECT str.id, str.code, str.name, str.schedule_id, str.vehicle_id, str.station_id, str.organization_id, str.active_flag FROM sector str, sector_user map WHERE map.namespace_id = ? AND map.user_id = ? AND map.sector_id = str.id AND map.active_flag = 1 AND str.active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, userDTO.getId());

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				SectorDTO sector = new SectorDTO();
				sector.setId(selectRS.getInt("str.id"));
				sector.setCode(selectRS.getString("str.code"));
				sector.setName(selectRS.getString("str.name"));

				List<ScheduleDTO> scheduleList = convertScheduleList(selectRS.getString("str.schedule_id"));
				List<BusVehicleDTO> vehicleList = convertVehicleList(selectRS.getString("str.vehicle_id"));
				List<StationDTO> stationList = convertStationList(selectRS.getString("str.station_id"));
				List<OrganizationDTO> organizationList = convertOrganizationList(selectRS.getString("str.organization_id"));
				sector.setSchedule(scheduleList);
				sector.setVehicle(vehicleList);
				sector.setStation(stationList);
				sector.setOrganization(organizationList);

				sector.setActiveFlag(selectRS.getInt("str.active_flag"));
				sectorList.add(sector);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return sectorList;
	}

	public List<UserDTO> getSectorUsers(AuthDTO authDTO, SectorDTO sectorDTO) {
		List<UserDTO> userList = new ArrayList<UserDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT usr.code, usr.first_name, usr.active_flag FROM sector sect, sector_user su, user usr WHERE sect.namespace_id = ? AND su.namespace_id = ? AND usr.namespace_id = ? AND sect.code = ? AND sect.id = su.sector_id AND su.user_id = usr.id AND sect.active_flag = 1 AND su.active_flag = 1 AND usr.active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, authDTO.getNamespace().getId());
			selectPS.setInt(3, authDTO.getNamespace().getId());
			selectPS.setString(4, sectorDTO.getCode());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				UserDTO userDTO = new UserDTO();
				userDTO.setCode(selectRS.getString("usr.code"));
				userDTO.setName(selectRS.getString("usr.first_name"));
				userDTO.setActiveFlag(selectRS.getInt("usr.active_flag"));
				userList.add(userDTO);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return userList;
	}

	public List<SectorDTO> getUserSectors(AuthDTO authDTO, UserDTO userDTO) {
		List<SectorDTO> sectorList = new ArrayList<>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT str.code, str.name, str.active_flag FROM sector str, sector_user map WHERE map.namespace_id = ? AND map.user_id = ? AND map.sector_id = str.id AND map.active_flag = 1 AND str.active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, userDTO.getId());

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				SectorDTO sector = new SectorDTO();
				sector.setCode(selectRS.getString("str.code"));
				sector.setName(selectRS.getString("str.name"));
				sector.setActiveFlag(selectRS.getInt("str.active_flag"));
				sectorList.add(sector);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return sectorList;
	}

	private List<ScheduleDTO> convertScheduleList(String existingIds) {
		List<ScheduleDTO> scheduleList = new ArrayList<>();
		if (StringUtil.isNotNull(existingIds)) {
			List<String> scheduleIds = Arrays.asList(existingIds.split(Text.COMMA));
			for (String scheduleId : scheduleIds) {
				if (StringUtil.isNull(scheduleId) || Numeric.ZERO.equals(scheduleId)) {
					continue;
				}

				ScheduleDTO scheduleDTO = new ScheduleDTO();
				scheduleDTO.setId(StringUtil.getIntegerValue(scheduleId));
				scheduleList.add(scheduleDTO);
			}
		}
		return scheduleList;
	}

	private List<BusVehicleDTO> convertVehicleList(String existingIds) {
		List<BusVehicleDTO> vehicleList = new ArrayList<>();
		if (StringUtil.isNotNull(existingIds)) {
			List<String> vehicleIds = Arrays.asList(existingIds.split(Text.COMMA));
			for (String vehicleId : vehicleIds) {
				if (StringUtil.isNull(vehicleId) || Numeric.ZERO.equals(vehicleId)) {
					continue;
				}

				BusVehicleDTO vehicleDTO = new BusVehicleDTO();
				vehicleDTO.setId(StringUtil.getIntegerValue(vehicleId));
				vehicleList.add(vehicleDTO);
			}
		}
		return vehicleList;
	}

	private List<StationDTO> convertStationList(String existingIds) {
		List<StationDTO> stationList = new ArrayList<>();
		if (StringUtil.isNotNull(existingIds)) {
			List<String> stationIds = Arrays.asList(existingIds.split(Text.COMMA));
			for (String stationId : stationIds) {
				if (StringUtil.isNull(stationId) || Numeric.ZERO.equals(stationId)) {
					continue;
				}

				StationDTO stationDTO = new StationDTO();
				stationDTO.setId(StringUtil.getIntegerValue(stationId));
				stationList.add(stationDTO);
			}
		}
		return stationList;
	}

	private List<OrganizationDTO> convertOrganizationList(String existingIds) {
		List<OrganizationDTO> organizationList = new ArrayList<>();
		if (StringUtil.isNotNull(existingIds)) {
			List<String> organizationIds = Arrays.asList(existingIds.split(Text.COMMA));
			for (String organizationId : organizationIds) {
				if (StringUtil.isNull(organizationId) || Numeric.ZERO.equals(organizationId)) {
					continue;
				}

				OrganizationDTO organizationDTO = new OrganizationDTO();
				organizationDTO.setId(StringUtil.getIntegerValue(organizationId));
				organizationList.add(organizationDTO);
			}
		}
		return organizationList;
	}
}
