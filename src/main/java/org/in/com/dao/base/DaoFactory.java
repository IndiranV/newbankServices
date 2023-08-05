package org.in.com.dao.base;

import java.util.List;

import org.in.com.dto.AuthDTO;

public interface DaoFactory<T> {

	public T get(AuthDTO authDTO, T dto);

	public List<T> getAll(AuthDTO authDTO);

	public T Update(AuthDTO authDTO, T dto);

	public T insert(AuthDTO authDTO, T dto);

	public T delete(AuthDTO authDTO, T dto);

	public T disable(AuthDTO authDTO, T dto);

	public T enable(AuthDTO authDTO, T dto);

}
