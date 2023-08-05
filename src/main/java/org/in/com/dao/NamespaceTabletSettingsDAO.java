package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.NamespaceDTO;
import org.in.com.dto.NamespaceTabletSettingsDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.utils.StringUtil;

import lombok.Cleanup;

public class NamespaceTabletSettingsDAO {

	public void updateNamespaceTabletSettings(AuthDTO authDTO, NamespaceTabletSettingsDTO tableSettings) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;

			@Cleanup
			CallableStatement termSt = connection.prepareCall("{CALL EZEE_SP_NAMESPACE_TABLET_SETTINGS_IUD( ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?)}");
			termSt.setInt(++pindex, authDTO.getNamespace().getId());
			termSt.setString(++pindex, StringUtils.join(tableSettings.getTabs(), ","));
			termSt.setInt(++pindex, tableSettings.getTripSyncPeriod());
			termSt.setString(++pindex, tableSettings.getFlagCodes());
			termSt.setInt(++pindex, tableSettings.getHideBookedTicketFare());
			termSt.setBigDecimal(++pindex, tableSettings.getMaxDiscountAmount());
			termSt.setInt(++pindex, tableSettings.getBookingOpenMinutes());
			termSt.setInt(++pindex, tableSettings.getTripChartOpenMinutes());
			termSt.setBigDecimal(++pindex, tableSettings.getMaxServiceChargePerSeat());
			termSt.setString(++pindex, tableSettings.getBookingType());
			termSt.setInt(++pindex, tableSettings.getForceReleaseFlag());
			termSt.setInt(++pindex, tableSettings.getActiveFlag());
			termSt.setInt(++pindex, authDTO.getUser().getId());
			termSt.setInt(++pindex, 0);
			termSt.registerOutParameter(++pindex, Types.INTEGER);
			termSt.execute();
			if (termSt.getInt("pitRowCount") == 0) {
				throw new ServiceException(ErrorCode.UPDATE_FAIL);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
	}
	
	public NamespaceTabletSettingsDTO getNamespaceTabletSettings(AuthDTO authDTO, NamespaceDTO namespace) {
		NamespaceTabletSettingsDTO tabletSettingsDTO = null;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT tabs, trip_sync_period, flag_codes, hide_booked_ticket_fare, max_discount_amount, booking_open_minutes, trip_chart_open_minutes, max_service_charge_per_seat, booking_type, force_release_flag, active_flag FROM namespace_tablet_settings WHERE namespace_id = ? AND active_flag = 1");
			selectPS.setInt(1, namespace.getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				tabletSettingsDTO = new NamespaceTabletSettingsDTO();
				
				String tabs = selectRS.getString("tabs");
				tabletSettingsDTO.setTabs(StringUtil.isNotNull(tabs) ? Arrays.asList(tabs.split(Text.COMMA)) : new ArrayList<>());
				
				tabletSettingsDTO.setTripSyncPeriod(selectRS.getInt("trip_sync_period"));
				tabletSettingsDTO.setFlagCodes(selectRS.getString("flag_codes"));
				tabletSettingsDTO.setMaxDiscountAmount(selectRS.getBigDecimal("max_discount_amount"));
				tabletSettingsDTO.setBookingOpenMinutes(selectRS.getInt("booking_open_minutes"));
				tabletSettingsDTO.setMaxServiceChargePerSeat(selectRS.getBigDecimal("max_service_charge_per_seat"));
				tabletSettingsDTO.setBookingType(selectRS.getString("booking_type"));
				tabletSettingsDTO.setHideBookedTicketFare(selectRS.getInt("hide_booked_ticket_fare"));
				tabletSettingsDTO.setForceReleaseFlag(selectRS.getInt("force_release_flag"));
				tabletSettingsDTO.setTripChartOpenMinutes(selectRS.getInt("trip_chart_open_minutes"));
				tabletSettingsDTO.setActiveFlag(selectRS.getInt("active_flag"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return tabletSettingsDTO;
	}
	
}

