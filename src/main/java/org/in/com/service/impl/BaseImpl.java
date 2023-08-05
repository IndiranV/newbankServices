package org.in.com.service.impl;

import java.util.List;

import org.in.com.cache.CacheCentral;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dao.BusDAO;
import org.in.com.dao.TicketDAO;
import org.in.com.dao.TripDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.MenuDTO;
import org.in.com.dto.MenuEventDTO;
import org.in.com.dto.NamespaceDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.MenuEventEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.NamespaceService;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BaseImpl extends CacheCentral {

	@Autowired
	NamespaceService namespaceService;

	protected boolean getPrivilege(AuthDTO authDTO, MenuEventEM menuEventEM) {
		if (authDTO.getPrivileges() == null) {
			return false;
		}

		for (MenuDTO menuDTO : authDTO.getPrivileges()) {
			if (menuDTO.getMenuEvent() != null && !menuDTO.getMenuEvent().getList().isEmpty())
				for (MenuEventDTO eventDTO : menuDTO.getMenuEvent().getList()) {
					try {
						if (eventDTO != null && menuEventEM.getOperationCode().equals(eventDTO.getOperationCode())) {
							return true;
						}
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
		}
		return false;
	}

	protected MenuEventDTO getPrivilegeV2(AuthDTO authDTO, List<MenuEventEM> menuEventList) {
		MenuEventDTO menuEventDTO = new MenuEventDTO();
		if (authDTO.getPrivileges() == null) {
			return menuEventDTO;
		}

		for (MenuDTO menuDTO : authDTO.getPrivileges()) {
			for (MenuEventEM menuEventEM : menuEventList) {
				if (StringUtil.isNotNull(menuDTO.getActionCode()) && menuDTO.getActionCode().equals(menuEventEM.getActionCode())) {
					if (menuDTO.getMenuEvent() != null && !menuDTO.getMenuEvent().getList().isEmpty()) {
						for (MenuEventDTO eventDTO : menuDTO.getMenuEvent().getList()) {
							if (eventDTO != null && menuEventEM.getOperationCode().equals(eventDTO.getOperationCode())) {
								menuEventDTO = new MenuEventDTO();
								menuEventDTO.setCode(eventDTO.getCode());
								menuEventDTO.setName(eventDTO.getName());
								menuEventDTO.setEnabledFlag(Numeric.ONE_INT);
								menuEventDTO.setActiveFlag(eventDTO.getActiveFlag());
								menuEventDTO.setPermissionFlag(eventDTO.getPermissionFlag());
								menuEventDTO.setOperationCode(eventDTO.getOperationCode());
								menuEventDTO.setAttr1Value(eventDTO.getAttr1Value());
							}
						}
					}
				}
			}
		}
		return menuEventDTO;
	}

	protected MenuEventDTO getPrivilegeV3(List<MenuDTO> privilegesList, List<MenuEventEM> menuEventList) {
		MenuEventDTO menuEventDTO = new MenuEventDTO();
		if (privilegesList == null) {
			return menuEventDTO;
		}

		for (MenuDTO menuDTO : privilegesList) {
			for (MenuEventEM menuEventEM : menuEventList) {
				if (StringUtil.isNotNull(menuDTO.getActionCode()) && menuDTO.getActionCode().equals(menuEventEM.getActionCode())) {
					if (menuDTO.getMenuEvent() != null && !menuDTO.getMenuEvent().getList().isEmpty()) {
						for (MenuEventDTO eventDTO : menuDTO.getMenuEvent().getList()) {
							if (eventDTO != null && menuEventEM.getOperationCode().equals(eventDTO.getOperationCode())) {
								menuEventDTO = new MenuEventDTO();
								menuEventDTO.setCode(eventDTO.getCode());
								menuEventDTO.setName(eventDTO.getName());
								menuEventDTO.setEnabledFlag(Numeric.ONE_INT);
								menuEventDTO.setActiveFlag(eventDTO.getActiveFlag());
								menuEventDTO.setPermissionFlag(eventDTO.getPermissionFlag());
								menuEventDTO.setOperationCode(eventDTO.getOperationCode());
								menuEventDTO.setAttr1Value(eventDTO.getAttr1Value());
							}
						}
					}
				}
			}
		}
		return menuEventDTO;
	}

	protected AuthDTO getNamespaceAuthDTO(TicketDTO ticketDTO) {
		TicketDAO ticketDAO = new TicketDAO();
		NamespaceDTO namespaceDTO = ticketDAO.getNamespace(ticketDTO);
		if (namespaceDTO.getId() == 0) {
			throw new ServiceException(ErrorCode.INVALID_CODE);
		}
		AuthDTO authDTO = new AuthDTO();
		authDTO.setNamespaceCode(namespaceService.getNamespace(namespaceDTO).getCode());
		authDTO.setDeviceMedium(ticketDTO.getDeviceMedium());
		return authDTO;
	}

	protected AuthDTO getNamespaceAuthDTO(TripDTO tripDTO) {
		TripDAO tripDAO = new TripDAO();
		NamespaceDTO namespaceDTO = tripDAO.getNamespace(tripDTO);
		if (namespaceDTO.getId() == 0) {
			throw new ServiceException(ErrorCode.INVALID_CODE);
		}
		AuthDTO authDTO = new AuthDTO();
		authDTO.setNamespaceCode(namespaceService.getNamespace(namespaceDTO).getCode());
		return authDTO;
	}

	protected AuthDTO getNamespaceAuthDTO(BusDTO busDTO) {
		BusDAO busDAO = new BusDAO();
		NamespaceDTO namespaceDTO = busDAO.getNamespace(busDTO);
		if (namespaceDTO.getId() == 0) {
			throw new ServiceException(ErrorCode.INVALID_CODE);
		}
		AuthDTO authDTO = new AuthDTO();
		authDTO.setNamespaceCode(namespaceService.getNamespace(namespaceDTO).getCode());
		return authDTO;
	}

	protected boolean isAllowApiTicketTransfer(AuthDTO authDTO) {
		boolean allow = Text.FALSE;
		for (UserDTO userDTO : authDTO.getNamespace().getProfile().getAllowApiTicketTransfer()) {
			if (userDTO.getId() == authDTO.getUser().getId()) {
				allow = Text.TRUE;
				break;
			}
		}
		return allow;
	}

	protected boolean isAllowApiTripInfo(AuthDTO authDTO) {
		boolean allow = Text.FALSE;
		for (UserDTO userDTO : authDTO.getNamespace().getProfile().getAllowApiTripInfo()) {
			if (userDTO.getId() == authDTO.getUser().getId()) {
				allow = Text.TRUE;
				break;
			}
		}
		return allow;
	}

	protected boolean isAllowApiTripChart(AuthDTO authDTO) {
		boolean allow = Text.FALSE;
		for (UserDTO userDTO : authDTO.getNamespace().getProfile().getAllowApiTripChart()) {
			if (userDTO.getId() == authDTO.getUser().getId()) {
				allow = Text.TRUE;
				break;
			}
		}
		return allow;
	}

	protected boolean isAllowApiTripChartAllPnr(AuthDTO authDTO) {
		boolean allow = Text.FALSE;
		for (UserDTO userDTO : authDTO.getNamespace().getProfile().getAllowApiTripChartAllPnr()) {
			if (userDTO.getId() == authDTO.getUser().getId()) {
				allow = Text.TRUE;
				break;
			}
		}
		return allow;
	}
}
