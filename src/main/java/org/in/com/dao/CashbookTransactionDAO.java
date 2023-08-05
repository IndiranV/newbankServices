package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.CashbookTransactionDTO;
import org.in.com.dto.CashbookTypeDTO;
import org.in.com.dto.CashbookVendorDTO;
import org.in.com.dto.ImageDetailsDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.CashbookAckStatusEM;
import org.in.com.dto.enumeration.CashbookCategoryEM;
import org.in.com.dto.enumeration.TransactionModeEM;
import org.in.com.exception.ServiceException;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;

import lombok.Cleanup;

public class CashbookTransactionDAO {
	public void updateCashBookTransaction(AuthDTO authDTO, CashbookTransactionDTO cashbookTransactionDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_CASHBOOK_TRANSACTION_IUD(?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?)}");
			callableStatement.setString(++pindex, cashbookTransactionDTO.getCode());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, DateUtil.convertDateTime(cashbookTransactionDTO.getTransactionDate()));
			callableStatement.setInt(++pindex, cashbookTransactionDTO.getCashbookCategory().getId());
			callableStatement.setString(++pindex, cashbookTransactionDTO.getReferenceCode());
			callableStatement.setString(++pindex, cashbookTransactionDTO.getCashbookType().getCode());
			callableStatement.setString(++pindex, cashbookTransactionDTO.getTransactionType());
			callableStatement.setBigDecimal(++pindex, cashbookTransactionDTO.getAmount());
			callableStatement.setString(++pindex, cashbookTransactionDTO.getUser().getCode());
			callableStatement.setString(++pindex, cashbookTransactionDTO.getCashbookVendor().getCode());
			callableStatement.setInt(++pindex, cashbookTransactionDTO.getTransactionMode().getId());
			callableStatement.setInt(++pindex, cashbookTransactionDTO.getAcknowledgeStatus().getId());
			callableStatement.setInt(++pindex, cashbookTransactionDTO.getPaymentStatusFlag());
			callableStatement.setString(++pindex, cashbookTransactionDTO.getImageIds());
			callableStatement.setString(++pindex, cashbookTransactionDTO.getRemarks());
			callableStatement.setInt(++pindex, cashbookTransactionDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				cashbookTransactionDTO.setCode(callableStatement.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public void updateCashbookTransactionImageDetails(AuthDTO authDTO, String referenceCode, String imageDetailsIds) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			PreparedStatement preparedStatement = connection.prepareStatement("UPDATE cashbook_transaction SET image_id = ? WHERE code = ? AND namespace_id = ? AND active_flag = 1");
			preparedStatement.setString(++pindex, imageDetailsIds);
			preparedStatement.setString(++pindex, referenceCode);
			preparedStatement.setInt(++pindex, authDTO.getNamespace().getId());
			preparedStatement.executeUpdate();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public boolean getRejectedCashbookTransactions(AuthDTO authDTO, List<CashbookTransactionDTO> cashbookTransactions) {
		boolean isRejectedTransactionExist = false;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			@Cleanup
			ResultSet selectRS = null;
			for (CashbookTransactionDTO cashbookTransactionDTO : cashbookTransactions) {
				selectPS = connection.prepareStatement("SELECT 1 FROM cashbook_transaction WHERE  namespace_id = ? AND code = ? AND acknowledge_status_id = 3 AND active_flag = 1");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setString(2, cashbookTransactionDTO.getCode());

				selectRS = selectPS.executeQuery();
				if (selectRS.next()) {
					isRejectedTransactionExist = true;
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return isRejectedTransactionExist;
	}

	public void updateCashbookTransactionStatus(AuthDTO authDTO, List<CashbookTransactionDTO> cashbookTransactions) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			selectPS = connection.prepareStatement("UPDATE cashbook_transaction SET acknowledge_status_id = ? WHERE namespace_id = ? AND code = ? AND active_flag = 1");
			for (CashbookTransactionDTO cashbookTransactionDTO : cashbookTransactions) {
				selectPS.setInt(1, cashbookTransactionDTO.getAcknowledgeStatus().getId());
				selectPS.setInt(2, authDTO.getNamespace().getId());
				selectPS.setString(3, cashbookTransactionDTO.getCode());
				selectPS.addBatch();
			}
			selectPS.executeBatch();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public void getCashbookTransactions(AuthDTO authDTO, CashbookTransactionDTO cashbookTransactionDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code, transaction_date, reference_type_id, reference_code, cashbook_type_code, transaction_type, amount, user_id, cashbook_vendor_code, payment_mode_id, acknowledge_status_id, image_id, remarks, active_flag, updated_by, updated_at FROM cashbook_transaction WHERE namespace_id = ? AND code = ? AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setString(2, cashbookTransactionDTO.getCode());

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				cashbookTransactionDTO.setCode(selectRS.getString("code"));
				cashbookTransactionDTO.setTransactionDate(DateUtil.getDateTime(selectRS.getString("transaction_date")));
				cashbookTransactionDTO.setCashbookCategory(CashbookCategoryEM.getCashbookCategoryEM(selectRS.getString("reference_type_id")));
				cashbookTransactionDTO.setReferenceCode(selectRS.getString("reference_code"));

				CashbookTypeDTO cashbookType = new CashbookTypeDTO();
				cashbookType.setCode(selectRS.getString("cashbook_type_code"));
				cashbookTransactionDTO.setCashbookType(cashbookType);

				cashbookTransactionDTO.setTransactionType(selectRS.getString("transaction_type"));
				cashbookTransactionDTO.setAmount(selectRS.getBigDecimal("amount"));

				UserDTO user = new UserDTO();
				user.setId(selectRS.getInt("user_id"));
				cashbookTransactionDTO.setUser(user);

				CashbookVendorDTO cashbookVendor = new CashbookVendorDTO();
				cashbookVendor.setCode(selectRS.getString("cashbook_vendor_code"));
				cashbookTransactionDTO.setCashbookVendor(cashbookVendor);

				cashbookTransactionDTO.setTransactionMode(TransactionModeEM.getTransactionModeEM(selectRS.getString("payment_mode_id")));
				cashbookTransactionDTO.setAcknowledgeStatus(CashbookAckStatusEM.getCashbookAckStatusEM(selectRS.getString("acknowledge_status_id")));
				cashbookTransactionDTO.setImages(convertImageDetails(selectRS.getString("image_id")));
				cashbookTransactionDTO.setRemarks(selectRS.getString("remarks"));
				cashbookTransactionDTO.setActiveFlag(selectRS.getInt("active_flag"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
	}

	private List<ImageDetailsDTO> convertImageDetails(String imageDetailsIds) {
		List<ImageDetailsDTO> imageDetailsList = new ArrayList<ImageDetailsDTO>();
		if (StringUtil.isNotNull(imageDetailsIds)) {
			List<String> imageIds = Arrays.asList(imageDetailsIds.split(Text.COMMA));
			for (String imageId : imageIds) {
				if (imageId.equals(Numeric.ZERO)) {
					continue;
				}
				ImageDetailsDTO imegDetailsDTO = new ImageDetailsDTO();
				imegDetailsDTO.setId(Integer.valueOf(imageId));
				imageDetailsList.add(imegDetailsDTO);
			}
		}
		return imageDetailsList;
	}
}
