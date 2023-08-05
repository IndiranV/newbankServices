package org.in.com.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NamespaceDeviceAuthDTO extends BaseDTO<NamespaceDeviceAuthDTO> {
	private NamespaceDeviceDTO namespaceDevice;
	private String refferenceType;
	private UserDTO user;
	private GroupDTO group;
}
