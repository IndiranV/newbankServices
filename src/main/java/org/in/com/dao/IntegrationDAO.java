package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.IntegrationDTO;
import org.in.com.dto.NamespaceDTO;
import org.in.com.dto.enumeration.IntegrationTypeEM;
import org.in.com.exception.ServiceException;

import lombok.Cleanup;

public class IntegrationDAO {

	public List<IntegrationDTO> getIntegration(AuthDTO authDTO, IntegrationTypeEM integrationType) {
		List<IntegrationDTO> list = new ArrayList<>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id, integration_type_id, account, access_token, access_url, provider, active_flag FROM namespace_integration WHERE namespace_id = ? AND integration_type_id = ? AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, integrationType.getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				IntegrationDTO integration = new IntegrationDTO();
				integration.setIntegrationtype(integrationType);
				integration.setId(selectRS.getInt("id"));
				integration.setIntegrationtype(IntegrationTypeEM.getIntegrationTypeEM(selectRS.getInt("integration_type_id")));
				integration.setAccount(selectRS.getString("account"));
				integration.setAccessToken(selectRS.getString("access_token"));
				integration.setAccessUrl(selectRS.getString("access_url"));
				integration.setProvider(selectRS.getString("provider"));
				integration.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(integration);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public IntegrationDTO getIntegration(AuthDTO authDTO, IntegrationDTO integrationDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT integration_type_id, account, access_token, access_url, provider, active_flag FROM namespace_integration WHERE namespace_id = ? AND id = ? AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, integrationDTO.getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				integrationDTO.setIntegrationtype(IntegrationTypeEM.getIntegrationTypeEM(selectRS.getInt("integration_type_id")));
				integrationDTO.setAccount(selectRS.getString("account"));
				integrationDTO.setAccessToken(selectRS.getString("access_token"));
				integrationDTO.setAccessUrl(selectRS.getString("access_url"));
				integrationDTO.setProvider(selectRS.getString("provider"));
				integrationDTO.setActiveFlag(selectRS.getInt("active_flag"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return integrationDTO;
	}

	public IntegrationDTO getIntegrationV2(NamespaceDTO namespaceDTO, IntegrationDTO integrationDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id, namespace_id, integration_type_id, account, access_token, access_url, provider, active_flag FROM namespace_integration WHERE integration_type_id = ? AND account = ? AND active_flag = 1");
			selectPS.setInt(1, integrationDTO.getIntegrationtype().getId());
			selectPS.setString(2, integrationDTO.getAccount());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				integrationDTO.setId(selectRS.getInt("id"));
				namespaceDTO.setId(selectRS.getInt("namespace_id"));
				integrationDTO.setIntegrationtype(IntegrationTypeEM.getIntegrationTypeEM(selectRS.getInt("integration_type_id")));
				integrationDTO.setAccount(selectRS.getString("account"));
				integrationDTO.setAccessToken(selectRS.getString("access_token"));
				integrationDTO.setAccessUrl(selectRS.getString("access_url"));
				integrationDTO.setProvider(selectRS.getString("provider"));
				integrationDTO.setActiveFlag(selectRS.getInt("active_flag"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return integrationDTO;
	}

	public void integrationUpdate(AuthDTO authDTO, IntegrationDTO integrationDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_NAMESPACE_INTEGRATION_IUD(?,?,?,?,?, ?,?,?,?,?)}");
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setInt(++pindex, integrationDTO.getIntegrationtype().getId());
			callableStatement.setString(++pindex, integrationDTO.getAccount());
			callableStatement.setString(++pindex, integrationDTO.getAccessToken());
			callableStatement.setString(++pindex, integrationDTO.getAccessUrl());
			callableStatement.setString(++pindex, integrationDTO.getProvider());
			callableStatement.setInt(++pindex, integrationDTO.getActiveFlag());
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

	public List<IntegrationDTO> getAllIntegration(AuthDTO authDTO) {
		List<IntegrationDTO> integrationList = new ArrayList<IntegrationDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT integration_type_id, account, access_token, access_url, provider, active_flag FROM namespace_integration WHERE namespace_id = ? AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				IntegrationDTO integrationDTO = new IntegrationDTO();
				integrationDTO.setIntegrationtype(IntegrationTypeEM.getIntegrationTypeEM(selectRS.getInt("integration_type_id")));
				integrationDTO.setAccount(selectRS.getString("account"));
				integrationDTO.setAccessToken(selectRS.getString("access_token"));
				integrationDTO.setAccessUrl(selectRS.getString("access_url"));
				integrationDTO.setProvider(selectRS.getString("provider"));
				integrationDTO.setActiveFlag(selectRS.getInt("active_flag"));
				integrationList.add(integrationDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return integrationList;
	}
}
