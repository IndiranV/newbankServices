package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleDiscountDTO;

public interface ScheduleDiscountService {
	public List<ScheduleDiscountDTO> get(AuthDTO authDTO);

	public boolean Update(AuthDTO authDTO, ScheduleDiscountDTO dto);

	public ScheduleDiscountDTO getByScheduleId(AuthDTO authDTO, ScheduleDTO scheduleDTO);

}
