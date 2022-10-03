package com.tms.exception;


public class InvalidCredentialException extends ApplicationException{

	private static final long serialVersionUID = 531808135300187837L;


	public InvalidCredentialException(final String message) {
		super(message);
	}
	
	public InvalidCredentialException(final String message, final Throwable cause) {
		super(message,cause);
	}

}
