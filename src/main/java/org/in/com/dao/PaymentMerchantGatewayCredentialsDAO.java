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
import org.in.com.dto.PaymentGatewayCredentialsDTO;
import org.in.com.dto.PaymentGatewayProviderDTO;
import org.in.com.exception.ServiceException;

public class PaymentMerchantGatewayCredentialsDAO {

	public List<PaymentGatewayCredentialsDTO> getallPgCredentials(AuthDTO authDTO) {
		List<PaymentGatewayCredentialsDTO> list = new ArrayList<PaymentGatewayCredentialsDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT pmgc.code,pmgc.access_code,pmgc.access_key,pmgc.pg_return_url,pmgc.app_return_url,pmgc.attr_1,pmgc.properties_file_name, pmgc.account_owner, pgp.code,pgp.name,pmgc.active_flag FROM payment_gateway_merchant_credentials pmgc, payment_gateway_provider pgp WHERE pmgc.active_flag < 2 AND pgp.id = pmgc.payment_gateway_provider_id AND pmgc.namespace_id = ?");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				PaymentGatewayCredentialsDTO pgDTO = new PaymentGatewayCredentialsDTO();
				pgDTO.setCode(selectRS.getString("pmgc.code"));

				PaymentGatewayProviderDTO gatewayProviderDTO = new PaymentGatewayProviderDTO();
				gatewayProviderDTO.setCode(selectRS.getString("pgp.code"));
				gatewayProviderDTO.setName(selectRS.getString("pgp.name"));
				pgDTO.setGatewayProvider(gatewayProviderDTO);

				pgDTO.setPgReturnUrl(selectRS.getString("pmgc.pg_return_url"));
				pgDTO.setAppReturnUrl(selectRS.getString("pmgc.app_return_url"));

				pgDTO.setAccessCode(selectRS.getString("pmgc.access_code"));
				pgDTO.setAccessKey(selectRS.getString("pmgc.access_key"));
				pgDTO.setAttr1(selectRS.getString("pmgc.attr_1"));
				pgDTO.setPropertiesFileName(selectRS.getString("pmgc.properties_file_name"));
				pgDTO.setAccountOwner(selectRS.getString("pmgc.account_owner"));
				pgDTO.setActiveFlag(selectRS.getInt("pmgc.active_flag"));
				list.add(pgDTO);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public PaymentGatewayCredentialsDTO updatePgMerchantCredentails(AuthDTO authDTO, PaymentGatewayCredentialsDTO dto) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{call  EZEE_SP_PAYMENT_GATEWAY_MERCHANT_CREDENTIALS_IUD(?,?,?,?,?,?, ?,?,?,?,?, ?,? )}");
			callableStatement.setString(++pindex, dto.getCode());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, dto.getGatewayProvider().getCode());
			callableStatement.setString(++pindex, dto.getPgReturnUrl());
			callableStatement.setString(++pindex, dto.getAppReturnUrl());
			callableStatement.setString(++pindex, dto.getAccessCode());
			callableStatement.setString(++pindex, dto.getAccessKey());
			callableStatement.setString(++pindex, dto.getAttr1());
			callableStatement.setString(++pindex, dto.getPropertiesFileName());
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

	public List<PaymentGatewayCredentialsDTO> getPgCredentials(AuthDTO authDTO, PaymentGatewayCredentialsDTO credentialsDTO) {
		List<PaymentGatewayCredentialsDTO> list = new ArrayList<PaymentGatewayCredentialsDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT pmgc.code,pmgc.access_code,pmgc.access_key,pmgc.attr_1,pmgc.properties_file_name,pmgc.return_url,pgp.code,pgp.name,pmr.code,pmr.name FROM payment_gateway_merchant_credentials pmgc, payment_merchant_register pmr, payment_gateway_provider pgp WHERE pmgc.active_flag < 2 AND pmr.id = pmgc.merchant_register_id AND pgp.id = pmgc.payment_gateway_provider_id AND pmgc.namespace_id = ?");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				PaymentGatewayCredentialsDTO pgDTO = new PaymentGatewayCredentialsDTO();
				pgDTO.setCode(selectRS.getString("pmgc.code"));

				PaymentGatewayProviderDTO gatewayProviderDTO = new PaymentGatewayProviderDTO();
				gatewayProviderDTO.setCode(selectRS.getString("pgp.code"));
				gatewayProviderDTO.setName(selectRS.getString("pgp.name"));
				pgDTO.setGatewayProvider(gatewayProviderDTO);

				pgDTO.setPgReturnUrl(selectRS.getString("pmgc.pg_return_url"));
				pgDTO.setAppReturnUrl(selectRS.getString("pmgc.app_return_url"));

				pgDTO.setAccessCode(selectRS.getString("pmgc.access_code"));
				pgDTO.setAccessKey(selectRS.getString("pmgc.access_key"));
				pgDTO.setAttr1(selectRS.getString("pmgc.attr_1"));
				pgDTO.setPropertiesFileName(selectRS.getString("pmgc.properties_file_name"));
				pgDTO.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(pgDTO);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public PaymentGatewayCredentialsDTO getNamespacePGCredentials(NamespaceDTO namespace, int gatewayProviderId) {
		PaymentGatewayCredentialsDTO gatewayCredentialsDTO = new PaymentGatewayCredentialsDTO();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("select id, access_code, access_key, attr_1, pg_return_url, properties_file_name   from payment_gateway_merchant_credentials where namespace_id = ? and payment_gateway_provider_id = ? and active_flag = 1");
			selectPS.setInt(1, namespace.getId());
			selectPS.setInt(2, gatewayProviderId);
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				gatewayCredentialsDTO.setId(selectRS.getInt("id"));
				gatewayCredentialsDTO.setAccessCode(selectRS.getString("access_code"));
				gatewayCredentialsDTO.setAccessKey(selectRS.getString("access_key"));
				gatewayCredentialsDTO.setAttr1(selectRS.getString("attr_1"));
				gatewayCredentialsDTO.setPropertiesFileName(selectRS.getString("properties_file_name"));
				gatewayCredentialsDTO.setPgReturnUrl(selectRS.getString("pg_return_url"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return gatewayCredentialsDTO;
	}

}
