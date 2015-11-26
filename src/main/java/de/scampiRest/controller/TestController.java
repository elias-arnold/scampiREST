package de.scampiRest.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import de.scampiRest.applib.ScampiCommunicator;
import de.scampiRest.data.RestScampiMessage;
import fi.tkk.netlab.dtn.scampi.applib.SCAMPIMessage;

@RestController
@RequestMapping(value = "/test")
public class TestController {
	private static final Logger logger = LoggerFactory.getLogger(TestController.class);
	@Autowired ScampiCommunicator scampiCommunicator;
	
	@RequestMapping(value = "/exeption")
	public String testException() {
		throw new NullPointerException();
	}
	
	@RequestMapping(value = "/newMessage", method = RequestMethod.GET)
	public RestScampiMessage newMessage(){
		SCAMPIMessage message = scampiCommunicator.getMessage("v1");
		message.putString("Name", "MyValue i want to send");
		message.putBinary("bin", new byte[1]);
		message.putInteger("int", 1234);
		message.putFloat("float", new Double(123123));
		return new RestScampiMessage(message, "testservice"); 
	}

}
