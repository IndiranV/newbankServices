package org.in.com.exception;

public class PaymentRequestException extends BaseException{

	private static final long serialVersionUID = 8453236182251098507L;

	public PaymentRequestException(ErrorCode error){
		super(error);
	}
	
	@Override
	public void processException() {
		
	}
}
