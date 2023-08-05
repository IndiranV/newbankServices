package org.in.com.dto;

import java.util.Map;

import lombok.Data;
import net.sf.json.JSONObject;

@Data
public class ProxyDTO {
	private String method;
	private Map<String, String> header;
	private JSONObject data;
	private String url;

}
