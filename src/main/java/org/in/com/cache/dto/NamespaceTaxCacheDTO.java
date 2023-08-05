package org.in.com.cache.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class NamespaceTaxCacheDTO implements Serializable {
	private static final long serialVersionUID = -3898647470154630106L;
	private int id;
	private String code;
	private String cgstValue;
	private String sgstValue;
	private String ugstValue;
	private String igstValue;
	private String gstin;
	private String tradeName;
	private int stateId;
}
