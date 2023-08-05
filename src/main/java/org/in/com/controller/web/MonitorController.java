package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.List;

import net.sf.ehcache.Cache;

import org.in.com.cache.EhcacheManager;
import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.dto.AuthDTO;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/{authtoken}/monitor")
public class MonitorController extends BaseController {

	@RequestMapping(value = "/ehcache", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<BaseIO>> getAllehcache(@PathVariable("authtoken") String authtoken) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<BaseIO> ehcacheList = new ArrayList<BaseIO>();
		if (authDTO != null) {
			String[] nameList = EhcacheManager.getCacheManager().getCacheNames();
			for (int count = 0; count < nameList.length; count++) {
				BaseIO cacheDTO = new BaseIO();
				cacheDTO.setName(nameList[count]);
				Cache cacheAttributes = EhcacheManager.getCacheManager().getCache(nameList[count]);
				if (cacheAttributes != null) {
					cacheDTO.setActiveFlag(cacheAttributes.getKeys().size());
				}
				else {
					cacheDTO.setActiveFlag(0);
				}
				ehcacheList.add(cacheDTO);
			}
		}
		return ResponseIO.success(ehcacheList);
	}
}
