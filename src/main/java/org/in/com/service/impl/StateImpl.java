package org.in.com.service.impl;

import java.util.List;

import org.in.com.cache.StateCache;
import org.in.com.dao.StateDAO;
import org.in.com.dto.StateDTO;
import org.in.com.service.StateService;
import org.springframework.stereotype.Service;

@Service
public class StateImpl extends StateCache implements StateService {

	public List<StateDTO> getAll() {
		StateDAO dao = new StateDAO();
		List<StateDTO> list = (List<StateDTO>) dao.getAllStates();
		return list;
	}

	public StateDTO getState(StateDTO state) {
		return super.getState(state);
	}
}
