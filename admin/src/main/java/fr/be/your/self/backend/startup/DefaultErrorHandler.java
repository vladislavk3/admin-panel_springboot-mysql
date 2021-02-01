package fr.be.your.self.backend.startup;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.be.your.self.backend.setting.Constants;
import fr.be.your.self.common.ErrorStatusCode;
import fr.be.your.self.dto.StatusResponse;
import fr.be.your.self.exception.BusinessException;
import fr.be.your.self.exception.InvalidException;
import fr.be.your.self.exception.ValidationException;
import fr.be.your.self.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class DefaultErrorHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultErrorHandler.class);
	
	private static final String API_URL_PREFIX = Constants.PATH.API_PREFIX + "/";
	private static final String ACCESS_DENIED_URL = Constants.PATH.ACCESS_DENIED;
	
	private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleJPAValidationError(HttpServletRequest request, HttpServletResponse response, 
    		MethodArgumentNotValidException exception) {
		logger.error("Validation error", exception);
		
		if (!this.isApiRequest(request)) {
    		return "redirect:" + ACCESS_DENIED_URL;	
    	}
		
        final BindingResult bindingResult = exception.getBindingResult();
        final FieldError fieldError = bindingResult.getFieldError();
        
        final StatusResponse result = new StatusResponse(false);
    	result.setCode(fieldError.getCode());
    	result.setMessage(fieldError.getDefaultMessage());
    	
    	this.writeResponseBody(response, result, HttpServletResponse.SC_BAD_REQUEST);
		return null;
    }
	
    @ExceptionHandler(value = ValidationException.class)
    public String handleException(HttpServletRequest request, HttpServletResponse response, 
    		ValidationException exception) {
    	logger.error("Validation error", exception);
    	
    	if (!this.isApiRequest(request)) {
    		return "redirect:" + ACCESS_DENIED_URL;	
    	}
    	
    	final StatusResponse result = new StatusResponse(false);
    	result.setCode(exception.getCode().getValue());
    	result.setMessage(exception.getMessage());
    	
    	final String data = exception.getData();
    	if (!StringUtils.isNullOrSpace(data)) {
    		result.addData("data", data);
    	}
    	
    	this.writeResponseBody(response, result, HttpServletResponse.SC_BAD_REQUEST);
    	
		return null;
    }
    
    @ExceptionHandler(value = InvalidException.class)
    public String handleException(HttpServletRequest request, HttpServletResponse response, InvalidException exception) {
    	logger.error("Invalid error", exception);
    	
    	if (!this.isApiRequest(request)) {
    		return "redirect:" + ACCESS_DENIED_URL;	
    	}
    	
    	final StatusResponse result = new StatusResponse(false);
    	result.setCode(exception.getCode().getValue());
    	result.setMessage("Invalid parameter");
    	
    	final String parameter = exception.getParameter();
    	if (!StringUtils.isNullOrSpace(parameter)) {
    		result.addData("parameter", parameter);
    	}
    	
    	this.writeResponseBody(response, result, HttpServletResponse.SC_BAD_REQUEST);
    	
		return null;
    }
    
    @ExceptionHandler(value = BusinessException.class)
    public String handleException(HttpServletRequest request, HttpServletResponse response, BusinessException exception) {
    	logger.error("Business error", exception);
    	
    	if (!this.isApiRequest(request)) {
    		return "redirect:" + ACCESS_DENIED_URL;	
    	}
    	
    	final StatusResponse result = new StatusResponse(false);
    	result.setCode(exception.getCode().getValue());
    	result.setMessage(exception.getMessage());
    	
    	final String data = exception.getData();
    	if (!StringUtils.isNullOrSpace(data)) {
    		result.addData("data", data);
    	}
    	
    	this.writeResponseBody(response, result, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    	
		return null;
    }
    
    @ExceptionHandler(value = Exception.class)
    public String handleException(HttpServletRequest request, HttpServletResponse response, Exception exception) {
    	logger.error("Business error", exception);
    	
    	if (!this.isApiRequest(request)) {
    		return "redirect:" + ACCESS_DENIED_URL;	
    	}
    	
    	final StatusResponse result = new StatusResponse(false);
    	
    	if (exception.getCause() instanceof BadCredentialsException) {
    		result.setCode(ErrorStatusCode.INVALID_CREDENTIALS.getValue());
    		result.setMessage(exception.getCause().getMessage());
    	} else {
    		result.setCode(ErrorStatusCode.PROCESSING_ERROR.getValue());
    		result.setMessage(exception.getMessage());
    	}
    	
    	this.writeResponseBody(response, result, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    	
		return null;
    }
    
    private void writeResponseBody(HttpServletResponse response, Object result, int status) {
    	response.setStatus(status);
    	response.setContentType("application/json");
    	
    	try {
			final String json = JSON_MAPPER.writeValueAsString(result);
			
			response.getWriter().write(json);
    	} catch (Exception ex) {
    		logger.error("Process error", ex);
		} finally {
    		try {
				response.flushBuffer();
			} catch (IOException ex) {}
    	}
    }
    
    private boolean isApiRequest(HttpServletRequest request) {
    	final String contextPath = request.getContextPath().toLowerCase();
		final String apiPath = (contextPath.endsWith("/") ? contextPath.substring(0, contextPath.length() - 1) : contextPath) + API_URL_PREFIX;
		
		final String requestURL = request.getRequestURI().toLowerCase();
		return requestURL.startsWith(apiPath);
    }
}
