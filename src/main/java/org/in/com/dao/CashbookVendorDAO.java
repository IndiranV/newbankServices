package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.CashbookVendorDTO;
import org.in.com.exception.ServiceException;

import lombok.Cleanup;

public class CashbookVendorDAO {
	public CashbookVendorDTO updateCashbookVendor(AuthDTO authDTO, CashbookVendorDTO cashbookVendorDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_CASHBOOK_VENDOR_IUD(?,?,?,?,?, ?,?,?,?,?, ?)}");
			callableStatement.setString(++pindex, cashbookVendorDTO.getCode());
			callableStatement.setString(++pindex, cashbookVendorDTO.getName());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, cashbookVendorDTO.getMobileNumber());
			callableStatement.setString(++pindex, cashbookVendorDTO.getAddress());
			callableStatement.setString(++pindex, cashbookVendorDTO.getEmail());
			callableStatement.setString(++pindex, cashbookVendorDTO.getBankDetails());
			callableStatement.setInt(++pindex, cashbookVendorDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				cashbookVendorDTO.setCode(callableStatement.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return cashbookVendorDTO;
	}

	public List<CashbookVendorDTO> getCashbookVendors(AuthDTO authDTO) {
		List<CashbookVendorDTO> list = new ArrayList<CashbookVendorDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code, name, mobile_number, address, email, bank_details, active_flag FROM cashbook_vendor WHERE namespace_id = ? AND active_flag < 2");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				CashbookVendorDTO cashbookVendorDTO = new CashbookVendorDTO();
				cashbookVendorDTO.setCode(selectRS.getString("code"));
				cashbookVendorDTO.setName(selectRS.getString("name"));
				cashbookVendorDTO.setMobileNumber(selectRS.getString("mobile_number"));
				cashbookVendorDTO.setAddress(selectRS.getString("address"));
				cashbookVendorDTO.setEmail(selectRS.getString("email"));
				cashbookVendorDTO.setBankDetails(selectRS.getString("bank_details"));
				cashbookVendorDTO.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(cashbookVendorDTO);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public CashbookVendorDTO getCashbookVendor(AuthDTO authDTO, CashbookVendorDTO cashbookVendorDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id, code, name, mobile_number, address, email, bank_details, active_flag FROM cashbook_vendor WHERE namespace_id = ? AND code = ? AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setString(2, cashbookVendorDTO.getCode());

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				cashbookVendorDTO.setId(selectRS.getInt("id"));
				cashbookVendorDTO.setCode(selectRS.getString("code"));
				cashbookVendorDTO.setName(selectRS.getString("name"));
				cashbookVendorDTO.setMobileNumber(selectRS.getString("mobile_number"));
				cashbookVendorDTO.setAddress(selectRS.getString("address"));
				cashbookVendorDTO.setEmail(selectRS.getString("email"));
				cashbookVendorDTO.setBankDetails(selectRS.getString("bank_details"));
				cashbookVendorDTO.setActiveFlag(selectRS.getInt("active_flag"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return cashbookVendorDTO;
	}
}
