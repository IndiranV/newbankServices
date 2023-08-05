package org.in.com.controller.web;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.controller.web.io.StationIO;
import org.in.com.controller.web.io.TicketDetailsIO;
import org.in.com.controller.web.io.TripSeatQuotaIO;
import org.in.com.controller.web.io.UserIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.TripSeatQuotaDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.SeatGendarEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.TripSeatQuotaService;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/{authtoken}/trip")
public class TripSeatQuotaController extends BaseController {
	@Autowired
	TripSeatQuotaService tripSeatQuotaService;

	@RequestMapping(value = "/seat/quota/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> updateTripSeatQuota(@PathVariable("authtoken") String authtoken, @RequestBody TripSeatQuotaIO tripSeatQuota) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			TripSeatQuotaDTO tripSeatQuotaDTO = new TripSeatQuotaDTO();

			TripDTO tripDTO = new TripDTO();
			List<TicketDetailsDTO> ticketDetails = new ArrayList<TicketDetailsDTO>();
			for (TicketDetailsIO seatLayout : tripSeatQuota.getQuotaDetails()) {
				TicketDetailsDTO ticketDetailsDTO = new TicketDetailsDTO();
				ticketDetailsDTO.setSeatCode(seatLayout.getSeatCode());
				ticketDetailsDTO.setSeatName(seatLayout.getSeatName());
				ticketDetailsDTO.setSeatGendar(SeatGendarEM.getSeatGendarEM(seatLayout.getPassengerGendar()));
				ticketDetailsDTO.setSeatFare(seatLayout.getSeatFare() != null ? seatLayout.getSeatFare() : BigDecimal.ZERO);
				ticketDetailsDTO.setAcBusTax(seatLayout.getServiceTax() != null ? seatLayout.getServiceTax() : BigDecimal.ZERO);
				ticketDetails.add(ticketDetailsDTO);
			}
			tripDTO.setCode(tripSeatQuota.getTrip().getTripCode());
			tripDTO.setTicketDetailsList(ticketDetails);
			tripSeatQuotaDTO.setTrip(tripDTO);

			// From Station & To Station
			StationDTO fromStation = new StationDTO();
			StationDTO toStation = new StationDTO();

			if (tripSeatQuota.getFromStation() != null && tripSeatQuota.getToStation() != null) {
				fromStation.setCode(tripSeatQuota.getFromStation().getCode());
				toStation.setCode(tripSeatQuota.getToStation().getCode());
			}
			tripSeatQuotaDTO.setFromStation(fromStation);
			tripSeatQuotaDTO.setToStation(toStation);

			// User
			UserDTO userDTO = new UserDTO();
			if (tripSeatQuota.getUser() == null || StringUtil.isNull(tripSeatQuota.getUser().getCode())) {
				throw new ServiceException(ErrorCode.INVALID_USER_CODE);
			}
			userDTO.setCode(tripSeatQuota.getUser().getCode());
			tripSeatQuotaDTO.setUser(userDTO);
			tripSeatQuotaDTO.setRelaseMinutes(tripSeatQuota.getRelaseMinutes());
			tripSeatQuotaDTO.setRemarks(tripSeatQuota.getRemarks());
			tripSeatQuotaDTO.setActiveFlag(tripSeatQuota.getActiveFlag());

			tripSeatQuotaService.updateTripSeatQuota(authDTO, tripSeatQuotaDTO);
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/{tripCode}/seat/quota", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<TripSeatQuotaIO>> getTripSeatQuota(@PathVariable("authtoken") String authtoken, @PathVariable("tripCode") String tripCode) throws Exception {
		List<TripSeatQuotaIO> tripSeatQuotaList = new ArrayList<TripSeatQuotaIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			TripDTO tripDTO = new TripDTO();
			tripDTO.setCode(tripCode);
			List<TripSeatQuotaDTO> list = tripSeatQuotaService.getAllTripSeatQuota(authDTO, tripDTO);

			for (TripSeatQuotaDTO tripSeatQuotaDTO : list) {
				TripSeatQuotaIO tripSeatQuotaIO = new TripSeatQuotaIO();
				TicketDetailsIO ticketDetails = new TicketDetailsIO();
				ticketDetails.setSeatCode(tripSeatQuotaDTO.getSeatDetails().getSeatCode());
				ticketDetails.setSeatFare(tripSeatQuotaDTO.getSeatDetails().getSeatFare());
				ticketDetails.setServiceTax(tripSeatQuotaDTO.getSeatDetails().getAcBusTax());
				ticketDetails.setSeatName(tripSeatQuotaDTO.getSeatDetails().getSeatName());
				ticketDetails.setPassengerGendar(tripSeatQuotaDTO.getSeatDetails().getSeatGendar().getCode());
				tripSeatQuotaIO.setQuotaSeat(ticketDetails);

				// From Station & To Station
				if (tripSeatQuotaDTO.getFromStation() != null && tripSeatQuotaDTO.getToStation() != null) {
					StationIO fromStation = new StationIO();
					fromStation.setCode(tripSeatQuotaDTO.getFromStation().getCode());
					fromStation.setName(tripSeatQuotaDTO.getFromStation().getName());
					tripSeatQuotaIO.setFromStation(fromStation);

					StationIO toStation = new StationIO();
					toStation.setCode(tripSeatQuotaDTO.getToStation().getCode());
					toStation.setName(tripSeatQuotaDTO.getToStation().getName());
					tripSeatQuotaIO.setToStation(toStation);
				}

				// User
				if (tripSeatQuotaDTO.getUser() != null) {
					UserIO user = new UserIO();
					user.setCode(tripSeatQuotaDTO.getUser().getCode());
					user.setName(tripSeatQuotaDTO.getUser().getName());
					tripSeatQuotaIO.setUser(user);
				}

				// Updated User
				if (tripSeatQuotaDTO.getUpdatedBy() != null) {
					UserIO updatedUser = new UserIO();
					updatedUser.setCode(tripSeatQuotaDTO.getUpdatedBy().getCode());
					updatedUser.setName(tripSeatQuotaDTO.getUpdatedBy().getName());
					tripSeatQuotaIO.setUpdatedUser(updatedUser);
				}
				tripSeatQuotaIO.setUpdatedAt(tripSeatQuotaDTO.getUpdatedAt());
				tripSeatQuotaIO.setRelaseMinutes(tripSeatQuotaDTO.getRelaseMinutes());
				tripSeatQuotaIO.setRemarks(tripSeatQuotaDTO.getRemarks());
				tripSeatQuotaIO.setActiveFlag(tripSeatQuotaDTO.getActiveFlag());
				tripSeatQuotaList.add(tripSeatQuotaIO);
			}
		}
		return ResponseIO.success(tripSeatQuotaList);
	}
}
