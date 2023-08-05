package org.in.com.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.in.com.constants.Constants;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dao.DiscountDAO;
import org.in.com.dao.UserCustomerDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BookingDTO;
import org.in.com.dto.BusSeatLayoutDTO;
import org.in.com.dto.DiscountCategoryDTO;
import org.in.com.dto.DiscountCouponDTO;
import org.in.com.dto.DiscountCriteriaDTO;
import org.in.com.dto.DiscountCriteriaSlabDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.RouteDTO;
import org.in.com.dto.StageFareDTO;
import org.in.com.dto.TicketAddonsDetailsDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.UserCustomerDTO;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.FareTypeEM;
import org.in.com.dto.enumeration.JourneyTypeEM;
import org.in.com.dto.enumeration.SeatGendarEM;
import org.in.com.dto.enumeration.SeatStatusEM;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.BusmapService;
import org.in.com.service.DiscountService;
import org.in.com.service.GroupService;
import org.in.com.service.ScheduleFareOverrideService;
import org.in.com.service.StationService;
import org.in.com.service.TicketService;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import hirondelle.date4j.DateTime;

@Service
public class DiscountImpl implements DiscountService {

	@Autowired
	BusmapService busmapService;
	@Autowired
	StationService stationService;
	@Autowired
	ScheduleFareOverrideService fareOverrideService;
	@Autowired
	TicketService ticketService;
	@Autowired
	GroupService groupService;

	public List<DiscountCriteriaDTO> getAll(AuthDTO authDTO) {
		DiscountDAO dao = new DiscountDAO();
		UserCustomerDAO customerDAO = new UserCustomerDAO();
		List<DiscountCriteriaDTO> list = dao.getAll(authDTO);
		for (DiscountCriteriaDTO discountDTO : list) {
			for (GroupDTO groupDTO : discountDTO.getGroupList()) {
				groupDTO = groupService.getGroup(authDTO, groupDTO);
			}
			if (discountDTO.getDiscountCoupon().getUserCustomer().getId() != Numeric.ZERO_INT) {
				customerDAO.getUserCustomer(authDTO, discountDTO.getDiscountCoupon().getUserCustomer());
			}
		}
		return list;
	}

	@Override
	public DiscountCriteriaDTO Update(AuthDTO authDTO, DiscountCriteriaDTO criteriaDTO) {
		DiscountDAO dao = new DiscountDAO();
		for (GroupDTO groupDTO : criteriaDTO.getGroupList()) {
			groupDTO = groupService.getGroup(authDTO, groupDTO);
		}
		return dao.updateDiscount(authDTO, criteriaDTO);
	}

	@Override
	public List<DiscountCriteriaDTO> get(AuthDTO authDTO, DiscountCriteriaDTO dto) {
		return null;
	}

	@Override
	public List<DiscountCriteriaDTO> getAllDiscountByCoupon(AuthDTO authDTO, DiscountCouponDTO dto) {
		DiscountDAO dao = new DiscountDAO();
		List<DiscountCriteriaDTO> list = dao.getAllByCoupon(authDTO, dto);
		for (DiscountCriteriaDTO discountDTO : list) {
			for (GroupDTO groupDTO : discountDTO.getGroupList()) {
				groupDTO = groupService.getGroup(authDTO, groupDTO);
			}
		}
		return list;
	}

	@Override
	public List<DiscountCriteriaDTO> getAllDiscountByCategory(AuthDTO authDTO, DiscountCategoryDTO dto) {
		// TODO Auto-generated method stub
		DiscountDAO dao = new DiscountDAO();
		List<DiscountCriteriaDTO> list = dao.getAllByCategory(authDTO, dto);
		for (DiscountCriteriaDTO discountDTO : list) {
			for (GroupDTO groupDTO : discountDTO.getGroupList()) {
				groupDTO = groupService.getGroup(authDTO, groupDTO);
			}
		}
		return list;
	}

	@Override
	public DiscountCriteriaDTO validateCouponCode(AuthDTO authDTO, BookingDTO bookingDTO) {
		String errorMessage = null;
		DiscountDAO dao = new DiscountDAO();
		DiscountCouponDTO discountCouponDTO = new DiscountCouponDTO();
		discountCouponDTO.setCoupon(bookingDTO.getCouponCode());
		List<DiscountCriteriaDTO> criterialist = dao.getAllByCouponCode(authDTO, discountCouponDTO);
		if (criterialist == null || criterialist.isEmpty()) {
			throw new ServiceException(ErrorCode.INVALID_DISCOUNT_CODE);
		}

		// Previous PNR as a coupon code
		TicketDTO preTicket = null;
		if (Constants.PREVIOUS_PNR_COUPEN.equals(discountCouponDTO.getCoupon())) {
			preTicket = validatePreviousTicketCoupen(authDTO, bookingDTO);
		}

		for (TicketDTO ticketDTO : bookingDTO.getTicketList()) {
			TripDTO returnTripDTO = busmapService.getSearchBusmapV3(authDTO, ticketDTO.getTripDTO());
			ticketDTO.setTripDTO(returnTripDTO);
			// Get seatFare
			Map<String, BusSeatLayoutDTO> fareMap = new HashMap<String, BusSeatLayoutDTO>();
			List<BusSeatLayoutDTO> seatLayoutDTOList = returnTripDTO.getBus().getBusSeatLayoutDTO().getList();

			Map<String, StageFareDTO> getFareMap = new HashMap<String, StageFareDTO>();
			// Group Wise Fare and Default Fare
			for (StageFareDTO fareDTO : returnTripDTO.getStage().getStageFare()) {
				if (fareDTO.getGroup().getId() != 0) {
					getFareMap.put(fareDTO.getGroup().getId() + fareDTO.getBusSeatType().getCode(), fareDTO);
				}
				else {
					getFareMap.put(fareDTO.getBusSeatType().getCode(), fareDTO);
				}
			}
			// Get Group Wise Fare and Default Fare
			for (BusSeatLayoutDTO seatLayoutDTO : seatLayoutDTOList) {
				if (seatLayoutDTO.getSeatStatus() == SeatStatusEM.ALLOCATED_YOU || seatLayoutDTO.getSeatStatus() == SeatStatusEM.AVAILABLE_ALL || seatLayoutDTO.getSeatStatus() == SeatStatusEM.AVAILABLE_MALE || seatLayoutDTO.getSeatStatus() == SeatStatusEM.AVAILABLE_FEMALE) {
					if (getFareMap.get(authDTO.getGroup().getId() + seatLayoutDTO.getBusSeatType().getCode()) != null) {

						// Seat Fare
						if (seatLayoutDTO.getFare() == null) {
							seatLayoutDTO.setFare(getFareMap.get(authDTO.getGroup().getId() + seatLayoutDTO.getBusSeatType().getCode()).getFare());
						}
						fareMap.put(seatLayoutDTO.getCode(), seatLayoutDTO);
					}
					else if (getFareMap.get(seatLayoutDTO.getBusSeatType().getCode()) != null) {
						if (seatLayoutDTO.getFare() == null) {
							seatLayoutDTO.setFare(getFareMap.get(seatLayoutDTO.getBusSeatType().getCode()).getFare());
						}
						fareMap.put(seatLayoutDTO.getCode(), seatLayoutDTO);
					}
				}
			}

			// Get seatFare
			for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
				BusSeatLayoutDTO seatLayoutDTO = fareMap.get(ticketDetailsDTO.getSeatCode());
				if (seatLayoutDTO == null) {
					throw new ServiceException(ErrorCode.SEAT_ALREADY_BLOCKED);
				}
				ticketDetailsDTO.setSeatFare(seatLayoutDTO.getFare());
				ticketDetailsDTO.setSeatCode(seatLayoutDTO.getCode());
				ticketDetailsDTO.setSeatName(seatLayoutDTO.getName());
				ticketDetailsDTO.setAcBusTax(seatLayoutDTO.getFare().divide(Numeric.ONE_HUNDRED, 2, RoundingMode.CEILING).multiply(returnTripDTO.getSchedule().getTax().getServiceTax()));
				ticketDetailsDTO.setSeatType(seatLayoutDTO.getBusSeatType().getCode());
				ticketDetailsDTO.setActiveFlag(1);
			}
			//
		}

		for (Iterator<DiscountCriteriaDTO> iterator = criterialist.iterator(); iterator.hasNext();) {
			DiscountCriteriaDTO criteriaDTO = iterator.next();
			DateTime txtrDate = criteriaDTO.isTravelDateFlag() ? bookingDTO.getTicketDTO(JourneyTypeEM.ONWARD_TRIP).getTripDTO().getTripDate() : DateUtil.NOW();
			// common validations
			if (criteriaDTO.getActiveFrom() != null && !txtrDate.gteq(new DateTime(criteriaDTO.getActiveFrom()).getStartOfDay())) {
				errorMessage = "Coupon has been expired";
				iterator.remove();
				continue;
			}
			if (criteriaDTO.getActiveTo() != null && !txtrDate.lteq(new DateTime(criteriaDTO.getActiveTo()).getEndOfDay())) {
				errorMessage = "Coupon has been expired";
				iterator.remove();
				continue;
			}
			if (criteriaDTO.getDayOfWeek() != null && criteriaDTO.getDayOfWeek().length() != 7) {
				iterator.remove();
				continue;
			}
			if (criteriaDTO.getDayOfWeek() != null && criteriaDTO.getDayOfWeek().substring(txtrDate.getWeekDay() - 1, txtrDate.getWeekDay()).equals("0")) {
				errorMessage = "Applicable only for Day of week";
				iterator.remove();
				continue;
			}
			// Check for group level or should be default
			if (authDTO.getNativeNamespaceCode().equals(authDTO.getNamespaceCode()) && !criteriaDTO.getGroupList().isEmpty() && BitsUtil.isGroupExists(criteriaDTO.getGroupList(), authDTO.getGroup()) == null) {
				errorMessage = "Not applicable for you";
				iterator.remove();
				continue;
			}
			if (!criteriaDTO.getMobileNumberList().isEmpty() && !criteriaDTO.getMobileNumberList().contains(bookingDTO.getPassengerMobile())) {
				errorMessage = "Not applicable for you";
				iterator.remove();
				continue;
			}
			DateTime tripDateTime = bookingDTO.getTicketDTO(JourneyTypeEM.ONWARD_TRIP).getTripDTO().getTripDateTimeV2();
			if (StringUtil.isNotNull(criteriaDTO.getServiceTiming()) && !validateServiceTimings(criteriaDTO.getServiceTiming(), tripDateTime.getHour())) {
				errorMessage = "Coupon has been expired";
				iterator.remove();
				continue;
			}
			DeviceMediumEM deviceMediumEM = BitsUtil.isDeviceMediumExists(criteriaDTO.getDeviceMedium(), authDTO.getDeviceMedium());
			if (deviceMediumEM == null) {
				errorMessage = "only applicable for" + criteriaDTO.getDeviceMediums();
				iterator.remove();
				continue;
			}

			if (criteriaDTO.getMinSeatCount() != 0 && criteriaDTO.getMinSeatCount() > bookingDTO.getTicketSeatCount()) {
				errorMessage = "Seat count should " + criteriaDTO.getMinSeatCount() + " above";
				iterator.remove();
				continue;
			}
			if (criteriaDTO.getMinTicketFare() != 0 && criteriaDTO.getMinTicketFare() > bookingDTO.getTransactionAmount().intValue()) {
				errorMessage = "Min transaction amount will " + criteriaDTO.getMinTicketFare() + " above";
				iterator.remove();
				continue;
			}
			if (criteriaDTO.isRegisteredUserFlag() && StringUtil.isNull(authDTO.getUser().getToken())) {
				errorMessage = "This coupon only for registed users";
				iterator.remove();
				continue;
			}
			if (criteriaDTO.isRoundTripFlag() && !bookingDTO.isRoundTripFlag()) {
				errorMessage = "only for round trip booking";
				iterator.remove();
				continue;
			}
		}
		DateTime now = DateTime.now(TimeZone.getDefault());
		for (TicketDTO ticketDTO : bookingDTO.getTicketList()) {
			RouteDTO routeDTO = null;
			for (Iterator<DiscountCriteriaDTO> iterator = criterialist.iterator(); iterator.hasNext();) {
				DiscountCriteriaDTO criteriaDTO = iterator.next();
				int minutiesDiff = DateUtil.getMinutiesDifferent(now, DateUtil.addMinituesToDate(ticketDTO.getTripDTO().getTripDate(), ticketDTO.getTripDTO().getStage().getFromStation().getMinitues()));
				if (criteriaDTO.getAfterBookingMinitues() != 0 && criteriaDTO.getAfterBookingMinitues() > minutiesDiff) {
					iterator.remove();
					continue;
				}
				if (criteriaDTO.getBeforeBookingMinitues() != 0 && criteriaDTO.getBeforeBookingMinitues() < minutiesDiff) {
					iterator.remove();
					continue;
				}
				if (criteriaDTO.getScheduleCode() != null && !criteriaDTO.getScheduleCode().isEmpty() && !criteriaDTO.getScheduleCode().contains(ticketDTO.getTripDTO().getSchedule().getCode())) {
					iterator.remove();
					continue;
				}
				if (routeDTO == null && criteriaDTO.getRouteCode() != null && !criteriaDTO.getRouteCode().isEmpty()) {
					routeDTO = stationService.getRouteDTO(authDTO, ticketDTO.getTripDTO().getStage().getFromStation().getStation(), ticketDTO.getTripDTO().getStage().getToStation().getStation());
					if (routeDTO == null || routeDTO.getId() == 0 || !criteriaDTO.getRouteCode().contains(routeDTO.getCode())) {
						iterator.remove();
						continue;
					}
				}
				if (criteriaDTO.getMaxUsageLimitPerUser() > 0) {
					int bookedticketCount = getTicketBookedCouponHistory(authDTO, criteriaDTO, ticketDTO.getPassengerMobile());
					if (criteriaDTO.getMaxUsageLimitPerUser() <= bookedticketCount) {
						errorMessage = "Already used in previous transaction";
						iterator.remove();
						continue;
					}
				}
			}
		}
		DiscountCriteriaDTO discountCriteriaDTO = null;
		for (DiscountCriteriaDTO criteriaDTO : criterialist) {
			if (discountCriteriaDTO == null) {
				discountCriteriaDTO = criteriaDTO;
			}
			if (DateUtil.getDayDifferent(new DateTime(criteriaDTO.getActiveFrom()), new DateTime(criteriaDTO.getActiveTo())) <= DateUtil.getDayDifferent(new DateTime(discountCriteriaDTO.getActiveFrom()), new DateTime(discountCriteriaDTO.getActiveTo()))) {
				discountCriteriaDTO = criteriaDTO;
			}
		}
		// Apply Slab
		int TotalSeatFare = bookingDTO.getTotalSeatFare().intValue();
		if (Constants.PREVIOUS_PNR_COUPEN.equals(discountCouponDTO.getCoupon()) && preTicket != null) {
			TotalSeatFare = preTicket.getAverageSeatFare();
		}
		if (discountCriteriaDTO != null && !discountCriteriaDTO.getSlabList().isEmpty()) {
			for (DiscountCriteriaSlabDTO slabDTO : discountCriteriaDTO.getSlabList()) {
				if (TotalSeatFare >= slabDTO.getSlabFromValue() && TotalSeatFare <= slabDTO.getSlabToValue()) {
					discountCriteriaDTO.setValue(slabDTO.getSlabValue());
					discountCriteriaDTO.setPercentageFlag(slabDTO.getSlabValueType().getId() == FareTypeEM.PERCENTAGE.getId() ? true : false);
					break;
				}
			}
		}
		return discountCriteriaDTO;
	}

	@Override
	public DiscountCriteriaDTO validateCouponCodeV3(AuthDTO authDTO, BookingDTO bookingDTO) {
		boolean allowValidation = Text.TRUE;
		DiscountDAO dao = new DiscountDAO();
		DiscountCouponDTO discountCouponDTO = new DiscountCouponDTO();
		discountCouponDTO.setCoupon(bookingDTO.getCouponCode());

		/** Validate Customer Discount */
		if (StringUtil.isContains(Constants.CUSTOMER_DISCOUNT, authDTO.getNamespace().getCode()) && authDTO.getUserCustomer() != null && authDTO.getUserCustomer().getId() != 0) {
			UserCustomerDTO userCustomerDTO = dao.checkCustomerCoupon(authDTO, discountCouponDTO);
			if (userCustomerDTO != null) {
				if (authDTO.getUserCustomer() != null && userCustomerDTO.getId() != authDTO.getUserCustomer().getId()) {
					throw new ServiceException(ErrorCode.INVALID_DISCOUNT_CODE);
				}
				discountCouponDTO.setCoupon(Constants.CUSTOMER_DISCOUNT_COUPON);
			}
		}

		List<DiscountCriteriaDTO> criterialist = dao.getAllByCouponCode(authDTO, discountCouponDTO);
		if (criterialist == null || criterialist.isEmpty()) {
			throw new ServiceException(ErrorCode.INVALID_DISCOUNT_CODE);
		}

		// Previous PNR as a coupon code
		TicketDTO preTicket = null;
		if (Constants.PREVIOUS_PNR_COUPEN.equals(discountCouponDTO.getCoupon())) {
			preTicket = validatePreviousTicketCoupen(authDTO, bookingDTO);
		}

		Map<String, SeatGendarEM> seatGendarMap = new HashMap<>();
		List<Integer> ages = new ArrayList<>();
		for (TicketDTO ticketDTO : bookingDTO.getTicketList()) {
			TripDTO returnTripDTO = busmapService.getSearchBusmapV3(authDTO, ticketDTO.getTripDTO());
			ticketDTO.setTripDTO(returnTripDTO);
			// Get seatFare
			Map<String, BusSeatLayoutDTO> fareMap = new HashMap<String, BusSeatLayoutDTO>();
			List<BusSeatLayoutDTO> seatLayoutDTOList = returnTripDTO.getBus().getBusSeatLayoutDTO().getList();

			Map<String, StageFareDTO> getFareMap = new HashMap<String, StageFareDTO>();
			// Group Wise Fare and Default Fare
			for (StageFareDTO fareDTO : returnTripDTO.getStage().getStageFare()) {
				if (fareDTO.getGroup().getId() != 0) {
					getFareMap.put(fareDTO.getGroup().getId() + fareDTO.getBusSeatType().getCode(), fareDTO);
				}
				else {
					getFareMap.put(fareDTO.getBusSeatType().getCode(), fareDTO);
				}
			}
			// Get Group Wise Fare and Default Fare
			for (BusSeatLayoutDTO seatLayoutDTO : seatLayoutDTOList) {
				if (seatLayoutDTO.getSeatStatus() == SeatStatusEM.ALLOCATED_YOU || seatLayoutDTO.getSeatStatus() == SeatStatusEM.AVAILABLE_ALL || seatLayoutDTO.getSeatStatus() == SeatStatusEM.AVAILABLE_MALE || seatLayoutDTO.getSeatStatus() == SeatStatusEM.AVAILABLE_FEMALE) {
					if (getFareMap.get(authDTO.getGroup().getId() + seatLayoutDTO.getBusSeatType().getCode()) != null) {

						// Seat Fare
						if (seatLayoutDTO.getFare() == null) {
							seatLayoutDTO.setFare(getFareMap.get(authDTO.getGroup().getId() + seatLayoutDTO.getBusSeatType().getCode()).getFare());
						}
						fareMap.put(seatLayoutDTO.getCode(), seatLayoutDTO);
					}
					else if (getFareMap.get(seatLayoutDTO.getBusSeatType().getCode()) != null) {
						if (seatLayoutDTO.getFare() == null) {
							seatLayoutDTO.setFare(getFareMap.get(seatLayoutDTO.getBusSeatType().getCode()).getFare());
						}
						fareMap.put(seatLayoutDTO.getCode(), seatLayoutDTO);
					}
				}
			}

			// Get seatFare
			for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
				BusSeatLayoutDTO seatLayoutDTO = fareMap.get(ticketDetailsDTO.getSeatCode());
				if (seatLayoutDTO == null) {
					throw new ServiceException(ErrorCode.SEAT_ALREADY_BLOCKED);
				}
				if (allowValidation && (ticketDetailsDTO.getSeatGendar() == null || ticketDetailsDTO.getPassengerAge() == Numeric.ZERO_INT)) {
					allowValidation = Text.FALSE;
				}
				ticketDetailsDTO.setSeatFare(seatLayoutDTO.getFare());
				ticketDetailsDTO.setSeatCode(seatLayoutDTO.getCode());
				ticketDetailsDTO.setSeatName(seatLayoutDTO.getName());
				ticketDetailsDTO.setAcBusTax(seatLayoutDTO.getFare().divide(Numeric.ONE_HUNDRED, 2, RoundingMode.CEILING).multiply(returnTripDTO.getSchedule().getTax().getServiceTax()));
				ticketDetailsDTO.setSeatType(seatLayoutDTO.getBusSeatType().getCode());
				ticketDetailsDTO.setActiveFlag(1);

				if (ticketDetailsDTO.getSeatGendar() != null && seatGendarMap.get(ticketDetailsDTO.getSeatGendar().getCode()) == null) {
					seatGendarMap.put(ticketDetailsDTO.getSeatGendar().getCode(), ticketDetailsDTO.getSeatGendar());
				}
				if (ticketDetailsDTO.getPassengerAge() != 0) {
					ages.add(ticketDetailsDTO.getPassengerAge());
				}
			}
		}

		List<SeatGendarEM> seatGendars = new ArrayList<>(seatGendarMap.values());

		for (Iterator<DiscountCriteriaDTO> iterator = criterialist.iterator(); iterator.hasNext();) {
			DiscountCriteriaDTO criteriaDTO = iterator.next();
			DateTime txtrDate = criteriaDTO.isTravelDateFlag() ? bookingDTO.getTicketDTO(JourneyTypeEM.ONWARD_TRIP).getTripDTO().getTripDate().getStartOfDay() : DateUtil.NOW();
			// common validations
			if (criteriaDTO.getActiveFrom() != null && !txtrDate.gteq(new DateTime(criteriaDTO.getActiveFrom()).getStartOfDay())) {
				iterator.remove();
				continue;
			}
			if (criteriaDTO.getActiveTo() != null && !txtrDate.lteq(new DateTime(criteriaDTO.getActiveTo()).getEndOfDay())) {
				iterator.remove();
				continue;
			}
			if (criteriaDTO.getDayOfWeek() != null && criteriaDTO.getDayOfWeek().length() != 7) {
				iterator.remove();
				continue;
			}
			if (criteriaDTO.getDayOfWeek() != null && criteriaDTO.getDayOfWeek().substring(txtrDate.getWeekDay() - 1, txtrDate.getWeekDay()).equals("0")) {
				iterator.remove();
				continue;
			}
			// Check for group level or should be default
			if (authDTO.getNativeNamespaceCode().equals(authDTO.getNamespaceCode()) && !criteriaDTO.getGroupList().isEmpty() && BitsUtil.isGroupExists(criteriaDTO.getGroupList(), authDTO.getGroup()) == null) {
				iterator.remove();
				continue;
			}
			if (!criteriaDTO.getMobileNumberList().isEmpty() && !criteriaDTO.getMobileNumberList().contains(bookingDTO.getPassengerMobile())) {
				iterator.remove();
				continue;
			}
			DateTime tripDateTime = bookingDTO.getTicketDTO(JourneyTypeEM.ONWARD_TRIP).getTripDTO().getTripDateTimeV2();
			if (StringUtil.isNotNull(criteriaDTO.getServiceTiming()) && !validateServiceTimings(criteriaDTO.getServiceTiming(), tripDateTime.getHour())) {
				iterator.remove();
				continue;
			}
			DeviceMediumEM deviceMediumEM = BitsUtil.isDeviceMediumExists(criteriaDTO.getDeviceMedium(), authDTO.getDeviceMedium());
			if (deviceMediumEM == null) {
				iterator.remove();
				continue;
			}
			SeatGendarEM existingSeatGender = BitsUtil.isExistSeatGender(criteriaDTO.getSeatGender(), seatGendars);
			if (!criteriaDTO.getSeatGender().isEmpty() && existingSeatGender == null) {
				iterator.remove();
				continue;
			}
			boolean isExist = BitsUtil.validateAgeV2(criteriaDTO.getAge(), ages);
			if (!isExist && StringUtil.isNotNull(criteriaDTO.getAge())) {
				iterator.remove();
				continue;
			}

			if (criteriaDTO.getMinSeatCount() != 0 && criteriaDTO.getMinSeatCount() > bookingDTO.getTicketSeatCount()) {
				iterator.remove();
				continue;
			}
			if (criteriaDTO.getMinTicketFare() != 0 && criteriaDTO.getMinTicketFare() > bookingDTO.getTransactionAmount().intValue()) {
				iterator.remove();
				continue;
			}
			if (criteriaDTO.isRegisteredUserFlag() && StringUtil.isNull(authDTO.getUser().getToken())) {
				iterator.remove();
				continue;
			}
			if (criteriaDTO.isRoundTripFlag() && !bookingDTO.isRoundTripFlag()) {
				iterator.remove();
				continue;
			}

		}
		DateTime now = DateTime.now(TimeZone.getDefault());
		for (TicketDTO ticketDTO : bookingDTO.getTicketList()) {
			RouteDTO routeDTO = null;
			for (Iterator<DiscountCriteriaDTO> iterator = criterialist.iterator(); iterator.hasNext();) {
				DiscountCriteriaDTO criteriaDTO = iterator.next();
				int minutiesDiff = DateUtil.getMinutiesDifferent(now, DateUtil.addMinituesToDate(ticketDTO.getTripDTO().getTripDate(), ticketDTO.getTripDTO().getStage().getFromStation().getMinitues()));
				if (criteriaDTO.getAfterBookingMinitues() != 0 && criteriaDTO.getAfterBookingMinitues() > minutiesDiff) {
					iterator.remove();
					continue;
				}
				if (criteriaDTO.getBeforeBookingMinitues() != 0 && criteriaDTO.getBeforeBookingMinitues() < minutiesDiff) {
					iterator.remove();
					continue;
				}
				if (criteriaDTO.getScheduleCode() != null && !criteriaDTO.getScheduleCode().isEmpty() && !criteriaDTO.getScheduleCode().contains(ticketDTO.getTripDTO().getSchedule().getCode())) {
					iterator.remove();
					continue;
				}
				if (routeDTO == null && criteriaDTO.getRouteCode() != null && !criteriaDTO.getRouteCode().isEmpty()) {
					routeDTO = stationService.getRouteDTO(authDTO, ticketDTO.getTripDTO().getStage().getFromStation().getStation(), ticketDTO.getTripDTO().getStage().getToStation().getStation());
					if (routeDTO == null || routeDTO.getId() == 0 || !criteriaDTO.getRouteCode().contains(routeDTO.getCode())) {
						iterator.remove();
						continue;
					}
				}
				if (criteriaDTO.getMaxUsageLimitPerUser() > 0) {
					if (criteriaDTO.getMaxUsageLimitPerUser() <= criteriaDTO.getDiscountCoupon().getUsedCount()) {
						iterator.remove();
						continue;
					}
				}
			}
		}

		Map<String, DiscountCriteriaDTO> seatGenderCriteria = new HashMap<>();
		for (SeatGendarEM seatGendar : seatGendars) {
			DiscountCriteriaDTO discountCriteriaDTO = null;

			for (DiscountCriteriaDTO criteriaDTO : criterialist) {
				if (criteriaDTO.getSeatGender() != null && !criteriaDTO.getSeatGender().isEmpty() && BitsUtil.isSeatGenderExists(criteriaDTO.getSeatGender(), seatGendar) == null) {
					continue;
				}
				if (discountCriteriaDTO == null) {
					discountCriteriaDTO = criteriaDTO;
				}
				if (DateUtil.getDayDifferent(new DateTime(criteriaDTO.getActiveFrom()), new DateTime(criteriaDTO.getActiveTo())) <= DateUtil.getDayDifferent(new DateTime(discountCriteriaDTO.getActiveFrom()), new DateTime(discountCriteriaDTO.getActiveTo()))) {
					discountCriteriaDTO = criteriaDTO;
				}
			}

			int totalSeatFare = bookingDTO.getTotalSeatFare().intValue();
			// Previous PNR
			if (Constants.PREVIOUS_PNR_COUPEN.equals(discountCouponDTO.getCoupon()) && preTicket != null) {
				totalSeatFare = preTicket.getAverageSeatFare();
			}

			// Apply Slab
			if (discountCriteriaDTO != null && !discountCriteriaDTO.getSlabList().isEmpty()) {
				for (DiscountCriteriaSlabDTO slabDTO : discountCriteriaDTO.getSlabList()) {
					if (totalSeatFare >= slabDTO.getSlabFromValue() && totalSeatFare <= slabDTO.getSlabToValue()) {
						discountCriteriaDTO.setValue(slabDTO.getSlabValue());
						discountCriteriaDTO.setPercentageFlag(slabDTO.getSlabValueType().getId() == FareTypeEM.PERCENTAGE.getId() ? true : false);
						break;
					}
				}
			}

			seatGenderCriteria.put(seatGendar.getCode(), discountCriteriaDTO);
		}

		// Apply Gender, Age restrictions
		DiscountCriteriaDTO discountCriteria = null;
		if (!seatGenderCriteria.isEmpty() && allowValidation) {
			discountCriteria = validateTicketCoupon(seatGenderCriteria, bookingDTO);
		}
		return discountCriteria;
	}

	private DiscountCriteriaDTO validateTicketCoupon(Map<String, DiscountCriteriaDTO> seatGenderCriteria, BookingDTO bookingDTO) {
		List<TicketAddonsDetailsDTO> discountList = new ArrayList<>();

		BigDecimal discountValue = BigDecimal.ZERO;
		BigDecimal maxDiscountValue = BigDecimal.ZERO;
		DiscountCouponDTO discountCoupon = null;

		for (TicketDTO ticketDTO : bookingDTO.getTicketList()) {
			for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
				DiscountCriteriaDTO discountCriteriaDTO = seatGenderCriteria.get(ticketDetailsDTO.getSeatGendar().getCode());
				if (discountCriteriaDTO == null) {
					continue;
				}
				if (StringUtil.isNotNull(discountCriteriaDTO.getAge()) && !BitsUtil.validateAge(discountCriteriaDTO.getAge(), ticketDetailsDTO.getPassengerAge())) {
					continue;
				}

				discountValue = new BigDecimal(discountCriteriaDTO.getValue());
				maxDiscountValue = new BigDecimal(discountCriteriaDTO.getMaxDiscountAmount());
				discountCoupon = discountCriteriaDTO.getDiscountCoupon();

				if (discountCriteriaDTO.isPercentageFlag()) {
					TicketAddonsDetailsDTO discountDetailsDTO = new TicketAddonsDetailsDTO();
					discountDetailsDTO.setValue(ticketDetailsDTO.getSeatFare().divide(Numeric.ONE_HUNDRED, 2, RoundingMode.CEILING).multiply(discountValue));
					if (maxDiscountValue.compareTo(BigDecimal.ZERO) == 1 && discountDetailsDTO.getValue().compareTo(maxDiscountValue) == 1) {
						discountDetailsDTO.setValue(maxDiscountValue);
					}
					discountDetailsDTO.setSeatCode(ticketDetailsDTO.getSeatCode());
					discountList.add(discountDetailsDTO);
				}
				else if (!discountCriteriaDTO.isPercentageFlag()) {
					TicketAddonsDetailsDTO discountDetailsDTO = new TicketAddonsDetailsDTO();
					discountDetailsDTO.setValue(discountValue);
					if (maxDiscountValue.compareTo(BigDecimal.ZERO) == 1 && discountDetailsDTO.getValue().compareTo(maxDiscountValue) == 1) {
						discountDetailsDTO.setValue(maxDiscountValue);
					}
					discountDetailsDTO.setSeatCode(ticketDetailsDTO.getSeatCode());
					discountList.add(discountDetailsDTO);
				}
			}
		}

		DiscountCriteriaDTO discountCriteria = null;
		if (!discountList.isEmpty()) {
			BigDecimal totalDiscountAmount = BigDecimal.ZERO;
			for (TicketAddonsDetailsDTO discountDetailsDTO : discountList) {
				totalDiscountAmount = totalDiscountAmount.add(discountDetailsDTO.getValue());
			}

			discountCriteria = new DiscountCriteriaDTO();
			discountCriteria.setValue(totalDiscountAmount.floatValue());
			discountCriteria.setMaxDiscountAmount(totalDiscountAmount.intValue());
			discountCriteria.setPercentageFlag(Text.FALSE);
			discountCriteria.setDiscountCoupon(discountCoupon);
		}
		return discountCriteria;
	}

	@Override
	public TicketDTO validatePreviousTicketCoupen(AuthDTO authDTO, BookingDTO bookingDTO) {
		if (bookingDTO.getAdditionalAttributes() == null || bookingDTO.getAdditionalAttributes().isEmpty() || StringUtil.isNull(bookingDTO.getAdditionalAttributes().get(Constants.PREVIOUS_PNR_COUPEN))) {
			throw new ServiceException(ErrorCode.INVALID_DISCOUNT_CODE);
		}

		TicketDTO ticketDTO = new TicketDTO();
		ticketDTO.setCode(bookingDTO.getAdditionalAttributes().get(Constants.PREVIOUS_PNR_COUPEN));
		ticketService.getTicketStatus(authDTO, ticketDTO);

		boolean sameMobileNumberUsed = Text.FALSE;
		for (TicketDTO ticket : bookingDTO.getTicketList()) {
			if (ticket.getPassengerMobile().equals(ticketDTO.getPassengerMobile())) {
				sameMobileNumberUsed = Text.TRUE;
				break;
			}
		}
		// Basic Validation
		if (!sameMobileNumberUsed) {
			throw new ServiceException(ErrorCode.CANCELLATION_VERIFICATION_MOBILE_FAIL);
		}
		if (ticketDTO.getTicketStatus().getId() != TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId()) {
			throw new ServiceException(ErrorCode.NOT_CONFIRM_BOOKED_TICKET);
		}
		if (ticketDTO.getTripDateTime().compareTo(DateUtil.NOW()) == 1) {
			throw new ServiceException(ErrorCode.TRIP_DATE_OVER, "Previous travel date PNR only allowed!");
		}
		if (DateUtil.getDayDifferent(ticketDTO.getTripDateTime(), DateUtil.NOW()) > 30) {
			throw new ServiceException(ErrorCode.TRIP_DATE_OVER, "Previous travel date should be below 30 days!");
		}

		// Validate PNR Used Already
		TicketAddonsDetailsDTO ticketAddonsDetailsDTO = ticketService.checkTicketUsed(authDTO, ticketDTO);
		if (ticketAddonsDetailsDTO != null && ticketAddonsDetailsDTO.getId() != 0) {
			throw new ServiceException(ErrorCode.DISCOUNT_CODE_USED_ALREDY);
		}
		return ticketDTO;
	}

	public Map<String, DiscountCriteriaDTO> applyCouponCode(AuthDTO authDTO, BookingDTO bookingDTO, TicketDTO ticketDTO, TicketDTO preTicket) {
		DiscountDAO dao = new DiscountDAO();
		DiscountCouponDTO discountCouponDTO = new DiscountCouponDTO();
		discountCouponDTO.setCoupon(bookingDTO.getCouponCode());

		/** Validate Customer Discount */
		if (StringUtil.isContains(Constants.CUSTOMER_DISCOUNT, authDTO.getNamespace().getCode()) && authDTO.getUserCustomer() != null && authDTO.getUserCustomer().getId() != 0) {
			UserCustomerDTO userCustomerDTO = dao.checkCustomerCoupon(authDTO, discountCouponDTO);
			if (userCustomerDTO != null) {
				if (authDTO.getUserCustomer() != null && userCustomerDTO.getId() != authDTO.getUserCustomer().getId()) {
					throw new ServiceException(ErrorCode.INVALID_DISCOUNT_CODE);
				}
				discountCouponDTO.setCoupon(Constants.CUSTOMER_DISCOUNT_COUPON);
			}
		}

		List<DiscountCriteriaDTO> criterialist = dao.getAllByCouponCode(authDTO, discountCouponDTO);
		if (criterialist == null || criterialist.isEmpty()) {
			throw new ServiceException(ErrorCode.INVALID_DISCOUNT_CODE);
		}

		Map<String, SeatGendarEM> seatGendarMap = new HashMap<>();
		List<Integer> ages = new ArrayList<>();
		for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
			if (ticketDetailsDTO.getSeatGendar() != null && seatGendarMap.get(ticketDetailsDTO.getSeatGendar().getCode()) == null) {
				seatGendarMap.put(ticketDetailsDTO.getSeatGendar().getCode(), ticketDetailsDTO.getSeatGendar());
			}
			if (ticketDetailsDTO.getPassengerAge() != 0) {
				ages.add(ticketDetailsDTO.getPassengerAge());
			}
		}

		List<SeatGendarEM> seatGendars = new ArrayList<>(seatGendarMap.values());

		for (Iterator<DiscountCriteriaDTO> iterator = criterialist.iterator(); iterator.hasNext();) {
			DiscountCriteriaDTO criteriaDTO = iterator.next();
			DateTime txtrDate = criteriaDTO.isTravelDateFlag() ? bookingDTO.getTicketDTO(JourneyTypeEM.ONWARD_TRIP).getTripDTO().getTripDate() : DateUtil.NOW();
			// common validations
			if (criteriaDTO.getActiveFrom() != null && !txtrDate.gteq(new DateTime(criteriaDTO.getActiveFrom()))) {
				iterator.remove();
				continue;
			}
			if (criteriaDTO.getActiveTo() != null && !txtrDate.lteq(new DateTime(criteriaDTO.getActiveTo()))) {
				iterator.remove();
				continue;
			}
			if (criteriaDTO.getDayOfWeek() != null && criteriaDTO.getDayOfWeek().length() != 7) {
				iterator.remove();
				continue;
			}
			if (criteriaDTO.getDayOfWeek() != null && criteriaDTO.getDayOfWeek().substring(txtrDate.getWeekDay() - 1, txtrDate.getWeekDay()).equals("0")) {
				iterator.remove();
				continue;
			}
			// Check for group level or should be default
			if (authDTO.getNativeNamespaceCode().equals(authDTO.getNamespaceCode()) && !criteriaDTO.getGroupList().isEmpty() && BitsUtil.isGroupExists(criteriaDTO.getGroupList(), authDTO.getGroup()) == null) {
				iterator.remove();
				continue;
			}
			if (!criteriaDTO.getMobileNumberList().isEmpty() && !criteriaDTO.getMobileNumberList().contains(bookingDTO.getPassengerMobile())) {
				iterator.remove();
				continue;
			}
			DateTime tripDateTime = bookingDTO.getTicketDTO(JourneyTypeEM.ONWARD_TRIP).getTripDTO().getTripDateTimeV2();
			if (StringUtil.isNotNull(criteriaDTO.getServiceTiming()) && !validateServiceTimings(criteriaDTO.getServiceTiming(), tripDateTime.getHour())) {
				iterator.remove();
				continue;
			}
			DeviceMediumEM deviceMediumEM = BitsUtil.isDeviceMediumExists(criteriaDTO.getDeviceMedium(), authDTO.getDeviceMedium());
			if (deviceMediumEM == null) {
				iterator.remove();
				continue;
			}
			SeatGendarEM existingSeatGender = BitsUtil.isExistSeatGender(criteriaDTO.getSeatGender(), seatGendars);
			if (!criteriaDTO.getSeatGender().isEmpty() && existingSeatGender == null) {
				iterator.remove();
				continue;
			}
			boolean isExist = BitsUtil.validateAgeV2(criteriaDTO.getAge(), ages);
			if (!isExist && StringUtil.isNotNull(criteriaDTO.getAge())) {
				iterator.remove();
				continue;
			}

			if (criteriaDTO.getMinSeatCount() != 0 && criteriaDTO.getMinSeatCount() > bookingDTO.getTicketSeatCount()) {
				iterator.remove();
				continue;
			}
			if (criteriaDTO.getMinTicketFare() != 0 && criteriaDTO.getMinTicketFare() > ticketDTO.getTotalFare().intValue()) {
				iterator.remove();
				continue;
			}
			if (criteriaDTO.isRegisteredUserFlag() && StringUtil.isNull(authDTO.getUser().getToken())) {
				iterator.remove();
				continue;
			}
			if (criteriaDTO.isRoundTripFlag() && !bookingDTO.isRoundTripFlag()) {
				iterator.remove();
				continue;
			}

		}
		DateTime now = DateTime.now(TimeZone.getDefault());
		RouteDTO routeDTO = null;
		for (Iterator<DiscountCriteriaDTO> iterator = criterialist.iterator(); iterator.hasNext();) {
			DiscountCriteriaDTO criteriaDTO = iterator.next();
			int minutiesDiff = DateUtil.getMinutiesDifferent(now, DateUtil.addMinituesToDate(ticketDTO.getTripDTO().getTripDate(), ticketDTO.getTripDTO().getStage().getFromStation().getMinitues()));
			if (criteriaDTO.getAfterBookingMinitues() != 0 && criteriaDTO.getAfterBookingMinitues() > minutiesDiff) {
				iterator.remove();
				continue;
			}
			if (criteriaDTO.getBeforeBookingMinitues() != 0 && criteriaDTO.getBeforeBookingMinitues() < minutiesDiff) {
				iterator.remove();
				continue;
			}
			if (criteriaDTO.getScheduleCode() != null && !criteriaDTO.getScheduleCode().isEmpty() && !criteriaDTO.getScheduleCode().contains(ticketDTO.getTripDTO().getSchedule().getCode())) {
				iterator.remove();
				continue;
			}
			if (routeDTO == null && criteriaDTO.getRouteCode() != null && !criteriaDTO.getRouteCode().isEmpty()) {
				routeDTO = stationService.getRouteDTO(authDTO, ticketDTO.getTripDTO().getStage().getFromStation().getStation(), ticketDTO.getTripDTO().getStage().getToStation().getStation());
				if (routeDTO == null || routeDTO.getId() == 0 || !criteriaDTO.getRouteCode().contains(routeDTO.getCode())) {
					iterator.remove();
					continue;
				}
			}
			if (criteriaDTO.getMaxUsageLimitPerUser() > 0) {
				if (criteriaDTO.getMaxUsageLimitPerUser() <= criteriaDTO.getDiscountCoupon().getUsedCount()) {
					iterator.remove();
					continue;
				}
			}
		}

		Map<String, DiscountCriteriaDTO> seatGenderCriteria = new HashMap<>();
		for (SeatGendarEM seatGendar : seatGendars) {
			DiscountCriteriaDTO discountCriteriaDTO = null;
			for (DiscountCriteriaDTO criteriaDTO : criterialist) {
				if (criteriaDTO.getSeatGender() != null && !criteriaDTO.getSeatGender().isEmpty() && BitsUtil.isSeatGenderExists(criteriaDTO.getSeatGender(), seatGendar) == null) {
					continue;
				}
				if (discountCriteriaDTO == null) {
					discountCriteriaDTO = criteriaDTO;
				}
				if (DateUtil.getDayDifferent(new DateTime(criteriaDTO.getActiveFrom()), new DateTime(criteriaDTO.getActiveTo())) <= DateUtil.getDayDifferent(new DateTime(discountCriteriaDTO.getActiveFrom()), new DateTime(discountCriteriaDTO.getActiveTo()))) {
					discountCriteriaDTO = criteriaDTO;
				}

				int totalSeatFare = bookingDTO.getTotalSeatFare().intValue();
				if (Constants.PREVIOUS_PNR_COUPEN.equals(bookingDTO.getCouponCode())) {
					totalSeatFare = preTicket.getAverageSeatFare();
				}

				// Apply Slab
				if (discountCriteriaDTO != null && !discountCriteriaDTO.getSlabList().isEmpty()) {
					for (DiscountCriteriaSlabDTO slabDTO : discountCriteriaDTO.getSlabList()) {
						if (totalSeatFare >= slabDTO.getSlabFromValue() && totalSeatFare <= slabDTO.getSlabToValue()) {
							discountCriteriaDTO.setValue(slabDTO.getSlabValue());
							discountCriteriaDTO.setPercentageFlag(slabDTO.getSlabValueType().getId() == FareTypeEM.PERCENTAGE.getId() ? true : false);
							break;
						}
					}
				}

				seatGenderCriteria.put(seatGendar.getCode(), discountCriteriaDTO);
			}
		}
		return seatGenderCriteria;
	}

	private int getTicketBookedCouponHistory(AuthDTO authDTO, DiscountCriteriaDTO criteriaDTO, String passengerMobileNo) {
		int bookedticketCount = 0;
		List<TicketDTO> ticketList = ticketService.findTicketbyMobileCouponHistory(authDTO, passengerMobileNo, criteriaDTO.getDiscountCoupon().getCoupon());
		for (TicketDTO ticket : ticketList) {
			if (ticket.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() || ticket.getTicketStatus().getId() == TicketStatusEM.TICKET_TRANSFERRED.getId()) {
				bookedticketCount++;
			}
		}
		return bookedticketCount;
	}

	@Override
	public List<DiscountCriteriaDTO> getAllAvailableDiscountOfferPage(AuthDTO authDTO) {
		DiscountDAO discountDAO = new DiscountDAO();
		List<DiscountCriteriaDTO> list = discountDAO.getAllAvailableDiscountOfferPage(authDTO);
		for (Iterator<DiscountCriteriaDTO> iterator = list.iterator(); iterator.hasNext();) {
			DiscountCriteriaDTO criteriaDTO = iterator.next();
			for (GroupDTO groupDTO : criteriaDTO.getGroupList()) {
				groupDTO = groupService.getGroup(authDTO, groupDTO);
			}
			if (DateUtil.getDayDifferent(DateUtil.NOW(), new DateTime(criteriaDTO.getActiveTo())) < 0) {
				iterator.remove();
			}
		}
		return list;
	}

	protected boolean validateServiceTimings(String serviceTimings, int tripHour) {
		boolean isExist = Text.FALSE;
		List<String> timingList = Arrays.asList(serviceTimings.split(Text.COMMA));
		for (String availableTime : timingList) {

			int startHour = StringUtil.getIntegerValue(availableTime.split(Text.HYPHEN)[Numeric.ZERO_INT]);
			int endHour = StringUtil.getIntegerValue(availableTime.split(Text.HYPHEN)[Numeric.ONE_INT]);

			if (tripHour >= startHour && tripHour <= endHour) {
				isExist = Text.TRUE;
				break;
			}
		}
		return isExist;
	}

	@Async
	public void updateDiscountCouponUsage(AuthDTO authDTO, TicketAddonsDetailsDTO ticketAddonsDetailsDTO) {
		DiscountDAO dao = new DiscountDAO();

		DiscountCouponDTO discountCoupon = new DiscountCouponDTO();
		discountCoupon.setCoupon(ticketAddonsDetailsDTO.getRefferenceCode());

		DiscountCouponDTO discountCoupondto = dao.getDiscountCoupon(authDTO, discountCoupon);
		if (discountCoupondto != null) {
			discountCoupondto.setUsedCount(discountCoupondto.getUsedCount() + 1);
			dao.updateDiscountCouponUsage(authDTO, discountCoupondto);
		}
	}

	@Override
	public DiscountCriteriaDTO getCustomerDiscountCriteria(AuthDTO authDTO, UserCustomerDTO userCustomerDTO) {
		DiscountDAO dao = new DiscountDAO();
		DiscountCouponDTO discountCouponDTO = new DiscountCouponDTO();
		discountCouponDTO.setCoupon(Constants.CUSTOMER_DISCOUNT_COUPON);

		List<DiscountCriteriaDTO> criterialist = dao.getAllByCouponCode(authDTO, discountCouponDTO);

		for (Iterator<DiscountCriteriaDTO> iterator = criterialist.iterator(); iterator.hasNext();) {
			DiscountCriteriaDTO criteriaDTO = iterator.next();
			DateTime txtrDate = DateUtil.NOW();
			// common validations
			if (criteriaDTO.getActiveFrom() != null && !txtrDate.gteq(new DateTime(criteriaDTO.getActiveFrom()))) {
				iterator.remove();
				continue;
			}
			if (criteriaDTO.getActiveTo() != null && !txtrDate.lteq(new DateTime(criteriaDTO.getActiveTo()))) {
				iterator.remove();
				continue;
			}
			if (criteriaDTO.getDayOfWeek() != null && criteriaDTO.getDayOfWeek().length() != 7) {
				iterator.remove();
				continue;
			}
			if (criteriaDTO.getDayOfWeek() != null && criteriaDTO.getDayOfWeek().substring(txtrDate.getWeekDay() - 1, txtrDate.getWeekDay()).equals("0")) {
				iterator.remove();
				continue;
			}
			// Check for group level or should be default
			if (authDTO.getNativeNamespaceCode().equals(authDTO.getNamespaceCode()) && !criteriaDTO.getGroupList().isEmpty() && BitsUtil.isGroupExists(criteriaDTO.getGroupList(), authDTO.getGroup()) == null) {
				iterator.remove();
				continue;
			}
			if (!criteriaDTO.getMobileNumberList().isEmpty() && !criteriaDTO.getMobileNumberList().contains(userCustomerDTO.getMobile())) {
				iterator.remove();
				continue;
			}
			DeviceMediumEM deviceMediumEM = BitsUtil.isDeviceMediumExists(criteriaDTO.getDeviceMedium(), authDTO.getDeviceMedium());
			if (deviceMediumEM == null) {
				iterator.remove();
				continue;
			}
			if (criteriaDTO.isRegisteredUserFlag() && StringUtil.isNull(authDTO.getUser().getToken())) {
				iterator.remove();
				continue;
			}
		}

		DiscountCriteriaDTO discountCriteriaDTO = new DiscountCriteriaDTO();
		if (!criterialist.isEmpty()) {
			discountCriteriaDTO = criterialist.get(0);
		}

		return discountCriteriaDTO;
	}

	@Override
	public DiscountCriteriaDTO getCustomerDiscountCoupon(AuthDTO authDTO, UserCustomerDTO userCustomerDTO) {
		DiscountDAO dao = new DiscountDAO();
		DiscountCriteriaDTO discount = dao.getCustomerDiscountCoupon(authDTO, userCustomerDTO);
		for (GroupDTO groupDTO : discount.getGroupList()) {
			groupDTO = groupService.getGroup(authDTO, groupDTO);
		}
		return discount;
	}
}