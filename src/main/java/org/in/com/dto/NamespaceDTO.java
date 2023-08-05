package org.in.com.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NamespaceDTO extends BaseDTO<NamespaceDTO> {
	private String contextToken;
	private NamespaceProfileDTO profile;
	// Alias Namespace concept
	private String aliasCode;
	private int lookupId;
}
