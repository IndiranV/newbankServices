package org.in.com.exception;

public class PaymentResponseException extends BaseException{

	private static final long serialVersionUID = -1163539738143461770L;

	public PaymentResponseException(ErrorCode error) {
		super(error);
	}

	@Override
	void processException() {
		
	}
	

	
}
