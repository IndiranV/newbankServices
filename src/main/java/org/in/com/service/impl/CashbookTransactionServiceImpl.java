package org.in.com.service.impl;

import java.util.List;

import org.in.com.dao.CashbookTransactionDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusVehicleDTO;
import org.in.com.dto.CashbookTransactionDTO;
import org.in.com.dto.OrganizationDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.enumeration.CashbookAckStatusEM;
import org.in.com.dto.enumeration.CashbookCategoryEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.BusVehicleService;
import org.in.com.service.CashbookTransactionService;
import org.in.com.service.CashbookVendorService;
import org.in.com.service.OrganizationService;
import org.in.com.service.ScheduleService;
import org.in.com.service.TicketService;
import org.in.com.service.TripService;
import org.in.com.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CashbookTransactionServiceImpl implements CashbookTransactionService {

	@Autowired
	UserService userService;
	@Autowired
	BusVehicleService busVehicleService;
	@Autowired
	TripService tripService;
	@Autowired
	OrganizationService organizationService;
	@Autowired
	TicketService ticketService;
	@Autowired
	ScheduleService scheduleService;
	@Autowired
	CashbookVendorService cashbookVendorService;

	@Override
	public void updateCashBookTransaction(AuthDTO authDTO, CashbookTransactionDTO dto) {
		if (CashbookCategoryEM.BRANCH.getId() == dto.getCashbookCategory().getId()) {
			OrganizationDTO organizationDTO = new OrganizationDTO();
			organizationDTO.setCode(dto.getReferenceCode());
			organizationService.getOrganization(authDTO, organizationDTO);
			if (organizationDTO.getId() == 0) {
				throw new ServiceException(ErrorCode.INVALID_ORGANIZATION);
			}

		}
		else if (CashbookCategoryEM.PNR.getId() == dto.getCashbookCategory().getId()) {
			TicketDTO ticketDTO = new TicketDTO();
			ticketDTO.setCode(dto.getReferenceCode());
			ticketService.getTicketStatus(authDTO, ticketDTO);
			if (ticketDTO.getId() == 0) {
				throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
			}
		}
		else if (CashbookCategoryEM.TRIP.getId() == dto.getCashbookCategory().getId()) {
			TripDTO tripDTO = new TripDTO();
			tripDTO.setCode(dto.getReferenceCode());
			tripService.getTrip(authDTO, tripDTO);
			if (tripDTO.getId() == 0) {
				throw new ServiceException(ErrorCode.INVALID_TRIP_CODE);
			}
		}
		else if (CashbookCategoryEM.VEHICLE.getId() == dto.getCashbookCategory().getId()) {
			BusVehicleDTO busVehicleDTO = new BusVehicleDTO();
			busVehicleDTO.setCode(dto.getReferenceCode());
			busVehicleService.getBusVehicles(authDTO, busVehicleDTO);
			if (busVehicleDTO.getId() == 0) {
				throw new ServiceException(ErrorCode.INVALID_VEHICLE_CODE);
			}
		}
		CashbookTransactionDAO cashbookTransactionDAO = new CashbookTransactionDAO();
		cashbookTransactionDAO.updateCashBookTransaction(authDTO, dto);
	}

	@Override
	public void updateCashbookTransactionStatus(AuthDTO authDTO, List<CashbookTransactionDTO> cashbookTransactions, CashbookAckStatusEM cashbookAckStatus) {
		CashbookTransactionDAO cashbookTransactionDAO = new CashbookTransactionDAO();
		boolean isRejectedTransactionExist = cashbookTransactionDAO.getRejectedCashbookTransactions(authDTO, cashbookTransactions);
		if (isRejectedTransactionExist && cashbookAckStatus.getId() == CashbookAckStatusEM.APPROVED.getId()) {
			throw new ServiceException(ErrorCode.UPDATE_FAIL, "Unable to approve rejected transactions");
		}

		cashbookTransactionDAO.updateCashbookTransactionStatus(authDTO, cashbookTransactions);
	}

	@Override
	public void updateCashbookTransactionImageDetails(AuthDTO authDTO, String referenceCode, String imageDetailsIds) {
		CashbookTransactionDAO cashbookTransactionDAO = new CashbookTransactionDAO();
		cashbookTransactionDAO.updateCashbookTransactionImageDetails(authDTO, referenceCode, imageDetailsIds);
	}

	@Override
	public void getCashbookTransaction(AuthDTO authDTO, CashbookTransactionDTO cashbookTransactionDTO) {
		CashbookTransactionDAO cashbookTransactionDAO = new CashbookTransactionDAO();
		cashbookTransactionDAO.getCashbookTransactions(authDTO, cashbookTransactionDTO);
	}
}
