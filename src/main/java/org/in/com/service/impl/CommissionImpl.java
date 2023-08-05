package org.in.com.service.impl;

import hirondelle.date4j.DateTime;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.in.com.cache.UserCache;
import org.in.com.cache.redis.RedisTripCacheService;
import org.in.com.constants.Numeric;
import org.in.com.dao.CommissionDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.CommissionDTO;
import org.in.com.dto.ExtraCommissionDTO;
import org.in.com.dto.ExtraCommissionSlabDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.RouteDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.CommissionTypeEM;
import org.in.com.dto.enumeration.DateTypeEM;
import org.in.com.dto.enumeration.FareTypeEM;
import org.in.com.dto.enumeration.SlabCalenderModeEM;
import org.in.com.dto.enumeration.SlabCalenderTypeEM;
import org.in.com.dto.enumeration.SlabModeEM;
import org.in.com.service.CommissionService;
import org.in.com.service.GroupService;
import org.in.com.service.StationService;
import org.in.com.service.TripService;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommissionImpl extends UserCache implements CommissionService {

	@Autowired
	StationService stationService;
	@Autowired
	TripService tripService;
	@Autowired
	private RedisTripCacheService cacheService;
	@Autowired
	GroupService groupService;

	public List<CommissionDTO> getAllCommission(AuthDTO authDTO, UserDTO user) {
		CommissionDAO dao = new CommissionDAO();
		List<CommissionDTO> list = dao.getUserCommission(authDTO, user);
		for (CommissionDTO commissinDTO : list) {
			if (commissinDTO.getAudit().getUser().getId() == 0) {
				continue;
			}
			commissinDTO.getAudit().setUser(getUserDTOById(authDTO, commissinDTO.getAudit().getUser()));
		}
		return list;
	}

	public CommissionDTO updateCommission(AuthDTO authDTO, UserDTO user, CommissionDTO commissionDTO) {
		CommissionDAO dao = new CommissionDAO();
		dao.Update(authDTO, user, commissionDTO);
		return commissionDTO;
	}

	public CommissionDTO getCommission(AuthDTO authDTO, UserDTO userDTO, CommissionTypeEM commissionType) {
		CommissionDAO dao = new CommissionDAO();
		CommissionDTO commissionDTO = dao.getTransactionCommissionDetails(authDTO, userDTO, commissionType);
		return commissionDTO;
	}

	public List<CommissionDTO> getCommissionV2(AuthDTO authDTO, UserDTO userDTO) {
		CommissionDAO dao = new CommissionDAO();
		return dao.getTransactionCommissionDetailsV2(authDTO, userDTO);
	}

	public CommissionDTO getUserTaxDetails(AuthDTO authDTO, UserDTO userDTO) {
		CommissionDAO dao = new CommissionDAO();
		return dao.getUserTaxDetails(authDTO, userDTO);
	}

	public List<ExtraCommissionDTO> getAllExtraCommission(AuthDTO authDTO) {
		CommissionDAO commissionDAO = new CommissionDAO();
		List<ExtraCommissionDTO> list = commissionDAO.getAllExtraCommission(authDTO);
		for (ExtraCommissionDTO commissionDTO : list) {
			if (commissionDTO.getGroup() != null) {
				for (GroupDTO groupDTO : commissionDTO.getGroup()) {
					groupService.getGroup(authDTO, groupDTO);
				}
			}
			else if (commissionDTO.getUser() != null) {
				for (UserDTO user : commissionDTO.getUser()) {
					getUserDTOById(authDTO, user);
				}
			}
		}
		return list;
	}

	public ExtraCommissionDTO getExtraCommission(AuthDTO authDTO, ExtraCommissionDTO commissionDTO) {
		CommissionDAO commissionDAO = new CommissionDAO();
		ExtraCommissionDTO extraCommissionDTO = commissionDAO.getExtraCommission(authDTO, commissionDTO);
		if (extraCommissionDTO.getId() != 0) {
			if (extraCommissionDTO.getGroup() != null) {
				for (GroupDTO groupDTO : extraCommissionDTO.getGroup()) {
					groupService.getGroup(authDTO, groupDTO);
				}
			}
			else if (extraCommissionDTO.getUser() != null) {
				for (UserDTO user : extraCommissionDTO.getUser()) {
					getUserDTOById(authDTO, user);
				}
			}
		}
		return extraCommissionDTO;
	}

	public void UpdateExtraCommission(AuthDTO authDTO, ExtraCommissionDTO commissionDTO) {
		if (commissionDTO.getGroup() != null) {
			for (GroupDTO groupDTO : commissionDTO.getGroup()) {
				groupService.getGroup(authDTO, groupDTO);
			}
		}
		else if (commissionDTO.getUser() != null) {
			for (UserDTO user : commissionDTO.getUser()) {
				getUserDTO(authDTO, user);
			}
		}

		CommissionDAO commissionDAO = new CommissionDAO();
		commissionDAO.UpdateExtraCommission(authDTO, commissionDTO);
		cacheService.clearAllExtraCommissionCache(authDTO);
	}

	public CommissionDTO getBookingExtraCommission(AuthDTO authDTO, UserDTO userDTO, CommissionDTO userCommissionDTO, TicketDTO ticketDTO) {
		CommissionDTO commissionDTO = null;
		List<ExtraCommissionDTO> commissionList = null;
		try {
			commissionList = cacheService.getAllExtraCommissionCache(authDTO);
			if (commissionList == null) {
				CommissionDAO commissionDAO = new CommissionDAO();
				commissionList = commissionDAO.getAllExtraCommission(authDTO);
				cacheService.putgetAllExtraCommissionCache(authDTO, commissionList);
			}
			List<RouteDTO> routeList = null;
			boolean userExtraCommissionFound = false;
			TripDTO tripDTO = ticketDTO.getTripDTO();
			for (Iterator<ExtraCommissionDTO> iterator = commissionList.iterator(); iterator.hasNext();) {
				ExtraCommissionDTO discountDTO = iterator.next();
				DateTime dateTime = ticketDTO.getTripDate();
				if (discountDTO.getDateType().getId() == DateTypeEM.TRANSACTION.getId()) {
					dateTime = DateUtil.NOW();
				}
				if (StringUtil.isNull(discountDTO.getActiveFrom()) || StringUtil.isNull(discountDTO.getActiveTo()) || StringUtil.isNull(discountDTO.getDayOfWeek())) {
					iterator.remove();
					continue;
				}
				// common validations
				if (discountDTO.getActiveFrom() != null && !dateTime.gteq(new DateTime(discountDTO.getActiveFrom()).getStartOfDay())) {
					iterator.remove();
					continue;
				}
				if (discountDTO.getActiveTo() != null && !dateTime.lteq(new DateTime(discountDTO.getActiveTo()).getEndOfDay())) {
					iterator.remove();
					continue;
				}
				if (discountDTO.getDayOfWeek() != null && discountDTO.getDayOfWeek().length() != 7) {
					iterator.remove();
					continue;
				}
				if (discountDTO.getDayOfWeek() != null && discountDTO.getDayOfWeek().substring(dateTime.getWeekDay() - 1, dateTime.getWeekDay()).equals("0")) {
					iterator.remove();
					continue;
				}
				if (discountDTO.getCommissionValue().compareTo(BigDecimal.ZERO) == 0) {
					iterator.remove();
					continue;
				}
				if (discountDTO.getGroup() != null && !discountDTO.getGroup().isEmpty() && BitsUtil.isGroupExists(discountDTO.getGroup(), userDTO.getGroup()) == null) {
					iterator.remove();
					continue;
				}
				if (discountDTO.getUser() != null && !discountDTO.getUser().isEmpty() && BitsUtil.isUserExists(discountDTO.getUser(), userDTO) == null) {
					iterator.remove();
					continue;
				}
				if (StringUtil.isNotNull(discountDTO.getScheduleCode())) {
					if (tripDTO == null || tripDTO.getSchedule() == null) {
						tripDTO = tripService.getTripDTOwithScheduleDetails(authDTO, ticketDTO.getTripDTO());
					}
					if (!discountDTO.getScheduleCode().contains(tripDTO.getSchedule().getCode())) {
						iterator.remove();
						continue;
					}
				}
				if (!discountDTO.getRouteList().isEmpty() && ticketDTO.getTripDTO().getSearch().getFromStation() != null && ticketDTO.getTripDTO().getSearch().getToStation() != null) {
					if (routeList == null) {
						routeList = stationService.getRoute(authDTO);
					}
					boolean status = applyCommissionRouteFilter(authDTO, routeList, discountDTO, ticketDTO.getTripDTO());
					if (!status) {
						iterator.remove();
						continue;
					}
				}
				if (ticketDTO.getTotalFare().compareTo(discountDTO.getMinTicketFare()) == -1) {
					iterator.remove();
					continue;
				}
				if (discountDTO.getMinSeatCount() > Numeric.ZERO_INT && ticketDTO.getTicketDetails().size() < discountDTO.getMinSeatCount()) {
					iterator.remove();
					continue;
				}
				// Exception and override
				for (Iterator<ExtraCommissionDTO> OverrideIterator = discountDTO.getOverrideList().iterator(); OverrideIterator.hasNext();) {
					ExtraCommissionDTO overrideStationDTO = OverrideIterator.next();
					if (StringUtil.isNull(overrideStationDTO.getActiveFrom()) || StringUtil.isNull(overrideStationDTO.getActiveTo()) || StringUtil.isNull(overrideStationDTO.getDayOfWeek())) {
						OverrideIterator.remove();
						continue;
					}
					// common validations
					if (overrideStationDTO.getActiveFrom() != null && !dateTime.gteq(new DateTime(overrideStationDTO.getActiveFrom()))) {
						OverrideIterator.remove();
						continue;
					}
					if (overrideStationDTO.getActiveTo() != null && !dateTime.lteq(new DateTime(overrideStationDTO.getActiveTo()))) {
						OverrideIterator.remove();
						continue;
					}
					if (overrideStationDTO.getDayOfWeek() != null && overrideStationDTO.getDayOfWeek().length() != 7) {
						OverrideIterator.remove();
						continue;
					}
					if (overrideStationDTO.getDayOfWeek() != null && overrideStationDTO.getDayOfWeek().substring(dateTime.getWeekDay() - 1, dateTime.getWeekDay()).equals("0")) {
						OverrideIterator.remove();
						continue;
					}
					// Remove if Exceptions
					iterator.remove();
					break;
				}

				if (discountDTO.getUser() != null && !discountDTO.getUser().isEmpty() && BitsUtil.isUserExists(discountDTO.getUser(), userDTO) != null) {
					userExtraCommissionFound = true;
				}
			}
			// remove if User wise assigned
			List<TripDTO> seatCountlist = null;
			for (Iterator<ExtraCommissionDTO> iterator = commissionList.iterator(); iterator.hasNext();) {
				ExtraCommissionDTO discountDTO = iterator.next();

				if (userExtraCommissionFound && discountDTO.getRefferenceType().equals("GR")) {
					iterator.remove();
					break;
				}
				if (discountDTO.getCommissionSlab() != null && seatCountlist == null) {
					seatCountlist = tripService.getTripWiseBookedSeatCount(authDTO, userDTO, getSlabDateRange(ticketDTO, commissionList));
				}
				if (discountDTO.getCommissionSlab() != null && seatCountlist != null) {
					boolean status = applyExtraCommissionSlab(seatCountlist, ticketDTO, discountDTO.getCommissionSlab());
					if (!status) {
						iterator.remove();
						continue;
					}
				}
			}

			ExtraCommissionDTO extraCommissionDTO = null;
			for (ExtraCommissionDTO discountDTO : commissionList) {
				if (extraCommissionDTO == null) {
					extraCommissionDTO = discountDTO;
				}
				if (DateUtil.getDayDifferent(new DateTime(discountDTO.getActiveFrom()), new DateTime(discountDTO.getActiveTo())) <= DateUtil.getDayDifferent(new DateTime(extraCommissionDTO.getActiveFrom()), new DateTime(extraCommissionDTO.getActiveTo()))) {
					extraCommissionDTO = discountDTO;
				}
			}

			// Max Commission Limit
			if (extraCommissionDTO != null && extraCommissionDTO.getMaxCommissionLimit().compareTo(BigDecimal.ZERO) == 1 && userCommissionDTO.getCommissionValue().add(extraCommissionDTO.getCommissionValue()).compareTo(extraCommissionDTO.getMaxCommissionLimit()) == 1) {
				extraCommissionDTO.setCommissionValue(extraCommissionDTO.getMaxCommissionLimit().subtract(userCommissionDTO.getCommissionValue()));
			}

			// Max Extra Commission Amount
			if (extraCommissionDTO != null && extraCommissionDTO.getMaxExtraCommissionAmount().compareTo(BigDecimal.ZERO) == 1 && extraCommissionDTO.getCommissionValueType().getId() == FareTypeEM.PERCENTAGE.getId() && (ticketDTO.getTotalSeatFare().multiply(extraCommissionDTO.getCommissionValue()).divide(Numeric.ONE_HUNDRED, 2)).compareTo(extraCommissionDTO.getMaxExtraCommissionAmount()) == 1) {
				extraCommissionDTO.setCommissionValue(extraCommissionDTO.getMaxExtraCommissionAmount().multiply(Numeric.ONE_HUNDRED).divide(ticketDTO.getTotalSeatFare(), RoundingMode.CEILING));
			}
			else if (extraCommissionDTO != null && extraCommissionDTO.getMaxExtraCommissionAmount().compareTo(BigDecimal.ZERO) == 1 && extraCommissionDTO.getCommissionValueType().getId() == FareTypeEM.FLAT.getId()) {
				extraCommissionDTO.setCommissionValue(extraCommissionDTO.getMaxExtraCommissionAmount());
			}

			if (extraCommissionDTO != null) {
				commissionDTO = new CommissionDTO();
				commissionDTO.setCommissionValue(extraCommissionDTO.getCommissionValue());
				commissionDTO.setCommissionValueType(extraCommissionDTO.getCommissionValueType());
				// Marking to Override Base commission value
				commissionDTO.setActiveFlag(extraCommissionDTO.getOverrideCommissionFlag() == 0 ? 1 : -1);
			}
		}
		catch (Exception e) {
			System.out.println("Error in Get Extra Commssion" + authDTO.getNamespaceCode() + " " + userDTO.getCode());
			e.printStackTrace();
		}
		return commissionDTO;
	}

	public List<CommissionDTO> getCommerceCommission(AuthDTO authDTO, UserDTO userDTO) {
		List<CommissionDTO> list = new ArrayList<CommissionDTO>();
		CommissionDAO dao = new CommissionDAO();
		for (CommissionTypeEM typeDTO : CommissionTypeEM.values()) {
			if (typeDTO.getId() == CommissionTypeEM.TICKETS_BOOKING.getId() || typeDTO.getId() == CommissionTypeEM.TICKETS_CANCEL_COMMISSION_ON_CHARGE.getId() || typeDTO.getId() == CommissionTypeEM.TICKET_CANCEL_COMMISSION_ON_TICKETFARE.getId()) {
				CommissionDTO commissionDTO = dao.getTransactionCommissionDetails(authDTO, userDTO, typeDTO);
				if (commissionDTO == null) {
					continue;
				}
				list.add(commissionDTO);
			}
		}
		return list;
	}

	public void updateExtraCommissionSlabDetails(AuthDTO authDTO, ExtraCommissionSlabDTO commissionSlabDTO) {
		CommissionDAO dao = new CommissionDAO();
		dao.updateExtraCommissionSlab(authDTO, commissionSlabDTO);
		cacheService.clearAllExtraCommissionCache(authDTO);
	}

	public List<ExtraCommissionSlabDTO> getAllExtraCommissionSlab(AuthDTO authDTO) {
		CommissionDAO dao = new CommissionDAO();
		return dao.getAllCommissionSlab(authDTO);
	}

	private boolean applyExtraCommissionSlab(List<TripDTO> seatCountlist, TicketDTO ticketDTO, ExtraCommissionSlabDTO commissionSlab) {
		int seatCount = 0;
		int seatAmount = 0;

		DateTime fromDate = null, toDate = null;
		if (commissionSlab.getSlabCalenderMode().getId() == SlabCalenderModeEM.FLEXI.getId()) {
			if (commissionSlab.getSlabCalenderType().getId() == SlabCalenderTypeEM.DAY.getId()) {
				fromDate = DateUtil.minusDaysToDate(DateUtil.NOW(), 1).getStartOfDay();
				toDate = DateUtil.minusDaysToDate(DateUtil.NOW(), 1).getEndOfDay();
			}
			else if (commissionSlab.getSlabCalenderType().getId() == SlabCalenderTypeEM.WEEK.getId()) {
				fromDate = DateUtil.minusDaysToDate(DateUtil.NOW(), 8).getStartOfDay();
				toDate = DateUtil.minusDaysToDate(DateUtil.NOW(), 1).getEndOfDay();
			}
			else if (commissionSlab.getSlabCalenderType().getId() == SlabCalenderTypeEM.MONTH.getId()) {
				fromDate = DateUtil.minusDaysToDate(DateUtil.NOW(), 31).getStartOfDay();
				toDate = DateUtil.minusDaysToDate(DateUtil.NOW(), 1).getEndOfDay();
			}
		}
		else if (commissionSlab.getSlabCalenderMode().getId() == SlabCalenderModeEM.STRICT.getId()) {
			if (commissionSlab.getSlabCalenderType().getId() == SlabCalenderTypeEM.DAY.getId()) {
				fromDate = ticketDTO.getTripDTO().getTripDate().getStartOfDay();
				toDate = ticketDTO.getTripDTO().getTripDate().getEndOfDay();
			}
			else if (commissionSlab.getSlabCalenderType().getId() == SlabCalenderTypeEM.WEEK.getId()) {
				fromDate = DateUtil.getWeekStartDate(ticketDTO.getTripDTO().getTripDate()).getStartOfDay();
				toDate = DateUtil.getWeekEndDate(ticketDTO.getTripDTO().getTripDate()).getEndOfDay();
			}
			else if (commissionSlab.getSlabCalenderType().getId() == SlabCalenderTypeEM.MONTH.getId()) {
				fromDate = ticketDTO.getTripDTO().getTripDate().getStartOfMonth();
				toDate = fromDate.plusDays(ticketDTO.getTripDTO().getTripDate().getNumDaysInMonth() - 1).getEndOfDay();
			}
		}
		for (TripDTO tripDTO : seatCountlist) {
			if (tripDTO.getTripDate().getStartOfDay().gteq(fromDate) && tripDTO.getTripDate().getStartOfDay().lteq(toDate)) {
				seatCount += tripDTO.getBookedSeatCount();
				seatAmount += tripDTO.getId();
			}
		}
		if (commissionSlab.getSlabMode().getId() == SlabModeEM.COUNT.getId() && seatCount >= commissionSlab.getSlabFromValue() && seatCount <= commissionSlab.getSlabToValue()) {
			return true;
		}
		else if (commissionSlab.getSlabMode().getId() == SlabModeEM.AMOUNT.getId() && seatAmount >= commissionSlab.getSlabFromValue() && seatAmount <= commissionSlab.getSlabToValue()) {
			return true;
		}
		return false;
	}

	private boolean applyCommissionRouteFilter(AuthDTO authDTO, List<RouteDTO> routeList, ExtraCommissionDTO discountDTO, TripDTO tripDTO) {
		for (RouteDTO routeDTO : routeList) {
			for (RouteDTO commRouteDTO : discountDTO.getRouteList()) {
				if (commRouteDTO.getCode().equals(routeDTO.getCode()) && routeDTO.getFromStation().getId() == tripDTO.getStage().getFromStation().getStation().getId() && routeDTO.getFromStation().getId() == tripDTO.getStage().getFromStation().getStation().getId()) {
					return true;
				}
			}
		}
		return false;
	}

	private ExtraCommissionDTO getSlabDateRange(TicketDTO ticketDTO, List<ExtraCommissionDTO> commissionList) {
		ExtraCommissionDTO commission = null;
		for (ExtraCommissionDTO extraCommissionDTO : commissionList) {
			if (extraCommissionDTO.getCommissionSlab() == null) {
				continue;
			}
			DateTime fromDate = null, toDate = null;
			ExtraCommissionSlabDTO commissionSlab = extraCommissionDTO.getCommissionSlab();
			if (commissionSlab.getSlabCalenderMode().getId() == SlabCalenderModeEM.FLEXI.getId()) {
				if (commissionSlab.getSlabCalenderType().getId() == SlabCalenderTypeEM.DAY.getId()) {
					fromDate = DateUtil.minusDaysToDate(DateUtil.NOW(), 1).getStartOfDay();
					toDate = DateUtil.minusDaysToDate(DateUtil.NOW(), 1).getEndOfDay();
				}
				else if (commissionSlab.getSlabCalenderType().getId() == SlabCalenderTypeEM.WEEK.getId()) {
					fromDate = DateUtil.minusDaysToDate(DateUtil.NOW(), 8).getStartOfDay();
					toDate = DateUtil.minusDaysToDate(DateUtil.NOW(), 1).getEndOfDay();
				}
				else if (commissionSlab.getSlabCalenderType().getId() == SlabCalenderTypeEM.MONTH.getId()) {
					fromDate = DateUtil.minusDaysToDate(DateUtil.NOW(), 31).getStartOfDay();
					toDate = DateUtil.minusDaysToDate(DateUtil.NOW(), 1).getEndOfDay();
				}
			}
			else if (commissionSlab.getSlabCalenderMode().getId() == SlabCalenderModeEM.STRICT.getId()) {
				if (commissionSlab.getSlabCalenderType().getId() == SlabCalenderTypeEM.DAY.getId()) {
					fromDate = ticketDTO.getTripDTO().getTripDate().getStartOfDay();
					toDate = ticketDTO.getTripDTO().getTripDate().getEndOfDay();
				}
				else if (commissionSlab.getSlabCalenderType().getId() == SlabCalenderTypeEM.WEEK.getId()) {
					fromDate = DateUtil.getWeekStartDate(ticketDTO.getTripDTO().getTripDate()).getStartOfDay();
					toDate = DateUtil.getWeekEndDate(ticketDTO.getTripDTO().getTripDate()).getEndOfDay();
				}
				else if (commissionSlab.getSlabCalenderType().getId() == SlabCalenderTypeEM.MONTH.getId()) {
					fromDate = ticketDTO.getTripDTO().getTripDate().getStartOfMonth();
					toDate = fromDate.plusDays(ticketDTO.getTripDTO().getTripDate().getNumDaysInMonth() - 1).getEndOfDay();
				}
			}
			if (commission == null) {
				commission = new ExtraCommissionDTO();
				commission.setActiveFrom(fromDate.format("YYYY-MM-DD"));
				commission.setActiveTo(toDate.format("YYYY-MM-DD"));
			}
			if (commission.getActiveFromDate().gt(fromDate)) {
				commission.setActiveFrom(fromDate.format("YYYY-MM-DD"));
			}
			if (commission.getActiveToDate().lt(toDate)) {
				commission.setActiveTo(toDate.format("YYYY-MM-DD"));
			}
		}
		return commission;
	}

}
