package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.PhoneNumberBlockDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;

import lombok.Cleanup;

public class PhoneNumberBlockDAO {

	public void updatePhoneNumberBlock(AuthDTO authDTO, PhoneNumberBlockDTO mobileBlockDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;

			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{call EZEE_SP_TICKET_PHONE_NUMBER_BLOCK_IUD(?,?,?,?,?, ?,?)}");
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, mobileBlockDTO.getMobile());
			callableStatement.setString(++pindex, mobileBlockDTO.getRemarks());
			callableStatement.setInt(++pindex, mobileBlockDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") == 0) {
				throw new ServiceException(ErrorCode.UPDATE_FAIL);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public boolean isExist(AuthDTO authDTO, PhoneNumberBlockDTO mobileBlockDTO) {
		boolean isExist = Text.FALSE;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT 1 FROM ticket_phone_number_block WHERE namespace_id = ? AND mobile = ? AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setString(2, mobileBlockDTO.getMobile());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				isExist = Text.TRUE;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return isExist;
	}

	public PhoneNumberBlockDTO getPhoneNumberBlock(AuthDTO authDTO, PhoneNumberBlockDTO phoneNumberBlock) {
		PhoneNumberBlockDTO phoneNumberBlockDTO = null;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id, mobile, remarks, active_flag FROM ticket_phone_number_block WHERE namespace_id = ? AND mobile = ? AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setString(2, phoneNumberBlock.getMobile());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				phoneNumberBlockDTO = new PhoneNumberBlockDTO();
				phoneNumberBlockDTO.setId(selectRS.getInt("id"));
				phoneNumberBlockDTO.setMobile(selectRS.getString("mobile"));
				phoneNumberBlockDTO.setRemarks(selectRS.getString("remarks"));
				phoneNumberBlockDTO.setActiveFlag(selectRS.getInt("active_flag"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}

		return phoneNumberBlockDTO;
	}

	public List<PhoneNumberBlockDTO> getAllPhoneNumberBlock(AuthDTO authDTO) {
		List<PhoneNumberBlockDTO> phoneNumberBlockList = new ArrayList<PhoneNumberBlockDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT mobile, remarks, active_flag FROM ticket_phone_number_block WHERE namespace_id = ? AND active_flag < 2");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				PhoneNumberBlockDTO phoneNumberBlock = new PhoneNumberBlockDTO();
				phoneNumberBlock.setMobile(selectRS.getString("mobile"));
				phoneNumberBlock.setRemarks(selectRS.getString("remarks"));
				phoneNumberBlock.setActiveFlag(selectRS.getInt("active_flag"));
				phoneNumberBlockList.add(phoneNumberBlock);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}

		return phoneNumberBlockList;
	}
}
