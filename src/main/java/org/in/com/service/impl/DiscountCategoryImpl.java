package org.in.com.service.impl;

import java.util.List;

import org.in.com.dao.DiscountDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.DiscountCategoryDTO;
import org.in.com.service.DiscountCategoryService;
import org.springframework.stereotype.Service;

@Service
public class DiscountCategoryImpl implements DiscountCategoryService {

	@Override
	public void reloadDiscount() {

	}

	public List<DiscountCategoryDTO> getAll(AuthDTO authDTO) {
		// TODO Auto-generated method stub
		DiscountDAO dao = new DiscountDAO();
		List<DiscountCategoryDTO> list = dao.getAllDiscountCategory(authDTO);
		return list;
	}

	public DiscountCategoryDTO Update(AuthDTO authDTO, DiscountCategoryDTO discountDto) {
		DiscountDAO dao = new DiscountDAO();
		return dao.updateDiscountCategoryCode(authDTO, discountDto);

	}

	@Override
	public List<DiscountCategoryDTO> get(AuthDTO authDTO, DiscountCategoryDTO dto) {
		DiscountDAO dao = new DiscountDAO();
		dao.getDiscountCategory(authDTO, dto);
		return null;
	}
}
