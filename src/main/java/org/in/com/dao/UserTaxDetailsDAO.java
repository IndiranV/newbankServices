package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.in.com.constants.Numeric;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.UserTaxDetailsDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.utils.DateUtil;

import lombok.Cleanup;

public class UserTaxDetailsDAO {

	public void updateUserTaxDetails(AuthDTO authDTO, UserTaxDetailsDTO userTaxDetailsDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_USER_TAX_DETAILS_IUD(?,?,?,?,?, ?,?,?,?)}");
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, DateUtil.convertDate(userTaxDetailsDTO.getFromDate()));
			callableStatement.setBigDecimal(++pindex, userTaxDetailsDTO.getTdsTaxValue());
			callableStatement.setString(++pindex, userTaxDetailsDTO.getPanCardCode());
			callableStatement.setInt(++pindex, userTaxDetailsDTO.getUser().getId());
			callableStatement.setInt(++pindex, userTaxDetailsDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") == 0 && userTaxDetailsDTO.getActiveFlag() == Numeric.ONE_INT) {
				throw new ServiceException(ErrorCode.UPDATE_FAIL);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public UserTaxDetailsDTO getUsertaxDetails(AuthDTO authDTO, UserDTO userDTO) {
		UserTaxDetailsDTO userTaxDetail = new UserTaxDetailsDTO();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			selectPS = connection.prepareStatement("SELECT tax.from_date, tax.tds_tax_value, tax.pan_card_code, usr.code, usr.first_name, tax.active_flag FROM user_tax_details tax, user usr WHERE usr.namespace_id = ? AND usr.code = ? AND tax.user_id = usr.id AND tax.namespace_id = usr.namespace_id AND tax.active_flag = 1 AND usr.active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setString(2, userDTO.getCode());

			if (selectPS != null) {
				@Cleanup
				ResultSet selectRS = selectPS.executeQuery();
				if (selectRS.next()) {
					userTaxDetail.setFromDate(DateUtil.getDateTime(selectRS.getString("tax.from_date")));
					userTaxDetail.setTdsTaxValue(selectRS.getBigDecimal("tax.tds_tax_value"));
					userTaxDetail.setPanCardCode(selectRS.getString("tax.pan_card_code"));

					UserDTO user = new UserDTO();
					user.setCode(selectRS.getString("usr.code"));
					user.setName(selectRS.getString("usr.first_name"));
					userTaxDetail.setUser(user);

					userTaxDetail.setActiveFlag(selectRS.getInt("tax.active_flag"));
				}
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return userTaxDetail;

	}

	public List<UserTaxDetailsDTO> getAllUserTaxdetails(AuthDTO authDTO) {
		List<UserTaxDetailsDTO> UserTaxDetailsList = new ArrayList<UserTaxDetailsDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			selectPS = connection.prepareStatement("SELECT tax.from_date, tax.tds_tax_value, tax.pan_card_code, usr.code, usr.first_name, tax.active_flag FROM user_tax_details tax, user usr WHERE tax.namespace_id = ? AND tax.user_id = usr.id AND tax.namespace_id = usr.namespace_id AND  usr.active_flag = 1 AND tax.active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				UserTaxDetailsDTO userTaxDetail = new UserTaxDetailsDTO();
				userTaxDetail.setFromDate(DateUtil.getDateTime(selectRS.getString("tax.from_date")));
				userTaxDetail.setTdsTaxValue(selectRS.getBigDecimal("tax.tds_tax_value"));
				userTaxDetail.setPanCardCode(selectRS.getString("tax.pan_card_code"));

				UserDTO userDTO = new UserDTO();
				userDTO.setCode(selectRS.getString("usr.code"));
				userDTO.setName(selectRS.getString("usr.first_name"));
				userTaxDetail.setUser(userDTO);

				userTaxDetail.setActiveFlag(selectRS.getInt("tax.active_flag"));
				UserTaxDetailsList.add(userTaxDetail);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}

		return UserTaxDetailsList;
	}

}
