package org.in.com.controller.web.io;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ImageDetailsIO extends BaseIO {
	private String imageUrlSlug;
	private String imageCategory;
}
