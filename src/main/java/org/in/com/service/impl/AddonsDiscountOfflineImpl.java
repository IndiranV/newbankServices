package org.in.com.service.impl;

import hirondelle.date4j.DateTime;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.in.com.cache.CacheCentral;
import org.in.com.constants.Numeric;
import org.in.com.dao.AddonsDiscountOfflineDAO;
import org.in.com.dto.AddonsDiscountOfflineDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BookingDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.enumeration.JourneyTypeEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.AddonsDiscountOfflineService;
import org.in.com.service.TripService;
import org.in.com.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AddonsDiscountOfflineImpl extends CacheCentral implements AddonsDiscountOfflineService {
	@Autowired
	TripService tripService;

	@Override
	public List<AddonsDiscountOfflineDTO> get(AuthDTO authDTO, AddonsDiscountOfflineDTO dto) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AddonsDiscountOfflineDTO> getAll(AuthDTO authDTO) {
		AddonsDiscountOfflineDAO offlineDiscountDAO = new AddonsDiscountOfflineDAO();
		return offlineDiscountDAO.getAllOfflineDiscount(authDTO);
	}

	@Override
	public AddonsDiscountOfflineDTO Update(AuthDTO authDTO, AddonsDiscountOfflineDTO offlineDiscountDTO) {
		AddonsDiscountOfflineDAO offlineDiscountDAO = new AddonsDiscountOfflineDAO();
		return offlineDiscountDAO.updateOfflineDiscount(authDTO, offlineDiscountDTO);
	}

	@Override
	public AddonsDiscountOfflineDTO getOfflineDiscount(AuthDTO authDTO, String offlineDiscountCode) {
		AddonsDiscountOfflineDAO offlineDiscountDAO = new AddonsDiscountOfflineDAO();
		return offlineDiscountDAO.getOfflineDiscount(authDTO, offlineDiscountCode);
	}

	@Override
	public AddonsDiscountOfflineDTO getAvailableDiscountOffline(AuthDTO authDTO, BookingDTO bookingDTO) {

		AddonsDiscountOfflineDAO offlineDiscountDAO = new AddonsDiscountOfflineDAO();
		List<AddonsDiscountOfflineDTO> list = offlineDiscountDAO.getAllOfflineDiscount(authDTO);
		if (list.isEmpty()) {
			throw new ServiceException(ErrorCode.INVALID_DISCOUNT_CODE);
		}

		List<AddonsDiscountOfflineDTO> finallist = new ArrayList<AddonsDiscountOfflineDTO>();
		DateTime now = DateUtil.NOW().getStartOfDay();

		GroupDTO groupDTO = authDTO.getGroup();
		TicketDTO ticketDTO = bookingDTO.getTicketDTO(JourneyTypeEM.ONWARD_TRIP);
		TripDTO tripDTO = tripService.getTripDTOwithScheduleDetails(authDTO, ticketDTO.getTripDTO());

		for (AddonsDiscountOfflineDTO discountOfflineDTO : list) {
			if (!discountOfflineDTO.getGroupCode().isEmpty() && !discountOfflineDTO.getGroupCode().contains(groupDTO.getCode())) {
				continue;
			}
			if (!discountOfflineDTO.getRouteCode().isEmpty() && !discountOfflineDTO.getRouteCode().contains(tripDTO.getSearch().getFromStation().getCode() + "-" + tripDTO.getSearch().getToStation().getCode())) {
				continue;
			}
			if (!discountOfflineDTO.getScheduleCode().isEmpty() && !discountOfflineDTO.getScheduleCode().contains(tripDTO.getSchedule().getCode())) {
				continue;
			}
			if (discountOfflineDTO.isTravelDateFlag() && !(tripDTO.getTripDate().gteq(new DateTime(discountOfflineDTO.getActiveFrom())) && tripDTO.getTripDate().lteq(new DateTime(discountOfflineDTO.getActiveTo())))) {
				continue;
			}
			if (!discountOfflineDTO.isTravelDateFlag() && !(now.gteq(new DateTime(discountOfflineDTO.getActiveFrom())) && now.lteq(new DateTime(discountOfflineDTO.getActiveTo())))) {
				continue;
			}
			if (discountOfflineDTO.isTravelDateFlag() && discountOfflineDTO.getDayOfWeek().substring(tripDTO.getTripDate().getWeekDay() - 1, tripDTO.getTripDate().getWeekDay()).equals("0")) {
				continue;
			}
			if (!discountOfflineDTO.isTravelDateFlag() && discountOfflineDTO.getDayOfWeek().substring(now.getWeekDay() - 1, now.getWeekDay()).equals("0")) {
				continue;
			}
			if (discountOfflineDTO.getMinSeatCount() != 0 && ticketDTO.getTicketDetails().size() < discountOfflineDTO.getMinSeatCount()) {
				continue;
			}
			if (discountOfflineDTO.getMinTicketFare() != 0 && !(discountOfflineDTO.getMinTicketFare() <= ticketDTO.getTotalSeatFare().intValue() / ticketDTO.getTicketDetails().size())) {
				continue;
			}
			finallist.add(discountOfflineDTO);
		}
		if (finallist.isEmpty()) {
			throw new ServiceException(ErrorCode.INVALID_DISCOUNT_CODE);
		}
		AddonsDiscountOfflineDTO addonsDiscountOfflineDTO = finallist.get(0);

		BigDecimal totalDiscountAmount = BigDecimal.ZERO;
		if (addonsDiscountOfflineDTO.isPercentageFlag()) {
			totalDiscountAmount = ticketDTO.getTotalSeatFare().multiply(addonsDiscountOfflineDTO.getValue()).divide(Numeric.ONE_HUNDRED, 2, RoundingMode.CEILING);
		}
		else {
			totalDiscountAmount = addonsDiscountOfflineDTO.getValue().multiply(new BigDecimal(ticketDTO.getTicketDetails().size()));
		}
		if (addonsDiscountOfflineDTO.getMaxDiscountAmount().compareTo(BigDecimal.ZERO) == 1 && totalDiscountAmount.compareTo(addonsDiscountOfflineDTO.getMaxDiscountAmount()) == 1) {
			totalDiscountAmount = addonsDiscountOfflineDTO.getMaxDiscountAmount();
		}
		addonsDiscountOfflineDTO.setValue(totalDiscountAmount);
		return addonsDiscountOfflineDTO;

	}
}
