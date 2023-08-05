package org.in.com.dto;

import org.in.com.dto.enumeration.IntegrationTypeEM;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class IntegrationDTO extends BaseDTO<IntegrationDTO> {
	private String account;
	private String accessToken;
	private String accessUrl;
	private IntegrationTypeEM integrationtype;
	private String provider; 
}
