package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.CashbookTypeDTO;
import org.in.com.dto.enumeration.TransactionModeEM;
import org.in.com.exception.ServiceException;

import lombok.Cleanup;

public class CashbookTypeDAO {
	public List<CashbookTypeDTO> getCashbookTypes(AuthDTO authDTO) {
		List<CashbookTypeDTO> list = new ArrayList<CashbookTypeDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code, name, transaction_type, transaction_mode_id, active_flag FROM cashbook_type WHERE namespace_id = ? AND active_flag < 2");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				CashbookTypeDTO cashbookTypeDTO = new CashbookTypeDTO();
				cashbookTypeDTO.setCode(selectRS.getString("code"));
				cashbookTypeDTO.setName(selectRS.getString("name"));
				cashbookTypeDTO.setTransactionType(selectRS.getString("transaction_type"));
				cashbookTypeDTO.setTransactionMode(TransactionModeEM.getTransactionModeEM(selectRS.getInt("transaction_mode_id")));
				cashbookTypeDTO.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(cashbookTypeDTO);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public void updateCashbookType(AuthDTO authDTO, CashbookTypeDTO dto) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_CASHBOOK_TYPE_IUD(?,?,?,?,?, ?,?,?,?)}");
			callableStatement.setString(++pindex, dto.getCode());
			callableStatement.setString(++pindex, dto.getName());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, dto.getTransactionType());
			callableStatement.setInt(++pindex, dto.getTransactionMode().getId());
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
	}
}
