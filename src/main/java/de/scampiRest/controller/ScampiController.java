package de.scampiRest.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.scampiRest.applib.ScampiCommunicator;
import de.scampiRest.data.RestScampiMessage;
import de.scampiRest.data.RestScampiMessageRepository;
import de.scampiRest.data.ScampiService;
import de.scampiRest.exception.DatabaseIdInUse;
import de.scampiRest.exception.NoMessageIdFound;

@RestController
public class ScampiController {
	private static final Logger logger = LoggerFactory.getLogger(ScampiController.class);
	@Autowired private ScampiCommunicator scampiCommunicator;
	@Autowired private RestScampiMessageRepository restScampiMessageRepository;
	
	@RequestMapping(value = "/")
	public String welcomeMessage() {
			return "Liberouter Rest Interface V0.1 alpha";
	}
	
	@RequestMapping(value = "/message/empty", method = RequestMethod.GET)
	public RestScampiMessage emptyScampi(){
		RestScampiMessage restMessage = new RestScampiMessage(ScampiCommunicator.getMessage(""), "");
		return restMessage;
	}
	
	@RequestMapping(value = "/message/stage", method = RequestMethod.POST)
	public String stageScampi(@RequestBody RestScampiMessage restScampiMessage ) throws DatabaseIdInUse{
		if (restScampiMessage.getAppTag() != null){
			throw new DatabaseIdInUse("The id is not null");
		}
		
		restScampiMessage = restScampiMessageRepository.insert(restScampiMessage);
		return restScampiMessage.getAppTag();
	}
	
	@RequestMapping(value = "/message/publish/{id}", method = RequestMethod.GET)
	public String publishScampi(@PathVariable String id) throws InterruptedException, NoMessageIdFound{
		RestScampiMessage restScampiMessage = restScampiMessageRepository.findOne(id);
		if (restScampiMessage == null){
			throw new NoMessageIdFound();
		}
		scampiCommunicator.publish(restScampiMessage.writeSCAMPIMessage(), restScampiMessage.getService());
		
		
		// TODO How do we handle the answere of scampi?
		return restScampiMessage.getAppTag();
	}
	
	@RequestMapping(value = "/subscribe/{serviceName}", method = RequestMethod.GET)
	public String subscribeScampi(@PathVariable String serviceName) throws InterruptedException{
		scampiCommunicator.subscribe(serviceName);
		return "done";
	}
	
	@RequestMapping(value = "/service", method = RequestMethod.GET)
	public List<ScampiService> getServices(){
		TreeMap<String, ScampiService> services = new TreeMap<String, ScampiService>();
		List<ScampiService> servicesList = new ArrayList<ScampiService>();
		List<RestScampiMessage> restScampiMessages = restScampiMessageRepository.findAll();
		
		for (RestScampiMessage restScampiMessage : restScampiMessages) {
			if (!services.containsKey(restScampiMessage.getService())){
				Integer messageCount = restScampiMessageRepository.findByService(restScampiMessage.getService()).size();
				ScampiService scampiService = new ScampiService(restScampiMessage.getService(), 
						"V1", "Default Description", messageCount);
				services.put(restScampiMessage.getService(), scampiService);
			}
		}
		
		Set<String> keys = services.keySet();
		for (String key : keys) {
			servicesList.add(services.get(key));
		}
		
		return servicesList;
	}
	
	@RequestMapping(value = "/service/{serviceName}", method = RequestMethod.GET)
	public List<RestScampiMessage> getServiceMessages(@RequestParam(value="appTag", required=false) String appTag, @PathVariable String serviceName){
		if (appTag == null){
			// Deliver all messages for a service
			List<RestScampiMessage> restScampiMessages = restScampiMessageRepository.findByService(serviceName);
			return restScampiMessages;
		} else {
			// Deliver only messages for a service with a given apptag
			
			List<RestScampiMessage> restScampiMessages = restScampiMessageRepository.findByAppTag(appTag);
			
			/*List<RestScampiMessage> restScampiMessages = restScampiMessageRepository.findByService(serviceName);
			List<RestScampiMessage> filteredRestScampiMessages = new ArrayList<RestScampiMessage>();
			
			for (Iterator<RestScampiMessage> iterator = restScampiMessages.iterator(); iterator.hasNext();) {
				RestScampiMessage restScampiMessage = (RestScampiMessage) iterator.next();
				if (restScampiMessage.getAppTag().contentEquals(appTag)){
					filteredRestScampiMessages.add(restScampiMessage);
				}
			}*/
			
			return restScampiMessages;
		}
		
	}
	
	@RequestMapping(value = "/service/{serviceName}/random", method = RequestMethod.GET)
	public List<RestScampiMessage> getServiceMessagesRandom(@PathVariable String serviceName, HttpServletResponse response) throws IOException{
			// Deliver all messages for a service
			List<RestScampiMessage> restScampiMessages = restScampiMessageRepository.findByService(serviceName);
			
			// Make it random... 
			
			RestScampiMessage restScampiMessage = restScampiMessages.get(0);
			
			response.sendRedirect("http://localhost/" 
				+ restScampiMessage.getService() 
				+ "/" 
				+ restScampiMessage.getAppTag() 
				+ "/index.html");
			
			return restScampiMessages;
	}
	
	@RequestMapping(value = "/service/{serviceName}/{number}", method = RequestMethod.GET)
	public List<RestScampiMessage> getServiceMessagesNumver(@PathVariable String serviceName, @PathVariable Integer number, HttpServletResponse response) throws IOException{
			// Deliver all messages for a service
			List<RestScampiMessage> restScampiMessages = restScampiMessageRepository.findByService(serviceName);
			
			// Make it random... 
			if (restScampiMessages.size() <= number || number < 0) {
				throw new IllegalArgumentException("index is not available");
			}
			RestScampiMessage restScampiMessage = restScampiMessages.get(number);
			
			response.sendRedirect("http://localhost/" 
				+ restScampiMessage.getService() 
				+ "/" 
				+ restScampiMessage.getAppTag() 
				+ "/index.html");
			
			return restScampiMessages;
	}

}
