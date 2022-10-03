package com.tms.exception;

public final class InvalidOldPasswordException extends RuntimeException {

	private static final long serialVersionUID = -2537059584741740285L;

	public InvalidOldPasswordException() {
		super();
	}

	public InvalidOldPasswordException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InvalidOldPasswordException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidOldPasswordException(String message) {
		super(message);
	}

	public InvalidOldPasswordException(Throwable cause) {
		super(cause);
	}
	
}
