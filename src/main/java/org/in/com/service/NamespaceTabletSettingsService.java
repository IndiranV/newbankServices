package org.in.com.service;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.NamespaceTabletSettingsDTO;

import net.sf.json.JSONObject;

public interface NamespaceTabletSettingsService extends BaseService<NamespaceTabletSettingsDTO> {

	public NamespaceTabletSettingsDTO getNamespaceTabletSetting(AuthDTO authDTO);

	public JSONObject getNamespaceTabletSettingJson(AuthDTO authDTO);
	
	public NamespaceTabletSettingsDTO getNamespaceTabletSettings(AuthDTO authDTO);
}
