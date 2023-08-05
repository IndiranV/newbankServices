package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.ScheduleBusDTO;
import org.in.com.dto.ScheduleDTO;

public interface ScheduleBusService {
	public List<ScheduleBusDTO> get(AuthDTO authDTO, ScheduleBusDTO dto);

	public ScheduleBusDTO Update(AuthDTO authDTO, ScheduleBusDTO dto);

	public ScheduleBusDTO getByScheduleId(AuthDTO authDTO, ScheduleDTO scheduleDTO);

	public ScheduleBusDTO getActiveScheduleBus(AuthDTO authDTO, ScheduleDTO scheduleDTO);

	public void checkScheduleBusmapChange(AuthDTO authDTO, ScheduleDTO scheduleDTO);

	public List<ScheduleDTO> getScheduleByBus(AuthDTO authDTO, BusDTO busDTO);
}
