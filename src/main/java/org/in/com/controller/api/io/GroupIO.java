package org.in.com.controller.api.io;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GroupIO extends BaseIO {
	private String decription;
}
