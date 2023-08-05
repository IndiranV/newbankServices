package org.in.com.controller.web.io;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GroupIO extends BaseIO {
	private String decription;
	private String color;
	private RoleIO role;
	private int userCount;
	private int level;

}
