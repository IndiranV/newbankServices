package org.in.com.service;

import java.util.Collection;
import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.BusSeatLayoutDTO;
import org.in.com.dto.BusTypeCategoryDTO;

public interface BusService extends BaseService<BusDTO> {
	
	public BusDTO getBus(AuthDTO authDTO, BusDTO busDTO);
	
	public String getBusCategoryByCode(String categoryCode);

	public String getBusCategoryUsingEM(String categoryCode);

	public Collection<?> getBusSeatType();

	public List<BusTypeCategoryDTO> getBusTypeCategory();

	public Collection<BusSeatLayoutDTO> getBusLayout(AuthDTO authDTO, BusDTO dto);

	public Collection<BusSeatLayoutDTO> getUpdateLayout(AuthDTO authDTO, BusDTO dto, List<BusSeatLayoutDTO> layoutList);

	public void UpdateSeatSequence(AuthDTO authDTO, BusDTO busDTO, List<BusSeatLayoutDTO> busDTOList);

}
