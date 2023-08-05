package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.CashbookTransactionDTO;
import org.in.com.dto.enumeration.CashbookAckStatusEM;

public interface CashbookTransactionService {
	public void updateCashBookTransaction(AuthDTO authDTO, CashbookTransactionDTO dto);

	public void updateCashbookTransactionStatus(AuthDTO authDTO, List<CashbookTransactionDTO> cashbookTransactions, CashbookAckStatusEM cashbookAckStatus);

	public void updateCashbookTransactionImageDetails(AuthDTO authDTO, String referenceCode, String imageDetailsIds);

	public void getCashbookTransaction(AuthDTO authDTO, CashbookTransactionDTO cashbookTransactionDTO);
}
