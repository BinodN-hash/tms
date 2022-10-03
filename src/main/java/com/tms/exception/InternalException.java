package com.tms.exception;

public class InternalException extends ApplicationException{

	private static final long serialVersionUID = -1870614008805292128L;

	public InternalException() {
		super();
	}
	
	public InternalException(final String message) {
		super(message);
	}
	
	public InternalException(final String message, final Throwable cause) {
		super(message,cause);
	}
	
	public InternalException(final Throwable cause) {
		super(cause);
	}
}
