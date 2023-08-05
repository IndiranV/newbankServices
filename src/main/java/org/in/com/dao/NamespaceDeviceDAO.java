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
import org.in.com.dto.NamespaceDTO;
import org.in.com.dto.NamespaceDeviceDTO;
import org.in.com.exception.ServiceException;

public class NamespaceDeviceDAO {

	public NamespaceDeviceDTO getNamespaceDevices(AuthDTO authDTO, String namespaceDeviceCode) {
		NamespaceDeviceDTO deviceDTO = new NamespaceDeviceDTO();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id, code, name, token FROM namespace_device WHERE code = ? AND namespace_id = ? AND active_flag = 1 ");
			selectPS.setString(1, namespaceDeviceCode);
			selectPS.setInt(2, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				deviceDTO.setId(selectRS.getInt("id"));
				deviceDTO.setCode(selectRS.getString("code"));
				deviceDTO.setName(selectRS.getString("name"));
				deviceDTO.setToken(selectRS.getString("token"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}

		return deviceDTO;
	}

	public List<NamespaceDeviceDTO> getAllNamespaceDevices(AuthDTO authDTO) {
		List<NamespaceDeviceDTO> list = new ArrayList<NamespaceDeviceDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code, name, token, remarks FROM namespace_device WHERE active_flag = 1 AND namespace_id = ?");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				NamespaceDeviceDTO dto = new NamespaceDeviceDTO();
				dto.setCode(selectRS.getString("code"));
				dto.setName(selectRS.getString("name"));
				dto.setToken(selectRS.getString("token"));
				dto.setRemarks(selectRS.getString("remarks"));
				list.add(dto);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}

		return list;
	}

	public NamespaceDeviceDTO updateNamespaceDevice(AuthDTO authDTO, NamespaceDeviceDTO dto) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_NAMESPACE_DEVICE_IUD(?,?,?,?,?, ?,?,?,?)}");
			callableStatement.setString(++pindex, dto.getCode());
			callableStatement.setString(++pindex, dto.getName());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, dto.getToken());
			callableStatement.setString(++pindex, dto.getRemarks());
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
			throw new ServiceException(e.getMessage());
		}
		return dto;
	}

	public NamespaceDeviceDTO getRegisterDevice(NamespaceDTO namespaceDTO, NamespaceDeviceDTO deviceDTO) {
		NamespaceDeviceDTO namespaceDeviceDTO = new NamespaceDeviceDTO();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id, code, name, token FROM namespace_device WHERE code = ? AND namespace_id = ? AND active_flag = 1 ");
			selectPS.setString(1, deviceDTO.getCode());
			selectPS.setInt(2, namespaceDTO.getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				namespaceDeviceDTO.setId(selectRS.getInt("id"));
				namespaceDeviceDTO.setCode(selectRS.getString("code"));
				namespaceDeviceDTO.setName(selectRS.getString("name"));
				namespaceDeviceDTO.setToken(selectRS.getString("token"));
				namespaceDeviceDTO.setActiveFlag(1);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}

		return namespaceDeviceDTO;
	}
}
