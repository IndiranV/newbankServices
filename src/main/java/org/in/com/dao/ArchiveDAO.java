package org.in.com.dao;

import hirondelle.date4j.DateTime;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Cleanup;

import org.apache.commons.lang3.StringUtils;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.DBQueryParamDTO;
import org.in.com.dto.enumeration.BusSeatTypeEM;
import org.in.com.dto.enumeration.PaymentGatewayTransactionTypeEM;
import org.in.com.dto.enumeration.PaymentTypeEM;
import org.in.com.dto.enumeration.RefundStatusEM;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.dto.enumeration.TransactionModeEM;
import org.in.com.dto.enumeration.TransactionTypeEM;
import org.in.com.dto.enumeration.UserRoleEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.impl.ArchiveImpl;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.springframework.stereotype.Component;

import com.google.common.base.Functions;
import com.google.common.collect.Lists;

@Component
public class ArchiveDAO {
	public static Map<String, String> codeMap = null;

	public List<Map<String, ?>> getTableForDrill(String tableName, String fromDateTime, String toDateTime) {
		List<Map<String, ?>> resultMapList = new ArrayList<>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			if (tableName.equals("login_history")) {
				selectPS = connection.prepareStatement("SELECT id, namespace_id AS namespace_code, user_id AS user_code, login_privider, device_medium, auth_token, updated_at FROM login_history WHERE updated_at >= ? AND updated_at <= ?");
				selectPS.setString(1, fromDateTime);
				selectPS.setString(2, toDateTime);
			}
			else if (tableName.equals("ticket")) {
				selectPS = connection.prepareStatement("SELECT id, namespace_id AS namespace_code, code, booking_code, user_id AS user_code, for_user_id AS for_user_code, trip_date, travel_minutes, from_station_id AS from_station_code, to_station_id AS to_station_code, boarding_point_id AS boarding_point_code, boarding_point_minutes, dropping_point_id AS dropping_point_code, dropping_point_minutes, bus_id AS bus_code, tax_id AS tax_code, schedule_id AS schedule_code, trip_code AS ticket_trip_code, trip_stage_code, mobile_number, email_id, cancellation_policy_id, reporting_minutes, journey_type, device_medium, ticket_status_id AS ticket_status_code, lookup_id, payment_gateway_partner_code AS payment_gateway_partner, remarks, ticket_at, active_flag, updated_by, updated_at FROM ticket WHERE ticket_at >= ? AND ticket_at <= ? AND active_flag < 3");
				selectPS.setString(1, fromDateTime);
				selectPS.setString(2, toDateTime);
			}
			else if (tableName.equals("ticket_detail")) {
				selectPS = connection.prepareStatement("SELECT id, ticket_id AS ticket_code, seat_name, seat_code, seat_type, ticket_status_id AS ticket_status_code, passenger_name, passenger_age, seat_gender, seat_fare, ac_bus_tax, travel_status_id AS travel_status_code, active_flag, updated_by, updated_at FROM ticket_detail WHERE active_flag < 3 AND updated_at >= ? AND updated_at <= ?");
				selectPS.setString(1, fromDateTime);
				selectPS.setString(2, toDateTime);
			}
			else if (tableName.equals("ticket_cancel_detail")) {
				selectPS = connection.prepareStatement("SELECT id, ticket_id AS ticket_code, ticket_detail_id, ticket_transaction_id AS ticket_transaction_code, cancellation_charges, refund_amount, active_flag, updated_by, updated_at FROM ticket_cancel_detail WHERE active_flag < 3 AND updated_at >= ? AND updated_at <= ?");
				selectPS.setString(1, fromDateTime);
				selectPS.setString(2, toDateTime);
			}
			else if (tableName.equals("ticket_cancel_transaction")) {
				selectPS = connection.prepareStatement("SELECT id, user_id AS user_code, ticket_id AS ticket_code, ticket_transaction_id AS ticket_transaction_code, charge_amount, revoke_commission, cancel_commission, refund_amount, ticket_status_id AS ticket_status_code, device_medium, refund_status AS refund_status_code, active_flag, updated_by, updated_at FROM ticket_cancel_transaction WHERE active_flag < 3 AND updated_at >= ? AND updated_at <= ?");
				selectPS.setString(1, fromDateTime);
				selectPS.setString(2, toDateTime);
			}
			else if (tableName.equals("ticket_transaction")) {
				selectPS = connection.prepareStatement("SELECT id, code, namespace_id AS namespace_code, user_id AS user_code, ticket_id AS ticket_code, transaction_type_id AS transaction_type_code, transaction_seat_count, transaction_amount, commission_amount, extra_commission_amount, addons_amount, ac_bus_tax, tds_tax, transaction_mode_id AS transaction_mode_code, active_flag, updated_by, updated_at FROM ticket_transaction WHERE active_flag < 3 AND updated_at >= ? AND updated_at <= ?");
				selectPS.setString(1, fromDateTime);
				selectPS.setString(2, toDateTime);
			}
			else if (tableName.equals("trip")) {
				selectPS = connection.prepareStatement("SELECT id, code, namespace_id AS namespace_code, schedule_id AS schedule_code, bus_id AS bus_code, EZEE_FN_MINITUES_TO_DATETIME(trip_date, trip_minutes) AS trip_date, trip_minutes, EZEE_FN_TRIP_STATUS(trip_status_flag) AS trip_status, active_flag, updated_by, updated_at FROM trip WHERE active_flag < 3 AND updated_at >= ? AND updated_at <= ?");
				selectPS.setString(1, fromDateTime);
				selectPS.setString(2, toDateTime);
			}
			else if (tableName.equals("trip_info")) {
				selectPS = connection.prepareStatement("SELECT id, namespace_id AS namespace_code, trip_id AS trip_code, bus_vehicle_id AS bus_vehicle_code, driver_mobile, driver_name, remarks, active_flag, updated_by, updated_at FROM trip_info WHERE active_flag < 3 AND updated_at >= ? AND updated_at <= ?");
				selectPS.setString(1, fromDateTime);
				selectPS.setString(2, toDateTime);
			}
			else if (tableName.equals("user")) {
				selectPS = connection.prepareStatement("SELECT id, code, username, namespace_id AS namespace_code, user_group_id AS group_code, organization_id AS organization_code, user_lookup_id, token, email, mobile, first_name, last_name, user_role_id AS user_role_code, payment_type AS payment_type_code,  active_flag, updated_by, updated_at FROM user WHERE user_role_id IN (1,3) AND active_flag < 3 AND updated_at >= ? AND updated_at <= ?");
				selectPS.setString(1, fromDateTime);
				selectPS.setString(2, toDateTime);
			}
			else if (tableName.equals("user_transaction")) {
				selectPS = connection.prepareStatement("SELECT id, namespace_id AS namespace_code, user_id AS user_code, reference_id, reference_code, transaction_type_id AS transaction_type_code, transaction_mode_id AS transaction_mode_code, transaction_amount, commission_amount, tds_tax,  credit_amount, debit_amount, closing_balance, active_flag, created_by, created_at FROM user_transaction WHERE active_flag < 3 AND created_at >= ? AND created_at <= ?");
				selectPS.setString(1, fromDateTime);
				selectPS.setString(2, toDateTime);
			}
			else if (tableName.equals("route")) {
				selectPS = connection.prepareStatement("SELECT id, namespace_id AS namespace_code, code, from_station_id AS from_station_code, to_station_id AS to_station_code, active_flag, updated_by, updated_at FROM route WHERE active_flag < 3 AND updated_at >= ? AND updated_at <= ?");
				selectPS.setString(1, fromDateTime);
				selectPS.setString(2, toDateTime);
			}
			else if (tableName.equals("bus_layout")) {
				selectPS = connection.prepareStatement("SELECT id, code, namespace_id AS namespace_code, bus_id AS bus_code, row_pos, column_pos, seat_name, layer, bus_seat_type_id AS bus_seat_type_code, sequence, orientation, active_flag, updated_by, updated_at FROM bus_layout WHERE active_flag < 3 AND updated_at >= ? AND updated_at <= ?");
				selectPS.setString(1, fromDateTime);
				selectPS.setString(2, toDateTime);
			}
			else if (tableName.equals("user_group")) {
				selectPS = connection.prepareStatement("SELECT id, code, name, user_role_id AS user_role_code, namespace_id AS namespace_code, level, active_flag, updated_by, updated_at FROM user_group WHERE active_flag < 3 AND updated_at >= ? AND updated_at <= ?");
				selectPS.setString(1, fromDateTime);
				selectPS.setString(2, toDateTime);
			}
			else if (tableName.equals("audit_ticket_log")) {
				selectPS = connection.prepareStatement("SELECT id, namespace_id AS namespace_code, ticket_code AS audit_ticket_code, user_id AS user_code, ticket_status_id AS ticket_status_code, event, device_medium, active_flag, updated_by, updated_at FROM audit_ticket_log WHERE active_flag < 3 AND updated_at >= ? AND updated_at <= ?");
				selectPS.setString(1, fromDateTime);
				selectPS.setString(2, toDateTime);
			}
			else if (tableName.equals("payment_gateway_transaction")) {
				selectPS = connection.prepareStatement("SELECT pgt.id, pgt.code, pgt.namespace_id AS namespace_code, pgt.user_id AS user_code, pgpt.name AS payment_gateway_partner_name, pgp.name AS payment_gateway_provider_name, pgt.payment_gateway_merchant_credentials_id AS payment_gateway_merchant_credentials_code, pgt.gateway_transaction_id, pgt.amount, pgt.order_code, pgt.service_charge, pgt.payment_gateway_transaction_type_id AS payment_gateway_transaction_type_code, pgt.device_medium, pgt.active_flag, pgt.updated_by, pgt.updated_at FROM payment_gateway_transaction pgt, payment_gateway_provider pgp, payment_gateway_partner pgpt WHERE pgp.id = pgt.payment_gateway_provider_id AND pgpt.id = pgt.payment_gateway_partner_id AND pgt.active_flag < 3 AND pgt.updated_at >= ? AND pgt.updated_at <= ?");
				selectPS.setString(1, fromDateTime);
				selectPS.setString(2, toDateTime);
			}
			else if (tableName.equals("user_commission")) {
				selectPS = connection.prepareStatement("SELECT id, code, namespace_id AS namespace_code, user_id AS user_code, commission_value, commission_value_type_id, credit_limit, commission_type, active_flag, updated_by, updated_at FROM user_commission WHERE active_flag < 3 AND updated_at >= ? AND updated_at <= ?");
				selectPS.setString(1, fromDateTime);
				selectPS.setString(2, toDateTime);
			}
			else if (tableName.equals("schedule")) {
				selectPS = connection.prepareStatement("SELECT id, code, namespace_id AS namespace_code, name, active_from, active_to, service_number, display_name, api_display_name, pnr_start_code, day_of_week, tax_id AS tax_code, category_id AS category_code, lookup_id, active_flag, updated_by, updated_at FROM schedule WHERE active_flag < 3 AND updated_at >= ? AND updated_at <= ?");
				selectPS.setString(1, fromDateTime);
				selectPS.setString(2, toDateTime);
			}
			else if (tableName.equals("payment_gateway_partner")) {
				selectPS = connection.prepareStatement("SELECT id, code, name, namespace_id AS namespace_code, payment_gateway_mode_id AS payment_gateway_mode_code, payment_gateway_provider_id AS payment_gateway_provider_code, api_provider_code, active_flag, updated_by, updated_at FROM payment_gateway_partner WHERE active_flag < 3 AND updated_at >= ? AND updated_at <= ?");
				selectPS.setString(1, fromDateTime);
				selectPS.setString(2, toDateTime);
			}
			else if (tableName.equals("payment_gateway_merchant_credentials")) {
				selectPS = connection.prepareStatement("SELECT id, code, namespace_id AS namespace_code, payment_gateway_provider_id AS payment_gateway_provider_code, access_code, access_key, app_return_url, pg_return_url, attr_1, properties_file_name, service_charge_value, percentage_flag, customer_bare, active_flag, updated_by, updated_at FROM payment_gateway_merchant_credentials WHERE active_flag < 3 AND updated_at >= ? AND updated_at <= ?");
				selectPS.setString(1, fromDateTime);
				selectPS.setString(2, toDateTime);
			}
			else if (tableName.equals("bus")) {
				selectPS = connection.prepareStatement("SELECT id, code, namespace_id AS namespace_code, name, category_code AS bus_category_code, display_name, seat_count, active_flag, updated_by, updated_at FROM bus WHERE active_flag < 3 AND updated_at >= ? AND updated_at <= ?");
				selectPS.setString(1, fromDateTime);
				selectPS.setString(2, toDateTime);
			}
			else if (tableName.equals("bus_vehicle")) {
				selectPS = connection.prepareStatement("SELECT code, name, bus_id AS bus_code, namespace_id AS namespace_code, registation_date, registation_number, lic_number, gps_device_code, gps_device_vendor_id, mobile_number, vehicle_type, active_flag, updated_by, updated_at FROM bus_vehicle WHERE active_flag < 3 AND updated_at >= ? AND updated_at <= ?");
				selectPS.setString(1, fromDateTime);
				selectPS.setString(2, toDateTime);
			}
			else if (tableName.equals("ticket_addons_detail")) {
				selectPS = connection.prepareStatement("SELECT id, ticket_id AS ticket_code, ticket_detail_id, ticket_addons_type_id, ticket_status_id, refference_id, value AS addon_value, active_flag, updated_by, updated_at FROM ticket_addons_detail WHERE active_flag < 3 AND updated_at >= ? AND updated_at <= ?");
				selectPS.setString(1, fromDateTime);
				selectPS.setString(2, toDateTime);
			}
			else if (tableName.equals("station_point")) {
				selectPS = connection.prepareStatement("SELECT id, namespace_id AS namespace_code, station_id AS station_code, code, name, address, landmark, contact_number, latitude, longitude, map_url, active_flag FROM station_point WHERE active_flag < 3 AND updated_at >= ? AND updated_at <= ?");
				selectPS.setString(1, fromDateTime);
				selectPS.setString(2, toDateTime);
			}
			else if (tableName.equals("addons_discount_category")) {
				selectPS = connection.prepareStatement("SELECT id, code, namespace_id AS namespace_code, name, description, active_flag, updated_by, updated_at FROM addons_discount_category WHERE active_flag < 3 AND updated_at >= ? AND updated_at <= ?");
				selectPS.setString(1, fromDateTime);
				selectPS.setString(2, toDateTime);
			}
			else if (tableName.equals("addons_discount_coupon")) {
				selectPS = connection.prepareStatement("SELECT id, code, namespace_id AS namespace_code, addons_discount_category_id AS addons_discount_category_code, coupon, active_description, error_description, active_flag, updated_by, updated_at FROM addons_discount_coupon WHERE active_flag < 3 AND updated_at >= ? AND updated_at <= ?");
				selectPS.setString(1, fromDateTime);
				selectPS.setString(2, toDateTime);
			}
			else if (tableName.equals("addons_discount_criteria")) {
				selectPS = connection.prepareStatement("SELECT id, code, namespace_id AS namespace_code, addons_discount_coupon_id AS addons_discount_coupon_code, user_group_id AS user_group_code, active_from, active_to, day_of_week, value, is_percentage_flag, is_travel_date_flag, is_round_trip_flag, is_registered_user_flag, is_show_offer_page_flag, max_usage_limit_per_user, max_discount_amount, min_seat_count, before_booking_minitues, after_booking_minitues, min_ticket_fare, device_medium, schedule_code, route_code, active_flag, updated_by, updated_at FROM addons_discount_criteria WHERE active_flag < 3 AND updated_at >= ? AND updated_at <= ?");
				selectPS.setString(1, fromDateTime);
				selectPS.setString(2, toDateTime);
			}
			else if (tableName.equals("addons_discount_offline")) {
				selectPS = connection.prepareStatement("SELECT id, code, namespace_id AS namespace_code, name, active_from_date, active_to_date, day_of_week, discount_value, is_percentage, is_travel_date, max_discount_amount, min_seat_count, min_seat_fare, user_group_code, schedule_code, route_code, active_flag, updated_by, updated_at FROM addons_discount_offline WHERE active_flag < 3 AND updated_at >= ? AND updated_at <= ?");
				selectPS.setString(1, fromDateTime);
				selectPS.setString(2, toDateTime);
			}
			else if (tableName.equals("audit_log")) {
				selectPS = connection.prepareStatement("SELECT id, code, namespace_id AS namespace_code, table_name, event, log1, log2, active_flag, updated_by, updated_at FROM audit_log WHERE active_flag < 3 AND updated_at >= ? AND updated_at <= ?");
				selectPS.setString(1, fromDateTime);
				selectPS.setString(2, toDateTime);
			}
			else if (tableName.equals("audit_report_log")) {
				selectPS = connection.prepareStatement("SELECT id, namespace_id AS namespace_code, report_code, user_id AS user_code, parameter_log, execution_time, result_row_count, status_log, active_flag, updated_at FROM audit_report_log WHERE active_flag < 3 AND updated_at >= ? AND updated_at <= ?");
				selectPS.setString(1, fromDateTime);
				selectPS.setString(2, toDateTime);
			}
			else if (tableName.equals("bus_vehicle_pickup_van")) {
				selectPS = connection.prepareStatement("SELECT id, namespace_id AS namespace_code, name, station_id AS station_code, active_flag, updated_by, updated_at FROM bus_vehicle_pickup_van WHERE active_flag < 3 AND updated_at >= ? AND updated_at <= ?");
				selectPS.setString(1, fromDateTime);
				selectPS.setString(2, toDateTime);
			}
			else if (tableName.equals("namespace_device")) {
				selectPS = connection.prepareStatement("SELECT id, namespace_id AS namespace_code, code, name, token, remarks, active_flag, updated_by, updated_at FROM namespace_device WHERE active_flag < 3 AND updated_at >= ? AND updated_at <= ?");
				selectPS.setString(1, fromDateTime);
				selectPS.setString(2, toDateTime);
			}
			else if (tableName.equals("namespace_device_auth")) {
				selectPS = connection.prepareStatement("SELECT id, namespace_id AS namespace_code, namespace_device_id AS namespace_device_code, refference_id, refference_type, active_flag, updated_by, updated_at FROM namespace_device_auth WHERE active_flag < 3 AND updated_at >= ? AND updated_at <= ?");
				selectPS.setString(1, fromDateTime);
				selectPS.setString(2, toDateTime);
			}
			else if (tableName.equals("notification_log")) {
				selectPS = connection.prepareStatement("SELECT id, namespace_id AS namespace_code, notification_mode, notification_type_id, participant_address, refference_code, transaction_count, request_log1, request_log2, response_log, active_flag, updated_by, updated_at FROM notification_log WHERE active_flag < 3 AND updated_at >= ? AND updated_at <= ?");
				selectPS.setString(1, fromDateTime);
				selectPS.setString(2, toDateTime);
			}
			else if (tableName.equals("payment_gateway_pre_transaction")) {
				selectPS = connection.prepareStatement("SELECT id, code, name, namespace_id AS namespace_code, payment_gateway_partner_id AS payment_gateway_partner_code, user_group_id AS group_code, device_medium, from_date, to_date, service_charge, transaction_type_id, active_flag, updated_by, updated_at FROM notification_log WHERE active_flag < 3 AND updated_at >= ? AND updated_at <= ?");
				selectPS.setString(1, fromDateTime);
				selectPS.setString(2, toDateTime);
			}
			else if (tableName.equals("payment_transaction")) {
				selectPS = connection.prepareStatement("SELECT id, code, namespace_id AS namespace_code, user_id AS user_code, transaction_mode_id AS transaction_mode_code, transaction_type_id AS transaction_type_code, transaction_amount, credit_debit_flag, amount_received_date, transaction_at, payment_handle_by, payment_acknowledge_status_id, user_transaction_id AS user_transaction_code, commission_amount, ac_bus_tax, tds_tax, remarks, active_flag, updated_by, updated_at FROM payment_transaction WHERE active_flag < 3 AND updated_at >= ? AND updated_at <= ?");
				selectPS.setString(1, fromDateTime);
				selectPS.setString(2, toDateTime);
			}
			else if (tableName.equals("schedule_dynamic_stage_fare")) {
				selectPS = connection.prepareStatement("SELECT id, code, namespace_id AS namespace_code, schedule_id AS schedule_code, active_from, active_to, day_of_week, status, lookup_id, active_flag, updated_by, updated_at FROM schedule_dynamic_stage_fare WHERE active_flag < 3 AND updated_at >= ? AND updated_at <= ?");
				selectPS.setString(1, fromDateTime);
				selectPS.setString(2, toDateTime);
			}
			else if (tableName.equals("schedule_dynamic_stage_fare_details")) {
				selectPS = connection.prepareStatement("SELECT id, namespace_id AS namespace_code, schedule_dynamic_stage_fare_id AS schedule_dynamic_stage_fare_code, min_fare, max_fare, from_station_id AS from_station_code, to_station_id AS to_station_code, active_flag, updated_by, updated_at FROM schedule_dynamic_stage_fare_details WHERE active_flag < 3 AND updated_at >= ? AND updated_at <= ?");
				selectPS.setString(1, fromDateTime);
				selectPS.setString(2, toDateTime);
			}
			else if (tableName.equals("trip_cash_head")) {
				selectPS = connection.prepareStatement("SELECT id, code, name, namespace_id AS namespace_code, descriptions, credit_debit_flag, active_flag, updated_by, updated_at FROM trip_cash_head WHERE active_flag < 3 AND updated_at >= ? AND updated_at <= ?");
				selectPS.setString(1, fromDateTime);
				selectPS.setString(2, toDateTime);
			}
			else if (tableName.equals("trip_cash_transaction")) {
				selectPS = connection.prepareStatement("SELECT id, code, namespace_id AS namespace_code, amount, trip_id AS trip_code, trip_cash_head_id AS trip_cash_head_code, transaction_mode_id AS transaction_mode_code, remarks, active_flag, updated_by, updated_at FROM trip_cash_transaction WHERE active_flag < 3 AND updated_at >= ? AND updated_at <= ?");
				selectPS.setString(1, fromDateTime);
				selectPS.setString(2, toDateTime);
			}
			else if (tableName.equals("user_feedback")) {
				selectPS = connection.prepareStatement("SELECT id, code, namespace_id AS namespace_code, ticket_code AS ticket, name, email, contact_number, comments, feedback_at, active_flag, updated_by, updated_at FROM user_feedback WHERE active_flag < 3 AND updated_at >= ? AND updated_at <= ?");
				selectPS.setString(1, fromDateTime);
				selectPS.setString(2, toDateTime);
			}
			else if (tableName.equals("namespace_tax")) {
				selectPS = connection.prepareStatement("SELECT id, code, namespace_id AS namespace_code, name, trade_name, gstin, state_id AS state_code, cgst, sgst, ugst, igst, sac_number, product_id, active_flag, updated_by, updated_at FROM namespace_tax WHERE active_flag < 3 AND updated_at >= ? AND updated_at <= ?");
				selectPS.setString(1, fromDateTime);
				selectPS.setString(2, toDateTime);
			}
			else {
				System.out.println("Archive Table Missing: " + tableName);
			}
			if (selectPS == null) {
				throw new ServiceException(ErrorCode.UNABLE_TO_PROVIDE_DATA);
			}
			@Cleanup
			ResultSet resultSet = selectPS.executeQuery();
			resultMapList = getResultList(resultSet, tableName);

			if (tableName.equals("user")) {
				List<Map<String, ?>> guestUserList = getGuestUser(fromDateTime, toDateTime);
				resultMapList.addAll(guestUserList);
			}
			return resultMapList;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public List<Map<String, ?>> getGuestUser(String fromDateTime, String toDateTime) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id, code, username, namespace_id AS namespace_code, user_group_id AS group_code, organization_id AS organization_code, user_lookup_id, token, email, mobile, first_name, last_name, user_role_id AS user_role_code, payment_type AS payment_type_code,  active_flag, updated_by, updated_at FROM user WHERE user_role_id = 2 AND username LIKE 'guest%' AND active_flag < 3 AND updated_at >= ? AND updated_at <= ?");
			selectPS.setString(1, fromDateTime);
			selectPS.setString(2, toDateTime);
			@Cleanup
			ResultSet resultSet = selectPS.executeQuery();
			return getResultList(resultSet, "user");
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	private List<Map<String, ?>> getResultList(ResultSet resultSet, String tableName) throws Exception {
		codeMap = new HashMap<String, String>();
		ResultSetMetaData metaData = resultSet.getMetaData();
		int columnCount = metaData.getColumnCount();
		Map<String, String> columns = new HashMap<>();
		for (int i = 1; i <= columnCount; i++) {
			String labelName = metaData.getColumnLabel(i);
			String columnClassName = metaData.getColumnClassName(i);
			if (labelName.equals("payment_gateway_mode_code") || labelName.equals("category_code") || labelName.equals("bus_category_code") || labelName.equals("payment_gateway_transaction_type_code") || labelName.equals("payment_gateway_merchant_credentials_code") || labelName.equals("payment_gateway_provider_code") || labelName.equals("payment_gateway_partner_code") || labelName.equals("bus_seat_type_code") || labelName.equals("payment_type_code") || labelName.equals("user_role_code") || labelName.equals("bus_vehicle_code") || labelName.equals("transaction_mode_code") || labelName.equals("transaction_type_code") || labelName.equals("refund_status_code") || labelName.equals("ticket_transaction_code") || labelName.equals("travel_status_code") || labelName.equals("ticket_status_code") || labelName.equals("user_code") || labelName.equals("for_user_code") || labelName.equals("from_station_code") || labelName.equals("to_station_code") || labelName.equals("boarding_point_code") || labelName.equals("dropping_point_code") || labelName.equals("bus_code") || labelName.equals("tax_code") || labelName.equals("schedule_code")) {
				columnClassName = "java.lang.String";
			}
			columns.put(labelName, columnClassName);
		}

		List<Map<String, ?>> resultMapList = new ArrayList<>();
		// Collect Data
		while (resultSet.next()) {
			Map<String, Object> resultsMap = new HashMap<>();
			// To find addons_amount is credit or debit
			int transactionType = 0;
			for (String columnName : columns.keySet()) {
				Object value = null;
				String dataType = columns.get(columnName);
				try {
					if (columnName.equals("trip_code")) {
						value = getCode("trip", resultSet.getInt("trip_code"));
					}
					else if (columnName.equals("namespace_code")) {
						value = getCode("namespace", resultSet.getInt("namespace_code"));
					}
					else if (columnName.equals("ticket_code")) {
						value = getCode("ticket", resultSet.getInt("ticket_code"));
					}
					else if (columnName.equals("ticket_status_code")) {
						value = TicketStatusEM.getTicketStatusEM(resultSet.getInt("ticket_status_code")).getCode();
					}
					else if (columnName.equals("refund_status_code")) {
						value = RefundStatusEM.getRefundStatusEM(resultSet.getInt("refund_status_code")).getCode();
					}
					else if (columnName.equals("transaction_type_code")) {
						transactionType = resultSet.getInt("transaction_type_code");
						value = TransactionTypeEM.getTransactionTypeEM(resultSet.getInt("transaction_type_code")).getCode();
					}
					else if (columnName.equals("transaction_mode_code")) {
						value = TransactionModeEM.getTransactionModeEM(resultSet.getInt("transaction_mode_code")).getCode();
					}
					else if (columnName.equals("user_role_code")) {
						value = UserRoleEM.getUserRoleEM(resultSet.getInt("user_role_code")).getCode();
					}
					else if (columnName.equals("payment_type_code")) {
						value = PaymentTypeEM.getPaymentTypeEM(resultSet.getInt("payment_type_code")).getCode();
					}
					else if (columnName.equals("ticket_transaction_code")) {
						value = getCode("ticket_transaction", resultSet.getInt("ticket_transaction_code"));
					}
					else if (columnName.equals("bus_seat_type_code")) {
						value = BusSeatTypeEM.getBusSeatTypeEM(resultSet.getInt("bus_seat_type_code")).getCode();
					}
					else if (columnName.equals("bus_vehicle_code")) {
						value = getCode("bus_vehicle", resultSet.getInt("bus_vehicle_code"));
					}
					else if (columnName.equals("payment_gateway_partner_code")) {
						value = getCode("payment_gateway_partner", resultSet.getInt("payment_gateway_partner_code"));
					}
					else if (columnName.equals("payment_gateway_provider_code")) {
						value = getCode("payment_gateway_provider", resultSet.getInt("payment_gateway_provider_code"));
					}
					else if (columnName.equals("payment_gateway_merchant_credentials_code")) {
						value = getCode("payment_gateway_merchant_credentials", resultSet.getInt("payment_gateway_merchant_credentials_code"));
					}
					else if (columnName.equals("payment_gateway_transaction_type_code")) {
						value = PaymentGatewayTransactionTypeEM.getPaymentGatewayTransactionTypeDTO(resultSet.getInt("payment_gateway_transaction_type_code")).getCode();
					}
					else if (columnName.equals("category_code")) {
						value = getCode("schedule_category", resultSet.getInt("category_code"));
					}
					else if (columnName.equals("payment_gateway_mode_code")) {
						value = getCode("payment_gateway_mode", resultSet.getInt("payment_gateway_mode_code"));
					}
					else if (columnName.equals("bus_vehicle_pickup_van_code")) {
						value = getCode("bus_vehicle_pickup_van", resultSet.getInt("bus_vehicle_pickup_van_code"));
					}
					else if (columnName.equals("addons_discount_category_code")) {
						value = getCode("addons_discount_category", resultSet.getInt("addons_discount_category_code"));
					}
					else if (columnName.equals("addons_discount_coupon_code")) {
						value = getCode("addons_discount_coupon", resultSet.getInt("addons_discount_coupon_code"));
					}
					else if (columnName.equals("schedule_dynamic_stage_fare_code")) {
						value = getCode("schedule_dynamic_stage_fare", resultSet.getInt("schedule_dynamic_stage_fare_code"));
					}
					else if (columnName.equals("trip_cash_head_code")) {
						value = getCode("trip_cash_head", resultSet.getInt("trip_cash_head_code"));
					}
					else if (columnName.equals("state_code")) {
						value = getCode("state", resultSet.getInt("state_code"));
					}
					else {
						if (!dataType.equals("java.lang.String") && !dataType.equals("java.lang.Date") && !dataType.equals("java.sql.Timestamp")) {
							value = resultSet.getString(columnName) != null ? resultSet.getString(columnName) : Numeric.ZERO;
						}
						else if (dataType.equals("java.sql.Timestamp")) {
							value = new DateTime(resultSet.getString(columnName).substring(0, 19)).format("YYYY-MM-DD hh:mm:ss");
						}
						else {
							value = resultSet.getString(columnName) != null ? resultSet.getString(columnName) : "NULL";
						}
					}

					// To find addons_amount is credit or debit
					if (transactionType == 1 && columnName.equals("addons_amount")) {
						columnName = "addons_amount_credit";

						resultsMap.put("addons_amount_debit", Numeric.ZERO);
					}
					else if (transactionType == 0 && columnName.equals("addons_amount")) {
						columnName = "addons_amount_debit";

						resultsMap.put("addons_amount_credit", Numeric.ZERO);
					}

					resultsMap.put(columnName, value);
				}
				catch (Exception e) {
					ArchiveImpl.ARCHIVE_LOGGER.info(e.getMessage());
					ArchiveImpl.ARCHIVE_LOGGER.info("Transaction Table Export - TableName:" + tableName + " ColumnName:" + columnName + " Value:" + value);
				}
			}
			resultMapList.add(resultsMap);
		}
		return resultMapList;
	}

	public List<Map<String, ?>> getMasterForDrill(String tableName, String fromDate, String toDate) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			if (tableName.equals("amenities")) {
				selectPS = connection.prepareStatement("SELECT id, code, name, active_flag, updated_by, updated_at FROM amenities WHERE updated_at >= ? AND updated_at <= ?");
				selectPS.setString(1, fromDate);
				selectPS.setString(2, toDate);
			}
			else if (tableName.equals("bus_seat_type")) {
				selectPS = connection.prepareStatement("SELECT id, code, name, active_flag, updated_by, updated_at FROM bus_seat_type WHERE updated_at >= ? AND updated_at <= ?");
				selectPS.setString(1, fromDate);
				selectPS.setString(2, toDate);
			}
			else if (tableName.equals("namespace")) {
				selectPS = connection.prepareStatement("SELECT id, code, name, context_token, mode, active_flag, updated_by, updated_at FROM namespace WHERE updated_at >= ? AND updated_at <= ? AND active_flag = 1");
				selectPS.setString(1, fromDate);
				selectPS.setString(2, toDate);
			}
			else if (tableName.equals("state")) {
				selectPS = connection.prepareStatement("SELECT id, code, name, active_flag, updated_by, updated_at FROM state WHERE updated_at >= ? AND updated_at <= ?");
				selectPS.setString(1, fromDate);
				selectPS.setString(2, toDate);
			}
			else if (tableName.equals("station")) {
				selectPS = connection.prepareStatement("SELECT id, code, name, state_id, active_flag, updated_by, updated_at FROM station WHERE updated_at >= ? AND updated_at <= ?");
				selectPS.setString(1, fromDate);
				selectPS.setString(2, toDate);
			}
			else if (tableName.equals("ticket_status")) {
				selectPS = connection.prepareStatement("SELECT id, code, name, active_flag, updated_by, updated_at FROM ticket_status WHERE updated_at >= ? AND updated_at <= ?");
				selectPS.setString(1, fromDate);
				selectPS.setString(2, toDate);
			}
			else if (tableName.equals("user_role")) {
				selectPS = connection.prepareStatement("SELECT id, code, name, active_flag, updated_by, updated_at FROM user_role WHERE updated_at >= ? AND updated_at <= ?");
				selectPS.setString(1, fromDate);
				selectPS.setString(2, toDate);
			}
			else if (tableName.equals("payment_gateway_provider")) {
				selectPS = connection.prepareStatement("SELECT id, code, name, service_name, active_flag, updated_by, updated_at FROM payment_gateway_provider WHERE updated_at >= ? AND updated_at <= ?");
				selectPS.setString(1, fromDate);
				selectPS.setString(2, toDate);
			}
			else if (tableName.equals("namespace_settings")) {
				selectPS = connection.prepareStatement("SELECT id, namespace_id AS namespace_code, allow_last_boarding_point_flag, cancellation_commission_revoke_flag ,notification_sms_flag, notification_email_flag, max_seat_per_transaction, seat_block_time, time_format, pnr_start_code, date_format, sender_mail_name, sendar_sms_name, domain_url, ticket_request_flag, ticket_rac , dropping_point_requried_flag, active_flag, updated_by, updated_at  FROM namespace_settings WHERE updated_at >= ? AND updated_at <= ?");
				selectPS.setString(1, fromDate);
				selectPS.setString(2, toDate);
			}
			else if (tableName.equals("payment_gateway_mode")) {
				selectPS = connection.prepareStatement("SELECT id, code, name, active_flag, updated_by, updated_at FROM payment_gateway_mode WHERE updated_at >= ? AND updated_at <= ?");
				selectPS.setString(1, fromDate);
				selectPS.setString(2, toDate);
			}
			@Cleanup
			ResultSet resultSet = selectPS.executeQuery();
			return getResultList(resultSet, tableName);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public String getCode(String tableName, int id) {
		String code = Text.EMPTY;
		String key = tableName + "_" + id;
		if (codeMap == null || codeMap.size() == Numeric.ZERO_INT || codeMap.get(key) == null) {
			try {
				@Cleanup
				Connection connection = ConnectDAO.getConnection();
				@Cleanup
				PreparedStatement selectPS = connection.prepareStatement("SELECT code FROM " + tableName + " WHERE id = ?");
				selectPS.setInt(1, id);
				@Cleanup
				ResultSet selectRS = selectPS.executeQuery();
				if (selectRS.next()) {
					code = selectRS.getString("code");
					codeMap.put(key, code);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				throw new ServiceException(e.getMessage());
			}
			finally {
				if (id == 0) {
					code = Text.NULL;
				}
				else if (StringUtil.isNull(code)) {
					throw new ServiceException(ErrorCode.UPDATE_FAIL);
				}
			}
		}
		else {
			code = codeMap.get(key);
		}
		return code;
	}

	public String getNamesapce(String tableName, String referenceCode, Map<String, String> namespaceCodeMap) {
		String code = Text.EMPTY;
		String key = tableName + "_" + referenceCode;
		if (namespaceCodeMap == null || namespaceCodeMap.size() == Numeric.ZERO_INT || namespaceCodeMap.get(key) == null) {
			try {
				@Cleanup
				Connection connection = ConnectDAO.getConnection();
				@Cleanup
				PreparedStatement selectPS = connection.prepareStatement("SELECT nam.code FROM " + tableName + " tmp, namespace nam WHERE tmp.code = ? AND tmp.namespace_id = nam.id");
				selectPS.setString(1, referenceCode);
				@Cleanup
				ResultSet selectRS = selectPS.executeQuery();
				if (selectRS.next()) {
					code = selectRS.getString("nam.code");

					namespaceCodeMap.put(key, code);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				throw new ServiceException(e.getMessage());
			}
			finally {
				if (StringUtil.isNull(code)) {
					System.out.println(tableName + "-" + referenceCode);
					throw new ServiceException(ErrorCode.UPDATE_FAIL);
				}
			}
		}
		else {
			code = namespaceCodeMap.get(key);
		}
		return code;
	}

	public List<Map<String, ?>> getQueryResultsMap(String query, List<DBQueryParamDTO> params) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			NamedPrepareStatement statement = new NamedPrepareStatement(connection, query);
			if (params != null) {
				for (DBQueryParamDTO param : params) {
					String name = param.getParamName();
					String value = param.getValue();
					if (StringUtil.isNotNull(value) && StringUtils.isNumeric(value) && value.length() <= 8) {
						statement.setInt(name, Integer.parseInt(value));
					}
					else if (StringUtil.isNotNull(value) && name.contains("Date") && DateUtil.isValidDate(value)) {
						statement.setString(name, value);
					}
					else if (StringUtil.isNotNull(value) && StringUtils.isAlphanumeric(value)) {
						statement.setString(name, value);
					}
					else if (!StringUtils.isBlank(value)) {
						statement.setString(name, value);
					}
				}
			}
			@Cleanup
			ResultSet resultSet = statement.executeQuery();
			return getResultMaps(resultSet);
		}
		catch (Exception e) {
			ArchiveImpl.ARCHIVE_LOGGER.info("query " + query + Lists.transform(params, Functions.toStringFunction()));
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	private List<Map<String, ?>> getResultMaps(ResultSet resultSet) throws Exception {
		ResultSetMetaData metaData = resultSet.getMetaData();
		int columnCount = metaData.getColumnCount();
		Map<String, String> columns = new HashMap<>();
		for (int i = 1; i <= columnCount; i++) {
			columns.put(metaData.getColumnLabel(i), metaData.getColumnClassName(i));
		}

		List<Map<String, ?>> resultMapList = new ArrayList<>();
		while (resultSet.next()) {
			Object value = null;
			Map<String, Object> resultsMap = new HashMap<>();
			for (String columnName : columns.keySet()) {
				String dataType = columns.get(columnName);
				switch (dataType) {
					case "java.lang.String":
						value = resultSet.getString(columnName);
						if (value == null) {
							value = Text.NULL;
						}
						break;
					case "java.lang.Integer":
						value = resultSet.getInt(columnName);
						break;
					case "java.lang.Double":
						value = resultSet.getDouble(columnName);
						break;
					case "java.math.BigDecimal":
						value = resultSet.getBigDecimal(columnName);
						break;
					case "java.lang.Long":
						value = resultSet.getLong(columnName);
						break;
					case "java.sql.Date":
						value = resultSet.getDate(columnName);
						break;
					case "java.sql.Timestamp":
						value = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(resultSet.getTimestamp(columnName));
						break;
					case "java.sql.Time":
						value = resultSet.getTime(columnName);
						break;
					default:
						continue;
				}
				resultsMap.put(columnName, value);
			}
			resultMapList.add(resultsMap);
		}
		return resultMapList;
	}
}
