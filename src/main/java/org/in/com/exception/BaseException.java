package org.in.com.exception;

public abstract class BaseException extends Exception {

	private static final long serialVersionUID = 1188021079833600917L;
	private ErrorCode error;
	
	public BaseException(ErrorCode error){
		this.error = error;
	}
	
	public ErrorCode getError(){
		return this.error;
	}
	
	public String getErrorCode() {
		return this.error != null ? this.error.getCode() : "Undefined Error";
	}
	public String getMessage() {
		return this.error != null ? this.error.getMessage(): "Undefined Error";
	}

	abstract void processException();
}
