package com.tms.exception;


public class ValidationException extends ApplicationException{

	private static final long serialVersionUID = -6387485447997701874L;

	public ValidationException(final String message) {
		super(message);
	}
	
	public ValidationException(final String message, final Throwable cause) {
		super(message,cause);
	}
	

}
