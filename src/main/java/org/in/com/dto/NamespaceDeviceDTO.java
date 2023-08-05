package org.in.com.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NamespaceDeviceDTO extends BaseDTO<NamespaceDeviceDTO> {
	private String token;
	private String remarks;
}
