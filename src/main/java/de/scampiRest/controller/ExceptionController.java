package de.scampiRest.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionController {
	private static final Logger logger = LoggerFactory.getLogger(ScampiController.class);
	
	@ExceptionHandler(IllegalArgumentException.class)
	void handleBadRequests(HttpServletResponse response, IllegalArgumentException exception) throws IOException {
		logger.debug("IllegalArgumentException", exception);
		response.sendError(HttpStatus.BAD_REQUEST.value());
	}
	
	@ExceptionHandler(NullPointerException.class)
	void handleBadRequests(HttpServletResponse response, NullPointerException exception) throws IOException {
		logger.debug("NullPointerException", exception);
		response.sendError(HttpStatus.BAD_REQUEST.value());
	}
	
	@ExceptionHandler(Exception.class)
	void handleBadRequests(HttpServletResponse response, Exception exception) throws IOException {
		logger.debug("Exception", exception);
		response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value());
	}

}
