package org.in.com.controller.web.io;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NamespaceBannerDetailsIO extends BaseIO {
	private String url;
	private String redirectUrl;
	private String alternateText;
	private int sequence;
	private BaseIO messageType;
}
