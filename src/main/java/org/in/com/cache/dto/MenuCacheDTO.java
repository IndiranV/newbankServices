package org.in.com.cache.dto;

import java.io.Serializable;

public class MenuCacheDTO implements Serializable {
	private static final long serialVersionUID = 5584012635244023201L;
	private int id;
	private int activeFlag;
	private String code;
	private String name;
	private String link;
	private String actionCode;
	private int exceptionFlag;
	private int enabledFlag;
	private int defaultFlag;
	private int displayFlag;
	private MenuCacheDTO lookupDTO;
	private MenuEventCacheDTO menuEventDTO;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getActiveFlag() {
		return activeFlag;
	}

	public void setActiveFlag(int activeFlag) {
		this.activeFlag = activeFlag;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getActionCode() {
		return actionCode;
	}

	public void setActionCode(String actionCode) {
		this.actionCode = actionCode;
	}

	public int getExceptionFlag() {
		return exceptionFlag;
	}

	public void setExceptionFlag(int exceptionFlag) {
		this.exceptionFlag = exceptionFlag;
	}

	public int getEnabledFlag() {
		return enabledFlag;
	}

	public void setEnabledFlag(int enabledFlag) {
		this.enabledFlag = enabledFlag;
	}

	public int getDefaultFlag() {
		return defaultFlag;
	}

	public void setDefaultFlag(int defaultFlag) {
		this.defaultFlag = defaultFlag;
	}

	public MenuCacheDTO getLookupDTO() {
		return lookupDTO;
	}

	public void setLookupDTO(MenuCacheDTO lookupDTO) {
		this.lookupDTO = lookupDTO;
	}

	public MenuEventCacheDTO getMenuEventDTO() {
		return menuEventDTO;
	}

	public void setMenuEventDTO(MenuEventCacheDTO menuEventDTO) {
		this.menuEventDTO = menuEventDTO;
	}

	public int getDisplayFlag() {
		return displayFlag;
	}

	public void setDisplayFlag(int displayFlag) {
		this.displayFlag = displayFlag;
	}
}
