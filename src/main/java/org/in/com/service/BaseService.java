package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;

public interface BaseService<T> {

	public List<T> get(AuthDTO authDTO, T dto);

	public List<T> getAll(AuthDTO authDTO);

	public T Update(AuthDTO authDTO, T dto);

}
