package org.in.com.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.in.com.aggregator.mercservices.MercService;
import org.in.com.cache.EhcacheManager;
import org.in.com.cache.MenuCache;
import org.in.com.config.ApplicationConfig;
import org.in.com.constants.Constants;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dao.MenuDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.MenuDTO;
import org.in.com.dto.MenuEventDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.MenuEventEM;
import org.in.com.dto.enumeration.SeverityPermissionTypeEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.GroupService;
import org.in.com.service.MenuService;
import org.in.com.service.UserService;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.sf.ehcache.Element;

@Service
public class MenuImpl extends MenuCache implements MenuService {

	@Autowired
	UserService userService;
	@Autowired
	GroupService groupService;
	@Autowired
	MercService mercService;

	public void reload() {
		super.reload();
	}

	public List<MenuDTO> getAll(AuthDTO authDTO) {
		MenuDAO dao = new MenuDAO();
		List<MenuDTO> list = dao.getAllMenu(authDTO);
		Map<Integer, MenuDTO> dtoMAP = new HashMap<Integer, MenuDTO>();
		for (MenuDTO menuDTO : list) {
			dtoMAP.put(menuDTO.getId(), menuDTO);
		}
		for (MenuDTO menuDTO : list) {
			if (menuDTO.getLookup().getId() != 0) {
				if (dtoMAP.get(menuDTO.getLookup().getId()) != null) {
					menuDTO.setLookup(dtoMAP.get(menuDTO.getLookup().getId()));
				}
			}
		}
		return list;
	}

	public List<MenuDTO> getAllforZoneSync(AuthDTO authDTO, String syncDate) {
		MenuDAO dao = new MenuDAO();
		List<MenuDTO> list = dao.getAllforZoneSync(authDTO, syncDate);
		Map<Integer, MenuDTO> dtoMAP = new HashMap<Integer, MenuDTO>();
		for (MenuDTO menuDTO : list) {
			dtoMAP.put(menuDTO.getId(), menuDTO);
		}
		for (MenuDTO menuDTO : list) {
			if (menuDTO.getLookup().getId() != 0) {
				if (dtoMAP.get(menuDTO.getLookup().getId()) != null) {
					menuDTO.setLookup(dtoMAP.get(menuDTO.getLookup().getId()));
				}
			}
		}
		return list;
	}

	public MenuDTO Update(AuthDTO authDTO, MenuDTO dto) {
		MenuDAO dao = new MenuDAO();
		if (!ArrayUtils.contains(Constants.SUPER_REGIONS_ZONE, ApplicationConfig.getServerZoneCode())) {
			throw new ServiceException(ErrorCode.INVALID_APPLICATION_ZONE);
		}
		if (dto.getLookup() != null && StringUtil.isNotNull(dto.getLookup().getCode())) {
			Element menuElement = EhcacheManager.getMenuEhCache().get(dto.getLookup().getCode());
			if (menuElement != null) {
				MenuDTO cacheDTO = (MenuDTO) menuElement.getObjectValue();
				dto.setLookup(cacheDTO);
				dto.setProductType(cacheDTO.getProductType());
			}
		}
		dao.MenuUID(authDTO, dto);
		dao.getMenuEventsUID(authDTO, dto);
		reload();
		return dto;
	}

	public List<MenuDTO> getRoleMenu(AuthDTO authDTO, MenuDTO menuDTO, String roleCode) {
		List<MenuDTO> processedlist = new ArrayList<>();
		MenuDAO dao = new MenuDAO();
		if (menuDTO.getUser() != null) {
			menuDTO.setUser(userService.getUser(authDTO, menuDTO.getUser()));
		}
		else if (menuDTO.getGroup() != null) {
			menuDTO.setGroup(groupService.getGroup(authDTO, menuDTO.getGroup()));
		}
		List<MenuDTO> rolelist = dao.getRoleMenu(authDTO, menuDTO, roleCode);
		Map<Integer, MenuDTO> roleMenuMap = new HashMap<Integer, MenuDTO>();
		for (MenuDTO dto : rolelist) {
			roleMenuMap.put(dto.getId(), dto);
		}

		// Group Menu List
		if ("NS".equals(roleCode)) {

			List<MenuEventEM> eventList = new ArrayList<MenuEventEM>();
			eventList.add(MenuEventEM.PRIVILEGE_DEFAULT);
			eventList.add(MenuEventEM.PRIVILEGE_MAJOR);
			eventList.add(MenuEventEM.PRIVILEGE_CRITICAL);
			eventList.add(MenuEventEM.PRIVILEGE_BLOCKER);
			SeverityPermissionTypeEM severityMenuEvent = getPrivilege(authDTO, eventList);

			List<MenuDTO> allMenuList = dao.getAllMenu(authDTO);
			for (Iterator<MenuDTO> iterator = allMenuList.iterator(); iterator.hasNext();) {
				MenuDTO roleMenuDTO = iterator.next();

				// If deleted/Disabled menu, remove it
				if (StringUtil.isNull(roleMenuDTO.getCode()) || roleMenuDTO.getActiveFlag() != 1) {
					iterator.remove();
					continue;
				}
				// Severity validation
				if (roleMenuDTO.getSeverity().getId() > severityMenuEvent.getId()) {
					iterator.remove();
					continue;
				}
				if (roleMenuDTO.getMenuEvent() != null && !roleMenuDTO.getMenuEvent().getList().isEmpty()) {
					for (Iterator<MenuEventDTO> eventiterator = roleMenuDTO.getMenuEvent().getList().iterator(); eventiterator.hasNext();) {
						MenuEventDTO menuEventDTO = eventiterator.next();
						if (menuEventDTO.getSeverity().getId() > severityMenuEvent.getId()) {
							eventiterator.remove();
							continue;
						}
					}
				}
				if (roleMenuMap.get(roleMenuDTO.getId()) != null) {
					roleMenuDTO.setEnabledFlag(1);
					MenuDTO dto = roleMenuMap.get(roleMenuDTO.getId());

					if (roleMenuDTO.getMenuEvent() != null && dto.getMenuEvent() != null) {
						Map<String, MenuEventDTO> eventMap = new HashMap<String, MenuEventDTO>();
						for (MenuEventDTO eventDTO : dto.getMenuEvent().getList()) {
							if (StringUtil.isNotNull(eventDTO.getCode())) {
								eventMap.put(eventDTO.getCode(), eventDTO);
							}
						}
						for (MenuEventDTO menuEventDTO : roleMenuDTO.getMenuEvent().getList()) {
							if (StringUtil.isNotNull(menuEventDTO.getCode())) {
								if (eventMap.get(menuEventDTO.getCode()) != null) {
									menuEventDTO.setEnabledFlag(1);
								}
							}
						}
					}
				}
				roleMenuDTO.setLookup(getMenuDTOById(roleMenuDTO.getLookup()));
			}
			processedlist.addAll(allMenuList);
		}
		else if ("GR".equals(roleCode)) {
			List<MenuDTO> namespacelist = dao.getNamespacePrivileges(authDTO);
			// Iterate and update menu details
			getMenuDetailsFromCache(namespacelist);

			for (Iterator<MenuDTO> menuIterator = namespacelist.iterator(); menuIterator.hasNext();) {
				MenuDTO roleMenuDTO = menuIterator.next();
				if (roleMenuMap.get(roleMenuDTO.getId()) != null) {
					roleMenuDTO.setEnabledFlag(1);
					MenuDTO dto = roleMenuMap.get(roleMenuDTO.getId());
					if (roleMenuDTO.getMenuEvent() != null && dto.getMenuEvent() != null) {
						// Convert to Map
						Map<String, MenuEventDTO> eventMap = new HashMap<String, MenuEventDTO>();
						for (MenuEventDTO eventDTO : dto.getMenuEvent().getList()) {
							if (StringUtil.isNotNull(eventDTO.getCode())) {
								eventMap.put(eventDTO.getCode(), eventDTO);
							}
						}
						// Check and update enable flag
						for (MenuEventDTO menuEventDTO : roleMenuDTO.getMenuEvent().getList()) {
							if (StringUtil.isNotNull(menuEventDTO.getCode())) {
								if (eventMap.get(menuEventDTO.getCode()) != null) {
									menuEventDTO.setEnabledFlag(1);
								}
							}
						}
					}
				}
			}
			processedlist.addAll(namespacelist);
		}
		else if ("UR".equals(roleCode)) {
			menuDTO.setGroup(menuDTO.getUser().getGroup());
			List<MenuDTO> grouplist = dao.getRoleMenu(authDTO, menuDTO, "GR");
			Map<Integer, MenuDTO> groupMenuMap = new HashMap<Integer, MenuDTO>();
			for (MenuDTO dto : grouplist) {
				groupMenuMap.put(dto.getId(), dto);
			}

			List<MenuDTO> namespacelist = dao.getNamespacePrivileges(authDTO);
			// Iterate and update menu details
			getMenuDetailsFromCache(namespacelist);

			for (Iterator<MenuDTO> menuIterator = namespacelist.iterator(); menuIterator.hasNext();) {
				MenuDTO roleMenuDTO = menuIterator.next();
				if (roleMenuMap.get(roleMenuDTO.getId()) != null) {
					roleMenuDTO.setEnabledFlag(1);
					MenuDTO dto = roleMenuMap.get(roleMenuDTO.getId());
					if (roleMenuDTO.getMenuEvent() != null && dto.getMenuEvent() != null) {
						// Convert to Map
						Map<String, MenuEventDTO> eventMap = new HashMap<String, MenuEventDTO>();
						for (MenuEventDTO eventDTO : dto.getMenuEvent().getList()) {
							if (StringUtil.isNotNull(eventDTO.getCode())) {
								eventMap.put(eventDTO.getCode(), eventDTO);
							}
						}
						// Check and update enable flag
						for (MenuEventDTO menuEventDTO : roleMenuDTO.getMenuEvent().getList()) {
							if (StringUtil.isNotNull(menuEventDTO.getCode())) {
								if (eventMap.get(menuEventDTO.getCode()) != null) {
									menuEventDTO.setEnabledFlag(1);
								}
							}
						}
					}
				}
				else if (groupMenuMap.get(roleMenuDTO.getId()) != null) {
					roleMenuDTO.setEnabledFlag(1);
					MenuDTO dto = groupMenuMap.get(roleMenuDTO.getId());
					if (roleMenuDTO.getMenuEvent() != null && dto.getMenuEvent() != null) {
						// Convert to Map
						Map<String, MenuEventDTO> eventMap = new HashMap<String, MenuEventDTO>();
						for (MenuEventDTO eventDTO : dto.getMenuEvent().getList()) {
							if (StringUtil.isNotNull(eventDTO.getCode())) {
								eventMap.put(eventDTO.getCode(), eventDTO);
							}
						}
						// Check and update enable flag
						for (MenuEventDTO menuEventDTO : roleMenuDTO.getMenuEvent().getList()) {
							if (StringUtil.isNotNull(menuEventDTO.getCode())) {
								if (eventMap.get(menuEventDTO.getCode()) != null) {
									menuEventDTO.setEnabledFlag(1);
								}
							}
						}
					}
				}
			}
			processedlist.addAll(namespacelist);
		}
		return processedlist;
	}

	public List<MenuDTO> get(AuthDTO authDTO, MenuDTO dto) {
		return null;
	}

	public List<MenuDTO> roleMenuIUD(AuthDTO authDTO, MenuDTO menuDTO) {
		MenuDAO dao = new MenuDAO();
		menuDTO.setId(getMenuDTO(menuDTO.getCode()).getId());
		if (menuDTO.getDisplayFlag() == 2) {
			throw new ServiceException(ErrorCode.INVALID_CREDENTIALS);
		}
		// Identify the exception
		if (menuDTO.getActiveFlag() == 0 && menuDTO.getUser() != null && StringUtil.isNotNull(menuDTO.getUser().getCode())) {
			MenuDTO groupMenuDTO = new MenuDTO();
			groupMenuDTO.setGroup(groupService.getGroup(authDTO, userService.getUserDTO(authDTO, menuDTO.getUser()).getGroup()));
			List<MenuDTO> grouplist = dao.getRoleMenu(authDTO, groupMenuDTO, "GR");
			Map<String, MenuDTO> groupMenuMap = new HashMap<String, MenuDTO>();
			for (MenuDTO dto : grouplist) {
				groupMenuMap.put(getMenuDTOById(dto).getCode(), dto);
			}
			if (groupMenuMap.get(menuDTO.getCode()) != null) {
				menuDTO.setExceptionFlag(1);
				menuDTO.setActiveFlag(1);
			}
		}

		MenuDTO repoMenu = null;
		if (menuDTO.getGroup() == null && menuDTO.getUser() == null) {
			repoMenu = dao.getUserMenu(authDTO, menuDTO);
		}
		MenuDAO.getUserMenuUID(authDTO, menuDTO);
		removeUserMenuCache();

		/** Push to Merc */
		if (repoMenu != null) {
			MenuDTO menu = dao.getMenuDTO(menuDTO);
			String action = "Enabled";
			if ((menuDTO.getActiveFlag() == 0) || ((repoMenu.getId() != 0) && menuDTO.getMenuEvent().getList().size() < repoMenu.getMenuEvent().getList().size())) {
				action = "Disabled";
			}
			mercService.indexMenuPrivilegeAuditHistory(authDTO, menu, action);
		}
		return null;
	}

	@Override
	public List<MenuDTO> getUserPrivileges(AuthDTO authDTO, UserDTO userDTO) {
		List<MenuDTO> privilegeMenu = new ArrayList<MenuDTO>();
		MenuDAO dao = new MenuDAO();
		List<MenuDTO> namespaceMenuList = dao.getNamespacePrivileges(authDTO);
		List<MenuDTO> groupMenuList = dao.getGroupPrivileges(authDTO, userDTO.getGroup());
		List<MenuDTO> userMenuList = dao.getUserPrivileges(authDTO, userDTO);
		Map<String, MenuDTO> namespaceMenuMap = new HashMap<>();
		Map<String, MenuDTO> groupMenuMap = new HashMap<>();
		// Namespace Menu List
		if (namespaceMenuList != null) {
			for (Iterator<MenuDTO> iterator = namespaceMenuList.iterator(); iterator.hasNext();) {
				MenuDTO roleMenuDTO = iterator.next();
				MenuDTO cachedto = getMenuDTOById(roleMenuDTO);
				// If deleted menu, remove it
				if (StringUtil.isNull(cachedto.getCode()) || cachedto.getActiveFlag() != 1) {
					iterator.remove();
					continue;
				}
				roleMenuDTO.setCode(cachedto.getCode());
				if (roleMenuDTO.getMenuEvent() != null) {
					for (Iterator<MenuEventDTO> menuEventItr = roleMenuDTO.getMenuEvent().getList().iterator(); menuEventItr.hasNext();) {
						MenuEventDTO menuEventDTO = menuEventItr.next();
						boolean foundFlag = false;
						for (MenuEventDTO cacheEventDTO : cachedto.getMenuEvent().getList()) {
							if (menuEventDTO.getCode().equals(cacheEventDTO.getCode()) && cacheEventDTO.getActiveFlag() == 1) {
								foundFlag = true;
								break;
							}
						}
						if (!foundFlag) {
							menuEventItr.remove();
						}
					}
				}
				namespaceMenuMap.put(roleMenuDTO.getCode(), roleMenuDTO);
			}
		}

		// Group Menu List
		if (groupMenuList != null) {
			for (Iterator<MenuDTO> menuIterator = groupMenuList.iterator(); menuIterator.hasNext();) {
				MenuDTO roleMenuDTO = menuIterator.next();
				MenuDTO cachedto = getMenuDTOById(roleMenuDTO);
				// If deleted menu, remove it
				if (StringUtil.isNull(cachedto.getCode()) || cachedto.getActiveFlag() != 1) {
					menuIterator.remove();
					continue;
				}

				roleMenuDTO.setCode(cachedto.getCode());
				if (namespaceMenuMap.get(roleMenuDTO.getCode()) == null) {
					menuIterator.remove();
					continue;
				}
				roleMenuDTO.setName(cachedto.getName());
				roleMenuDTO.setLink(cachedto.getLink());
				roleMenuDTO.setDisplayFlag(cachedto.getDisplayFlag());
				roleMenuDTO.setActiveFlag(cachedto.getActiveFlag());
				roleMenuDTO.setActionCode(cachedto.getActionCode());
				roleMenuDTO.setSeverity(cachedto.getSeverity());
				List<String> tagList = new ArrayList<>();
				for (String tag : cachedto.getTagList()) {
					tagList.add(tag);
				}
				roleMenuDTO.setTagList(tagList);
				roleMenuDTO.setProductType(cachedto.getProductType());
				roleMenuDTO.setLookup(cachedto.getLookup());

				if (roleMenuDTO.getMenuEvent() != null) {
					for (Iterator<MenuEventDTO> eventIterator = roleMenuDTO.getMenuEvent().getList().iterator(); eventIterator.hasNext();) {
						MenuEventDTO menuEventDTO = eventIterator.next();

						boolean foundFlag = false;
						for (MenuEventDTO cacheEventDTO : namespaceMenuMap.get(roleMenuDTO.getCode()).getMenuEvent().getList()) {
							if (menuEventDTO.getCode().equals(cacheEventDTO.getCode())) {
								foundFlag = true;
								break;
							}
						}
						if (!foundFlag) {
							eventIterator.remove();
							continue;
						}
						if (menuEventDTO.getCode() != null) {
							MenuEventDTO eventDTO = getEventDTO(cachedto, menuEventDTO.getCode());
							if (eventDTO != null) {
								menuEventDTO.setCode(eventDTO.getCode());
								menuEventDTO.setName(eventDTO.getName());
								menuEventDTO.setActiveFlag(eventDTO.getActiveFlag());
								menuEventDTO.setPermissionFlag(eventDTO.getPermissionFlag());
								menuEventDTO.setOperationCode(eventDTO.getOperationCode());
								menuEventDTO.setSeverity(eventDTO.getSeverity());
								menuEventDTO.setAttr1Value(eventDTO.getAttr1Value());
							}
						}
					}
				}
				groupMenuMap.put(roleMenuDTO.getCode(), roleMenuDTO);
			}
		}
		// User Menu List
		if (userMenuList != null) {
			for (Iterator<MenuDTO> iterator = userMenuList.iterator(); iterator.hasNext();) {
				MenuDTO roleMenuDTO = iterator.next();
				MenuDTO cachedto = getMenuDTOById(roleMenuDTO);
				// If deleted menu, remove it
				if (StringUtil.isNull(cachedto.getCode()) || cachedto.getActiveFlag() != 1) {
					iterator.remove();
					continue;
				}
				roleMenuDTO.setCode(cachedto.getCode());
				roleMenuDTO.setName(cachedto.getName());
				roleMenuDTO.setLink(cachedto.getLink());
				roleMenuDTO.setDisplayFlag(cachedto.getDisplayFlag());
				roleMenuDTO.setActiveFlag(cachedto.getActiveFlag());
				roleMenuDTO.setActionCode(cachedto.getActionCode());
				roleMenuDTO.setSeverity(cachedto.getSeverity());
				List<String> tagList = new ArrayList<>();
				for (String tag : cachedto.getTagList()) {
					tagList.add(tag);
				}
				roleMenuDTO.setTagList(tagList);
				roleMenuDTO.setProductType(cachedto.getProductType());
				roleMenuDTO.setLookup(cachedto.getLookup());

				// Remove if Exception
				if (roleMenuDTO.getExceptionFlag() == 1) {
					groupMenuMap.remove(roleMenuDTO.getCode());
					iterator.remove();
					continue;
				}
				if (namespaceMenuMap.get(roleMenuDTO.getCode()) == null) {
					iterator.remove();
					continue;
				}
				// Remove if Group Level
				if (groupMenuMap.get(roleMenuDTO.getCode()) != null) {
					groupMenuMap.remove(roleMenuDTO.getCode());
				}
				if (roleMenuDTO.getMenuEvent() != null) {
					for (Iterator<MenuEventDTO> eventIterator = roleMenuDTO.getMenuEvent().getList().iterator(); eventIterator.hasNext();) {
						MenuEventDTO menuEventDTO = eventIterator.next();

						boolean foundFlag = false;
						for (MenuEventDTO cacheEventDTO : namespaceMenuMap.get(roleMenuDTO.getCode()).getMenuEvent().getList()) {
							if (menuEventDTO.getCode().equals(cacheEventDTO.getCode())) {
								foundFlag = true;
								break;
							}
						}
						if (!foundFlag) {
							eventIterator.remove();
							continue;
						}

						if (menuEventDTO.getCode() != null) {
							MenuEventDTO eventDTO = getEventDTO(cachedto, menuEventDTO.getCode());
							if (eventDTO != null) {
								menuEventDTO.setCode(eventDTO.getCode());
								menuEventDTO.setName(eventDTO.getName());
								menuEventDTO.setSeverity(eventDTO.getSeverity());
								menuEventDTO.setActiveFlag(eventDTO.getActiveFlag());
								menuEventDTO.setPermissionFlag(eventDTO.getPermissionFlag());
								menuEventDTO.setOperationCode(eventDTO.getOperationCode());
								menuEventDTO.setAttr1Value(eventDTO.getAttr1Value());
								menuEventDTO.setSeverity(eventDTO.getSeverity());
							}
						}
					}
				}
			}
		}
		privilegeMenu.addAll(new ArrayList<MenuDTO>(groupMenuMap.values()));
		privilegeMenu.addAll(userMenuList);
		return privilegeMenu;
	}

	private MenuEventDTO getEventDTO(MenuDTO menuDTO, String EventCode) {
		for (MenuEventDTO menuEventDTO : menuDTO.getMenuEvent().getList()) {
			if (menuEventDTO != null && menuEventDTO.getCode().equals(EventCode)) {
				return menuEventDTO;
			}
		}
		return null;
	}

	private void getMenuDetailsFromCache(List<MenuDTO> list) {
		for (Iterator<MenuDTO> menuIterator = list.iterator(); menuIterator.hasNext();) {
			MenuDTO roleMenuDTO = menuIterator.next();
			final MenuDTO cachedto = getMenuDTOById(roleMenuDTO);
			if (cachedto.getActiveFlag() != 1) {
				menuIterator.remove();
				continue;
			}
			roleMenuDTO.setCode(cachedto.getCode());
			roleMenuDTO.setName(cachedto.getName());
			roleMenuDTO.setActionCode(cachedto.getActionCode());
			List<String> tagList = new ArrayList<>();
			for (String tag : cachedto.getTagList()) {
				tagList.add(tag);
			}
			roleMenuDTO.setTagList(tagList);
			roleMenuDTO.setLink(cachedto.getLink());
			roleMenuDTO.setDisplayFlag(cachedto.getDisplayFlag());
			roleMenuDTO.setSeverity(cachedto.getSeverity());
			roleMenuDTO.setProductType(cachedto.getProductType());
			roleMenuDTO.setActiveFlag(cachedto.getActiveFlag());
			if (roleMenuDTO.getMenuEvent() != null) {
				for (MenuEventDTO menuEventDTO : roleMenuDTO.getMenuEvent().getList()) {
					MenuEventDTO cacheEventDTO = getEventDTO(cachedto, menuEventDTO.getCode());
					if (cacheEventDTO != null) {
						menuEventDTO.setName(cacheEventDTO.getName());
						menuEventDTO.setOperationCode(cacheEventDTO.getOperationCode());
						menuEventDTO.setAttr1Value(cacheEventDTO.getAttr1Value());
						menuEventDTO.setSeverity(cacheEventDTO.getSeverity());
						menuEventDTO.setActiveFlag(cacheEventDTO.getActiveFlag());
					}
				}
			}
			roleMenuDTO.setLookup(getMenuDTOById(cachedto.getLookup()));
		}
	}

	@Override
	public void restoreDefault(AuthDTO authDTO, UserDTO userDTO) {
		MenuDAO dao = new MenuDAO();
		userDTO = userService.getUser(authDTO.getNamespaceCode(), userDTO.getCode());
		dao.restoreDefault(authDTO, userDTO);
	}

	@Override
	public List<MenuDTO> getRoleMenu(AuthDTO authDTO, MenuDTO menuDTO) {
		MenuDAO dao = new MenuDAO();
		List<MenuDTO> menuList = dao.getRoleMenu(authDTO, menuDTO);

		for (MenuDTO menu : menuList) {
			if (menu.getUser() != null) {
				menu.setUser(userService.getUser(authDTO, menu.getUser()));
			}
			else if (menu.getGroup() != null) {
				menu.setGroup(groupService.getGroup(authDTO, menu.getGroup()));
			}
		}

		return menuList;
	}

	@Override
	public List<MenuEventDTO> getUserPreDefinedPrivileges(AuthDTO authDTO, UserDTO userDTO) {
		List<MenuEventDTO> menuEvents = getUserPreDefinedPrivilegesCache(authDTO, userDTO);

		if (menuEvents == null) {
			List<MenuDTO> userMenuList = getUserPrivileges(authDTO, userDTO);
			menuEvents = new ArrayList<>();
			List<MenuEventEM> menuEventEMs = Arrays.asList(MenuEventEM.values());

			Map<String, MenuEventEM> menuEventMap = new HashMap<>();
			for (MenuEventEM menuEventEM : menuEventEMs) {
				menuEventMap.put(menuEventEM.getOperationCode(), menuEventEM);
			}

			for (MenuDTO menuDTO : userMenuList) {
				if (menuEventMap == null || menuEventMap.isEmpty()) {
					break;
				}
				for (MenuEventEM menuEventEM : menuEventEMs) {
					if (StringUtil.isNotNull(menuDTO.getActionCode()) && menuDTO.getActionCode().equals(menuEventEM.getActionCode()) && menuDTO.getMenuEvent() != null && !menuDTO.getMenuEvent().getList().isEmpty()) {
						for (MenuEventDTO eventDTO : menuDTO.getMenuEvent().getList()) {
							if (menuEventMap != null && !menuEventMap.isEmpty() && eventDTO != null && menuEventEM.getOperationCode().equals(eventDTO.getOperationCode())) {
								MenuEventDTO menuEventDTO = new MenuEventDTO();
								menuEventDTO.setCode(eventDTO.getCode());
								menuEventDTO.setName(eventDTO.getName());
								menuEventDTO.setEnabledFlag(Numeric.ONE_INT);
								menuEventDTO.setSeverity(eventDTO.getSeverity());
								menuEventDTO.setActiveFlag(eventDTO.getActiveFlag());
								menuEventDTO.setPermissionFlag(eventDTO.getPermissionFlag());
								menuEventDTO.setOperationCode(eventDTO.getOperationCode());
								menuEventDTO.setAttr1Value(eventDTO.getAttr1Value());
								menuEvents.add(menuEventDTO);
								menuEventMap.remove(eventDTO.getOperationCode());
							}
						}
					}
				}
			}

			putUserPreDefinedPrivilegesCache(authDTO, userDTO, menuEvents);
		}
		return menuEvents;
	}

	public List<Map<String, String>> getUserPermissionReport(AuthDTO authDTO, String userCodes) {
		List<Map<String, String>> finalList = new ArrayList<>();
		MenuDAO dao = new MenuDAO();
		List<MenuDTO> namespaceMenuList = dao.getNamespacePrivileges(authDTO);
		Map<String, MenuDTO> namespaceMenuMap = getNamespacePrivilegeMap(authDTO, namespaceMenuList);

		if (!namespaceMenuMap.isEmpty()) {
			Map<String, List<MenuDTO>> privilegeMap = dao.getRoleMenuPremission(authDTO, userCodes);
			List<MenuDTO> groupMenuList = privilegeMap.get("GR");
			List<MenuDTO> userMenuList = privilegeMap.get("UR");
			Map<String, List<UserDTO>> groupWiseUserMap = getGroupWiseUser(authDTO, userCodes);
			Map<String, MenuDTO> groupMenuMap = validateGroupMenu(groupMenuList, namespaceMenuMap, groupWiseUserMap);
			// User Menu List
			if (userMenuList != null) {
				for (Iterator<MenuDTO> iterator = userMenuList.iterator(); iterator.hasNext();) {
					MenuDTO roleMenuDTO = iterator.next();
					// Remove if Exception
					String mapKey = roleMenuDTO.getCode() + Text.UNDER_SCORE + roleMenuDTO.getUser().getGroup().getId() + Text.UNDER_SCORE + roleMenuDTO.getUser().getCode();
					if (roleMenuDTO.getExceptionFlag() == 1) {
						groupMenuMap.remove(mapKey);
						iterator.remove();
						continue;
					}
					if (namespaceMenuMap.get(roleMenuDTO.getCode()) == null) {
						iterator.remove();
						continue;
					}
					// Remove if Group Level
					if (groupMenuMap.get(mapKey) != null) {
						groupMenuMap.remove(mapKey);
					}
					if (roleMenuDTO.getMenuEvent() != null) {
						for (Iterator<MenuEventDTO> eventIterator = roleMenuDTO.getMenuEvent().getList().iterator(); eventIterator.hasNext();) {
							MenuEventDTO menuEventDTO = eventIterator.next();

							boolean foundFlag = false;
							for (MenuEventDTO cacheEventDTO : namespaceMenuMap.get(roleMenuDTO.getCode()).getMenuEvent().getList()) {
								if (menuEventDTO.getCode().equals(cacheEventDTO.getCode())) {
									foundFlag = true;
									break;
								}
							}
							if (!foundFlag) {
								eventIterator.remove();
								continue;
							}
						}
					}
				}
			}

			Map<String, MenuDTO> userMenuMap = getUserMenuMap(userMenuList);
			Map<String, MenuDTO> userNamespaceMap = convertNamespacePrivilegeMap(authDTO, userCodes, namespaceMenuList);

			Map<String, UserDTO> userGroupCodeMap = new HashMap<>();
			for (String userCode : userCodes.split(Text.COMMA)) {
				UserDTO userDTO = new UserDTO();
				userDTO.setCode(userCode);
				userDTO = userService.getUser(authDTO, userDTO);
				userDTO.setName(userDTO.getName() + (StringUtil.isNotNull(userDTO.getLastname()) ? Text.SINGLE_SPACE + userDTO.getLastname() : Text.EMPTY));
				userDTO.setGroup(groupService.getGroup(authDTO, userDTO.getGroup()));
				userGroupCodeMap.put(userDTO.getCode(), userDTO);
			}

			Map<String, String> uniqueUserMenuMap = new HashMap<>();
			for (Entry<String, MenuDTO> entryMap : userNamespaceMap.entrySet()) {
				String key = entryMap.getKey();
				MenuDTO menu = entryMap.getValue();

				UserDTO userDTO = userGroupCodeMap.get(key.split("\\_")[1]);
				String groupKey = menu.getCode() + Text.UNDER_SCORE + userDTO.getGroup().getId() + Text.UNDER_SCORE + userDTO.getCode();

				if (userMenuMap.get(key) == null && groupMenuMap.get(groupKey) == null) {
					continue;
				}
				MenuDTO menuDTO = null;
				if (userMenuMap.get(key) != null) {
					menuDTO = userMenuMap.get(key);
				}
				else if (groupMenuMap.get(groupKey) != null) {
					menuDTO = groupMenuMap.get(groupKey);
				}
				menuDTO.setUser(userDTO);

				String userCacheKey = menuDTO.getCode() + Text.UNDER_SCORE + menuDTO.getUser().getCode();
				if (uniqueUserMenuMap.get(userCacheKey) != null) {
					continue;
				}
				Map<String, String> dataMap = new HashMap<>();
				dataMap.put("menu_code", menuDTO.getCode());
				dataMap.put("menu_name", menuDTO.getName());
				dataMap.put("user_code", menuDTO.getUser().getCode());
				dataMap.put("user_name", menuDTO.getUser().getName());
				dataMap.put("menu_events", StringUtils.join(menuDTO.getMenuEvent().getList().stream().map(user -> user.getCode()).collect(Collectors.toList()), ','));
				finalList.add(dataMap);

				uniqueUserMenuMap.put(userCacheKey, menuDTO.getUser().getCode());
			}
		}
		return finalList;
	}

	private Map<String, MenuDTO> convertNamespacePrivilegeMap(AuthDTO authDTO, String userCodes, List<MenuDTO> namespaceMenuList) {
		Map<String, MenuDTO> userNamespaceMenuMap = new HashMap<>();
		for (String userCode : userCodes.split(Text.COMMA)) {
			for (MenuDTO menuDTO : namespaceMenuList) {
				userNamespaceMenuMap.put(menuDTO.getCode() + Text.UNDER_SCORE + userCode, menuDTO);
			}
		}
		return userNamespaceMenuMap;
	}

	private Map<String, MenuDTO> getNamespacePrivilegeMap(AuthDTO authDTO, List<MenuDTO> namespaceMenuList) {
		Map<String, MenuDTO> namespaceMenuMap = new HashMap<>();
		// Namespace Menu List
		if (namespaceMenuList != null) {
			for (Iterator<MenuDTO> iterator = namespaceMenuList.iterator(); iterator.hasNext();) {
				MenuDTO roleMenuDTO = iterator.next();
				MenuDTO cachedto = getMenuDTOById(roleMenuDTO);
				// If deleted menu, remove it
				if (cachedto.getCode() == null || cachedto.getActiveFlag() != 1) {
					iterator.remove();
					continue;
				}
				roleMenuDTO.setCode(cachedto.getCode());
				if (roleMenuDTO.getMenuEvent() != null) {
					for (Iterator<MenuEventDTO> menuEventItr = roleMenuDTO.getMenuEvent().getList().iterator(); menuEventItr.hasNext();) {
						MenuEventDTO menuEventDTO = menuEventItr.next();
						boolean foundFlag = false;
						for (MenuEventDTO cacheEventDTO : cachedto.getMenuEvent().getList()) {
							if (menuEventDTO.getCode().equals(cacheEventDTO.getCode()) && cacheEventDTO.getActiveFlag() == 1) {
								foundFlag = true;
								break;
							}
						}
						if (!foundFlag) {
							menuEventItr.remove();
						}
					}
				}
				namespaceMenuMap.put(roleMenuDTO.getCode(), roleMenuDTO);
			}
		}
		return namespaceMenuMap;
	}

	private Map<String, MenuDTO> validateGroupMenu(List<MenuDTO> groupMenuList, Map<String, MenuDTO> namespaceMenuMap, Map<String, List<UserDTO>> groupWiseUserMap) {
		Map<String, MenuDTO> groupMenuMap = new HashMap<>();
		for (Iterator<MenuDTO> menuIterator = groupMenuList.iterator(); menuIterator.hasNext();) {
			MenuDTO roleMenuDTO = menuIterator.next();
			MenuDTO cachedto = getMenuDTOById(roleMenuDTO);
			roleMenuDTO.setCode(cachedto.getCode());
			if (namespaceMenuMap.get(roleMenuDTO.getCode()) == null) {
				menuIterator.remove();
				continue;
			}
			if (roleMenuDTO.getMenuEvent() != null) {
				for (Iterator<MenuEventDTO> eventIterator = roleMenuDTO.getMenuEvent().getList().iterator(); eventIterator.hasNext();) {
					MenuEventDTO menuEventDTO = eventIterator.next();

					boolean foundFlag = false;
					for (MenuEventDTO cacheEventDTO : namespaceMenuMap.get(roleMenuDTO.getCode()).getMenuEvent().getList()) {
						if (menuEventDTO.getCode().equals(cacheEventDTO.getCode())) {
							foundFlag = true;
							break;
						}
					}
					if (!foundFlag) {
						eventIterator.remove();
						continue;
					}
				}
			}
			List<UserDTO> userList = groupWiseUserMap.get(roleMenuDTO.getGroup().getCode());
			for (UserDTO userDTO : userList) {
				groupMenuMap.put(roleMenuDTO.getCode() + Text.UNDER_SCORE + userDTO.getGroup().getId() + Text.UNDER_SCORE + userDTO.getCode(), roleMenuDTO);
			}
		}
		return groupMenuMap;
	}

	private Map<String, MenuDTO> getUserMenuMap(List<MenuDTO> menuList) {
		Map<String, MenuDTO> userMenuMap = new HashMap<>();
		for (MenuDTO menuDTO : menuList) {
			userMenuMap.put(menuDTO.getCode() + Text.UNDER_SCORE + menuDTO.getUser().getCode(), menuDTO);
		}
		return userMenuMap;
	}

	private Map<String, List<UserDTO>> getGroupWiseUser(AuthDTO authDTO, String userCodes) {
		Map<String, List<UserDTO>> userGroupMap = new HashMap<>();
		for (String userCode : userCodes.split(Text.COMMA)) {
			UserDTO userDTO = new UserDTO();
			userDTO.setCode(userCode);
			userDTO = userService.getUser(authDTO, userDTO);
			userDTO.setName(userDTO.getName() + (StringUtil.isNotNull(userDTO.getLastname()) ? Text.SINGLE_SPACE + userDTO.getLastname() : Text.EMPTY));
			userDTO.setGroup(groupService.getGroup(authDTO, userDTO.getGroup()));
			String key = userDTO.getGroup().getCode();

			if (userGroupMap.get(key) != null) {
				List<UserDTO> existUserList = userGroupMap.get(key);
				existUserList.add(userDTO);
				userGroupMap.put(key, existUserList);
			}
			else {
				List<UserDTO> userList = new ArrayList<>();
				userList.add(userDTO);
				userGroupMap.put(userDTO.getGroup().getCode(), userList);
			}
		}
		return userGroupMap;
	}

	private SeverityPermissionTypeEM getPrivilege(AuthDTO authDTO, List<MenuEventEM> menuEventList) {
		SeverityPermissionTypeEM severity = null;
		if (authDTO.getPrivileges() != null && !authDTO.getPrivileges().isEmpty()) {
			for (MenuDTO menuDTO : authDTO.getPrivileges()) {
				for (MenuEventEM menuEventEM : menuEventList) {
					if (StringUtil.isNotNull(menuDTO.getActionCode()) && menuDTO.getActionCode().equals(menuEventEM.getActionCode())) {
						if (menuDTO.getMenuEvent() != null && !menuDTO.getMenuEvent().getList().isEmpty()) {
							for (MenuEventDTO eventDTO : menuDTO.getMenuEvent().getList()) {
								if (eventDTO != null && menuEventEM.getOperationCode().equals(eventDTO.getOperationCode())) {
									severity = SeverityPermissionTypeEM.getSeverityOperationCode(eventDTO.getOperationCode());
									break;
								}
							}
						}
					}
					if (severity != null) {
						break;
					}
				}
				if (severity != null) {
					break;
				}
			}
		}
		if (severity == null) {
			severity = SeverityPermissionTypeEM.PERMISSION_DEFAULT;
		}
		return severity;
	}
}
