package com.tms.controller.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ErrorResponse {

	private boolean status;
	private boolean sessionExpired;
	private List<Message> errors;
	private String statusCode;
}
