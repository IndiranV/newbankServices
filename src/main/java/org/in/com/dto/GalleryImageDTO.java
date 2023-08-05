package org.in.com.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GalleryImageDTO extends BaseDTO<GalleryImageDTO> {
	private String imageURL;

}
