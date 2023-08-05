package org.in.com.dto;

import org.in.com.dto.enumeration.MediaTypeEM;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NamespaceBannerDetailsDTO extends BaseDTO<NamespaceBannerDetailsDTO> {
	private String url;
	private String redirectUrl;
	private String alternateText;
	private int sequence;
	private MediaTypeEM mediaType;
}
