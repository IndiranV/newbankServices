package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleEnrouteBookControlDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.TripSeatQuotaDTO;

public interface ScheduleEnrouteBookControlService {

	public ScheduleEnrouteBookControlDTO getScheduleEnrouteBookControl(AuthDTO authDTO, ScheduleEnrouteBookControlDTO scheduleEnrouteBookControl);

	public List<ScheduleEnrouteBookControlDTO> getAll(AuthDTO authDTO, ScheduleDTO scheduleDTO);

	public ScheduleEnrouteBookControlDTO Update(AuthDTO authDTO, ScheduleEnrouteBookControlDTO scheduleEnrouteBookControl);

	public void applyScheduleEnrouteBookControl(AuthDTO authDTO, TripDTO tripDTO);

	public List<ScheduleEnrouteBookControlDTO> getScheduleEnrouteBookControl(AuthDTO authDTO, ScheduleDTO scheduleDTO);

	public void applyScheduleEnrouteBookControl(AuthDTO authDTO, TripDTO tripDTO, List<ScheduleEnrouteBookControlDTO> scheduleEnrouteBookControlList, List<TicketDetailsDTO> ticketDetails, List<TripSeatQuotaDTO> tripSeatQuatoList);

}
