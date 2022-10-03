package com.tms.util;


public class WebHelper {
	public static WebResponse responsePayload(Object payload){
		WebResponse webResponse = new WebResponse();
		webResponse.setStatus(true);
		webResponse.setPayload(payload);
		return webResponse;
	}
	
	public static WebResponse responseError(String msg){
		WebResponse webResponse = new WebResponse();
		webResponse.setMessage(msg);
		return webResponse;
	}
	
	public static WebResponse responseSuccess(String msg){
		WebResponse webResponse = new WebResponse();
		webResponse.setStatus(true);
		webResponse.setMessage(msg);
		return webResponse;
	}
	

}
