package org.in.com.service;

import java.util.List;
import java.util.Map;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.MenuDTO;
import org.in.com.dto.MenuEventDTO;
import org.in.com.dto.UserDTO;

public interface MenuService extends BaseService<MenuDTO> {
	public void reload();

	public List<MenuDTO> getRoleMenu(AuthDTO authDTO, MenuDTO menuDTO, String rolecode);

	public List<MenuDTO> roleMenuIUD(AuthDTO authDTO, MenuDTO menuDTO);

	public List<MenuDTO> getUserPrivileges(AuthDTO authDTO, UserDTO userDTO);

	public List<MenuDTO> getAllforZoneSync(AuthDTO authDTO, String syncDate);

	public void restoreDefault(AuthDTO authDTO, UserDTO userDTO);

	public List<MenuDTO> getRoleMenu(AuthDTO authDTO, MenuDTO menuDTO);

	public List<MenuEventDTO> getUserPreDefinedPrivileges(AuthDTO authDTO, UserDTO userDTO);
	
	public List<Map<String, String>> getUserPermissionReport(AuthDTO authDTO, String userCodes);

}
