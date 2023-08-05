package org.in.com.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.in.com.dao.ScheduleCategoryDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleCategoryDTO;
import org.in.com.service.ScheduleCategoryService;
import org.springframework.stereotype.Service;

@Service
public class ScheduleCategoryImpl implements ScheduleCategoryService {

	@Override
	public List<ScheduleCategoryDTO> getAll(AuthDTO authDTO) {
		ScheduleCategoryDAO categoryDAO = new ScheduleCategoryDAO();
		return categoryDAO.getAllCategory(authDTO);
	}

	public Map<Integer, ScheduleCategoryDTO> getCategoryMap(AuthDTO authDTO) {
		ScheduleCategoryDAO categoryDAO = new ScheduleCategoryDAO();
		List<ScheduleCategoryDTO> categoryList = categoryDAO.getAllCategory(authDTO);
		Map<Integer, ScheduleCategoryDTO> categoryMap = new HashMap<Integer, ScheduleCategoryDTO>();
		for (ScheduleCategoryDTO categoryDTO : categoryList) {
			categoryMap.put(categoryDTO.getId(), categoryDTO);
		}
		return categoryMap;
	}

	@Override
	public void Update(AuthDTO authDTO, ScheduleCategoryDTO categoryDTO) {
		ScheduleCategoryDAO categoryDAO = new ScheduleCategoryDAO();
		categoryDAO.update(authDTO, categoryDTO);
	}

	@Override
	public ScheduleCategoryDTO getCategory(AuthDTO authDTO, ScheduleCategoryDTO categoryDTO) {
		ScheduleCategoryDAO categoryDAO = new ScheduleCategoryDAO();
		return categoryDAO.getCategory(authDTO, categoryDTO);
	}

}
