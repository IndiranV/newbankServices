package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.CancellationTermDTO;
import org.in.com.dto.ScheduleCancellationTermDTO;
import org.in.com.dto.ScheduleDTO;

public interface ScheduleCancellationTermService {
	public List<ScheduleCancellationTermDTO> get(AuthDTO authDTO, ScheduleCancellationTermDTO dto);

	public ScheduleCancellationTermDTO Update(AuthDTO authDTO, ScheduleCancellationTermDTO cancellationTermDTO);

	public boolean CheckCancellationTermUsed(AuthDTO authDTO, CancellationTermDTO dto);

	public ScheduleCancellationTermDTO getByScheduleId(AuthDTO authDTO, ScheduleDTO scheduleDTO);

}
