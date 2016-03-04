package de.scampiRest.applib;

import java.util.concurrent.TimeUnit;

import fi.tkk.netlab.dtn.scampi.applib.AppLib;
import fi.tkk.netlab.dtn.scampi.applib.SCAMPIMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.scampiRest.controller.ScampiController;
import de.scampiRest.data.RestScampiMessage;
import de.scampiRest.data.RestScampiMessageRepository;
import fi.tkk.netlab.dtn.scampi.applib.*;

import fi.tkk.netlab.dtn.scampi.*;

/**
 * Sample Scampi application that sends and receives Hello World! messages. For
 * explanation see the Scampi Application Developer Guide.
 *
 * @author Elias Arnold
 */
@DependsOn("scampiMessageHandler")
public class ScampiCommunicator {
	static private final AppLib APP_LIB = AppLib.builder().build();
	private static final Logger logger = LoggerFactory.getLogger(ScampiCommunicator.class);
	private final static String wildcardSubscribe = "Hello Service"; 
	@Autowired private String storagePath;
	@Autowired private RestScampiMessageRepository restScampiMessageRepository;
	@Autowired private ScampiMessageHandler scampiMessageHandler;
	private static ScampiCommunicator self;
	
	
	public ScampiCommunicator() {
		// Setup
		APP_LIB.start();
		APP_LIB.addLifecycleListener(new ScampiLifeCyclePrinter());
		tryReconnect();
		self = this;
	}

	public void subscribe(String service) throws InterruptedException{
		APP_LIB.subscribe(service);
	}
	
	public void publish(SCAMPIMessage message, String service) throws InterruptedException{
		APP_LIB.publish(message, service);
	}
	
	public void saveInDatabase(RestScampiMessage restScampiMessage){
		
		if (!restScampiMessageRepository.exists(restScampiMessage.getAppTag())){
			restScampiMessageRepository.insert(restScampiMessage);
		}
		// restScampiMessageRepository.insert(restScampiMessage);
	}
	
	public static SCAMPIMessage getMessage(String version){
		SCAMPIMessage message = SCAMPIMessage.builder()
				.lifetime( 1, TimeUnit.DAYS )
				.persistent( false ) 
				.appTag( version ) .build();
		return message;
	}

	public static ScampiCommunicator getSelf() {
		return self;
	}

	public String getStoragePath() {
		return storagePath;
	}
	
	public void tryReconnect(){
		try {
			// APP_LIB.addLifecycleListener(new ScampiLifeCyclePrinter());
			APP_LIB.connect();

			// Subscribe to all service
			if (scampiMessageHandler == null){
				APP_LIB.addMessageReceivedCallback(new ScampiMessageHandler());
			} else {
				APP_LIB.addMessageReceivedCallback(scampiMessageHandler);
			}
			APP_LIB.subscribe(wildcardSubscribe);
		} catch (InterruptedException e) {
			logger.error("could not reconnect", e);
		}
	}

	public void setScampiMessageHandler() {
		APP_LIB.addMessageReceivedCallback(scampiMessageHandler);
	}
	
	public RestScampiMessageRepository getRestScampiMessageRepository(){
		return this.restScampiMessageRepository;
	}

	
}
