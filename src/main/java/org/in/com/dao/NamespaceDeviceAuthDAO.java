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
import org.in.com.dto.GroupDTO;
import org.in.com.dto.NamespaceDeviceAuthDTO;
import org.in.com.dto.NamespaceDeviceDTO;
import org.in.com.dto.UserDTO;
import org.in.com.exception.ServiceException;

public class NamespaceDeviceAuthDAO {

	public List<NamespaceDeviceAuthDTO> getAll(AuthDTO authDTO) {
		List<NamespaceDeviceAuthDTO> list = new ArrayList<NamespaceDeviceAuthDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id, code, namespace_device_id,refference_id, refference_type FROM namespace_device_auth WHERE namespace_id = ? AND active_flag = 1 ");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				NamespaceDeviceAuthDTO dto = new NamespaceDeviceAuthDTO();
				NamespaceDeviceDTO namespaceDeviceDTO = new NamespaceDeviceDTO();
				namespaceDeviceDTO.setId(selectRS.getInt("namespace_device_id"));
				dto.setNamespaceDevice(namespaceDeviceDTO);
				dto.setId(selectRS.getInt("id"));
				dto.setCode(selectRS.getString("code"));
				dto.setRefferenceType(selectRS.getString("refference_type"));
				int refferenceId = selectRS.getInt("refference_id");
				if (refferenceId != 0 && dto.getRefferenceType().equals("UR")) {
					UserDTO userDTO = new UserDTO();
					userDTO.setId(refferenceId);
					dto.setUser(userDTO);
				}
				else if (refferenceId != 0 && dto.getRefferenceType().equals("GR")) {
					GroupDTO groupDTO = new GroupDTO();
					groupDTO.setId(refferenceId);
					dto.setGroup(groupDTO);
				}
				list.add(dto);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public List<NamespaceDeviceAuthDTO> getDeviceAllAuth(AuthDTO authDTO, NamespaceDeviceDTO namespaceDeviceDTO) {
		List<NamespaceDeviceAuthDTO> list = new ArrayList<NamespaceDeviceAuthDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT nda.id, nda.code, nd.code, nd.name, nda.refference_id, nda.refference_type FROM namespace_device_auth nda, namespace_device nd WHERE nd.active_flag = 1 AND nda.namespace_device_id = nd.id AND nda.active_flag = 1 AND nd.namespace_id = ? AND nd.code = ?");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setString(2, namespaceDeviceDTO.getCode());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				NamespaceDeviceAuthDTO dto = new NamespaceDeviceAuthDTO();
				dto.setId(selectRS.getInt("nda.id"));
				dto.setCode(selectRS.getString("nda.code"));
				namespaceDeviceDTO.setCode(selectRS.getString("nd.code"));
				namespaceDeviceDTO.setName(selectRS.getString("nd.name"));
				dto.setNamespaceDevice(namespaceDeviceDTO);
				dto.setRefferenceType(selectRS.getString("nda.refference_type"));
				int refferenceId = selectRS.getInt("nda.refference_id");
				if (refferenceId != 0 && dto.getRefferenceType().equals("UR")) {
					UserDTO userDTO = new UserDTO();
					userDTO.setId(refferenceId);
					dto.setUser(userDTO);
				}
				else if (refferenceId != 0 && dto.getRefferenceType().equals("GR")) {
					GroupDTO groupDTO = new GroupDTO();
					groupDTO.setId(refferenceId);
					dto.setGroup(groupDTO);
				}
				list.add(dto);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public NamespaceDeviceAuthDTO updateNamespaceDeviceAuth(AuthDTO authDTO, NamespaceDeviceAuthDTO dto) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_NAMESPACE_DEVICE_AUTH_IUD(?,?,?,?,?, ?,?,?,?)}");
			int pindex = 0;
			String reffernceCode = null;
			if (dto.getRefferenceType().equals("UR")) {
				reffernceCode = dto.getUser().getCode() != null ? dto.getUser().getCode() : null;
			}
			else if (dto.getRefferenceType().equals("GR")) {
				reffernceCode = dto.getGroup().getCode() != null ? dto.getGroup().getCode() : null;
			}
			callableStatement.setString(++pindex, dto.getCode());
			callableStatement.setString(++pindex, dto.getNamespaceDevice() != null ? dto.getNamespaceDevice().getCode() : null);
			callableStatement.setString(++pindex, reffernceCode);
			callableStatement.setString(++pindex, dto.getRefferenceType());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
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
}
