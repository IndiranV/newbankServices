package org.in.com.service;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleBusDTO;
import org.in.com.dto.ScheduleCancellationTermDTO;
import org.in.com.dto.ScheduleControlDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleStageDTO;
import org.in.com.dto.ScheduleStationDTO;
import org.in.com.dto.ScheduleStationPointDTO;

public interface ScheduleActivityService {
	public boolean scheduleActivity(AuthDTO authDTO, ScheduleDTO scheduleDTO);

	public void scheduleStationActivity(AuthDTO authDTO, ScheduleStationDTO scheduleStationDTO);

	public void scheduleStationPointActivity(AuthDTO authDTO, ScheduleStationPointDTO scheduleStationPointDTO);

	public void scheduleBusActivity(AuthDTO authDTO, ScheduleBusDTO scheduleBusDTO);

	public void scheduleStageActivity(AuthDTO authDTO, ScheduleStageDTO scheduleStageDTO);

	public void scheduleCancellationTermsActivity(AuthDTO authDTO, ScheduleCancellationTermDTO scheduleCancellationTermDTO);

	public void scheduleBookingControlActivity(AuthDTO authDTO, ScheduleControlDTO scheduleControlDTO);
}
