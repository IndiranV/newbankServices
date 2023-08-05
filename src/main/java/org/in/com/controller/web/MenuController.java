package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.in.com.constants.Numeric;
import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.GroupIO;
import org.in.com.controller.web.io.MenuEventIO;
import org.in.com.controller.web.io.MenuIO;
import org.in.com.controller.web.io.ProductIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.controller.web.io.UserIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.MenuDTO;
import org.in.com.dto.MenuEventDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.ProductTypeEM;
import org.in.com.dto.enumeration.SeverityPermissionTypeEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.MenuService;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/{authtoken}/menu")
public class MenuController extends BaseController {
	@Autowired
	MenuService menuService;

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<MenuIO>> getAllMenu(@PathVariable("authtoken") String authtoken, @RequestParam(required = false, defaultValue = "-1") int activeFlag) throws Exception {
		List<MenuIO> menuList = new ArrayList<MenuIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<MenuDTO> list = (List<MenuDTO>) menuService.getAll(authDTO);
			for (MenuDTO menuDTO : list) {
				if (activeFlag != -1 && activeFlag != menuDTO.getActiveFlag()) {
					continue;
				}
				MenuIO menuio = new MenuIO();
				menuio.setCode(menuDTO.getCode());
				menuio.setName(menuDTO.getName());
				menuio.setLink(menuDTO.getLink());
				BaseIO severity = new BaseIO();
				severity.setCode(menuDTO.getSeverity().getCode());
				severity.setName(menuDTO.getSeverity().getName());
				menuio.setSeverity(severity);
				menuio.setDisplayFlag(menuDTO.getDisplayFlag());
				menuio.setActionCode(menuDTO.getActionCode());
				menuio.setActionCode(menuDTO.getActionCode());
				List<String> tagList = new ArrayList<>();
				for (String tag : menuDTO.getTagList()) {
					tagList.add(tag);
				}
				menuio.setTagList(tagList);

				BaseIO productIO = new BaseIO();
				productIO.setCode(menuDTO.getProductType().getCode());
				productIO.setName(menuDTO.getProductType().getName());
				productIO.setActiveFlag(1);
				menuio.setProductType(productIO);

				if (menuDTO.getLookup() != null) {
					MenuIO lookup = new MenuIO();
					lookup.setCode(menuDTO.getLookup().getCode());
					lookup.setName(menuDTO.getLookup().getName());
					lookup.setLink(menuDTO.getLookup().getLink());
					menuio.setLookup(lookup);
				}
				List<MenuEventIO> eventList = new ArrayList<MenuEventIO>();
				for (MenuEventDTO eventDTO : menuDTO.getMenuEvent().getList()) {
					MenuEventIO eventIO = new MenuEventIO();
					eventIO.setCode(eventDTO.getCode());
					BaseIO eventSeverity = new BaseIO();
					eventSeverity.setCode(eventDTO.getSeverity().getCode());
					eventSeverity.setName(eventDTO.getSeverity().getName());
					eventIO.setSeverity(eventSeverity);
					eventIO.setActiveFlag(eventDTO.getActiveFlag());
					eventIO.setName(eventDTO.getName());
					eventIO.setOperationCode(eventDTO.getOperationCode());
					eventIO.setAttr1Value(eventDTO.getAttr1Value());
					eventIO.setPermissionType(MenuEventDTO.getPermission(eventDTO.getPermissionFlag()));
					if (StringUtil.isNull(eventIO.getName())) {
						continue;
					}
					eventList.add(eventIO);
				}
				menuio.setEventList(eventList);
				menuio.setActiveFlag(menuDTO.getActiveFlag());
				menuList.add(menuio);
			}
		}
		return ResponseIO.success(menuList);
	}

	@RequestMapping(value = "/zonesync", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<MenuIO>> getAllforZoneSync(@PathVariable("authtoken") String authtoken, String syncDate) throws Exception {
		List<MenuIO> menuList = new ArrayList<MenuIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<MenuDTO> list = (List<MenuDTO>) menuService.getAllforZoneSync(authDTO, syncDate);
			for (MenuDTO menuDTO : list) {
				MenuIO menuio = new MenuIO();
				menuio.setCode(menuDTO.getCode());
				menuio.setName(menuDTO.getName());
				menuio.setLink(menuDTO.getLink());
				menuio.setDisplayFlag(menuDTO.getDisplayFlag());
				BaseIO severity = new BaseIO();
				severity.setCode(menuDTO.getSeverity().getCode());
				severity.setName(menuDTO.getSeverity().getName());
				menuio.setSeverity(severity);
				menuio.setActionCode(menuDTO.getActionCode());
				List<String> tagList = new ArrayList<>();
				for (String tag : menuDTO.getTagList()) {
					tagList.add(tag);
				}
				menuio.setTagList(tagList);

				BaseIO productIO = new BaseIO();
				productIO.setCode(menuDTO.getProductType().getCode());
				productIO.setName(menuDTO.getProductType().getName());
				productIO.setActiveFlag(1);
				menuio.setProductType(productIO);

				if (menuDTO.getLookup() != null) {
					MenuIO lookup = new MenuIO();
					lookup.setCode(menuDTO.getLookup().getCode());
					lookup.setName(menuDTO.getLookup().getName());
					lookup.setLink(menuDTO.getLookup().getLink());
					menuio.setLookup(lookup);
				}
				List<MenuEventIO> eventList = new ArrayList<MenuEventIO>();
				for (MenuEventDTO eventDTO : menuDTO.getMenuEvent().getList()) {
					MenuEventIO eventIO = new MenuEventIO();
					eventIO.setCode(eventDTO.getCode());
					BaseIO eventSeverity = new BaseIO();
					eventSeverity.setCode(eventDTO.getSeverity().getCode());
					eventSeverity.setName(eventDTO.getSeverity().getName());
					eventIO.setSeverity(eventSeverity);
					eventIO.setActiveFlag(eventDTO.getActiveFlag());
					eventIO.setName(eventDTO.getName());
					eventIO.setOperationCode(eventDTO.getOperationCode());
					eventIO.setAttr1Value(eventDTO.getAttr1Value());
					eventIO.setPermissionType(MenuEventDTO.getPermission(eventDTO.getPermissionFlag()));
					eventList.add(eventIO);
				}
				menuio.setEventList(eventList);
				menuio.setActiveFlag(menuDTO.getActiveFlag());
				menuList.add(menuio);
			}
		}
		return ResponseIO.success(menuList);
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<MenuIO> getMenuUID(@PathVariable("authtoken") String authtoken, @RequestBody MenuIO menuIO) throws Exception {
		MenuIO updatedMenuIO = new MenuIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			MenuDTO menuDTO = new MenuDTO();
			menuDTO.setCode(menuIO.getCode());
			menuDTO.setName(menuIO.getName());
			menuDTO.setLink(menuIO.getLink());
			menuDTO.setActionCode(menuIO.getActionCode());
			List<String> tagList = new ArrayList<>();
			if (menuIO.getTagList() != null) {
				for (String tag : menuIO.getTagList()) {
					tagList.add(tag);
				}
			}
			menuDTO.setTagList(tagList);
			menuDTO.setSeverity(menuIO.getSeverity() != null ? SeverityPermissionTypeEM.getSeverityPermissionTypeEM(menuIO.getSeverity().getCode()) : SeverityPermissionTypeEM.NOT_AVAILABLE);
			menuDTO.setActiveFlag(menuIO.getActiveFlag());
			menuDTO.setProductType(ProductTypeEM.getProductTypeEM(menuIO.getProductType() != null ? menuIO.getProductType().getCode() : null));
			menuDTO.setDisplayFlag(menuIO.getDisplayFlag());
			MenuDTO lookupmenuDTO = new MenuDTO();
			lookupmenuDTO.setCode(menuIO.getLookup() != null ? menuIO.getLookup().getCode() : null);
			menuDTO.setLookup(lookupmenuDTO);
			List<MenuEventDTO> eventList = new ArrayList<>();
			if (menuIO.getEventList() != null && !menuIO.getEventList().isEmpty()) {
				for (MenuEventIO menuEventIO : menuIO.getEventList()) {
					MenuEventDTO eventDTO = new MenuEventDTO();
					eventDTO.setCode(menuEventIO.getCode());
					eventDTO.setOperationCode(menuEventIO.getOperationCode());
					eventDTO.setAttr1Value(menuEventIO.getAttr1Value());
					eventDTO.setName(menuEventIO.getName());
					eventDTO.setPermissionFlag(MenuEventDTO.getPermission(menuEventIO.getPermissionType()));
					eventDTO.setSeverity(menuEventIO.getSeverity() != null ? SeverityPermissionTypeEM.getSeverityPermissionTypeEM(menuEventIO.getSeverity().getCode()) : SeverityPermissionTypeEM.NOT_AVAILABLE);
					eventDTO.setActiveFlag(menuEventIO.getActiveFlag());
					if (StringUtil.isNull(menuEventIO.getName())) {
						continue;
					}
					eventList.add(eventDTO);
				}
			}
			MenuEventDTO eventDTO = new MenuEventDTO();
			eventDTO.setList(eventList);
			menuDTO.setMenuEvent(eventDTO);
			menuService.Update(authDTO, menuDTO);
			if (menuDTO.getCode() != null) {
				updatedMenuIO.setCode(menuDTO.getCode());
				updatedMenuIO.setActiveFlag(menuDTO.getActiveFlag());
			}
		}
		return ResponseIO.success(updatedMenuIO);
	}

	@RequestMapping(value = "/role/{rolecode}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<MenuIO>> getRoleMenu(@PathVariable("authtoken") String authtoken, @PathVariable("rolecode") String rolecode, @RequestBody MenuIO menu) throws Exception {
		List<MenuIO> menuList = new ArrayList<MenuIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			MenuDTO menuDTO = new MenuDTO();
			if ("GR".equals(rolecode)) {
				GroupDTO groupDTO = new GroupDTO();
				groupDTO.setCode(menu.getGroup().getCode());
				menuDTO.setGroup(groupDTO);
			}
			else if ("UR".equals(rolecode)) {
				UserDTO userDTO = new UserDTO();
				userDTO.setCode(menu.getUser().getCode());
				menuDTO.setUser(userDTO);
			}

			List<MenuDTO> list = menuService.getRoleMenu(authDTO, menuDTO, rolecode);
			for (MenuDTO dto : list) {
				MenuIO menuio = new MenuIO();
				menuio.setCode(dto.getCode());
				menuio.setName(dto.getName());
				menuio.setLink(dto.getLink());
				menuio.setEnabledFlag(dto.getEnabledFlag());
				menuio.setActionCode(dto.getActionCode());
				List<String> tagList = new ArrayList<>();
				for (String tag : dto.getTagList()) {
					tagList.add(tag);
				}
				menuio.setTagList(tagList);
				menuio.setDisplayFlag(dto.getDisplayFlag());
				menuio.setExceptionFlag(dto.getExceptionFlag());

				BaseIO productIO = new BaseIO();
				productIO.setCode(dto.getProductType().getCode());
				productIO.setName(dto.getProductType().getName());
				productIO.setActiveFlag(1);
				menuio.setProductType(productIO);

				if (dto.getLookup() != null) {
					MenuIO lookup = new MenuIO();
					lookup.setCode(dto.getLookup().getCode());
					lookup.setName(dto.getLookup().getName());
					lookup.setLink(dto.getLookup().getLink());
					menuio.setLookup(lookup);
				}
				List<MenuEventIO> eventList = new ArrayList<MenuEventIO>();
				for (MenuEventDTO eventDTO : dto.getMenuEvent().getList()) {
					MenuEventIO eventIO = new MenuEventIO();
					eventIO.setCode(eventDTO.getCode());
					eventIO.setName(eventDTO.getName());
					eventIO.setEnabledFlag(eventDTO.getEnabledFlag());
					eventIO.setOperationCode(eventDTO.getOperationCode() != null ? eventDTO.getOperationCode() : "NA");
					eventIO.setPermissionType(MenuEventDTO.getPermission(eventDTO.getPermissionFlag()));
					eventList.add(eventIO);
				}
				if (dto.getGroup() != null) {
					GroupIO groupIO = new GroupIO();
					groupIO.setCode(dto.getGroup().getCode());
					groupIO.setName(dto.getGroup().getName());
					menuio.setGroup(groupIO);
				}
				if (dto.getUser() != null) {
					UserIO userIO = new UserIO();
					userIO.setCode(dto.getUser().getCode());
					userIO.setName(dto.getUser().getName());
					menuio.setUser(userIO);
				}
				menuio.setEventList(eventList);
				menuio.setActiveFlag(dto.getActiveFlag());
				menuList.add(menuio);
			}
		}
		return ResponseIO.success(menuList);
	}

	@RequestMapping(value = "/role/{rolecode}/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<MenuIO> updateRoleMenu(@PathVariable("authtoken") String authtoken, @PathVariable("rolecode") String rolecode, @RequestBody MenuIO menuIO) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			MenuDTO menuDTO = new MenuDTO();
			menuDTO.setCode(menuIO.getCode());
			menuDTO.setActiveFlag(menuIO.getActiveFlag());
			menuDTO.setExceptionFlag(menuIO.getExceptionFlag());
			List<MenuEventDTO> eventList = new ArrayList<>();
			if (menuIO.getEventList() != null && !menuIO.getEventList().isEmpty()) {
				for (MenuEventIO menuEventIO : menuIO.getEventList()) {
					if (StringUtil.isNotNull(menuEventIO.getCode())) {
						MenuEventDTO eventDTO = new MenuEventDTO();
						eventDTO.setCode(menuEventIO.getCode());
						eventDTO.setActiveFlag(menuEventIO.getActiveFlag());
						eventList.add(eventDTO);
					}
				}
			}
			if (menuIO.getGroup() != null && "GR".equals(rolecode)) {
				GroupDTO groupDTO = new GroupDTO();
				groupDTO.setCode(menuIO.getGroup().getCode());
				menuDTO.setGroup(groupDTO);
			}
			if (menuIO.getUser() != null && "UR".equals(rolecode)) {
				UserDTO userDTO = new UserDTO();
				userDTO.setCode(menuIO.getUser().getCode());
				menuDTO.setUser(userDTO);
			}
			MenuEventDTO eventDTO = new MenuEventDTO();
			eventDTO.setList(eventList);
			menuDTO.setMenuEvent(eventDTO);
			menuService.roleMenuIUD(authDTO, menuDTO);
			if (menuDTO.getCode() != null) {
				menuIO.setCode(menuDTO.getCode());
				menuIO.setActiveFlag(menuDTO.getActiveFlag());
			}
		}
		return ResponseIO.success(menuIO);
	}

	@RequestMapping(value = "/user/{userCode}/restore/default", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> restoreDefault(@PathVariable("authtoken") String authtoken, @PathVariable("userCode") String userCode) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			UserDTO userDTO = new UserDTO();
			userDTO.setCode(userCode);
			menuService.restoreDefault(authDTO, userDTO);
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/product", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<ProductIO>> getBusSeatType(@PathVariable("authtoken") String authtoken) throws Exception {
		List<ProductIO> productList = new ArrayList<ProductIO>();
		ProductTypeEM[] list = ProductTypeEM.values();
		for (ProductTypeEM dto : list) {
			ProductIO productIO = new ProductIO();
			productIO.setCode(dto.getCode());
			productIO.setName(dto.getName());
			productIO.setDomainUrl(dto.getDomainUrl());
			productIO.setActiveFlag(1);
			productList.add(productIO);
		}
		return ResponseIO.success(productList);

	}

	@RequestMapping(value = "/{menuCode}/assign", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<BaseIO>> getRoleMenu(@PathVariable("authtoken") String authtoken, @PathVariable("menuCode") String menuCode) throws Exception {
		List<BaseIO> list = new ArrayList<>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		MenuDTO menuDTO = new MenuDTO();
		menuDTO.setCode(menuCode);

		List<MenuDTO> menuList = menuService.getRoleMenu(authDTO, menuDTO);

		for (MenuDTO menu : menuList) {
			if (menu.getUser() != null) {
				BaseIO user = new BaseIO();
				user.setCode(menu.getUser().getCode());
				user.setName(menu.getUser().getName());
				user.setActiveFlag(Numeric.ONE_INT);
				list.add(user);
			}
			if (menu.getGroup() != null) {
				BaseIO group = new BaseIO();
				group.setCode(menu.getGroup().getCode());
				group.setName(menu.getGroup().getName());
				group.setActiveFlag(Numeric.TWO_INT);
				list.add(group);
			}
		}
		return ResponseIO.success(list);
	}

	@RequestMapping(value = "/user/permission", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<Map<String, String>>> getUserPermissionReport(@PathVariable("authtoken") String authtoken, @RequestParam(required = true) String userCodes) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (StringUtil.isNull(userCodes)) {
			throw new ServiceException(ErrorCode.INVALID_USER_CODE);
		}
		List<Map<String, String>> results = menuService.getUserPermissionReport(authDTO, userCodes);
		return ResponseIO.success(results);
	}

}
