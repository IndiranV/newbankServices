package org.in.com.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.in.com.constants.Numeric;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.ScheduleSeatAutoReleaseDTO;
import org.in.com.dto.StageDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.dto.TicketExtraDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.enumeration.MinutesTypeEM;
import org.in.com.dto.enumeration.ReleaseModeEM;
import org.in.com.dto.enumeration.ReleaseTypeEM;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.service.CancelTicketService;
import org.in.com.service.ConfirmSeatsService;
import org.in.com.service.ScheduleBusService;
import org.in.com.service.ScheduleSeatAutoReleaseService;
import org.in.com.service.ScheduleTripService;
import org.in.com.service.TicketPhoneBookAutoReleaseService;
import org.in.com.service.TicketService;
import org.in.com.service.TripService;
import org.in.com.service.helper.HelperUtil;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hirondelle.date4j.DateTime;

@Service
public class TicketPhoneBookAutoReleaseServiceImpl extends HelperUtil implements TicketPhoneBookAutoReleaseService {
	@Autowired
	ScheduleSeatAutoReleaseService autoReleaseService;
	@Autowired
	TripService tripService;
	@Autowired
	ScheduleTripService scheduleTripService;
	@Autowired
	CancelTicketService cancelTicketService;
	@Autowired
	ScheduleBusService busService;
	@Autowired
	TicketService ticketService;
	@Autowired
	ConfirmSeatsService confirmSeatsService;

	public List<TicketDTO> releaseTicket(AuthDTO authDTO, TripDTO tripDTO) {

		System.out.println("TicketPhoneBookAutoReleaseService : " + tripDTO.getCode());

		tripDTO = scheduleTripService.getTripDetails(authDTO, tripDTO);
		tripService.getBookedBlockedSeats(authDTO, tripDTO);

		Map<Integer, List<ScheduleSeatAutoReleaseDTO>> groupSeatReleaseMap = new HashMap<>();
		DateTime now = DateTime.now(TimeZone.getDefault());
		for (Iterator<ScheduleSeatAutoReleaseDTO> itrSchedule = tripDTO.getSchedule().getSeatAutoReleaseList().iterator(); itrSchedule.hasNext();) {
			ScheduleSeatAutoReleaseDTO autoReleaseDTO = itrSchedule.next();

			if (autoReleaseDTO.getReleaseTypeEM().getId() != ReleaseTypeEM.RELEASE_PHONE.getId()) {
				itrSchedule.remove();
				continue;
			}
			if (autoReleaseDTO.getMinutesTypeEM().getId() == MinutesTypeEM.MINUTES.getId()) {
				if (autoReleaseDTO.getReleaseModeEM().getId() == ReleaseModeEM.RELEASE_SCHEDULE.getId() || autoReleaseDTO.getReleaseModeEM().getId() == ReleaseModeEM.RELEASE_STAGE.getId()) {
					int minutiesDiff = DateUtil.getMinutiesDifferent(now, tripDTO.getStage().getTravelDate());
					if (minutiesDiff > autoReleaseDTO.getReleaseMinutes()) {
						itrSchedule.remove();
						continue;
					}
				}
			}
			else if (autoReleaseDTO.getMinutesTypeEM().getId() == MinutesTypeEM.AM.getId() || autoReleaseDTO.getMinutesTypeEM().getId() == MinutesTypeEM.PM.getId()) {
				int minutiesDiff = DateUtil.getMinutiesDifferent(now, DateUtil.addMinituesToDate(tripDTO.getTripDate(), (autoReleaseDTO.getMinutesTypeEM().getId() == MinutesTypeEM.AM.getId()) ? autoReleaseDTO.getReleaseMinutes() : autoReleaseDTO.getReleaseMinutes() + 720));
				if (minutiesDiff > autoReleaseDTO.getReleaseMinutes()) {
					itrSchedule.remove();
					continue;
				}
			}
			if (autoReleaseDTO.getGroups() != null && !autoReleaseDTO.getGroups().isEmpty()) {
				for (GroupDTO groupDTO : autoReleaseDTO.getGroups()) {
					if (groupSeatReleaseMap.get(groupDTO.getId()) == null) {
						List<ScheduleSeatAutoReleaseDTO> scheduleSeatAutoReleaseList = new ArrayList<>();
						scheduleSeatAutoReleaseList.add(autoReleaseDTO);
						groupSeatReleaseMap.put(groupDTO.getId(), scheduleSeatAutoReleaseList);
					}
					else {
						List<ScheduleSeatAutoReleaseDTO> scheduleSeatAutoReleaseList = groupSeatReleaseMap.get(groupDTO.getId());
						scheduleSeatAutoReleaseList.add(autoReleaseDTO);
						groupSeatReleaseMap.put(groupDTO.getId(), scheduleSeatAutoReleaseList);
					}
				}
			}
			else {
				if (groupSeatReleaseMap.get(Numeric.ZERO_INT) == null) {
					List<ScheduleSeatAutoReleaseDTO> scheduleSeatAutoReleaseList = new ArrayList<>();
					scheduleSeatAutoReleaseList.add(autoReleaseDTO);
					groupSeatReleaseMap.put(Numeric.ZERO_INT, scheduleSeatAutoReleaseList);
				}
				else {
					List<ScheduleSeatAutoReleaseDTO> scheduleSeatAutoReleaseList = groupSeatReleaseMap.get(Numeric.ZERO_INT);
					scheduleSeatAutoReleaseList.add(autoReleaseDTO);
					groupSeatReleaseMap.put(Numeric.ZERO_INT, scheduleSeatAutoReleaseList);
				}
			}
		}

		List<TicketDetailsDTO> cancelSeatList = new ArrayList<TicketDetailsDTO>();
		for (TicketDetailsDTO detailsDTO : tripDTO.getTicketDetailsList()) {
			if (detailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
				detailsDTO.setUser(getUserDTOById(authDTO, detailsDTO.getUser()));

				// Validate PBL Block Live Time
				if (BitsUtil.validateBlockReleaseTime(detailsDTO.getBlockReleaseMinutes(), tripDTO.getTripDateTimeV2(), detailsDTO.getUpdatedAt())) {
					cancelSeatList.add(detailsDTO);
					continue;
				}

				// Validate Schedule Seat Auto Release
				if (groupSeatReleaseMap.get(detailsDTO.getUser().getGroup().getId()) != null) {
					List<ScheduleSeatAutoReleaseDTO> autoReleaseDTOList = groupSeatReleaseMap.get(detailsDTO.getUser().getGroup().getId());
					for (ScheduleSeatAutoReleaseDTO autoReleaseDTO : autoReleaseDTOList) {
						if (autoReleaseDTO.getMinutesTypeEM().getId() == MinutesTypeEM.MINUTES.getId() && autoReleaseDTO.getReleaseModeEM().getId() == ReleaseModeEM.RELEASE_SCHEDULE.getId()) {
							int minutiesDiff = DateUtil.getMinutiesDifferent(now, tripDTO.getStage().getTravelDate());
							if (minutiesDiff <= autoReleaseDTO.getReleaseMinutes()) {
								cancelSeatList.add(detailsDTO);
							}
						}
						else if (autoReleaseDTO.getMinutesTypeEM().getId() == MinutesTypeEM.MINUTES.getId() && autoReleaseDTO.getReleaseModeEM().getId() == ReleaseModeEM.RELEASE_STAGE.getId()) {
							for (StageDTO stageDTO : tripDTO.getStageList()) {
								if (stageDTO.getFromStation().getStation().getId() == detailsDTO.getFromStation().getId() && stageDTO.getToStation().getStation().getId() == detailsDTO.getToStation().getId()) {
									int minutiesDiff = DateUtil.getMinutiesDifferent(now, DateUtil.addMinituesToDate(tripDTO.getTripDate(), stageDTO.getFromStation().getMinitues()));
									if (minutiesDiff <= autoReleaseDTO.getReleaseMinutes()) {
										cancelSeatList.add(detailsDTO);
									}
								}
							}
						}
						else if ((autoReleaseDTO.getMinutesTypeEM().getId() == MinutesTypeEM.AM.getId() || autoReleaseDTO.getMinutesTypeEM().getId() == MinutesTypeEM.PM.getId()) && autoReleaseDTO.getReleaseModeEM().getId() == ReleaseModeEM.RELEASE_STAGE.getId()) {
							cancelSeatList.add(detailsDTO);
						}
					}
				}
				else if (groupSeatReleaseMap.get(Numeric.ZERO_INT) != null) {
					List<ScheduleSeatAutoReleaseDTO> autoReleaseDTOList = groupSeatReleaseMap.get(Numeric.ZERO_INT);
					for (ScheduleSeatAutoReleaseDTO autoReleaseDTO : autoReleaseDTOList) {
						if (autoReleaseDTO.getMinutesTypeEM().getId() == MinutesTypeEM.MINUTES.getId() && autoReleaseDTO.getReleaseModeEM().getId() == ReleaseModeEM.RELEASE_SCHEDULE.getId()) {
							int minutiesDiff = DateUtil.getMinutiesDifferent(now, tripDTO.getStage().getTravelDate());
							if (minutiesDiff <= autoReleaseDTO.getReleaseMinutes()) {
								cancelSeatList.add(detailsDTO);
							}
						}
						else if (autoReleaseDTO.getMinutesTypeEM().getId() == MinutesTypeEM.MINUTES.getId() && autoReleaseDTO.getReleaseModeEM().getId() == ReleaseModeEM.RELEASE_STAGE.getId()) {
							for (StageDTO stageDTO : tripDTO.getStageList()) {
								if (stageDTO.getFromStation().getStation().getId() == detailsDTO.getFromStation().getId() && stageDTO.getToStation().getStation().getId() == detailsDTO.getToStation().getId()) {
									int minutiesDiff = DateUtil.getMinutiesDifferent(now, DateUtil.addMinituesToDate(tripDTO.getTripDate(), stageDTO.getFromStation().getMinitues()));
									if (minutiesDiff <= autoReleaseDTO.getReleaseMinutes()) {
										cancelSeatList.add(detailsDTO);
									}
								}
							}
						}
						else if ((autoReleaseDTO.getMinutesTypeEM().getId() == MinutesTypeEM.AM.getId() || autoReleaseDTO.getMinutesTypeEM().getId() == MinutesTypeEM.PM.getId()) && autoReleaseDTO.getReleaseModeEM().getId() == ReleaseModeEM.RELEASE_SCHEDULE.getId()) {
							int minutiesDiff = DateUtil.getMinutiesDifferent(now, DateUtil.addMinituesToDate(tripDTO.getTripDate(), (autoReleaseDTO.getMinutesTypeEM().getId() == MinutesTypeEM.AM.getId()) ? autoReleaseDTO.getReleaseMinutes() : autoReleaseDTO.getReleaseMinutes() + 720));
							if (minutiesDiff <= 0) {
								cancelSeatList.add(detailsDTO);
							}
						}
						else if ((autoReleaseDTO.getMinutesTypeEM().getId() == MinutesTypeEM.AM.getId() || autoReleaseDTO.getMinutesTypeEM().getId() == MinutesTypeEM.PM.getId()) && autoReleaseDTO.getReleaseModeEM().getId() == ReleaseModeEM.RELEASE_STAGE.getId()) {
							int minutiesDiff = DateUtil.getMinutiesDifferent(now, DateUtil.addMinituesToDate(tripDTO.getTripDate(), (autoReleaseDTO.getMinutesTypeEM().getId() == MinutesTypeEM.AM.getId()) ? autoReleaseDTO.getReleaseMinutes() : autoReleaseDTO.getReleaseMinutes() + 720));
							if (minutiesDiff <= 0) {
								cancelSeatList.add(detailsDTO);
							}
						}
					}
				}
			}
		}
		// Seat to Ticket conversion
		Map<String, TicketDTO> ticketMap = new HashMap<String, TicketDTO>();
		for (TicketDetailsDTO detailsDTO : cancelSeatList) {
			TicketDTO ticketDTO = null;
			if (ticketMap.get(detailsDTO.getTicketCode()) == null) {
				ticketDTO = new TicketDTO();
				List<TicketDetailsDTO> seatDetailsList = new ArrayList<TicketDetailsDTO>();
				ticketDTO.setTicketDetails(seatDetailsList);
			}
			else {
				ticketDTO = ticketMap.get(detailsDTO.getTicketCode());
			}
			ticketDTO.setCode(detailsDTO.getTicketCode());
			ticketDTO.getTicketDetails().add(detailsDTO);
			ticketMap.put(detailsDTO.getTicketCode(), ticketDTO);
		}

		// Phone booked ticket cancel
		List<TicketDTO> ticketList = new ArrayList<TicketDTO>();
		for (TicketDTO ticketDTO : ticketMap.values()) {
			try {
				ticketDTO.setOverideFlag(true);
				System.out.println("TicketPhoneBookAutoReleaseService PNR : " + ticketDTO.getCode());
				cancelTicketService.cancelPhoneBooking(authDTO, ticketDTO);
				ticketList.add(ticketDTO);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return ticketList;
	}

	@Override
	public List<TicketDTO> confirmAndReleasePhoneBlockTicket(AuthDTO authDTO, DateTime tripDate) {
		List<TicketDTO> ticketList = new ArrayList<TicketDTO>();
		List<TripDTO> trips = tripService.getTripsByTripDate(authDTO, tripDate);
		for (TripDTO tripDTO : trips) {
			try {
				tripDTO = scheduleTripService.getTripDetails(authDTO, tripDTO);
				if (tripDTO.getSchedule().getSeatAutoReleaseList().isEmpty()) {
					continue;
				}
				System.out.println("TicketPhoneBookAutoReleaseService : " + tripDTO.getCode());
				tripService.getBookedBlockedSeats(authDTO, tripDTO);

				Map<String, List<ScheduleSeatAutoReleaseDTO>> groupSeatReleaseMap = new HashMap<>();
				Map<String, List<ScheduleSeatAutoReleaseDTO>> groupSeatBookMap = new HashMap<>();
				DateTime now = DateTime.now(TimeZone.getDefault());
				for (Iterator<ScheduleSeatAutoReleaseDTO> itrSchedule = tripDTO.getSchedule().getSeatAutoReleaseList().iterator(); itrSchedule.hasNext();) {
					ScheduleSeatAutoReleaseDTO autoReleaseDTO = itrSchedule.next();

					if (autoReleaseDTO.getReleaseTypeEM().getId() != ReleaseTypeEM.RELEASE_PHONE.getId() && autoReleaseDTO.getReleaseTypeEM().getId() != ReleaseTypeEM.CONFIRM_PHONE.getId()) {
						itrSchedule.remove();
						continue;
					}
					if (autoReleaseDTO.getMinutesTypeEM().getId() == MinutesTypeEM.MINUTES.getId()) {
						if (autoReleaseDTO.getReleaseModeEM().getId() == ReleaseModeEM.RELEASE_SCHEDULE.getId() || autoReleaseDTO.getReleaseModeEM().getId() == ReleaseModeEM.RELEASE_STAGE.getId()) {
							int minutiesDiff = DateUtil.getMinutiesDifferent(now, tripDTO.getStage().getTravelDate());
							if (minutiesDiff > autoReleaseDTO.getReleaseMinutes()) {
								itrSchedule.remove();
								continue;
							}
						}
					}
					else if (autoReleaseDTO.getMinutesTypeEM().getId() == MinutesTypeEM.AM.getId() || autoReleaseDTO.getMinutesTypeEM().getId() == MinutesTypeEM.PM.getId()) {
						int minutiesDiff = DateUtil.getMinutiesDifferent(now, DateUtil.addMinituesToDate(tripDTO.getTripDate(), (autoReleaseDTO.getMinutesTypeEM().getId() == MinutesTypeEM.AM.getId()) ? autoReleaseDTO.getReleaseMinutes() : autoReleaseDTO.getReleaseMinutes() + 720));
						if (minutiesDiff > autoReleaseDTO.getReleaseMinutes()) {
							itrSchedule.remove();
							continue;
						}
					}
					if (autoReleaseDTO.getReleaseTypeEM().getId() == ReleaseTypeEM.RELEASE_PHONE.getId() && autoReleaseDTO.getGroups() != null && !autoReleaseDTO.getGroups().isEmpty()) {
						for (GroupDTO groupDTO : autoReleaseDTO.getGroups()) {
							if (groupSeatReleaseMap.get(groupDTO.getId() + "_RLEASE") == null) {
								List<ScheduleSeatAutoReleaseDTO> scheduleSeatAutoReleaseList = new ArrayList<>();
								scheduleSeatAutoReleaseList.add(autoReleaseDTO);
								groupSeatReleaseMap.put(groupDTO.getId() + "_RLEASE", scheduleSeatAutoReleaseList);
							}
							else {
								List<ScheduleSeatAutoReleaseDTO> scheduleSeatAutoReleaseList = groupSeatReleaseMap.get(groupDTO.getId() + "_RLEASE");
								scheduleSeatAutoReleaseList.add(autoReleaseDTO);
								groupSeatReleaseMap.put(groupDTO.getId() + "_RLEASE", scheduleSeatAutoReleaseList);
							}
						}
					}
					else if (autoReleaseDTO.getReleaseTypeEM().getId() == ReleaseTypeEM.RELEASE_PHONE.getId()) {
						if (groupSeatReleaseMap.get(Numeric.ZERO + "_RLEASE") == null) {
							List<ScheduleSeatAutoReleaseDTO> scheduleSeatAutoReleaseList = new ArrayList<>();
							scheduleSeatAutoReleaseList.add(autoReleaseDTO);
							groupSeatReleaseMap.put(Numeric.ZERO + "_RLEASE", scheduleSeatAutoReleaseList);
						}
						else {
							List<ScheduleSeatAutoReleaseDTO> scheduleSeatAutoReleaseList = groupSeatReleaseMap.get(Numeric.ZERO + "_RLEASE");
							scheduleSeatAutoReleaseList.add(autoReleaseDTO);
							groupSeatReleaseMap.put(Numeric.ZERO + "_RLEASE", scheduleSeatAutoReleaseList);
						}
					}
					else if (autoReleaseDTO.getReleaseTypeEM().getId() == ReleaseTypeEM.CONFIRM_PHONE.getId() && autoReleaseDTO.getGroups() != null && !autoReleaseDTO.getGroups().isEmpty()) {
						for (GroupDTO groupDTO : autoReleaseDTO.getGroups()) {
							if (groupSeatBookMap.get(groupDTO.getId() + "_BOOK") == null) {
								List<ScheduleSeatAutoReleaseDTO> scheduleSeatAutoReleaseList = new ArrayList<>();
								scheduleSeatAutoReleaseList.add(autoReleaseDTO);
								groupSeatBookMap.put(groupDTO.getId() + "_BOOK", scheduleSeatAutoReleaseList);
							}
							else {
								List<ScheduleSeatAutoReleaseDTO> scheduleSeatAutoReleaseList = groupSeatBookMap.get(groupDTO.getId() + "_BOOK");
								scheduleSeatAutoReleaseList.add(autoReleaseDTO);
								groupSeatBookMap.put(groupDTO.getId() + "_BOOK", scheduleSeatAutoReleaseList);
							}
						}
					}
					else if (autoReleaseDTO.getReleaseTypeEM().getId() == ReleaseTypeEM.CONFIRM_PHONE.getId()) {
						if (groupSeatBookMap.get(Numeric.ZERO + "_BOOK") == null) {
							List<ScheduleSeatAutoReleaseDTO> scheduleSeatAutoReleaseList = new ArrayList<>();
							scheduleSeatAutoReleaseList.add(autoReleaseDTO);
							groupSeatBookMap.put(Numeric.ZERO + "_BOOK", scheduleSeatAutoReleaseList);
						}
						else {
							List<ScheduleSeatAutoReleaseDTO> scheduleSeatAutoReleaseList = groupSeatBookMap.get(Numeric.ZERO + "_BOOK");
							scheduleSeatAutoReleaseList.add(autoReleaseDTO);
							groupSeatBookMap.put(Numeric.ZERO + "_BOOK", scheduleSeatAutoReleaseList);
						}
					}
				}

				List<TicketDetailsDTO> cancelSeatList = new ArrayList<TicketDetailsDTO>();
				List<TicketDetailsDTO> confirmSeatList = new ArrayList<TicketDetailsDTO>();
				List<String> linkPayTickets = new ArrayList<>();
				for (TicketDetailsDTO detailsDTO : tripDTO.getTicketDetailsList()) {
					if (detailsDTO.getTicketStatus().getId() != TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
						continue;
					}
					detailsDTO.setUser(getUserDTOById(authDTO, detailsDTO.getUser()));

					// Validate PBL Block Live Time
					if (BitsUtil.validateBlockReleaseTime(detailsDTO.getBlockReleaseMinutes(), tripDTO.getTripDateTimeV2(), detailsDTO.getUpdatedAt())) {
						cancelSeatList.add(detailsDTO);
						continue;
					}

					if (linkPayTickets.isEmpty() || !linkPayTickets.contains(detailsDTO.getTicketCode())) {
						TicketDTO ticketDTO = new TicketDTO();
						ticketDTO.setCode(detailsDTO.getTicketCode());
						TicketExtraDTO ticketExtraDTO = ticketService.getTicketExtra(authDTO, ticketDTO);
						if (StringUtil.isNotNull(ticketExtraDTO.getLinkPay())) {
							linkPayTickets.add(detailsDTO.getTicketCode());
							continue;
						}
					}

					// Validate Schedule Seat Auto Release
					detailsDTO.setUser(getUserDTOById(authDTO, detailsDTO.getUser()));
					if (groupSeatReleaseMap.get(detailsDTO.getUser().getGroup().getId() + "_RLEASE") != null) {
						List<ScheduleSeatAutoReleaseDTO> autoReleaseDTOList = groupSeatReleaseMap.get(detailsDTO.getUser().getGroup().getId() + "_RLEASE");
						for (ScheduleSeatAutoReleaseDTO autoReleaseDTO : autoReleaseDTOList) {
							if (autoReleaseDTO.getMinutesTypeEM().getId() == MinutesTypeEM.MINUTES.getId() && autoReleaseDTO.getReleaseModeEM().getId() == ReleaseModeEM.RELEASE_SCHEDULE.getId()) {
								int minutiesDiff = DateUtil.getMinutiesDifferent(now, tripDTO.getStage().getTravelDate());
								if (minutiesDiff <= autoReleaseDTO.getReleaseMinutes()) {
									cancelSeatList.add(detailsDTO);
								}
							}
							else if (autoReleaseDTO.getMinutesTypeEM().getId() == MinutesTypeEM.MINUTES.getId() && autoReleaseDTO.getReleaseModeEM().getId() == ReleaseModeEM.RELEASE_STAGE.getId()) {
								for (StageDTO stageDTO : tripDTO.getStageList()) {
									if (stageDTO.getFromStation().getStation().getId() == detailsDTO.getFromStation().getId() && stageDTO.getToStation().getStation().getId() == detailsDTO.getToStation().getId()) {
										int minutiesDiff = DateUtil.getMinutiesDifferent(now, DateUtil.addMinituesToDate(tripDTO.getTripDate(), stageDTO.getFromStation().getMinitues()));
										if (minutiesDiff <= autoReleaseDTO.getReleaseMinutes()) {
											cancelSeatList.add(detailsDTO);
										}
									}
								}
							}
							else if ((autoReleaseDTO.getMinutesTypeEM().getId() == MinutesTypeEM.AM.getId() || autoReleaseDTO.getMinutesTypeEM().getId() == MinutesTypeEM.PM.getId()) && autoReleaseDTO.getReleaseModeEM().getId() == ReleaseModeEM.RELEASE_STAGE.getId()) {
								cancelSeatList.add(detailsDTO);
							}
						}
					}
					else if (groupSeatReleaseMap.get(Numeric.ZERO_INT + "_RLEASE") != null) {
						List<ScheduleSeatAutoReleaseDTO> autoReleaseDTOList = groupSeatReleaseMap.get(Numeric.ZERO_INT + "_RLEASE");
						for (ScheduleSeatAutoReleaseDTO autoReleaseDTO : autoReleaseDTOList) {
							if (autoReleaseDTO.getMinutesTypeEM().getId() == MinutesTypeEM.MINUTES.getId() && autoReleaseDTO.getReleaseModeEM().getId() == ReleaseModeEM.RELEASE_SCHEDULE.getId()) {
								int minutiesDiff = DateUtil.getMinutiesDifferent(now, tripDTO.getStage().getTravelDate());
								if (minutiesDiff <= autoReleaseDTO.getReleaseMinutes()) {
									cancelSeatList.add(detailsDTO);
								}
							}
							else if (autoReleaseDTO.getMinutesTypeEM().getId() == MinutesTypeEM.MINUTES.getId() && autoReleaseDTO.getReleaseModeEM().getId() == ReleaseModeEM.RELEASE_STAGE.getId()) {
								for (StageDTO stageDTO : tripDTO.getStageList()) {
									if (stageDTO.getFromStation().getStation().getId() == detailsDTO.getFromStation().getId() && stageDTO.getToStation().getStation().getId() == detailsDTO.getToStation().getId()) {
										int minutiesDiff = DateUtil.getMinutiesDifferent(now, DateUtil.addMinituesToDate(tripDTO.getTripDate(), stageDTO.getFromStation().getMinitues()));
										if (minutiesDiff <= autoReleaseDTO.getReleaseMinutes()) {
											cancelSeatList.add(detailsDTO);
										}
									}
								}
							}
							else if ((autoReleaseDTO.getMinutesTypeEM().getId() == MinutesTypeEM.AM.getId() || autoReleaseDTO.getMinutesTypeEM().getId() == MinutesTypeEM.PM.getId()) && autoReleaseDTO.getReleaseModeEM().getId() == ReleaseModeEM.RELEASE_SCHEDULE.getId()) {
								int minutiesDiff = DateUtil.getMinutiesDifferent(now, DateUtil.addMinituesToDate(tripDTO.getTripDate(), (autoReleaseDTO.getMinutesTypeEM().getId() == MinutesTypeEM.AM.getId()) ? autoReleaseDTO.getReleaseMinutes() : autoReleaseDTO.getReleaseMinutes() + 720));
								if (minutiesDiff <= 0) {
									cancelSeatList.add(detailsDTO);
								}
							}
							else if ((autoReleaseDTO.getMinutesTypeEM().getId() == MinutesTypeEM.AM.getId() || autoReleaseDTO.getMinutesTypeEM().getId() == MinutesTypeEM.PM.getId()) && autoReleaseDTO.getReleaseModeEM().getId() == ReleaseModeEM.RELEASE_STAGE.getId()) {
								int minutiesDiff = DateUtil.getMinutiesDifferent(now, DateUtil.addMinituesToDate(tripDTO.getTripDate(), (autoReleaseDTO.getMinutesTypeEM().getId() == MinutesTypeEM.AM.getId()) ? autoReleaseDTO.getReleaseMinutes() : autoReleaseDTO.getReleaseMinutes() + 720));
								if (minutiesDiff <= 0) {
									cancelSeatList.add(detailsDTO);
								}
							}
						}
					}

					if (groupSeatBookMap.get(detailsDTO.getUser().getGroup().getId() + "_BOOK") != null) {
						List<ScheduleSeatAutoReleaseDTO> autoReleaseDTOList = groupSeatBookMap.get(detailsDTO.getUser().getGroup().getId() + "_BOOK");
						for (ScheduleSeatAutoReleaseDTO autoReleaseDTO : autoReleaseDTOList) {
							if (autoReleaseDTO.getMinutesTypeEM().getId() == MinutesTypeEM.MINUTES.getId() && autoReleaseDTO.getReleaseModeEM().getId() == ReleaseModeEM.RELEASE_SCHEDULE.getId()) {
								int minutiesDiff = DateUtil.getMinutiesDifferent(now, tripDTO.getStage().getTravelDate());
								if (minutiesDiff <= autoReleaseDTO.getReleaseMinutes()) {
									confirmSeatList.add(detailsDTO);
								}
							}
							else if (autoReleaseDTO.getMinutesTypeEM().getId() == MinutesTypeEM.MINUTES.getId() && autoReleaseDTO.getReleaseModeEM().getId() == ReleaseModeEM.RELEASE_STAGE.getId()) {
								for (StageDTO stageDTO : tripDTO.getStageList()) {
									if (stageDTO.getFromStation().getStation().getId() == detailsDTO.getFromStation().getId() && stageDTO.getToStation().getStation().getId() == detailsDTO.getToStation().getId()) {
										int minutiesDiff = DateUtil.getMinutiesDifferent(now, DateUtil.addMinituesToDate(tripDTO.getTripDate(), stageDTO.getFromStation().getMinitues()));
										if (minutiesDiff <= autoReleaseDTO.getReleaseMinutes()) {
											confirmSeatList.add(detailsDTO);
										}
									}
								}
							}
							else if ((autoReleaseDTO.getMinutesTypeEM().getId() == MinutesTypeEM.AM.getId() || autoReleaseDTO.getMinutesTypeEM().getId() == MinutesTypeEM.PM.getId()) && autoReleaseDTO.getReleaseModeEM().getId() == ReleaseModeEM.RELEASE_STAGE.getId()) {
								confirmSeatList.add(detailsDTO);
							}
						}
					}
					else if (groupSeatBookMap.get(Numeric.ZERO_INT + "_BOOK") != null) {
						List<ScheduleSeatAutoReleaseDTO> autoReleaseDTOList = groupSeatBookMap.get(Numeric.ZERO_INT + "_BOOK");
						for (ScheduleSeatAutoReleaseDTO autoReleaseDTO : autoReleaseDTOList) {
							if (autoReleaseDTO.getMinutesTypeEM().getId() == MinutesTypeEM.MINUTES.getId() && autoReleaseDTO.getReleaseModeEM().getId() == ReleaseModeEM.RELEASE_SCHEDULE.getId()) {
								int minutiesDiff = DateUtil.getMinutiesDifferent(now, tripDTO.getStage().getTravelDate());
								if (minutiesDiff <= autoReleaseDTO.getReleaseMinutes()) {
									confirmSeatList.add(detailsDTO);
								}
							}
							else if (autoReleaseDTO.getMinutesTypeEM().getId() == MinutesTypeEM.MINUTES.getId() && autoReleaseDTO.getReleaseModeEM().getId() == ReleaseModeEM.RELEASE_STAGE.getId()) {
								for (StageDTO stageDTO : tripDTO.getStageList()) {
									if (stageDTO.getFromStation().getStation().getId() == detailsDTO.getFromStation().getId() && stageDTO.getToStation().getStation().getId() == detailsDTO.getToStation().getId()) {
										int minutiesDiff = DateUtil.getMinutiesDifferent(now, DateUtil.addMinituesToDate(tripDTO.getTripDate(), stageDTO.getFromStation().getMinitues()));
										if (minutiesDiff <= autoReleaseDTO.getReleaseMinutes()) {
											confirmSeatList.add(detailsDTO);
										}
									}
								}
							}
							else if ((autoReleaseDTO.getMinutesTypeEM().getId() == MinutesTypeEM.AM.getId() || autoReleaseDTO.getMinutesTypeEM().getId() == MinutesTypeEM.PM.getId()) && autoReleaseDTO.getReleaseModeEM().getId() == ReleaseModeEM.RELEASE_SCHEDULE.getId()) {
								int minutiesDiff = DateUtil.getMinutiesDifferent(now, DateUtil.addMinituesToDate(tripDTO.getTripDate(), (autoReleaseDTO.getMinutesTypeEM().getId() == MinutesTypeEM.AM.getId()) ? autoReleaseDTO.getReleaseMinutes() : autoReleaseDTO.getReleaseMinutes() + 720));
								if (minutiesDiff <= 0) {
									confirmSeatList.add(detailsDTO);
								}
							}
							else if ((autoReleaseDTO.getMinutesTypeEM().getId() == MinutesTypeEM.AM.getId() || autoReleaseDTO.getMinutesTypeEM().getId() == MinutesTypeEM.PM.getId()) && autoReleaseDTO.getReleaseModeEM().getId() == ReleaseModeEM.RELEASE_STAGE.getId()) {
								int minutiesDiff = DateUtil.getMinutiesDifferent(now, DateUtil.addMinituesToDate(tripDTO.getTripDate(), (autoReleaseDTO.getMinutesTypeEM().getId() == MinutesTypeEM.AM.getId()) ? autoReleaseDTO.getReleaseMinutes() : autoReleaseDTO.getReleaseMinutes() + 720));
								if (minutiesDiff <= 0) {
									confirmSeatList.add(detailsDTO);
								}
							}
						}
					}
				}

				/** Book Seat to Ticket conversion */
				Map<String, TicketDTO> confirmTicketMap = new HashMap<String, TicketDTO>();
				for (TicketDetailsDTO detailsDTO : confirmSeatList) {
					TicketDTO ticketDTO = null;
					if (confirmTicketMap.get(detailsDTO.getTicketCode()) == null) {
						ticketDTO = new TicketDTO();
						List<TicketDetailsDTO> seatDetailsList = new ArrayList<TicketDetailsDTO>();
						ticketDTO.setTicketDetails(seatDetailsList);
					}
					else {
						ticketDTO = confirmTicketMap.get(detailsDTO.getTicketCode());
					}
					ticketDTO.setCode(detailsDTO.getTicketCode());
					ticketDTO.getTicketDetails().add(detailsDTO);
					confirmTicketMap.put(detailsDTO.getTicketCode(), ticketDTO);
				}

				/** Phone booked ticket confirm */
				for (TicketDTO ticketDTO : confirmTicketMap.values()) {
					try {
						ticketDTO.setOverideFlag(true);
						System.out.println("TicketPhoneBookAutoReleaseService - Confirm PNR : " + ticketDTO.getCode());
						confirmSeatsService.confirmPhoneBooking(authDTO, ticketDTO);
						ticketList.add(ticketDTO);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
				/** Cancel Seat to Ticket conversion */
				Map<String, TicketDTO> cancelTicketMap = new HashMap<String, TicketDTO>();
				for (TicketDetailsDTO detailsDTO : cancelSeatList) {
					TicketDTO ticketDTO = null;
					if (cancelTicketMap.get(detailsDTO.getTicketCode()) == null) {
						ticketDTO = new TicketDTO();
						List<TicketDetailsDTO> seatDetailsList = new ArrayList<TicketDetailsDTO>();
						ticketDTO.setTicketDetails(seatDetailsList);
					}
					else {
						ticketDTO = cancelTicketMap.get(detailsDTO.getTicketCode());
					}
					ticketDTO.setCode(detailsDTO.getTicketCode());
					ticketDTO.getTicketDetails().add(detailsDTO);
					cancelTicketMap.put(detailsDTO.getTicketCode(), ticketDTO);
				}

				/** Phone booked ticket cancel */
				for (TicketDTO ticketDTO : cancelTicketMap.values()) {
					try {
						ticketDTO.setOverideFlag(true);
						System.out.println("TicketPhoneBookAutoReleaseService PNR : " + ticketDTO.getCode());
						cancelTicketService.cancelPhoneBooking(authDTO, ticketDTO);
						ticketList.add(ticketDTO);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				System.out.println("TicketPhoneBookAutoConfirmService Failed : " + tripDTO.getCode());
			}
		}
		return ticketList;
	}
}
