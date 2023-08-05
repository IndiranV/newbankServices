package org.in.com.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DiscountCategoryDTO extends BaseDTO<DiscountCategoryDTO> {
	private String description;

}
