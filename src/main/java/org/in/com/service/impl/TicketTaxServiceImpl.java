package org.in.com.service.impl;

import org.apache.commons.lang3.text.WordUtils;
import org.in.com.aggregator.mail.EmailService;
import org.in.com.constants.Text;
import org.in.com.dao.TicketTaxDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BookingDTO;
import org.in.com.dto.NamespaceTaxDTO;
import org.in.com.dto.StateDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketTaxDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.enumeration.StateEM;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.NamespaceTaxService;
import org.in.com.service.StateService;
import org.in.com.service.TicketService;
import org.in.com.service.TicketTaxService;
import org.in.com.utils.DateUtil;
import org.in.com.utils.GSTINValidator;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class TicketTaxServiceImpl implements TicketTaxService {
	@Autowired
	TicketService ticketService;
	@Autowired
	NamespaceTaxService taxService;
	@Autowired
	EmailService emailService;
	@Autowired
	StateService stateService;

	@Override
	public void addTicketTax(AuthDTO authDTO, TicketDTO ticket, TicketTaxDTO ticketTax) {
		if (StringUtil.isNotNull(ticketTax.getGstin())) {
			TicketTaxDAO ticketTaxDAO = new TicketTaxDAO();
			ticketTaxDAO.addTicketTax(authDTO, ticket, ticketTax);
		}
	}

	@Override
	public TicketTaxDTO getTicketTax(AuthDTO authDTO, TicketDTO ticket) {
		TicketDTO ticketDTO = ticketService.getTicketStatus(authDTO, ticket);
		if (ticketDTO.getTicketStatus().getId() != TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId()) {
			throw new ServiceException(ErrorCode.NOT_CONFIRM_BOOKED_TICKET);
		}
		TicketTaxDAO ticketTaxDAO = new TicketTaxDAO();
		TicketTaxDTO ticketTaxDTO = ticketTaxDAO.getTicketTax(authDTO, ticketDTO);
		if (ticketTaxDTO.getId() == 0) {
			throw new ServiceException(ErrorCode.INVALID_GSTIN);
		}
		return ticketTaxDTO;
	}

	@Override
	public void updateTicketTax(AuthDTO authDTO, TicketDTO ticket, TicketTaxDTO ticketTaxDTO) {
		TicketDTO repoTicketDTO = new TicketDTO();
		repoTicketDTO.setCode(ticket.getCode());
		ticketService.getTicketStatus(authDTO, repoTicketDTO);
		if (repoTicketDTO.getTax() == null || repoTicketDTO.getTax().getId() == 0) {
			throw new ServiceException(ErrorCode.UNABLE_TO_UPDATE);
		}

		// check GST already updated
		TicketTaxDAO ticketTaxDAO = new TicketTaxDAO();
		TicketTaxDTO ticketTaxExist = ticketTaxDAO.getTicketTax(authDTO, repoTicketDTO);
		if (StringUtil.isNotNull(ticketTaxExist.getGstin())) {
			throw new ServiceException(ErrorCode.UNABLE_TO_UPDATE, "Already GST Details updated");
		}
		repoTicketDTO.setTax(taxService.getTaxbyId(authDTO, repoTicketDTO.getTax()));

		if (DateUtil.getDayDifferent(repoTicketDTO.getTripDate().getStartOfDay(), DateUtil.NOW().getStartOfDay()) > 10) {
			throw new ServiceException(ErrorCode.UNABLE_TO_UPDATE, "Can't update the behind GSTR date");
		}

		// Calculate Ticket Tax
		TicketTaxDTO ticketTax = validateTicketTax(authDTO, repoTicketDTO, ticketTaxDTO);
		ticketTaxDAO.addTicketTax(authDTO, repoTicketDTO, ticketTax);
	}

	private TicketTaxDTO validateTicketTax(AuthDTO authDTO, TicketDTO repoTicketDTO, TicketTaxDTO ticketTaxDTO) {
		NamespaceTaxDTO namespaceTaxDTO = repoTicketDTO.getTax();
		if (namespaceTaxDTO == null || StringUtil.isNull(namespaceTaxDTO.getCode()) || StringUtil.isNull(ticketTaxDTO.getGstin())) {
			throw new ServiceException(ErrorCode.UNABLE_TO_UPDATE, "Invalid GSTIN");
		}
		ticketTaxDTO.setGstin(ticketTaxDTO.getGstin().toUpperCase());
		ticketTaxDTO.setTradeName(WordUtils.capitalize(StringUtil.substring(StringUtil.removeUnknownSymbol(ticketTaxDTO.getTradeName()), 120)));
		ticketTaxDTO.setEmail(StringUtil.isNull(ticketTaxDTO.getEmail(), Text.NA));
		return ticketTaxDTO;
	}

	@Async
	public void sendTaxInvoiceEmail(AuthDTO authDTO, TicketDTO ticketDTO) {
		ticketDTO.setTax(taxService.getTaxbyId(authDTO, ticketDTO.getTax()));

		TicketTaxDAO ticketTaxDAO = new TicketTaxDAO();
		TicketTaxDTO ticketTaxDTO = ticketTaxDAO.getTicketTax(authDTO, ticketDTO);
		if (ticketTaxDTO.getId() != 0) {
			emailService.sendTaxInvoiceEmail(authDTO, ticketDTO, ticketTaxDTO);
		}
	}

	@Override
	public NamespaceTaxDTO getTax(AuthDTO authDTO, TripDTO tripDTO, BookingDTO bookingDTO) {
		NamespaceTaxDTO namespaceTax = tripDTO.getSchedule().getTax();

		StateEM stateEM = GSTINValidator.getState(bookingDTO.getAdditionalAttributes().get(Text.GST_IN).toUpperCase());
		StateDTO state = stateService.getState(tripDTO.getStage().getFromStation().getStation().getState());

		if (stateEM != null && !stateEM.getCode().equals(state.getCode())) {
			StateDTO stateDTO = new StateDTO();
			stateDTO.setCode(stateEM.getCode());
			stateDTO = stateService.getState(stateDTO);

			NamespaceTaxDTO tax = taxService.getTaxbyState(authDTO, stateDTO);
			if (tax != null && tax.getId() != 0) {
				namespaceTax = tax;
			}
		}
		return namespaceTax;
	}

	@Override
	public NamespaceTaxDTO getTaxByGstin(AuthDTO authDTO, String gstin) {
		NamespaceTaxDTO namespaceTax = new NamespaceTaxDTO();

		StateEM stateEM = GSTINValidator.getState(gstin);

		if (stateEM == null) {
			throw new ServiceException(ErrorCode.INVALID_STATE);
		}
		StateDTO stateDTO = new StateDTO();
		stateDTO.setCode(stateEM.getCode());
		stateDTO = stateService.getState(stateDTO);

		namespaceTax = taxService.getTaxbyState(authDTO, stateDTO);
		return namespaceTax;
	}
}
