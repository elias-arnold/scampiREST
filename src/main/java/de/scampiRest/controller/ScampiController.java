package de.scampiRest.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.scampiRest.applib.ScampiCommunicator;

@RestController
public class ScampiController {
	private static final Logger logger = LoggerFactory.getLogger(ScampiController.class);
	@Autowired ScampiCommunicator scampiCommunicator;
	
	@RequestMapping(value = "/")
	public String welcomeMessage() {
			return "Hello World";
	}

}
