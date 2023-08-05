package org.in.com.exception;

public class DAOException extends BaseException{

	private static final long serialVersionUID = -4828842400272512087L;

	public DAOException(ErrorCode error) {
		super(error);
	}

	@Override
	void processException() {
		
	}

}
