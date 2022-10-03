package com.tms.exception;


public class InvalidPasswordException extends ApplicationException{

	
	private static final long serialVersionUID = -7863675227901158120L;

	public InvalidPasswordException(final String message) {
		super(message);
	}
	
	public InvalidPasswordException(final String message, final Throwable cause) {
		super(message,cause);
	}
	

}
