package org.in.com.controller.web.io;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MenuIO extends BaseIO {
	private String link;
	private String actionCode;
	private List<String> tagList;
	private BaseIO severity;
	private int enabledFlag;
	private int displayFlag;
	private MenuIO lookup;
	private List<MenuEventIO> eventList;
	private BaseIO productType;
	private int exceptionFlag;
	private GroupIO group;
	private UserIO user;
}
