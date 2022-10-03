package com.tms.util;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WebResponse {
	
	private Boolean status;
	private String message;
	private Object payload;

}
