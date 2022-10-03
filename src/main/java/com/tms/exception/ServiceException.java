package com.tms.exception;

public class ServiceException extends RuntimeException{

	private static final long serialVersionUID = 8815514879134358063L;

	public ServiceException(String message) {
		super(message);
	}
}
