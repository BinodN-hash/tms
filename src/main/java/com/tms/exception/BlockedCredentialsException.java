package com.tms.exception;


public class BlockedCredentialsException extends ApplicationException{

	private static final long serialVersionUID = 531808135300187837L;

	public BlockedCredentialsException(final String message) {
		super(message);
	}
	
	public BlockedCredentialsException(final String message, final Throwable cause) {
		super(message,cause);
	}
	

}
