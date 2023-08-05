/**
 *
 */
package org.in.com.exception;

import org.in.com.controller.web.io.ResponseIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public ResponseEntity<Object> handleServiceException(ServiceException e, WebRequest request) {
		ResponseIO<Object> response;
		if (e.getErrorCode() != null) {
			response = ResponseIO.failure(e.getErrorCode(), e.getData());
		}
		else {
			response = ResponseIO.failure("500", e.getMessage());
		}
		return handleExceptionInternal(e, response, new HttpHeaders(), HttpStatus.OK, request);
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public ResponseEntity<Object> handleNullPointerException(NullPointerException e, WebRequest request) {
		logger.error("Defensive check issue - Null Pointer Access - ", e);
		e.printStackTrace();
		ResponseIO<Object> response = ResponseIO.failure(ErrorCode.UNDEFINE_EXCEPTION);
		return handleExceptionInternal(e, response, new HttpHeaders(), HttpStatus.OK, request);
	}

}
