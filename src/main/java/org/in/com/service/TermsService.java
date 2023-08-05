package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.TermDTO;

public interface TermsService extends BaseService<TermDTO> {

	List<TermDTO> getTermsAndConditions(AuthDTO authDTO, TermDTO termDTO,ScheduleDTO scheduleDTO);

	List<TermDTO> getTermsAndConditions(AuthDTO authDTO,String tagValue);

}
