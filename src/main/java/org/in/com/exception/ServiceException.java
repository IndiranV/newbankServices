package org.in.com.exception;

public class ServiceException extends RuntimeException {

	/** System generated Serial Version UID. */
	private static final long serialVersionUID = 7737856834032610669L;

	private Object data;

	private ErrorCode errorCode;

	/**
	 * Default Constructor
	 */
	public ServiceException() {

	}

	/**
	 * @param errorCode
	 *            code
	 */
	public ServiceException(Integer errorCode) {
		this(ErrorCode.getErrorCode(errorCode.toString()), null);
	}

	/**
	 * @param message
	 */
	public ServiceException(String message) {
		this(message, null);
	}

	public ServiceException(ErrorCode errorCode) {
		this.errorCode = errorCode;
	}

	/**
	 * Used to send failure message with response data
	 * 
	 * @param errorCode
	 * @param data
	 */
	public ServiceException(ErrorCode errorCode, Object data) {
		this.errorCode = errorCode;
		this.data = data;
	}

	/**
	 * @param message
	 * @param data
	 */
	public ServiceException(String message, Object data) {
		super(message);
		this.data = data;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public ErrorCode getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(ErrorCode errorCode) {
		this.errorCode = errorCode;
	}

	public String toString() {
		if (errorCode != null) {
			return errorCode.getCode() + "-" + errorCode.getMessage();
		}
		return null;
	}

}
