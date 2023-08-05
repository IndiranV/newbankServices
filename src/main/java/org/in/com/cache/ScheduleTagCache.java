package org.in.com.cache;

import java.util.ArrayList;
import java.util.List;

import org.in.com.dao.ScheduleTagDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleTagDTO;

import net.sf.ehcache.Element;

public class ScheduleTagCache {
	private static String CACHEKEY = "SHTAG_";

	protected List<ScheduleTagDTO> getScheduleTags(AuthDTO authDTO) {
		List<ScheduleTagDTO> scheduleTags = new ArrayList<ScheduleTagDTO>();
		String key = CACHEKEY + authDTO.getNamespaceCode();
		Element elementKey = EhcacheManager.getScheduleTagEhCache().get(key);
		if (elementKey != null) {
			scheduleTags = (List<ScheduleTagDTO>) elementKey.getObjectValue();
		}
		else {
			ScheduleTagDAO dao = new ScheduleTagDAO();
			List<ScheduleTagDTO> list = dao.getAll(authDTO);
			Element element = new Element(key, list);
			EhcacheManager.getScheduleTagEhCache().put(element);
		}
		return scheduleTags;
	}

	protected void removeScheduleTags(AuthDTO authDTO) {
		String key = CACHEKEY + authDTO.getNamespaceCode();
		EhcacheManager.getScheduleTagEhCache().remove(key);
	}

}
