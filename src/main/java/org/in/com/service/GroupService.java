package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.UserRoleEM;

public interface GroupService extends BaseService<GroupDTO> {

	public List<UserDTO> getUserDTO(AuthDTO authDTO, GroupDTO dto);

	public GroupDTO getGroup(AuthDTO authDTO, GroupDTO group);

	public List<GroupDTO> getActiveGroup(AuthDTO authDTO);

	void Update(AuthDTO authDTO, GroupDTO groupDTO, UserDTO userDTO);

	public List<GroupDTO> getUserRoleGroup(AuthDTO authDTO, UserRoleEM userRole);
	
	public GroupDTO checkAndUpdateGroup(AuthDTO authDTO);

}
