package org.in.com.dto;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BusTypeCategoryDTO extends BaseDTO<BusTypeCategoryDTO> {
	private List<BusTypeCategoryDetailsDTO> categoryList;
}
