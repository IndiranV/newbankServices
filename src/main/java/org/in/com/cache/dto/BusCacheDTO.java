package org.in.com.cache.dto;

import java.io.Serializable;
import java.util.List;

public class BusCacheDTO implements Serializable {
	private static final long serialVersionUID = -2463462794581341438L;
	private int id;
	private int activeFlag;
	private String code;
	private String name;
	private String categoryCode;
	private String displayName;
	private int seatCount;
	private List<BusSeatLayoutCacheDTO> busSeatLayoutDTO;

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

	public String getCategoryCode() {
		return categoryCode;
	}

	public void setCategoryCode(String categoryCode) {
		this.categoryCode = categoryCode;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public int getSeatCount() {
		return seatCount;
	}

	public void setSeatCount(int seatCount) {
		this.seatCount = seatCount;
	}

	public List<BusSeatLayoutCacheDTO> getBusSeatLayoutDTO() {
		return busSeatLayoutDTO;
	}

	public void setBusSeatLayoutDTO(List<BusSeatLayoutCacheDTO> busSeatLayoutDTO) {
		this.busSeatLayoutDTO = busSeatLayoutDTO;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
