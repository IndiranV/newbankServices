package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import lombok.Cleanup;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.OrganizationDTO;
import org.in.com.dto.StateDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.UserDTO;
import org.in.com.exception.ServiceException;

public class OrganizationDAO {
	public List<OrganizationDTO> getAllOrganizations(AuthDTO authDTO) {
		List<OrganizationDTO> list = new ArrayList<OrganizationDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectUserCountPS = connection.prepareStatement("SELECT COUNT(1) as usercount FROM user WHERE namespace_id = ? AND organization_id = ? AND user_role_id = 1 AND active_flag < 2");
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT org.id,org.code,org.name,org.short_code,org.address1,org.address2, org.contact,org.pincode,org.lat_lon,stat.name,stat.code,te.code,te.name,org.office_working_minutes,org.active_flag FROM organization org,station stat ,state te where namespace_id = ?     AND te.id = stat.state_id AND org.active_flag < 2 and stat.id = org.station_id");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				OrganizationDTO dto = new OrganizationDTO();
				dto.setId(selectRS.getInt("org.id"));
				dto.setCode(selectRS.getString("org.code"));
				dto.setName(selectRS.getString("org.name"));
				dto.setShortCode(selectRS.getString("org.short_code"));
				dto.setAddress1(selectRS.getString("org.address1"));
				dto.setAddress2(selectRS.getString("org.address2"));
				dto.setContact(selectRS.getString("org.contact"));
				dto.setPincode(selectRS.getString("org.pincode"));
				dto.setLatLon(selectRS.getString("org.lat_lon"));
				dto.setWorkingMinutes(selectRS.getInt("org.office_working_minutes"));
				dto.setActiveFlag(selectRS.getInt("org.active_flag"));
				StateDTO stateDTO = new StateDTO();
				stateDTO.setCode(selectRS.getString("te.code"));
				stateDTO.setName(selectRS.getString("te.name"));
				StationDTO stationDTO = new StationDTO();
				stationDTO.setState(stateDTO);
				stationDTO.setCode(selectRS.getString("stat.code"));
				stationDTO.setName(selectRS.getString("stat.name"));
				dto.setStation(stationDTO);
				selectUserCountPS.setInt(1, authDTO.getNamespace().getId());
				selectUserCountPS.setInt(2, dto.getId());
				@Cleanup
				ResultSet selectgrgrRS = selectUserCountPS.executeQuery();
				if (selectgrgrRS.next()) {
					dto.setUserCount(selectgrgrRS.getInt("usercount"));
				}
				list.add(dto);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	public List<OrganizationDTO> getOrganization(AuthDTO authDTO, OrganizationDTO organizationDTO) {
		List<OrganizationDTO> list = new ArrayList<OrganizationDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectUserC0untPS = connection.prepareStatement("SELECT COUNT(1) as usercount FROM user WHERE namespace_id = ? AND organization_id = ? AND user_role_id = 1 AND active_flag < 2");
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT org.id,org.code,org.name,org.short_code,org.address1,org.address2,org.contact,org.pincode,org.lat_lon,stat.name,stat.code,te.code,te.name,org.office_working_minutes,org.active_flag FROM organization org,station stat ,state te where namespace_id = ?     AND te.id = stat.state_id AND org.active_flag < 2 and stat.id = org.station_id AND org.code = ?");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setString(2, organizationDTO.getCode());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				OrganizationDTO dto = new OrganizationDTO();
				dto.setId(selectRS.getInt("org.id"));
				dto.setCode(selectRS.getString("org.code"));
				dto.setName(selectRS.getString("org.name"));
				dto.setShortCode(selectRS.getString("org.short_code"));
				dto.setAddress1(selectRS.getString("org.address1"));
				dto.setAddress2(selectRS.getString("org.address2"));
				dto.setContact(selectRS.getString("org.contact"));
				dto.setPincode(selectRS.getString("org.pincode"));
				dto.setLatLon(selectRS.getString("org.lat_lon"));
				dto.setWorkingMinutes(selectRS.getInt("org.office_working_minutes"));
				dto.setActiveFlag(selectRS.getInt("org.active_flag"));
				StateDTO stateDTO = new StateDTO();
				stateDTO.setCode(selectRS.getString("te.code"));
				stateDTO.setName(selectRS.getString("te.name"));
				StationDTO stationDTO = new StationDTO();
				stationDTO.setState(stateDTO);
				stationDTO.setCode(selectRS.getString("stat.code"));
				stationDTO.setName(selectRS.getString("stat.name"));
				dto.setStation(stationDTO);
				selectUserC0untPS.setInt(1, authDTO.getNamespace().getId());
				selectUserC0untPS.setInt(2, dto.getId());
				@Cleanup
				ResultSet selectgrgrRS = selectUserC0untPS.executeQuery();
				if (selectgrgrRS.next()) {
					dto.setUserCount(selectgrgrRS.getInt("usercount"));
				}
				list.add(dto);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return list;
	}

	public OrganizationDTO getOrganizationDTO(AuthDTO authDTO, OrganizationDTO organizationDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			if (organizationDTO.getId() != 0) {
				selectPS = connection.prepareStatement("SELECT org.id,org.code,org.name,org.short_code,org.address1,org.address2,org.contact,org.pincode,org.lat_lon,stat.name,stat.code,te.code,te.name,org.office_working_minutes,org.active_flag FROM organization org,station stat ,state te where namespace_id = ?     AND te.id = stat.state_id AND org.active_flag < 2 and stat.id = org.station_id AND org.id = ?");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setInt(2, organizationDTO.getId());
			}
			else {
				selectPS = connection.prepareStatement("SELECT org.id,org.code,org.name,org.short_code,org.address1,org.address2,org.contact,org.pincode,org.lat_lon,stat.name,stat.code,te.code,te.name,org.office_working_minutes,org.active_flag FROM organization org,station stat ,state te where namespace_id = ?     AND te.id = stat.state_id AND org.active_flag < 2 and stat.id = org.station_id AND org.code = ?");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setString(2, organizationDTO.getCode());
			}
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				organizationDTO.setId(selectRS.getInt("org.id"));
				organizationDTO.setCode(selectRS.getString("org.code"));
				organizationDTO.setName(selectRS.getString("org.name"));
				organizationDTO.setShortCode(selectRS.getString("org.short_code"));
				organizationDTO.setAddress1(selectRS.getString("org.address1"));
				organizationDTO.setAddress2(selectRS.getString("org.address2"));
				organizationDTO.setContact(selectRS.getString("org.contact"));
				organizationDTO.setPincode(selectRS.getString("org.pincode"));
				organizationDTO.setLatLon(selectRS.getString("org.lat_lon"));
				organizationDTO.setWorkingMinutes(selectRS.getInt("org.office_working_minutes"));
				organizationDTO.setActiveFlag(selectRS.getInt("org.active_flag"));
				StateDTO stateDTO = new StateDTO();
				stateDTO.setCode(selectRS.getString("te.code"));
				stateDTO.setName(selectRS.getString("te.name"));
				StationDTO stationDTO = new StationDTO();
				stationDTO.setState(stateDTO);
				stationDTO.setCode(selectRS.getString("stat.code"));
				stationDTO.setName(selectRS.getString("stat.name"));
				organizationDTO.setStation(stationDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return organizationDTO;
	}

	public OrganizationDTO getOrganizationIUD(AuthDTO authDTO, OrganizationDTO organizationDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;

			@Cleanup
			CallableStatement termSt = connection.prepareCall("{CALL EZEE_SP_USER_ORGANIZATION_IUD( ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?)}");
			termSt.setString(++pindex, organizationDTO.getCode());
			termSt.setInt(++pindex, authDTO.getNamespace().getId());
			termSt.setString(++pindex, organizationDTO.getName());
			termSt.setString(++pindex, organizationDTO.getShortCode());
			termSt.setString(++pindex, organizationDTO.getAddress1());
			termSt.setString(++pindex, organizationDTO.getAddress2());
			termSt.setString(++pindex, organizationDTO.getContact());
			termSt.setString(++pindex, organizationDTO.getPincode());
			termSt.setString(++pindex, organizationDTO.getLatLon());
			termSt.setInt(++pindex, organizationDTO.getStation().getId());
			termSt.setInt(++pindex, organizationDTO.getWorkingMinutes());
			termSt.setInt(++pindex, organizationDTO.getActiveFlag());
			termSt.setInt(++pindex, authDTO.getUser().getId());
			termSt.setInt(++pindex, 0);
			termSt.registerOutParameter(++pindex, Types.INTEGER);
			termSt.execute();
			if (termSt.getInt("pitRowCount") > 0) {
				organizationDTO.setCode(termSt.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return organizationDTO;
	}

	public OrganizationDTO getOrganizationMapUser(AuthDTO authDTO, OrganizationDTO organizationDTO, UserDTO userDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement preparedStatement = connection.prepareStatement("UPDATE user,organization SET user.organization_id = organization.id WHERE user.namespace_id = ? and  user.namespace_id =organization.namespace_id AND organization.code = ? AND user.code = ? ");
			preparedStatement.setInt(1, authDTO.getNamespace().getId());
			preparedStatement.setString(2, organizationDTO.getCode());
			preparedStatement.setString(3, userDTO.getCode());
			int status = preparedStatement.executeUpdate();
			if (status == 0) {
				throw new ServiceException(201);
			}
			userDTO.setActiveFlag(status);
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return organizationDTO;
	}

	public List<OrganizationDTO> getOrganizationDTO(Connection connection, AuthDTO authDTO) {
		List<OrganizationDTO> list = new ArrayList<OrganizationDTO>();
		try {
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT org.id,org.code,org.name,org.address1,org.address2, org.contact, org.pincode,org.lat_lon, org.office_working_minutes,org.active_flag, stn.code, stn.name FROM organization org, station stn  where org.namespace_id = ? AND stn.id = org.station_id AND org.active_flag < 2  ");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				OrganizationDTO dto = new OrganizationDTO();
				dto.setId(selectRS.getInt("org.id"));
				dto.setCode(selectRS.getString("org.code"));
				dto.setName(selectRS.getString("org.name"));
				dto.setAddress1(selectRS.getString("org.address1"));
				dto.setAddress2(selectRS.getString("org.address2"));
				dto.setContact(selectRS.getString("org.contact"));
				dto.setPincode(selectRS.getString("org.pincode"));
				dto.setLatLon(selectRS.getString("org.lat_lon"));
				dto.setWorkingMinutes(selectRS.getInt("org.office_working_minutes"));
				dto.setActiveFlag(selectRS.getInt("org.active_flag"));
				
				StationDTO stationDTO = new StationDTO();
				stationDTO.setCode(selectRS.getString("stn.code"));
				stationDTO.setName(selectRS.getString("stn.name"));
				dto.setStation(stationDTO);
				list.add(dto);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
}
