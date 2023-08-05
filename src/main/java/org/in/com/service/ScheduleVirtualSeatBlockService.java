package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleVirtualSeatBlockDTO;

public interface ScheduleVirtualSeatBlockService {
	public void updateScheduleVirtualSeatBlock(AuthDTO authDTO, ScheduleVirtualSeatBlockDTO scheduleVirtualSeatBlock);

	public List<ScheduleVirtualSeatBlockDTO> getScheduleVirtualSeatBlock(AuthDTO authDTO);
}
