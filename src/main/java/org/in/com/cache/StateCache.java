package org.in.com.cache;

import net.sf.ehcache.Element;

import org.in.com.dao.StateDAO;
import org.in.com.dto.StateDTO;
import org.in.com.utils.StringUtil;

public class StateCache {
	private static String CACHEKEY = "STATE_";

	protected StateDTO getState(StateDTO state) {
		if (StringUtil.isNotNull(state.getCode())) {
			state = getStateDTObyCode(state);
		}
		else if (state.getId() != 0) {
			state = getStateDTObyId(state);
		}
		return state;
	}

	private StateDTO getStateDTObyCode(StateDTO state) {
		Element element = EhcacheManager.getStateEhCache().get(state.getCode());
		if (element != null) {
			state = (StateDTO) element.getObjectValue();
		}
		else {
			StateDAO dao = new StateDAO();
			state = dao.getStates(state);
			if (state.getId() != 0 && StringUtil.isNotNull(state.getCode())) {
				element = new Element(state.getCode(), state);
				EhcacheManager.getStateEhCache().put(element);
			}
		}
		return state;
	}

	private StateDTO getStateDTObyId(StateDTO state) {
		String key = CACHEKEY + state.getId();
		Element elementKey = EhcacheManager.getStateEhCache().get(key);
		if (elementKey != null) {
			String stateCode = (String) elementKey.getObjectValue();
			state.setCode(stateCode);
		}
		else {
			StateDAO dao = new StateDAO();
			state = dao.getStates(state);
			if (state.getId() != 0 && StringUtil.isNotNull(state.getCode())) {
				Element element = new Element(key, state.getCode());
				EhcacheManager.getStateEhCache().put(element);
			}
		}
		return getStateDTObyCode(state);
	}
}
