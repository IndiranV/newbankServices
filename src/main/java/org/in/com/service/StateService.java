package org.in.com.service;

import java.util.List;

import org.in.com.dto.StateDTO;

public interface StateService {

	public List<StateDTO> getAll();

	public StateDTO getState(StateDTO state);
}
