package org.in.com.cache.dto;

import java.io.Serializable;

public class BusSeatLayoutCacheDTO implements Serializable {
	private static final long serialVersionUID = 2445535067548380077L;
	private int id;
	private int activeFlag;
	private int rowPos;
	private int colPos;
	private int sequence;
	private int layer;
	private int orientation;
	private String code;
	private String name;
	private String busSeatTypeCode;

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

	public int getRowPos() {
		return rowPos;
	}

	public void setRowPos(int rowPos) {
		this.rowPos = rowPos;
	}

	public int getColPos() {
		return colPos;
	}

	public void setColPos(int colPos) {
		this.colPos = colPos;
	}

	public int getLayer() {
		return layer;
	}

	public void setLayer(int layer) {
		this.layer = layer;
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

	public String getBusSeatTypeCode() {
		return busSeatTypeCode;
	}

	public void setBusSeatTypeCode(String busSeatTypeCode) {
		this.busSeatTypeCode = busSeatTypeCode;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public int getSequence() {
		return sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	public int getOrientation() {
		return orientation;
	}

	public void setOrientation(int orientation) {
		this.orientation = orientation;
	}

}
