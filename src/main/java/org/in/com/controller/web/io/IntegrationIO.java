package org.in.com.controller.web.io;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class IntegrationIO extends BaseIO {
	private String account;
	private String accessToken;
	private String accessUrl;
	private String provider;
	private BaseIO integrationType;

}
