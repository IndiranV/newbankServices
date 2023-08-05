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
import org.in.com.dto.PaymentGatewayPartnerDTO;
import org.in.com.dto.PaymentGatewayProviderDTO;
import org.in.com.dto.PaymentModeDTO;
import org.in.com.exception.ServiceException;
import org.in.com.utils.StringUtil;

public class PaymentGatewayPartnerDAO {

	public List<PaymentGatewayPartnerDTO> getAllPgPartner(AuthDTO authDTO) {
		List<PaymentGatewayPartnerDTO> list = new ArrayList<PaymentGatewayPartnerDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT ppar.code, ppar.name, ppar.offer_notes, ppar.offer_terms, ppar.api_provider_code AS api_provider_code, ppro.code AS pg_provider_code, ppro.name AS pg_provider_name, pgm.code AS pg_code, pgm.name AS pg_name, ppar.active_flag AS active_flag FROM payment_gateway_partner ppar, payment_gateway_provider ppro, payment_gateway_mode pgm WHERE ppar.namespace_id = ? AND ppro.id = ppar.payment_gateway_provider_id AND pgm.id = ppar.payment_gateway_mode_id AND ppar.active_flag < 2 AND ppro.active_flag = 1 AND pgm.active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				PaymentGatewayPartnerDTO pgProDTO = new PaymentGatewayPartnerDTO();
				pgProDTO.setCode(selectRS.getString("ppar.code"));
				pgProDTO.setName(selectRS.getString("ppar.name"));
				pgProDTO.setApiProviderCode(selectRS.getString("api_provider_code"));
				pgProDTO.setOfferNotes(selectRS.getString("ppar.offer_notes"));

				String offerTerms = selectRS.getString("ppar.offer_terms");
				pgProDTO.setOfferTerms(convertOfferTermsList(offerTerms));

				PaymentModeDTO modeDTO = new PaymentModeDTO();
				modeDTO.setCode(selectRS.getString("pg_code"));
				modeDTO.setName(selectRS.getString("pg_name"));
				pgProDTO.setPaymentMode(modeDTO);

				PaymentGatewayProviderDTO gatewayProviderDTO = new PaymentGatewayProviderDTO();
				gatewayProviderDTO.setCode(selectRS.getString("pg_provider_code"));
				gatewayProviderDTO.setName(selectRS.getString("pg_provider_name"));
				pgProDTO.setGatewayProvider(gatewayProviderDTO);

				pgProDTO.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(pgProDTO);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public void getPgPartnerUpdate(AuthDTO authDTO, PaymentGatewayPartnerDTO dto) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_PAYMENT_GATEWAY_PARTNER_IUD(?,?,?,?,?, ?,?,?,?,?, ?,?)}");
			callableStatement.setString(++pindex, dto.getCode());
			callableStatement.setString(++pindex, dto.getName());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, dto.getGatewayProvider().getCode());
			callableStatement.setString(++pindex, dto.getPaymentMode().getCode());
			callableStatement.setString(++pindex, dto.getApiProviderCode());
			callableStatement.setString(++pindex, dto.getOfferNotes());
			callableStatement.setString(++pindex, dto.getOfferTerm());
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
	}

	private List<String> convertOfferTermsList(String offerTerms) {
		List<String> offerTermsList = new ArrayList<String>();
		if (StringUtil.isNotNull(offerTerms)) {
			for (String term : offerTerms.split("\\|")) {
				offerTermsList.add(term);
			}
		}
		return offerTermsList;
	}

}
