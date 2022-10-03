package com.tms.exception;


import com.tms.controller.model.Message;
import com.tms.controller.model.RestResponse;
import com.tms.properties.SetUpProperties;
import com.tms.util.RestHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import static org.springframework.http.HttpStatus.*;


@ControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

	private static SetUpProperties setup = SetUpProperties.getInstance();
	
	@Autowired
	private Environment env;
	
	@ExceptionHandler({ InvalidCredentialException.class })
	@ResponseBody
	public ResponseEntity<RestResponse> handleInvalidCredentialExceptionException(final InvalidCredentialException ex) {
		Message msg = resolveError(ex);
		return RestHelper.responseError(msg.getMessage(), UNAUTHORIZED);
	}
	
	@ExceptionHandler({ BlockedCredentialsException.class })
	@ResponseBody
	public ResponseEntity<RestResponse> handleBlockedCredentialsException(final BlockedCredentialsException ex) {
		Message msg = resolveError(ex);
		return RestHelper.responseError(msg.getMessage(), UNAUTHORIZED);
	}

	@ExceptionHandler({ ValidationException.class })
	@ResponseBody
	public ResponseEntity<RestResponse> handleValidationException(final Exception ex) {
		Message msg = resolveError(ex);
		return RestHelper.responseError(msg.getMessage(), BAD_REQUEST);
	}
	
	@ExceptionHandler({ ApplicationException.class })
	@ResponseBody
	public ResponseEntity<RestResponse> handleApplicationException(final ApplicationException ex) {
		Message msg = resolveError(ex);
		return RestHelper.responseError(msg.getMessage(), BAD_REQUEST);
	}
	
	@ExceptionHandler({ MaxUploadSizeExceededException.class })
	@ResponseBody
	public ResponseEntity<RestResponse> handleMaxUploadSizeExceededException(final MaxUploadSizeExceededException ex, final String statusCode) {
		
		return RestHelper.responseError(String.format(setup.getProperty("settings.message.image.upload.size.exceeded"), env.getProperty("spring.servlet.multipart.max-file-size")), BAD_REQUEST);
	}
	
	@ExceptionHandler({ InternalException.class, Exception.class })
	@ResponseBody
	public ResponseEntity<RestResponse> handleInternalException(final Exception ex) {
		Message msg = resolveError(ex);
		return RestHelper.responseError(msg.getMessage(), SERVICE_UNAVAILABLE);
	}
	@ExceptionHandler({ RecordNotFoundException.class })
	@ResponseBody
	public ResponseEntity<RestResponse> handleRecordNotFoundException(final RecordNotFoundException ex) {
		Message msg = resolveError(ex);
		return RestHelper.responseError(msg.getMessage(), NOT_FOUND);
	}
	
	@ExceptionHandler({ EmptyListException.class })
	@ResponseBody
	public ResponseEntity<RestResponse> handleEmptyListException(final Exception ex) {
		Message msg = resolveError(ex);
		return RestHelper.responseError(msg.getMessage(), NOT_FOUND);
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		RestResponse restResponse = new RestResponse();
		ex.getBindingResult().getAllErrors().forEach((error) -> {
			restResponse.addError(error.getDefaultMessage());
		});
		restResponse.setStatus(false);

		return new ResponseEntity<>(restResponse, HttpStatus.BAD_REQUEST);
	}
	
	private static Message resolveError(final Exception t) {
		return new Message(t.getMessage());
	}

}
