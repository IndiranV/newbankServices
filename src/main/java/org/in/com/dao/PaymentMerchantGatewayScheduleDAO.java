package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.OrderInitRequestDTO;
import org.in.com.dto.PaymentGatewayPartnerDTO;
import org.in.com.dto.PaymentGatewayProviderDTO;
import org.in.com.dto.PaymentGatewayScheduleDTO;
import org.in.com.dto.PaymentModeDTO;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.OrderTypeEM;
import org.in.com.exception.ServiceException;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;

import hirondelle.date4j.DateTime;
import lombok.Cleanup;

public class PaymentMerchantGatewayScheduleDAO {

	public List<PaymentGatewayScheduleDTO> getAllPgMerchantSchedule(AuthDTO authDTO) {
		List<PaymentGatewayScheduleDTO> list = new ArrayList<PaymentGatewayScheduleDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT pmgs.name ,pmgs.code AS code, pmgs.device_medium, pgp.code AS pg_partner_code, pgp.name AS pg_partner_name,pgpr.code AS provider_code,pgpr.name AS provider_name, ur.code AS user_group_code, ur.name AS user_group_name, pmgs.from_date AS from_date, pmgs.to_date AS to_date, pmgs.active_flag AS active_flag,service_charge,order_type_id, precedence  FROM payment_gateway_merchant_schedule pmgs, payment_gateway_provider pgpr, payment_gateway_partner pgp, user_group ur WHERE pmgs.namespace_id = ? AND pgp.active_flag = 1 AND pmgs.active_flag < 2 AND pgp.id = pmgs.payment_gateway_partner_id AND ur.id = pmgs.user_group_id AND pgp.payment_gateway_provider_id = pgpr.id AND pgpr.active_flag = 1 ");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				PaymentGatewayScheduleDTO pgDTO = new PaymentGatewayScheduleDTO();
				pgDTO.setCode(selectRS.getString("code"));
				pgDTO.setName(selectRS.getString("pmgs.name"));

				List<DeviceMediumEM> deviceMediumList = convertDeviceMediumList(selectRS.getString("pmgs.device_medium"));
				pgDTO.setDeviceMedium(deviceMediumList);

				GroupDTO groupDTO = new GroupDTO();
				groupDTO.setCode(selectRS.getString("user_group_code"));
				groupDTO.setName(selectRS.getString("user_group_name"));
				pgDTO.setGroup(groupDTO);

				PaymentGatewayPartnerDTO gatewayPartnerDTO = new PaymentGatewayPartnerDTO();
				gatewayPartnerDTO.setCode(selectRS.getString("pg_partner_code"));
				gatewayPartnerDTO.setName(selectRS.getString("pg_partner_name"));
				pgDTO.setGatewayPartner(gatewayPartnerDTO);

				PaymentGatewayProviderDTO gatewayProviderDTO = new PaymentGatewayProviderDTO();
				gatewayProviderDTO.setCode(selectRS.getString("provider_code"));
				gatewayProviderDTO.setName(selectRS.getString("provider_name"));
				gatewayPartnerDTO.setGatewayProvider(gatewayProviderDTO);

				pgDTO.setFromDate(selectRS.getString("from_date"));
				pgDTO.setToDate(selectRS.getString("to_date"));
				pgDTO.setServiceCharge(selectRS.getBigDecimal("service_charge"));
				pgDTO.setPrecedence(selectRS.getInt("precedence"));
				pgDTO.setOrderType(OrderTypeEM.getOrderTypeEM(selectRS.getInt("order_type_id")));

				pgDTO.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(pgDTO);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public PaymentGatewayScheduleDTO getPgModeUpdate(AuthDTO authDTO, PaymentGatewayScheduleDTO dto) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{call  EZEE_SP_PAYMENT_GATEWAY_MERCHANT_SCHEDULE_IUD(?,?,?,?,? ,?,?,?,?,? ,?,?,?,?,?)}");
			callableStatement.setString(++pindex, dto.getCode());
			callableStatement.setString(++pindex, dto.getName());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, dto.getGatewayPartner().getCode());
			callableStatement.setString(++pindex, dto.getGroup().getCode());
			callableStatement.setString(++pindex, dto.getDeviceMediums());
			callableStatement.setString(++pindex, dto.getFromDate());
			callableStatement.setString(++pindex, dto.getToDate());
			callableStatement.setBigDecimal(++pindex, dto.getServiceCharge());
			callableStatement.setInt(++pindex, dto.getOrderType().getId());
			callableStatement.setInt(++pindex, dto.getPrecedence());
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
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return null;
	}

	public List<PaymentGatewayScheduleDTO> getActiveSchedulePaymentGateway(AuthDTO authDTO, OrderTypeEM orderType) {
		List<PaymentGatewayScheduleDTO> list = new ArrayList<PaymentGatewayScheduleDTO>();
		try {
			DateTime now = DateUtil.NOW();
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement(" SELECT pgp.name, pgp.code, mde.code, mde.name, service_charge, device_medium, precedence, pgp.offer_notes, pgp.offer_terms FROM payment_gateway_merchant_schedule pmgs, payment_gateway_partner pgp, payment_gateway_provider pgpr, payment_gateway_mode mde WHERE pmgs.namespace_id = ? AND pgp.namespace_id = pmgs.namespace_id AND pmgs.active_flag = 1 AND pgp.active_flag = 1 AND mde.active_flag = 1 AND pgpr.active_flag = 1 AND pgp.payment_gateway_provider_id = pgpr.id AND mde.id = pgp.payment_gateway_mode_id AND pgp.id = pmgs.payment_gateway_partner_id AND  pmgs.order_type_id = ? AND pmgs.user_group_id = ? AND pmgs.from_date <= ? AND pmgs.to_date >= ? ");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, orderType.getId());
			selectPS.setInt(3, authDTO.getGroup().getId());
			selectPS.setString(4, now.format("YYYY-MM-DD"));
			selectPS.setString(5, now.format("YYYY-MM-DD"));
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				PaymentGatewayScheduleDTO scheduleDTO = new PaymentGatewayScheduleDTO();
				scheduleDTO.setServiceCharge(selectRS.getBigDecimal("service_charge"));
				scheduleDTO.setPrecedence(selectRS.getInt("precedence"));

				List<DeviceMediumEM> deviceMediumList = convertDeviceMediumList(selectRS.getString("device_medium"));
				scheduleDTO.setDeviceMedium(deviceMediumList);

				PaymentGatewayPartnerDTO pgDTO = new PaymentGatewayPartnerDTO();
				pgDTO.setCode(selectRS.getString("pgp.code"));
				pgDTO.setName(selectRS.getString("pgp.name"));
				pgDTO.setOfferNotes(selectRS.getString("pgp.offer_notes"));

				String offerTerms = selectRS.getString("pgp.offer_terms");
				pgDTO.setOfferTerms(convertOfferTermsList(offerTerms));

				PaymentModeDTO modeDTO = new PaymentModeDTO();
				modeDTO.setCode(selectRS.getString("mde.code"));
				modeDTO.setName(selectRS.getString("mde.name"));
				pgDTO.setPaymentMode(modeDTO);
				scheduleDTO.setGatewayPartner(pgDTO);

				list.add(scheduleDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return list;
	}
	public List<PaymentGatewayScheduleDTO> getVertexScheduledPaymentGateway(AuthDTO authDTO,DateTime fromDate,DateTime toDate ) {
		List<PaymentGatewayScheduleDTO> list = new ArrayList<PaymentGatewayScheduleDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement(" SELECT pgp.name, pgp.code, mde.code, mde.name, service_charge, device_medium, precedence, pgp.offer_notes, pgp.offer_terms FROM payment_gateway_merchant_schedule pmgs, payment_gateway_partner pgp, payment_gateway_provider pgpr, payment_gateway_mode mde, payment_gateway_merchant_credentials pgcdls WHERE pmgs.namespace_id = ? AND pgp.namespace_id = pmgs.namespace_id AND pgp.namespace_id = pgcdls.namespace_id AND pgcdls.active_flag = 1 AND pmgs.active_flag = 1 AND pgp.active_flag = 1 AND mde.active_flag = 1 AND pgpr.active_flag = 1 AND pgp.payment_gateway_provider_id = pgpr.id AND mde.id = pgp.payment_gateway_mode_id AND pgp.id = pmgs.payment_gateway_partner_id AND  pgcdls.payment_gateway_provider_id = pgpr.id AND pmgs.from_date <= ? AND pmgs.to_date >= ? AND pgcdls.account_owner = ?");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setString(2, fromDate.format("YYYY-MM-DD"));
			selectPS.setString(3, toDate.format("YYYY-MM-DD"));
			selectPS.setString(4, "EZEE");
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				PaymentGatewayScheduleDTO scheduleDTO = new PaymentGatewayScheduleDTO();
				scheduleDTO.setServiceCharge(selectRS.getBigDecimal("service_charge"));
				scheduleDTO.setPrecedence(selectRS.getInt("precedence"));
				
				List<DeviceMediumEM> deviceMediumList = convertDeviceMediumList(selectRS.getString("device_medium"));
				scheduleDTO.setDeviceMedium(deviceMediumList);
				
				PaymentGatewayPartnerDTO pgDTO = new PaymentGatewayPartnerDTO();
				pgDTO.setCode(selectRS.getString("pgp.code"));
				pgDTO.setName(selectRS.getString("pgp.name"));
				pgDTO.setOfferNotes(selectRS.getString("pgp.offer_notes"));
				
				String offerTerms = selectRS.getString("pgp.offer_terms");
				pgDTO.setOfferTerms(convertOfferTermsList(offerTerms));
				
				PaymentModeDTO modeDTO = new PaymentModeDTO();
				modeDTO.setCode(selectRS.getString("mde.code"));
				modeDTO.setName(selectRS.getString("mde.name"));
				pgDTO.setPaymentMode(modeDTO);
				scheduleDTO.setGatewayPartner(pgDTO);
				
				list.add(scheduleDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public PaymentGatewayScheduleDTO getPaymentGatewayForNamespace(AuthDTO authDTO, OrderInitRequestDTO orderInitRequestDTO) throws Exception {
		PaymentGatewayScheduleDTO gatewayScheduleDTO = new PaymentGatewayScheduleDTO();
		try {
			DateTime now = DateUtil.NOW();
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("select pgsch.payment_gateway_partner_id,pgp.payment_gateway_provider_id,pgp.api_provider_code,pgpr.code,pgpr.service_name,service_charge from payment_gateway_merchant_schedule pgsch join payment_gateway_partner pgp on pgsch.payment_gateway_partner_id = pgp.id join payment_gateway_provider pgpr on pgp.payment_gateway_provider_id = pgpr.id where pgsch.namespace_id = ?  AND pgp.code = ? AND from_date <= ? AND to_date >= ? AND pgsch.order_type_id = ? AND pgsch.active_flag = 1 AND pgpr.active_flag = 1 AND pgp.active_flag = 1 ");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setString(2, orderInitRequestDTO.getPartnerCode());
			selectPS.setString(3, now.format("YYYY-MM-DD"));
			selectPS.setString(4, now.format("YYYY-MM-DD"));
			selectPS.setInt(5, orderInitRequestDTO.getOrderType().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				PaymentGatewayProviderDTO paymentProviderDTO = new PaymentGatewayProviderDTO();
				paymentProviderDTO.setCode(selectRS.getString("pgpr.code"));
				paymentProviderDTO.setServiceName(selectRS.getString("pgpr.service_name"));
				paymentProviderDTO.setId(selectRS.getInt("pgp.payment_gateway_provider_id"));
				gatewayScheduleDTO.setServiceCharge(selectRS.getBigDecimal("service_charge"));
				PaymentGatewayPartnerDTO paymentGatewayPartner = new PaymentGatewayPartnerDTO();
				paymentGatewayPartner.setGatewayProvider(paymentProviderDTO);
				paymentGatewayPartner.setApiProviderCode(selectRS.getString("pgp.api_provider_code"));
				paymentGatewayPartner.setId(selectRS.getInt("pgsch.payment_gateway_partner_id"));
				gatewayScheduleDTO.setGatewayPartner(paymentGatewayPartner);
			}
		}
		catch (Exception e) {
			throw e;
		}
		return gatewayScheduleDTO;
	}

	private List<DeviceMediumEM> convertDeviceMediumList(String deviceMediumIds) {
		List<DeviceMediumEM> deviceMediumList = new ArrayList<>();
		if (StringUtil.isNotNull(deviceMediumIds)) {
			List<String> deviceMediums = Arrays.asList(deviceMediumIds.split(Text.COMMA));
			if (deviceMediums != null) {
				for (String deviceMedium : deviceMediums) {
					if (StringUtil.isNull(deviceMedium)) {
						continue;
					}
					deviceMediumList.add(DeviceMediumEM.getDeviceMediumEM(StringUtil.getIntegerValue(deviceMedium)));
				}
			}
		}
		return deviceMediumList;
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
