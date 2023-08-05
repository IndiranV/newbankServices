package org.in.com.service.helper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.in.com.cache.CacheCentral;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusSeatLayoutDTO;
import org.in.com.dto.RouteDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleFareAutoOverrideDTO;
import org.in.com.dto.ScheduleSeatFareDTO;
import org.in.com.dto.ScheduleStageDTO;
import org.in.com.dto.ScheduleTimeOverrideDTO;
import org.in.com.dto.SearchDTO;
import org.in.com.dto.StageDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.TripSeatQuotaDTO;
import org.in.com.dto.enumeration.BusCategoryTypeEM;
import org.in.com.dto.enumeration.BusSeatTypeEM;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.FareOverrideTypeEM;
import org.in.com.dto.enumeration.FareTypeEM;
import org.in.com.dto.enumeration.OverrideTypeEM;
import org.in.com.dto.enumeration.SeatGendarEM;
import org.in.com.dto.enumeration.SeatGenderRestrictionEM;
import org.in.com.dto.enumeration.SeatStatusEM;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.dto.enumeration.TravelStatusEM;
import org.in.com.dto.enumeration.TripActivitiesEM;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;

import hirondelle.date4j.DateTime;

public class HelperUtil extends CacheCentral {

	protected void applyBookedBlockedSeat(AuthDTO authDTO, TripDTO tripDTO) {
		Map<String, List<TicketDetailsDTO>> statusMAP = new HashMap<String, List<TicketDetailsDTO>>();
		if (tripDTO != null && tripDTO.getTicketDetailsList() != null && !tripDTO.getTicketDetailsList().isEmpty()) {
			for (TicketDetailsDTO ticketDetailsDTO : tripDTO.getTicketDetailsList()) {
				if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.TMP_BLOCKED_TICKET.getId() && DateUtil.getMinutiesDifferent(ticketDetailsDTO.getUpdatedAt(), DateUtil.NOW()) > authDTO.getNamespace().getProfile().getSeatBlockTime()) {
					continue;
				}
				if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.TENTATIVE_BLOCK_CANCELLED.getId()) {
					continue;
				}
				// Validate PBL Block Live Time
				if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId() && BitsUtil.validateBlockReleaseTime(ticketDetailsDTO.getBlockReleaseMinutes(), tripDTO.getTripDateTime(), ticketDetailsDTO.getUpdatedAt())) {
					continue;
				}
				if (ticketDetailsDTO.getTicketExtra() != null && ticketDetailsDTO.getTicketExtra().getTravelStatus() != null && ticketDetailsDTO.getTicketExtra().getTravelStatus().getId() == TravelStatusEM.NOT_TRAVELED.getId()) {
					continue;
				}

				if (tripDTO.getReleatedStageCodeList().contains(ticketDetailsDTO.getTripStageCode())) {
					if (ticketDetailsDTO.getTicketStatus().getId() != TicketStatusEM.CONFIRM_CANCELLED_TICKETS.getId() && ticketDetailsDTO.getTicketStatus().getId() != TicketStatusEM.PHONE_BOOKING_CANCELLED.getId()) {
						List<TicketDetailsDTO> list = null;
						if (statusMAP.get(ticketDetailsDTO.getSeatCode()) == null) {
							list = new ArrayList<TicketDetailsDTO>();
						}
						else {
							list = statusMAP.get(ticketDetailsDTO.getSeatCode());
						}
						list.add(ticketDetailsDTO);
						statusMAP.put(ticketDetailsDTO.getSeatCode(), list);
					}
				}
			}
		}

		Map<String, String> seatCodeMap = new HashMap<>();
		Map<String, BusSeatLayoutDTO> socialDistanceMap = new HashMap<>();
		Map<String, Integer> seatStatusMap = new HashMap<>();
		boolean isLayout1X1 = tripDTO.getBus().checkLayoutCategory(BusCategoryTypeEM.LAYOUT_1X1);

		for (BusSeatLayoutDTO seatLayoutDTO : tripDTO.getBus().getBusSeatLayoutDTO().getList()) {
			List<TicketDetailsDTO> list = statusMAP.get(seatLayoutDTO.getCode());
			if (list != null && !list.isEmpty()) {
				for (TicketDetailsDTO ticketDetailsDTO : list) {
					if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId()) {
						seatLayoutDTO.setSeatStatus(SeatStatusEM.BOOKED);
					}
					else if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.TMP_BLOCKED_TICKET.getId()) {
						seatLayoutDTO.setSeatStatus(SeatStatusEM.TEMP_BLOCKED);
						seatLayoutDTO.setUpdatedAt(ticketDetailsDTO.getUpdatedAt());
					}
					else if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
						seatLayoutDTO.setSeatStatus(SeatStatusEM.PHONE_BLOCKED);
					}
					else if (seatLayoutDTO.getSeatStatus() == null) {
						seatLayoutDTO.setSeatStatus(SeatStatusEM.AVAILABLE_ALL);
					}

					// Copy ticket details
					if ((SeatStatusEM.BOOKED.getId() == seatLayoutDTO.getSeatStatus().getId() || SeatStatusEM.BLOCKED.getId() == seatLayoutDTO.getSeatStatus().getId() || SeatStatusEM.TEMP_BLOCKED.getId() == seatLayoutDTO.getSeatStatus().getId() || SeatStatusEM.PHONE_BLOCKED.getId() == seatLayoutDTO.getSeatStatus().getId()) && StringUtil.isNull(seatLayoutDTO.getTicketCode())) {
						seatLayoutDTO.setSeatGendar(ticketDetailsDTO.getSeatGendar());
						seatLayoutDTO.setPassengerAge(ticketDetailsDTO.getPassengerAge());
						seatLayoutDTO.setPassengerName(ticketDetailsDTO.getPassengerName());
						seatLayoutDTO.setContactNumber(ticketDetailsDTO.getContactNumber());
						seatLayoutDTO.setTicketCode(ticketDetailsDTO.getTicketCode());
						seatLayoutDTO.setBoardingPointName(ticketDetailsDTO.getBoardingPointName());
						seatLayoutDTO.setStationPoint(ticketDetailsDTO.getStationPoint());
						seatLayoutDTO.setUser(getUserDTOById(authDTO, ticketDetailsDTO.getUser()));
						seatLayoutDTO.setGroup(getGroupDTOById(authDTO, seatLayoutDTO.getUser().getGroup()));
						seatLayoutDTO.setFromStation(getStationDTObyId(ticketDetailsDTO.getFromStation()));
						seatLayoutDTO.setToStation(getStationDTObyId(ticketDetailsDTO.getToStation()));
						seatLayoutDTO.setUpdatedAt(ticketDetailsDTO.getUpdatedAt());
						if (authDTO.getDeviceMedium().getId() != DeviceMediumEM.API_USER.getId()) {
							seatLayoutDTO.setFare(ticketDetailsDTO.getSeatFare());
						}

						if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId() && ticketDetailsDTO.getTicketExtra() != null && ticketDetailsDTO.getTicketExtra().getBlockReleaseMinutes() != 0) {
							seatLayoutDTO.setReleaseAt(BitsUtil.getBlockReleaseDateTime(ticketDetailsDTO.getTicketExtra().getBlockReleaseMinutes(), tripDTO.getTripDateTimeV2(), ticketDetailsDTO.getUpdatedAt()));
						}
					}
				}
			}

			seatCodeMap.put(seatLayoutDTO.getCode(), seatLayoutDTO.getLayer() + "_" + seatLayoutDTO.getRowPos() + "_" + seatLayoutDTO.getColPos());
			if (seatLayoutDTO.getSeatStatus() != null) {
				socialDistanceMap.put(seatLayoutDTO.getLayer() + "_" + seatLayoutDTO.getRowPos() + "_" + seatLayoutDTO.getColPos(), seatLayoutDTO);
				seatStatusMap.put(seatLayoutDTO.getCode(), seatLayoutDTO.getSeatStatus().getId());
			}
		}

		/** Validate Social Distance Seats */
		boolean enableSocialDistanceAmenities = Text.FALSE;
		Map<String, String> additionalAttributes = new HashMap<String, String>();
		for (BusSeatLayoutDTO seatLayoutDTO : tripDTO.getBus().getBusSeatLayoutDTO().getList()) {
			if (!isLayout1X1 && seatLayoutDTO.getSeatStatus() != null && seatLayoutDTO.getSeatStatus().getId() == SeatStatusEM.SOCIAL_DISTANCE_BLOCK.getId()) {
				String seatPos = seatCodeMap.get(seatLayoutDTO.getCode());
				enableSocialDistanceAmenities = Text.TRUE;
				if (seatPos != null) {
					int layer = Integer.parseInt(seatPos.split("_")[0]);
					int rowCount = Integer.parseInt(seatPos.split("_")[1]);
					int colCount = Integer.parseInt(seatPos.split("_")[2]);

					BusSeatLayoutDTO adjucentSeat = null;
					if (seatLayoutDTO.getOrientation() == 0) {
						/** Left Side seat */
						if (socialDistanceMap.get(layer + "_" + (rowCount + 1) + "_" + colCount) != null && socialDistanceMap.get(layer + "_" + (rowCount - 1) + "_" + colCount) == null) {
							adjucentSeat = socialDistanceMap.get(layer + "_" + (rowCount + 1) + "_" + colCount);
						}
						/** Right Side seat */
						else if (socialDistanceMap.get(layer + "_" + (rowCount + 1) + "_" + colCount) == null && socialDistanceMap.get(layer + "_" + (rowCount - 1) + "_" + colCount) != null) {
							adjucentSeat = socialDistanceMap.get(layer + "_" + (rowCount - 1) + "_" + colCount);
						}
						/** Middle seat */
						else if (socialDistanceMap.get(layer + "_" + (rowCount + 1) + "_" + colCount) != null && socialDistanceMap.get(layer + "_" + (rowCount - 1) + "_" + colCount) != null) {
							adjucentSeat = socialDistanceMap.get(layer + "_" + (rowCount - 1) + "_" + colCount);
							if (adjucentSeat != null && adjucentSeat.getSeatStatus() != null && adjucentSeat.getSeatStatus().getId() != SeatStatusEM.BOOKED.getId() && adjucentSeat.getSeatStatus().getId() != SeatStatusEM.PHONE_BLOCKED.getId()) {
								adjucentSeat = socialDistanceMap.get(layer + "_" + (rowCount + 1) + "_" + colCount);
							}
						}
					}
					else if (seatLayoutDTO.getOrientation() == 1) {
						/** Left Side seat */
						if (socialDistanceMap.get(layer + "_" + rowCount + "_" + (colCount + 1)) != null && socialDistanceMap.get(layer + "_" + rowCount + "_" + (colCount - 1)) == null) {
							adjucentSeat = socialDistanceMap.get(layer + "_" + rowCount + "_" + (colCount + 1));
						}
						/** Right Side seat */
						else if (socialDistanceMap.get(layer + "_" + rowCount + "_" + (colCount + 1)) == null && socialDistanceMap.get(layer + "_" + rowCount + "_" + (colCount - 1)) != null) {
							adjucentSeat = socialDistanceMap.get(layer + "_" + rowCount + "_" + (colCount - 1));
						}
						/** Middle seat */
						else if (socialDistanceMap.get(layer + "_" + rowCount + "_" + (colCount - 1)) != null && socialDistanceMap.get(layer + "_" + rowCount + "_" + (colCount + 1)) != null) {
							adjucentSeat = socialDistanceMap.get(layer + "_" + rowCount + "_" + (colCount - 1));
							if (adjucentSeat != null && adjucentSeat.getSeatStatus() != null && adjucentSeat.getSeatStatus().getId() != SeatStatusEM.BOOKED.getId() && adjucentSeat.getSeatStatus().getId() != SeatStatusEM.PHONE_BLOCKED.getId()) {
								adjucentSeat = socialDistanceMap.get(layer + "_" + rowCount + "_" + (colCount + 1));
							}
						}
					}

					if (adjucentSeat != null && adjucentSeat.getSeatStatus() != null && adjucentSeat.getSeatStatus().getId() == SeatStatusEM.SOCIAL_DISTANCE_BLOCK.getId() && seatLayoutDTO.getSeatStatus().getId() == SeatStatusEM.SOCIAL_DISTANCE_BLOCK.getId()) {
						additionalAttributes.put(SeatStatusEM.SOCIAL_DISTANCE_BLOCK.getCode(), "DYNAMIC");
					}
					else if (adjucentSeat != null && adjucentSeat.getSeatStatus() != null && adjucentSeat.getSeatStatus().getId() != SeatStatusEM.SOCIAL_DISTANCE_BLOCK.getId() && seatLayoutDTO.getSeatStatus().getId() == SeatStatusEM.SOCIAL_DISTANCE_BLOCK.getId() && additionalAttributes.get(SeatStatusEM.SOCIAL_DISTANCE_BLOCK.getCode()) == null) {
						additionalAttributes.put(SeatStatusEM.SOCIAL_DISTANCE_BLOCK.getCode(), "STATIC");
					}

					/**
					 * Change seat status, if social distance blocked
					 * seat booked
					 */
					if (adjucentSeat != null && adjucentSeat.getSeatStatus() != null && (adjucentSeat.getSeatStatus().getId() == SeatStatusEM.SOCIAL_DISTANCE_BLOCK.getId() || seatStatusMap.get(adjucentSeat.getCode()) == SeatStatusEM.SOCIAL_DISTANCE_BLOCK.getId())) {
						seatLayoutDTO.setSeatStatus(SeatStatusEM.AVAILABLE_ALL);
					}
					else if (adjucentSeat == null) {
						seatLayoutDTO.setSeatStatus(SeatStatusEM.AVAILABLE_ALL);
					}

				}
			}
			if (seatLayoutDTO.getUser() != null && seatLayoutDTO.getUser().getId() != 0) {
				seatLayoutDTO.setUser(getUserDTOById(authDTO, seatLayoutDTO.getUser()));
			}
			if (seatLayoutDTO.getGroup() != null && seatLayoutDTO.getGroup().getId() != 0) {
				seatLayoutDTO.setGroup(getGroupDTOById(authDTO, seatLayoutDTO.getGroup()));
			}
		}

		/** If social distance applied, append social distance amenities */
		if (enableSocialDistanceAmenities) {
			tripDTO.getActivities().add(TripActivitiesEM.SOCIAL_DISTANCING);
			tripDTO.getAdditionalAttributes().putAll(additionalAttributes);
		}
	}

	/*
	 * For Search, no need gender validation and adjacent Seat logic
	 */
	protected void applySearchBookedBlockedSeat(AuthDTO authDTO, TripDTO tripDTO, List<TicketDetailsDTO> ticketDetails) {
		Map<String, List<TicketDetailsDTO>> statusMAP = new HashMap<String, List<TicketDetailsDTO>>();
		if (ticketDetails != null && !ticketDetails.isEmpty()) {
			for (TicketDetailsDTO ticketDetailsDTO : ticketDetails) {
				if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.TMP_BLOCKED_TICKET.getId() && DateUtil.getMinutiesDifferent(ticketDetailsDTO.getUpdatedAt(), DateUtil.NOW()) > authDTO.getNamespace().getProfile().getSeatBlockTime()) {
					continue;
				}
				if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.TENTATIVE_BLOCK_CANCELLED.getId()) {
					continue;
				}
				// Validate PBL Block Live Time
				if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId() && BitsUtil.validateBlockReleaseTime(ticketDetailsDTO.getBlockReleaseMinutes(), tripDTO.getTripDateTime(), ticketDetailsDTO.getUpdatedAt())) {
					continue;
				}
				if (ticketDetailsDTO.getTicketExtra() != null && ticketDetailsDTO.getTicketExtra().getTravelStatus() != null && ticketDetailsDTO.getTicketExtra().getTravelStatus().getId() == TravelStatusEM.NOT_TRAVELED.getId()) {
					continue;
				}

				if (tripDTO.getReleatedStageCodeList().contains(ticketDetailsDTO.getTripStageCode())) {
					if (ticketDetailsDTO.getTicketStatus().getId() != TicketStatusEM.CONFIRM_CANCELLED_TICKETS.getId() && ticketDetailsDTO.getTicketStatus().getId() != TicketStatusEM.PHONE_BOOKING_CANCELLED.getId()) {
						List<TicketDetailsDTO> list = null;
						if (statusMAP.get(ticketDetailsDTO.getSeatCode()) == null) {
							list = new ArrayList<TicketDetailsDTO>();
						}
						else {
							list = statusMAP.get(ticketDetailsDTO.getSeatCode());
						}
						list.add(ticketDetailsDTO);
						statusMAP.put(ticketDetailsDTO.getSeatCode(), list);
					}
				}
			}
		}

		for (BusSeatLayoutDTO seatLayoutDTO : tripDTO.getBus().getBusSeatLayoutDTO().getList()) {

			List<TicketDetailsDTO> list = statusMAP.get(seatLayoutDTO.getCode());
			if (list != null && !list.isEmpty()) {
				for (TicketDetailsDTO ticketDetailsDTO : list) {
					if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId()) {
						seatLayoutDTO.setSeatStatus(SeatStatusEM.BOOKED);
					}
					else if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.TMP_BLOCKED_TICKET.getId()) {
						seatLayoutDTO.setSeatStatus(SeatStatusEM.TEMP_BLOCKED);
						seatLayoutDTO.setUpdatedAt(ticketDetailsDTO.getUpdatedAt());
					}
					else if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
						seatLayoutDTO.setSeatStatus(SeatStatusEM.PHONE_BLOCKED);
					}
					else if (seatLayoutDTO.getSeatStatus() == null) {
						seatLayoutDTO.setSeatStatus(SeatStatusEM.AVAILABLE_ALL);
					}

				}
			}
		}

	}

	protected void applyTripSeatQuota(AuthDTO authDTO, TripDTO tripDTO, List<TripSeatQuotaDTO> tripSeatQuatoList) {
		try {
			Map<String, List<TripSeatQuotaDTO>> quotaMAP = new HashMap<String, List<TripSeatQuotaDTO>>();
			if (tripDTO != null && tripSeatQuatoList != null && !tripSeatQuatoList.isEmpty()) {
				for (TripSeatQuotaDTO seatQuotaDTO : tripSeatQuatoList) {
					if ((seatQuotaDTO.getFromStation().getId() == 0 && seatQuotaDTO.getToStation().getId() == 0) || tripDTO.getReleatedStageCodeList().contains(getGeneratedTripStageCode(authDTO, tripDTO.getSchedule(), tripDTO, seatQuotaDTO.getFromStation(), seatQuotaDTO.getToStation()))) {
						List<TripSeatQuotaDTO> list = null;
						if (quotaMAP.get(seatQuotaDTO.getSeatDetails().getSeatCode()) == null) {
							list = new ArrayList<TripSeatQuotaDTO>();
						}
						else {
							list = quotaMAP.get(seatQuotaDTO.getSeatDetails().getSeatCode());
						}
						list.add(seatQuotaDTO);
						quotaMAP.put(seatQuotaDTO.getSeatDetails().getSeatCode(), list);
					}
				}
			}
			for (BusSeatLayoutDTO seatLayoutDTO : tripDTO.getBus().getBusSeatLayoutDTO().getList()) {
				List<TripSeatQuotaDTO> list = quotaMAP.get(seatLayoutDTO.getCode());
				if (list != null && !list.isEmpty()) {
					for (TripSeatQuotaDTO seatQuotaDTO : list) {
						seatLayoutDTO.setSeatStatus(SeatStatusEM.QUOTA_SEAT);

						// Copy ticket details
						seatLayoutDTO.setSeatGendar(seatQuotaDTO.getSeatDetails().getSeatGendar());
						if (seatQuotaDTO.getSeatDetails().getSeatFare().compareTo(BigDecimal.ZERO) > 0) {
							seatLayoutDTO.setFare(seatQuotaDTO.getSeatDetails().getSeatFare());
						}
						seatLayoutDTO.setUser(getUserDTOById(authDTO, seatQuotaDTO.getUser()));
						seatLayoutDTO.setFromStation(getStationDTObyId(seatQuotaDTO.getFromStation()));
						seatLayoutDTO.setToStation(getStationDTObyId(seatQuotaDTO.getToStation()));
						seatLayoutDTO.setUpdatedAt(new DateTime(seatQuotaDTO.getUpdatedAt()));
						seatLayoutDTO.setPassengerName(seatQuotaDTO.getUpdatedBy().getName());

						if (seatLayoutDTO.getUser() != null && seatLayoutDTO.getUser().getId() != 0) {
							seatLayoutDTO.setUser(getUserDTOById(authDTO, seatLayoutDTO.getUser()));
						}
						if (seatLayoutDTO.getGroup() != null && seatLayoutDTO.getGroup().getId() != 0) {
							seatLayoutDTO.setGroup(getGroupDTOById(authDTO, seatLayoutDTO.getGroup()));
						}
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void applyMultiStageGendarValidations(AuthDTO authDTO, TripDTO tripDTO, List<TripSeatQuotaDTO> tripSeatQuatoList) {
		Map<String, List<TicketDetailsDTO>> statusMAP = new HashMap<String, List<TicketDetailsDTO>>();
		Map<String, List<SeatGendarEM>> seatGenderMAP = new HashMap<String, List<SeatGendarEM>>();
		// skip gender validation is 1x1
		boolean isLayout1X1 = tripDTO.getBus().checkLayoutCategory(BusCategoryTypeEM.LAYOUT_1X1);

		if (tripDTO != null && tripDTO.getTicketDetailsList() != null && !tripDTO.getTicketDetailsList().isEmpty()) {
			for (TicketDetailsDTO ticketDetailsDTO : tripDTO.getTicketDetailsList()) {
				if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.TMP_BLOCKED_TICKET.getId() && DateUtil.getMinutiesDifferent(ticketDetailsDTO.getUpdatedAt(), DateUtil.NOW()) > authDTO.getNamespace().getProfile().getSeatBlockTime()) {
					continue;
				}
				// Validate PBL Block Live Time
				if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId() && BitsUtil.validateBlockReleaseTime(ticketDetailsDTO.getBlockReleaseMinutes(), tripDTO.getTripDateTime(), ticketDetailsDTO.getUpdatedAt())) {
					continue;
				}
				if (ticketDetailsDTO.getTicketExtra() != null && ticketDetailsDTO.getTicketExtra().getTravelStatus() != null && ticketDetailsDTO.getTicketExtra().getTravelStatus().getId() == TravelStatusEM.NOT_TRAVELED.getId()) {
					continue;
				}
				if (tripDTO.getReleatedStageCodeList().contains(ticketDetailsDTO.getTripStageCode())) {
					if (ticketDetailsDTO.getTicketStatus().getId() != TicketStatusEM.CONFIRM_CANCELLED_TICKETS.getId() && ticketDetailsDTO.getTicketStatus().getId() != TicketStatusEM.PHONE_BOOKING_CANCELLED.getId()) {
						List<TicketDetailsDTO> list = null;
						if (statusMAP.get(ticketDetailsDTO.getSeatCode()) == null) {
							list = new ArrayList<TicketDetailsDTO>();
						}
						else {
							list = statusMAP.get(ticketDetailsDTO.getSeatCode());
						}
						list.add(ticketDetailsDTO);
						statusMAP.put(ticketDetailsDTO.getSeatCode(), list);
					}
				}
			}
		}
		if (tripSeatQuatoList != null && !tripSeatQuatoList.isEmpty()) {
			for (TripSeatQuotaDTO seatQuotaDTO : tripSeatQuatoList) {
				if ((seatQuotaDTO.getFromStation().getId() == 0 && seatQuotaDTO.getToStation().getId() == 0) || tripDTO.getReleatedStageCodeList().contains(getGeneratedTripStageCode(authDTO, tripDTO.getSchedule(), tripDTO, seatQuotaDTO.getFromStation(), seatQuotaDTO.getToStation()))) {
					List<TicketDetailsDTO> list = null;
					if (statusMAP.get(seatQuotaDTO.getSeatDetails().getSeatCode()) == null) {
						list = new ArrayList<TicketDetailsDTO>();
					}
					else {
						list = statusMAP.get(seatQuotaDTO.getSeatDetails().getSeatCode());
					}
					TicketDetailsDTO ticketDetailsDTO = new TicketDetailsDTO();
					ticketDetailsDTO.setSeatCode(seatQuotaDTO.getSeatDetails().getSeatCode());
					ticketDetailsDTO.setTicketStatus(TicketStatusEM.TRIP_SEAT_QUOTA);
					ticketDetailsDTO.setSeatGendar(seatQuotaDTO.getSeatDetails().getSeatGendar());
					list.add(ticketDetailsDTO);
					statusMAP.put(seatQuotaDTO.getSeatDetails().getSeatCode(), list);
				}
			}
		}
		// Seat Gender preferences apply for Next seats
		for (BusSeatLayoutDTO seatLayoutDTO : tripDTO.getBus().getBusSeatLayoutDTO().getList()) {
			if (seatLayoutDTO.getSeatGendar() != null && (seatLayoutDTO.getSeatGendar().getId() == SeatGendarEM.MALE.getId() || seatLayoutDTO.getSeatGendar().getId() == SeatGendarEM.FEMALE.getId())) {
				List<TicketDetailsDTO> list = null;
				if (statusMAP.get(seatLayoutDTO.getCode()) == null) {
					list = new ArrayList<TicketDetailsDTO>();
				}
				else {
					list = statusMAP.get(seatLayoutDTO.getCode());
				}
				TicketDetailsDTO ticketDetailsDTO = new TicketDetailsDTO();
				ticketDetailsDTO.setSeatCode(seatLayoutDTO.getCode());
				ticketDetailsDTO.setTicketStatus(TicketStatusEM.TRIP_SEAT_QUOTA);
				ticketDetailsDTO.setSeatGendar(seatLayoutDTO.getSeatGendar());
				list.add(ticketDetailsDTO);
				statusMAP.put(seatLayoutDTO.getCode(), list);
			}
		}
		for (BusSeatLayoutDTO seatLayoutDTO : tripDTO.getBus().getBusSeatLayoutDTO().getList()) {
			List<TicketDetailsDTO> list = statusMAP.get(seatLayoutDTO.getCode());
			if (list != null && !list.isEmpty()) {
				for (TicketDetailsDTO ticketDetailsDTO : list) {
					String seatPos = seatLayoutDTO.getLayer() + "_" + seatLayoutDTO.getColPos() + "_" + seatLayoutDTO.getRowPos();

					// all Stage wise seat Gender
					if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() || ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId() || ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.TMP_BLOCKED_TICKET.getId() || ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.TRIP_SEAT_QUOTA.getId()) {
						if (seatGenderMAP.get(seatPos) == null) {
							List<SeatGendarEM> genderList = new ArrayList<SeatGendarEM>();
							genderList.add(ticketDetailsDTO.getSeatGendar());
							seatGenderMAP.put(seatPos, genderList);
						}
						else {
							List<SeatGendarEM> genderList = seatGenderMAP.get(seatPos);
							genderList.add(ticketDetailsDTO.getSeatGendar());
							seatGenderMAP.put(seatPos, genderList);
						}
					}
				}
			}
		}
		// Gender Validations
		for (BusSeatLayoutDTO seatLayoutDTO : tripDTO.getBus().getBusSeatLayoutDTO().getList()) {
			Integer colCount = seatLayoutDTO.getColPos();
			Integer rowCount = seatLayoutDTO.getRowPos();
			Integer orientation = seatLayoutDTO.getOrientation();
			Integer layer = seatLayoutDTO.getLayer();
			if (!isLayout1X1 && orientation == 0 && seatLayoutDTO.getSeatStatus().getId() == SeatStatusEM.AVAILABLE_ALL.getId()) {
				if (seatGenderMAP.get(layer + "_" + colCount + "_" + (rowCount + 1)) != null) {
					Integer seatGender = isSameGenderAllStage(seatGenderMAP.get(layer + "_" + colCount + "_" + (rowCount + 1)));
					seatLayoutDTO.setSeatStatus(seatGender == SeatStatusEM.AVAILABLE_MALE.getId() ? SeatStatusEM.AVAILABLE_MALE : seatGender == SeatStatusEM.AVAILABLE_FEMALE.getId() ? SeatStatusEM.AVAILABLE_FEMALE : seatGender == SeatStatusEM.BLOCKED.getId() ? SeatStatusEM.BLOCKED : SeatStatusEM.AVAILABLE_ALL);
					continue;
				}
				else if (seatGenderMAP.get(layer + "_" + colCount + "_" + (rowCount - 1)) != null) {
					Integer seatGender = isSameGenderAllStage(seatGenderMAP.get(layer + "_" + colCount + "_" + (rowCount - 1)));
					seatLayoutDTO.setSeatStatus(seatGender == SeatStatusEM.AVAILABLE_MALE.getId() ? SeatStatusEM.AVAILABLE_MALE : seatGender == SeatStatusEM.AVAILABLE_FEMALE.getId() ? SeatStatusEM.AVAILABLE_FEMALE : seatGender == SeatStatusEM.BLOCKED.getId() ? SeatStatusEM.BLOCKED : SeatStatusEM.AVAILABLE_ALL);
				}
			}
			if (!isLayout1X1 && orientation == 1 && seatLayoutDTO.getSeatStatus().getId() == SeatStatusEM.AVAILABLE_ALL.getId()) {
				if (seatGenderMAP.get(layer + "_" + (colCount + 1) + "_" + rowCount) != null) {
					Integer seatGender = isSameGenderAllStage(seatGenderMAP.get(layer + "_" + (colCount + 1) + "_" + rowCount));
					seatLayoutDTO.setSeatStatus(seatGender == SeatStatusEM.AVAILABLE_MALE.getId() ? SeatStatusEM.AVAILABLE_MALE : seatGender == SeatStatusEM.AVAILABLE_FEMALE.getId() ? SeatStatusEM.AVAILABLE_FEMALE : seatGender == SeatStatusEM.BLOCKED.getId() ? SeatStatusEM.BLOCKED : SeatStatusEM.AVAILABLE_ALL);
					continue;
				}
				else if (seatGenderMAP.get(layer + "_" + (colCount - 1) + "_" + rowCount) != null) {
					Integer seatGender = isSameGenderAllStage(seatGenderMAP.get(layer + "_" + (colCount - 1) + "_" + rowCount));
					seatLayoutDTO.setSeatStatus(seatGender == SeatStatusEM.AVAILABLE_MALE.getId() ? SeatStatusEM.AVAILABLE_MALE : seatGender == SeatStatusEM.AVAILABLE_FEMALE.getId() ? SeatStatusEM.AVAILABLE_FEMALE : seatGender == SeatStatusEM.BLOCKED.getId() ? SeatStatusEM.BLOCKED : SeatStatusEM.AVAILABLE_ALL);
				}
			}
		}
		// Gender Restriction Validations
		SeatGenderRestrictionEM gendarRestriction = authDTO.getNamespace().getProfile().getSeatGendarRestriction();
		if (gendarRestriction.getId() != SeatGenderRestrictionEM.SIMILAR_GENDER.getId()) {
			for (BusSeatLayoutDTO seatLayoutDTO : tripDTO.getBus().getBusSeatLayoutDTO().getList()) {
				if (gendarRestriction.getId() == SeatGenderRestrictionEM.FAMALE_SUPERIOR.getId() && seatLayoutDTO.getSeatStatus().getId() == SeatStatusEM.AVAILABLE_MALE.getId()) {
					seatLayoutDTO.setSeatStatus(SeatStatusEM.AVAILABLE_ALL);
				}
				else if (gendarRestriction.getId() == SeatGenderRestrictionEM.ANY_GENDER.getId() && (seatLayoutDTO.getSeatStatus().getId() == SeatStatusEM.AVAILABLE_MALE.getId() || seatLayoutDTO.getSeatStatus().getId() == SeatStatusEM.AVAILABLE_FEMALE.getId())) {
					seatLayoutDTO.setSeatStatus(SeatStatusEM.AVAILABLE_ALL);
				}
			}
		}
	}

	protected String getGeneratedTripStageCode(AuthDTO authDTO, ScheduleDTO scheduleDTO, TripDTO tripDTO, ScheduleStageDTO scheduleStageDTO) {
		return authDTO.getNamespace().getId() + "N" + scheduleDTO.getId() + "S" + DateUtil.getCompressDate(tripDTO.getTripDate()) + "D" + scheduleStageDTO.getFromStation().getId() + "T" + scheduleStageDTO.getToStation().getId();
	}

	protected String getGeneratedTripStageCode(AuthDTO authDTO, ScheduleDTO scheduleDTO, TripDTO tripDTO, StationDTO fromStationDTO, StationDTO toStationDTO) {
		return authDTO.getNamespace().getId() + "N" + scheduleDTO.getId() + "S" + DateUtil.getCompressDate(tripDTO.getTripDate()) + "D" + fromStationDTO.getId() + "T" + toStationDTO.getId();
	}

	protected String getGeneratedTripCode(AuthDTO authDTO, ScheduleDTO scheduleDTO, TripDTO tripDTO) {
		return authDTO.getNamespace().getId() + "N" + scheduleDTO.getId() + "S" + DateUtil.getCompressDate(tripDTO.getTripDate()) + "D";
	}

	public String getGeneratedTripCodeV2(AuthDTO authDTO, ScheduleDTO scheduleDTO, TripDTO tripDTO) {
		return getGeneratedTripCode(authDTO, scheduleDTO, tripDTO);
	}

	protected String getGeneratedTripStageCode(AuthDTO authDTO, ScheduleDTO scheduleDTO, SearchDTO searchDTO, StageDTO stageDTO) {
		return authDTO.getNamespace().getId() + "N" + scheduleDTO.getId() + "S" + DateUtil.getCompressDate(searchDTO.getTravelDate()) + "D" + stageDTO.getFromStation().getStation().getId() + "T" + stageDTO.getToStation().getStation().getId();
	}

	protected String getGeneratedTripCode(AuthDTO authDTO, ScheduleDTO scheduleDTO, SearchDTO searchDTO) {
		return authDTO.getNamespace().getId() + "N" + scheduleDTO.getId() + "S" + DateUtil.getCompressDate(searchDTO.getTravelDate()) + "D";
	}

	protected List<TripDTO> getUniqueTripList(List<TripDTO> nonUniqueAccountList) {
		Map<String, TripDTO> uniqueAccountsMapList = new HashMap<String, TripDTO>();
		if (nonUniqueAccountList != null && !nonUniqueAccountList.isEmpty()) {
			for (TripDTO nprDto : nonUniqueAccountList) {
				uniqueAccountsMapList.put(nprDto.getCode(), nprDto);
			}
		}
		return new ArrayList<TripDTO>(uniqueAccountsMapList.values());
	}

	protected int getStationTimeOverride(ScheduleTimeOverrideDTO timeOverrideDTO, int stationMinitues) {
		int finalStationMinitues = stationMinitues;
		if (timeOverrideDTO.getOverrideType().getId() == OverrideTypeEM.DECREASE_VALUE.getId()) {
			finalStationMinitues = stationMinitues - timeOverrideDTO.getOverrideMinutes();
		}
		else if (timeOverrideDTO.getOverrideType().getId() == OverrideTypeEM.INCREASE_VALUE.getId()) {
			finalStationMinitues = stationMinitues + timeOverrideDTO.getOverrideMinutes();
		}
		return finalStationMinitues;
	}

	protected BigDecimal applyFareAutoOverride(StageDTO stageDTO, double fare, List<ScheduleFareAutoOverrideDTO> fareOverrideDTOList, DateTime tripDate, BusSeatTypeEM busSeatTypeEM) {
		BigDecimal stageFare = new BigDecimal(fare);

		// Identify and remove the generic fare
		if (fareOverrideDTOList != null) {
			boolean groupSpecificFoundFlag = false;
			boolean seatTypeSpecificFoundFlag = false;
			boolean routeSpecificFoundFlag = false;
			for (ScheduleFareAutoOverrideDTO autoOverrideDTO : fareOverrideDTOList) {
				if (!groupSpecificFoundFlag) {
					groupSpecificFoundFlag = !autoOverrideDTO.getGroupList().isEmpty() ? true : false;
				}
				if (!seatTypeSpecificFoundFlag) {
					BusSeatTypeEM busSeatType = BitsUtil.existBusSeatType(autoOverrideDTO.getBusSeatType(), busSeatTypeEM);
					seatTypeSpecificFoundFlag = busSeatType != null ? true : false;
				}
				if (!routeSpecificFoundFlag) {
					routeSpecificFoundFlag = !autoOverrideDTO.getRouteList().isEmpty() ? true : false;
				}
			}
			for (Iterator<ScheduleFareAutoOverrideDTO> iterator = fareOverrideDTOList.iterator(); iterator.hasNext();) {
				ScheduleFareAutoOverrideDTO fareOverrideDTO = iterator.next();
				if (fareOverrideDTO.getGroupList().isEmpty() && groupSpecificFoundFlag) {
					iterator.remove();
					continue;
				}
				BusSeatTypeEM busSeatType = BitsUtil.existBusSeatType(fareOverrideDTO.getBusSeatType(), busSeatTypeEM);
				if (seatTypeSpecificFoundFlag && busSeatType == null) {
					iterator.remove();
					continue;
				}
				if (routeSpecificFoundFlag && fareOverrideDTO.getRouteList().isEmpty()) {
					iterator.remove();
					continue;
				}
			}
			// Sorting Trips
			Collections.sort(fareOverrideDTOList, new Comparator<ScheduleFareAutoOverrideDTO>() {
				@Override
				public int compare(ScheduleFareAutoOverrideDTO t1, ScheduleFareAutoOverrideDTO t2) {
					return new CompareToBuilder().append(t2.getActiveFrom(), t1.getActiveFrom()).append(t2.getActiveTo(), t1.getActiveTo()).toComparison();
				}
			});
			// Identify specific recent fare
			ScheduleFareAutoOverrideDTO recentScheduleFareAutoDTO = null;
			for (Iterator<ScheduleFareAutoOverrideDTO> iterator = fareOverrideDTOList.iterator(); iterator.hasNext();) {
				ScheduleFareAutoOverrideDTO fareOverrideDTO = iterator.next();
				if (recentScheduleFareAutoDTO == null) {
					recentScheduleFareAutoDTO = fareOverrideDTO;
					continue;
				}
				if (DateUtil.getDayDifferent(new DateTime(fareOverrideDTO.getActiveFrom()), new DateTime(fareOverrideDTO.getActiveTo())) > DateUtil.getDayDifferent(new DateTime(recentScheduleFareAutoDTO.getActiveFrom()), new DateTime(recentScheduleFareAutoDTO.getActiveTo()))) {
					iterator.remove();
					continue;
				}

			}
		}
		// Schedule Fare auto override
		if (fareOverrideDTOList != null && !fareOverrideDTOList.isEmpty()) {
			for (ScheduleFareAutoOverrideDTO fareAutoOverrideDTO : fareOverrideDTOList) {
				if (fareAutoOverrideDTO.getOverrideMinutes() != 0 && (DateUtil.getMinutiesDifferent(DateUtil.NOW(), DateUtil.addMinituesToDate(tripDate, stageDTO.getFromStation().getMinitues())) >= fareAutoOverrideDTO.getOverrideMinutes())) {
					continue;
				}
				if (fareAutoOverrideDTO.getRouteList().isEmpty() || existStageInRouteList(fareAutoOverrideDTO.getRouteList(), stageDTO.getFromStation().getStation(), stageDTO.getToStation().getStation())) {
					stageFare = fareAutoOverrideDTO.getFare();
				}
			}
		}
		return stageFare;
	}

	protected BigDecimal calculateSeatFare(ScheduleSeatFareDTO seatFareDTO, BigDecimal seatFare) {
		if (seatFareDTO.getFareOverrideType().getId() == FareOverrideTypeEM.FINAL_FARE.getId()) {
			seatFare = seatFareDTO.getSeatFare();
		}
		else if (seatFareDTO.getFareOverrideType().getId() == FareOverrideTypeEM.DECREASE_FARE.getId()) {
			if (seatFareDTO.getFareType().getId() == FareTypeEM.FLAT.getId()) {
				seatFare = seatFare.subtract(seatFareDTO.getSeatFare());
			}
			else if (seatFareDTO.getFareType().getId() == FareTypeEM.PERCENTAGE.getId()) {
				seatFare = seatFare.subtract(seatFare.multiply(seatFareDTO.getSeatFare()).divide(Numeric.ONE_HUNDRED, 2));
			}
		}
		else if (seatFareDTO.getFareOverrideType().getId() == FareOverrideTypeEM.INCREASE_FARE.getId()) {
			if (seatFareDTO.getFareType().getId() == FareTypeEM.FLAT.getId()) {
				seatFare = seatFare.add(seatFareDTO.getSeatFare());
			}
			else if (seatFareDTO.getFareType().getId() == FareTypeEM.PERCENTAGE.getId()) {
				seatFare = seatFare.add(seatFare.multiply(seatFareDTO.getSeatFare()).divide(Numeric.ONE_HUNDRED, 2));
			}
		}
		return seatFare;
	}

	protected List<ScheduleFareAutoOverrideDTO> getFareAutoOverrideList(AuthDTO authDTO, ScheduleDTO scheduleDTO, StageDTO stageDTO, BusSeatTypeEM busSeatType) {
		List<ScheduleFareAutoOverrideDTO> list = new ArrayList<ScheduleFareAutoOverrideDTO>();
		for (ScheduleFareAutoOverrideDTO fareAutoOverrideDTO : scheduleDTO.getFareAutoOverrideList()) {
			if (fareAutoOverrideDTO.getOverrideMinutes() != 0 && (DateUtil.getMinutiesDifferent(DateUtil.NOW(), DateUtil.addMinituesToDate(scheduleDTO.getTripDate(), stageDTO.getFromStation().getMinitues())) >= fareAutoOverrideDTO.getOverrideMinutes())) {
				continue;
			}
			BusSeatTypeEM busSeatTypeEM = BitsUtil.existBusSeatType(fareAutoOverrideDTO.getBusSeatType(), busSeatType);
			if (busSeatTypeEM == null) {
				continue;
			}
			if (!fareAutoOverrideDTO.getRouteList().isEmpty() && !existStageInRouteList(fareAutoOverrideDTO.getRouteList(), stageDTO.getFromStation().getStation(), stageDTO.getToStation().getStation())) {
				continue;
			}
			list.add(fareAutoOverrideDTO);
		}
		return list;
	}

	protected boolean existStageInRouteList(List<RouteDTO> routeList, StationDTO fromStationDTO, StationDTO toStationDTO) {
		boolean status = false;
		// Route List
		for (RouteDTO routeDTO : routeList) {
			if (routeDTO.getFromStation().getId() == fromStationDTO.getId() && routeDTO.getToStation().getId() == toStationDTO.getId()) {
				status = true;
				break;
			}
		}
		return status;
	}

	private Integer isSameGenderAllStage(List<SeatGendarEM> genderList) {
		Integer finalGender = null;
		for (SeatGendarEM gender : genderList) {
			finalGender = finalGender == null ? gender.getId() : gender.getId() == finalGender ? finalGender : SeatStatusEM.BLOCKED.getId();
		}
		return finalGender;
	}

}
