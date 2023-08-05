package org.in.com.service.impl;

import java.util.Iterator;
import java.util.List;

import org.in.com.cache.GroupCache;
import org.in.com.constants.Numeric;
import org.in.com.dao.GroupDAO;
import org.in.com.dao.UserDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.UserRoleEM;
import org.in.com.service.GroupService;
import org.in.com.utils.StringUtil;
import org.springframework.stereotype.Service;

@Service
public class GroupImpl extends GroupCache implements GroupService {
	public GroupDTO getGroup(AuthDTO authDTO, GroupDTO group) {
		GroupDTO groupDTO = null;
		if (group.getId() != 0) {
			groupDTO = getGroupDTOById(authDTO, group);
		}
		else if (StringUtil.isNotNull(group.getCode())) {
			groupDTO = getGroupDTO(authDTO, group);
		}
		else {
			groupDTO = group;
		}
		return groupDTO;
	}

	public List<UserDTO> getUserDTO(AuthDTO authDTO, GroupDTO dto) {
		UserDAO dao = new UserDAO();
		return dao.getAllUserInGroups(authDTO, dto);
	}

	public List<GroupDTO> getAll(AuthDTO authDTO) {
		GroupDAO dao = new GroupDAO();
		return dao.getAllGroups(authDTO);
	}

	public GroupDTO Update(AuthDTO authDTO, GroupDTO groupDTO) {
		if (StringUtil.isNull(groupDTO.getCode()) && groupDTO.getRole() == null && StringUtil.isNotNull(groupDTO.getName()) && "BusBuddy".equalsIgnoreCase(StringUtil.trimAllSpaces(groupDTO.getName()))) {
			groupDTO.setRole(UserRoleEM.TABLET_POB_ROLE);
		}
		else if (StringUtil.isNull(groupDTO.getCode()) && groupDTO.getRole() == null && StringUtil.isNotNull(groupDTO.getName()) && "VehicleDriver".equalsIgnoreCase(StringUtil.trimAllSpaces(groupDTO.getName()))) {
			groupDTO.setRole(UserRoleEM.DRIVER);
		}
		else if (groupDTO.getRole() == null) {
			groupDTO.setRole(authDTO.getUser().getUserRole());
		}
		GroupDAO dao = new GroupDAO();
		dao.getGroupIUD(authDTO, groupDTO);
		removeGroupDTO(authDTO, groupDTO);
		return groupDTO;
	}

	public void Update(AuthDTO authDTO, GroupDTO groupDTO, UserDTO userDTO) {
		GroupDAO dao = new GroupDAO();
		dao.getGroupMapUser(authDTO, groupDTO, userDTO);
		// removeUserDTO(authDTO, userDTO);

	}

	public List<GroupDTO> get(AuthDTO authDTO, GroupDTO dto) {
		return null;
	}

	@Override
	public List<GroupDTO> getActiveGroup(AuthDTO authDTO) {
		GroupDAO dao = new GroupDAO();
		return dao.getAllActiveGroup(authDTO);
	}

	@Override
	public List<GroupDTO> getUserRoleGroup(AuthDTO authDTO, UserRoleEM userRole) {
		List<GroupDTO> groupList = getAll(authDTO);
		for (Iterator<GroupDTO> iterator = groupList.iterator(); iterator.hasNext();) {
			GroupDTO groupDTO = iterator.next();
			if (groupDTO.getRole().getId() != userRole.getId()) {
				iterator.remove();
			}
		}
		return groupList;
	}

	public GroupDTO checkAndUpdateGroup(AuthDTO authDTO) {
		GroupDAO dao = new GroupDAO();
		GroupDTO groupDTO = dao.checkVehicleDriverGroup(authDTO);
		if (groupDTO == null) {
			groupDTO =  new GroupDTO();
			groupDTO.setName("VehicleDriver");
			groupDTO.setDecription("Vehicle Driver");
			groupDTO.setColor("ffffff");
			groupDTO.setRole(UserRoleEM.DRIVER);
			groupDTO.setActiveFlag(Numeric.ONE_INT);
			
			Update(authDTO, groupDTO);
		}
		return groupDTO;
	}
}
