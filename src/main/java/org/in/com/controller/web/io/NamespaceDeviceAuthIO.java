package org.in.com.controller.web.io;

import lombok.Data;


@Data
public class NamespaceDeviceAuthIO extends BaseIO {
	private String refferenceType;
	private UserIO user;
	private GroupIO group;
	private NamespaceDeviceIO namespaceDevice;
}
