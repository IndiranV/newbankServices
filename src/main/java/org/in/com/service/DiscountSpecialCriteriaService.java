package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.DiscountSpecialCriteriaDTO;

public interface DiscountSpecialCriteriaService {

	public DiscountSpecialCriteriaDTO updateSpecialDiscountCriteria(AuthDTO authDTO, DiscountSpecialCriteriaDTO discount);

	public List<DiscountSpecialCriteriaDTO> getAllSpecialDiscountCriteria(AuthDTO authDTO);

	public DiscountSpecialCriteriaDTO getSpecialDiscountCriteriaByCode(AuthDTO authDTO, DiscountSpecialCriteriaDTO discount);

	public List<DiscountSpecialCriteriaDTO> getSpecialDiscountCriteria(AuthDTO authDTO, ScheduleDTO schedule);

}
