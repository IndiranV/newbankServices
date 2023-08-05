package org.in.com.controller.commerce;

import org.in.com.constants.Numeric;
import org.in.com.controller.commerce.io.ResponseIO;
import org.in.com.controller.web.BaseController;
import org.in.com.controller.web.io.BaseIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TripDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.BusmapService;
import org.in.com.service.SearchService;
import org.in.com.service.TicketEditService;
import org.in.com.service.TicketService;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/{authtoken}/commerce/edit/{ticketCode}")
public class CommerceEditController extends BaseController {
	@Autowired
	TicketEditService editService;
	@Autowired
	TicketService ticketService;
	@Autowired
	SearchService searchService;
	@Autowired
	BusmapService busmapService;

	@RequestMapping(value = "/boardingPoint/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> editBoardingPoint(@PathVariable("authtoken") String authtoken, @PathVariable("ticketCode") String ticketCode, String oldBoardingPointCode, String newBoardingPointCode, String emailId) throws Exception {

		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {

			if (StringUtil.isNull(ticketCode) || StringUtil.isNull(newBoardingPointCode) || StringUtil.isNull(oldBoardingPointCode)) {
				ResponseIO.failure("ED01", "Requried parameter, should not be null");
			}
			TicketDTO ticketDTO = new TicketDTO();
			ticketDTO.setCode(ticketCode);
			ticketDTO = ticketService.getTicketStatus(authDTO, ticketDTO);
			if (ticketDTO.getId() == 0 || (ticketDTO.getTicketStatus().getId() != 1 && ticketDTO.getTicketStatus().getId() != 5)) {
				throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
			}
			if (!ticketDTO.getPassengerEmailId().equalsIgnoreCase(emailId)) {
				throw new ServiceException(ErrorCode.CANCELLATION_VERIFICATION_USER_FAIL);
			}

			TripDTO tripDTO = busmapService.getSearchBusmapV3(authDTO, ticketDTO.getTripDTO());
			StationPointDTO oldStationPoint = null;
			StationPointDTO newStationPoint = null;
			for (StationPointDTO pointDTO : tripDTO.getStage().getFromStation().getStationPoint()) {
				if (pointDTO.getCode().equals(oldBoardingPointCode)) {
					oldStationPoint = pointDTO;
					continue;
				}
				if (pointDTO.getCode().equals(newBoardingPointCode)) {
					newStationPoint = pointDTO;
					continue;
				}
			}
			if (oldStationPoint != null && newStationPoint != null && oldStationPoint.getId() == ticketDTO.getBoardingPoint().getId()) {
				ticketDTO.setBoardingPoint(newStationPoint);
				ticketDTO.getBoardingPoint().setMinitues(tripDTO.getStage().getFromStation().getMinitues() + ticketDTO.getBoardingPoint().getMinitues());
				String event = "edit Boarding Point : " + oldStationPoint.getName() + " changed to " + newStationPoint.getName();
				editService.editBoardingPoint(authDTO, ticketDTO, event, Numeric.ZERO_INT);
			}
			else {
				throw new ServiceException(ErrorCode.UNABLE_TO_UPDATE);
			}

		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/mobile/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> passengerMobile(@PathVariable("authtoken") String authtoken, @PathVariable("ticketCode") String ticketCode, String oldMobileNumber, String newMobileNumber, String emailId) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {

			if (StringUtil.isNull(ticketCode) || StringUtil.isNull(oldMobileNumber) || StringUtil.isNull(newMobileNumber)) {
				ResponseIO.failure("ED01", "Requried parameter, should not be null");
			}
			TicketDTO ticketDTO = new TicketDTO();
			ticketDTO.setCode(ticketCode);
			ticketDTO = ticketService.getTicketStatus(authDTO, ticketDTO);
			if (ticketDTO.getId() == 0) {
				throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
			}
			if (!ticketDTO.getPassengerEmailId().equalsIgnoreCase(emailId)) {
				throw new ServiceException(ErrorCode.CANCELLATION_VERIFICATION_USER_FAIL);
			}
			if (ticketDTO.getPassengerMobile().equals(oldMobileNumber) && StringUtil.isNumeric(newMobileNumber)) {
				ticketDTO.setPassengerMobile(newMobileNumber);
				String event = "edit Mobile Number : " + oldMobileNumber + " changed to " + newMobileNumber;
				editService.editMobileNumber(authDTO, ticketDTO, event, Numeric.ZERO_INT);
			}
			else {
				throw new ServiceException(ErrorCode.UNABLE_TO_UPDATE);
			}

		}
		return ResponseIO.success();
	}

}
