package org.in.com.dto;

import java.util.List;

import org.in.com.dto.enumeration.ProductTypeEM;
import org.in.com.dto.enumeration.SeverityPermissionTypeEM;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MenuDTO extends BaseDTO<MenuDTO> {
	private String link;
	private String actionCode;
	private List<String> tagList;
	private int exceptionFlag;
	private int enabledFlag;
	private SeverityPermissionTypeEM severity;
	private int displayFlag;
	private MenuDTO lookup;
	private MenuEventDTO menuEvent;
	private ProductTypeEM productType;
	private GroupDTO group;
	private UserDTO user;

}
