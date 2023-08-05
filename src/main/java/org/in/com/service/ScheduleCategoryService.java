package org.in.com.service;

import java.util.List;
import java.util.Map;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleCategoryDTO;

public interface ScheduleCategoryService {

	public List<ScheduleCategoryDTO> getAll(AuthDTO authDTO);

	public Map<Integer, ScheduleCategoryDTO> getCategoryMap(AuthDTO authDTO);

	public void Update(AuthDTO authDTO, ScheduleCategoryDTO categoryDTO);
	
	public ScheduleCategoryDTO getCategory(AuthDTO authDTO, ScheduleCategoryDTO categoryDTO);
}
