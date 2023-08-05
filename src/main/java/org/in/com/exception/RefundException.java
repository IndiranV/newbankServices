package org.in.com.exception;

public class RefundException extends BaseException{

	private static final long serialVersionUID = 6396728248835639978L;

	public RefundException(ErrorCode errorCode) {
		super(errorCode);
	}

	@Override
	void processException() {
	}

}
