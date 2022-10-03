package com.tms.exception;


public class RecordNotFoundException extends ApplicationException{

	private static final long serialVersionUID = -1256484643567813622L;

	public RecordNotFoundException(final String message) {
		super(message);
	}
}
