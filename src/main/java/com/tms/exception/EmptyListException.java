package com.tms.exception;


public class EmptyListException extends RuntimeException{

	private static final long serialVersionUID = -1256484643567813622L;

	public EmptyListException(final String message) {
		super(message);
	}
}
