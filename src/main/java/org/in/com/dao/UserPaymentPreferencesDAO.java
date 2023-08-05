package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

import lombok.Cleanup;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.UserPaymentPreferencesDTO;
import org.in.com.dto.enumeration.FrequencyModeEM;
import org.in.com.dto.enumeration.PreferenceTypeEM;
import org.in.com.exception.ServiceException;

public class UserPaymentPreferencesDAO {
	public void getUserPaymentPreferencesDTO(AuthDTO authDTO, UserPaymentPreferencesDTO preferencesDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			if (preferencesDTO.getUser().getId() != 0) {
				selectPS = connection.prepareStatement("SELECT pref.id, preferenced_type,frequency_mode,travel_date_flag,day_of_month,day_of_week,day_of_time,email_address,pref.active_flag FROM user_payment_preferences WHERE namespace_id = ?  AND user_id = ? AND pref.active_flag = 1");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setInt(2, preferencesDTO.getUser().getId());
			}
			else {
				selectPS = connection.prepareStatement("SELECT pref.id, preferenced_type,frequency_mode,travel_date_flag,day_of_month,day_of_week,day_of_time,email_address,pref.active_flag FROM user_payment_preferences pref,user usr WHERE usr.namespace_id  = pref.namespace_id AND pref.namespace_id = ?  AND pref.user_id = usr.id AND usr.code = ? AND pref.active_flag = 1");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setString(2, preferencesDTO.getUser().getCode());
			}

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				preferencesDTO.setId(selectRS.getInt("id"));
				preferencesDTO.setPreferenceType(PreferenceTypeEM.getPreferenceTypeEM(selectRS.getString("preferenced_type")));
				preferencesDTO.setFrequencyMode(FrequencyModeEM.getFrequencyModeEM(selectRS.getString("frequency_mode")));
				preferencesDTO.setTravelDateFlag(selectRS.getInt("travel_date_flag"));
				preferencesDTO.setDayOfMonth(selectRS.getInt("day_of_month"));
				preferencesDTO.setDayOfWeek(selectRS.getString("day_of_week"));
				preferencesDTO.setDayOfTime(selectRS.getString("day_of_time"));
				preferencesDTO.setEmailAddress(selectRS.getString("email_address"));
				preferencesDTO.setActiveFlag(selectRS.getInt("active_flag"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public void Update(AuthDTO authDTO, UserPaymentPreferencesDTO preferencesDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{call  EZEE_SP_USER_PAYMENT_PREFERENCES_IUD(?,?,?,?,? ,?,?,?,?,? ,?,?,?)}");
			callableStatement.setString(++pindex, preferencesDTO.getUser().getCode());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setInt(++pindex, preferencesDTO.getTravelDateFlag());
			callableStatement.setString(++pindex, preferencesDTO.getPreferenceType().getCode());
			callableStatement.setString(++pindex, preferencesDTO.getFrequencyMode().getCode());
			callableStatement.setString(++pindex, preferencesDTO.getDayOfTime());
			callableStatement.setString(++pindex, preferencesDTO.getDayOfWeek());
			callableStatement.setInt(++pindex, preferencesDTO.getDayOfMonth());

			callableStatement.setString(++pindex, preferencesDTO.getEmailAddress());
			callableStatement.setInt(++pindex, preferencesDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				preferencesDTO.setCode(callableStatement.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
	}
}
