package org.in.com.controller.web.io;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NamespaceIO extends BaseIO {
	private String contextToken;
	private NamespaceProfileIO namespaceProfile;
}
