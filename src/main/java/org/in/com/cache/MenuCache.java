package org.in.com.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.in.com.dao.MenuDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.MenuDTO;
import org.in.com.dto.MenuEventDTO;
import org.in.com.dto.UserDTO;
import org.in.com.utils.StringUtil;

import net.sf.ehcache.Element;

public class MenuCache {
	private static String CACHEKEY = "MNMN";

	public void reload() {
		MenuDAO dao = new MenuDAO();
		List<MenuDTO> list = (List<MenuDTO>) dao.getAllMenu(null);
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
		EhcacheManager.getMenuEhCache().removeAll();
		for (MenuDTO menuDTO : list) {
			String key = CACHEKEY + "_" + menuDTO.getId();
			Element elementKey = new Element(key, menuDTO.getCode().toString());
			Element element = new Element(menuDTO.getCode(), menuDTO);
			EhcacheManager.getMenuEhCache().put(elementKey);
			EhcacheManager.getMenuEhCache().put(element);
		}
	}

	public MenuDTO getMenuDTO(MenuDTO menuDTO) {
		Element element = EhcacheManager.getMenuEhCache().get(menuDTO.getCode());
		if (element != null) {
			menuDTO = (MenuDTO) element.getObjectValue();
		}
		else {
			MenuDAO menuDAO = new MenuDAO();
			menuDTO = menuDAO.getMenuDTO(menuDTO);
			if (menuDTO != null) {
				element = new Element(menuDTO.getCode(), menuDTO);
				EhcacheManager.getMenuEhCache().put(element);
			}
		}

		return menuDTO;
	}

	protected MenuDTO getMenuDTOById(MenuDTO menuDTO) {
		String menuCode = null;
		if (menuDTO != null) {
			String key = CACHEKEY + "_" + menuDTO.getId();
			Element elementKey = EhcacheManager.getMenuEhCache().get(key);
			if (elementKey != null) {
				menuCode = (String) elementKey.getObjectValue();
				menuDTO.setCode(menuCode);
			}
			menuDTO = getMenuDTO(menuDTO);
			if (elementKey == null && StringUtil.isNotNull(menuDTO.getCode()) && menuDTO.getId() != 0) {
				key = CACHEKEY + "_" + menuDTO.getId();
				elementKey = new Element(key, menuDTO.getCode());
				EhcacheManager.getMenuEhCache().put(elementKey);
			}
		}
		return menuDTO;
	}

	protected MenuDTO getMenuDTO(String menuCode) {
		MenuDTO menuDTO = new MenuDTO();
		menuDTO.setCode(menuCode);
		return getMenuDTO(menuDTO);
	}

	protected void removeMenuDTO(AuthDTO authDTO, MenuDTO menuDTO) {
		EhcacheManager.getMenuEhCache().remove(menuDTO.getCode());
	}

	public List<MenuEventDTO> getUserPreDefinedPrivilegesCache(AuthDTO authDTO, UserDTO userDTO) {
		List<MenuEventDTO> menuEvents = null;
		Element element = EhcacheManager.getUserMenuCache().get("MNUSR_" + userDTO.getCode());
		if (element != null) {
			menuEvents = (List<MenuEventDTO>) element.getObjectValue();
		}
		return menuEvents;
	}

	public void putUserPreDefinedPrivilegesCache(AuthDTO authDTO, UserDTO userDTO, List<MenuEventDTO> menuEvents) {
		Element element = new Element("MNUSR_" + userDTO.getCode(), menuEvents);
		EhcacheManager.getUserMenuCache().put(element);
	}

	public void removeUserMenuCache() {
		EhcacheManager.getUserMenuCache().removeAll();
	}
}
