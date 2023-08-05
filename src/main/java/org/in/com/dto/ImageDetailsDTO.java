package org.in.com.dto;

import org.in.com.dto.enumeration.ImageCategoryEM;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ImageDetailsDTO extends BaseDTO<ImageDetailsDTO> {
	private String imageUrlSlug;
	private ImageCategoryEM imageCategory;
}
