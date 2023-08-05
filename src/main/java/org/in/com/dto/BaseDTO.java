package org.in.com.dto;

import java.util.List;

import lombok.Data;

@Data
public class BaseDTO<T>  {
 	private int id;
	private String code;
	private String name;
	private int activeFlag;
	private List<T> list;
 
}
