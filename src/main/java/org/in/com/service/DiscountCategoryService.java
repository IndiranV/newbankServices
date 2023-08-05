package org.in.com.service;

import org.in.com.dto.DiscountCategoryDTO;

public interface DiscountCategoryService extends BaseService<DiscountCategoryDTO> {

	public void reloadDiscount();

}
