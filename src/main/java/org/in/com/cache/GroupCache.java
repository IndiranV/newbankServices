package org.in.com.cache;

import net.sf.ehcache.Element;

import org.in.com.cache.dto.GroupCacheDTO;
import org.in.com.dao.GroupDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.enumeration.UserRoleEM;
import org.in.com.utils.StringUtil;

public class GroupCache extends TaxCache {
	private static String CACHEKEY = "GRGR";

	protected GroupDTO getGroupDTO(AuthDTO authDTO, GroupDTO groupDTO) {
		Element element = EhcacheManager.getGroupEhCache().get(groupDTO.getCode());
		if (element != null) {
			GroupCacheDTO groupCacheDTO = (GroupCacheDTO) element.getObjectValue();
			bindGroupFromCache(groupCacheDTO, groupDTO);
		}
		else {
			GroupDAO groupDAO = new GroupDAO();
			groupDAO.getGroupDTO(authDTO, groupDTO);
			if (groupDTO != null && groupDTO.getId() != 0 && StringUtil.isNotNull(groupDTO.getCode())) {
				GroupCacheDTO groupCacheDTO = bindGroupToCache(groupDTO);
				element = new Element(groupDTO.getCode(), groupCacheDTO);
				EhcacheManager.getGroupEhCache().put(element);
			}
		}
		return groupDTO;
	}

	private GroupCacheDTO bindGroupToCache(GroupDTO groupDTO) {
		GroupCacheDTO groupCache = new GroupCacheDTO();
		groupCache.setId(groupDTO.getId());
		groupCache.setName(groupDTO.getName());
		groupCache.setCode(groupDTO.getCode());
		groupCache.setLevel(groupDTO.getLevel());
		groupCache.setRoleCode(groupDTO.getRole().getCode());
		groupCache.setColor(groupDTO.getColor());
		groupCache.setActiveFlag(groupDTO.getActiveFlag());
		return groupCache;
	}

	private void bindGroupFromCache(GroupCacheDTO groupCache, GroupDTO groupDTO) {
		groupDTO.setId(groupCache.getId());
		groupDTO.setName(groupCache.getName());
		groupDTO.setCode(groupCache.getCode());
		groupDTO.setColor(groupCache.getColor());
		groupDTO.setLevel(groupCache.getLevel());
		groupDTO.setRole(UserRoleEM.getUserRoleEM(groupCache.getRoleCode()));
		groupDTO.setActiveFlag(groupCache.getActiveFlag());
	}

	protected GroupDTO getGroupDTOById(AuthDTO authDTO, GroupDTO groupDTO) {
		String groupCode = null;
		String key = CACHEKEY + authDTO.getNamespace().getId() + "_" + groupDTO.getId();
		Element elementKey = EhcacheManager.getGroupEhCache().get(key);
		if (elementKey != null) {
			groupCode = (String) elementKey.getObjectValue();
			groupDTO.setCode(groupCode);
		}
		groupDTO = getGroupDTO(authDTO, groupDTO);
		if (elementKey == null && StringUtil.isNotNull(groupDTO.getCode()) && groupDTO.getId() != 0) {
			key = CACHEKEY + authDTO.getNamespace().getId() + "_" + groupDTO.getId();
			elementKey = new Element(key, groupDTO.getCode());
			EhcacheManager.getGroupEhCache().put(elementKey);
		}
		return groupDTO;
	}

	protected void removeGroupDTO(AuthDTO authDTO, GroupDTO groupDTO) {
		EhcacheManager.getGroupEhCache().remove(groupDTO.getCode());
	}

}
