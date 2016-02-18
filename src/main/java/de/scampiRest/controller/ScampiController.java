package de.scampiRest.controller;

import java.io.File;
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
import org.springframework.beans.factory.annotation.Value;
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
	@Value("${scampi.publicStorage}")
	private String nginxPath;
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
	public TreeMap<String, String> stageScampi(@RequestBody RestScampiMessage restScampiMessage ) throws DatabaseIdInUse{
		if (restScampiMessage.getAppTag() == null || restScampiMessage.getAppTag() == ""){
			restScampiMessage.setAppTag(null);
			
			restScampiMessage = restScampiMessageRepository.insert(restScampiMessage);
			
			List<RestScampiMessage> allMessagesFromService = restScampiMessageRepository.findByService(restScampiMessage.getService());
			// Check if there is any valid main file still existing in the former messages for the service
			if (allMessagesFromService.size() > 0){
				for (RestScampiMessage oldRestScampiMessage : allMessagesFromService) {
					if (oldRestScampiMessage.getBinaryMap().containsKey("main")){
						File mainFile = new File(nginxPath + "/" + oldRestScampiMessage.getBinaryMap().get("main") + ".zip");
						if (mainFile.exists()){
							try {
								restScampiMessage.addNewBinary(mainFile, mainFile.getName(), "main");
							} catch (Exception e) {
								logger.debug("",e);
							}
							break;
						}
					}
				}
			}
			
			TreeMap<String, String> tm = new TreeMap<String, String>();
			tm.put("id", restScampiMessage.getAppTag());
			return tm;
		}
		throw new DatabaseIdInUse("The id is not null");
	}
	
	@RequestMapping(value = "/message/publish/{id}", method = RequestMethod.GET)
	public TreeMap<String, String> publishScampi(@PathVariable String id) throws InterruptedException, NoMessageIdFound{
		RestScampiMessage restScampiMessage = restScampiMessageRepository.findOne(id);
		if (restScampiMessage == null){
			throw new NoMessageIdFound();
		}
		scampiCommunicator.publish(restScampiMessage.writeSCAMPIMessage(), restScampiMessage.getService());
		
		
		// TODO How do we handle the answere of scampi?
		TreeMap<String, String> tm = new TreeMap<String, String>();
		tm.put("id", restScampiMessage.getAppTag());
		return tm;
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
			
			response.sendRedirect("http://myliberouter.org/" 
				+ restScampiMessage.getService() 
				+ "/" 
				+ restScampiMessage.getAppTag() 
				+ "/");
			
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
			
			response.sendRedirect("http://myliberouter.org/" 
				+ restScampiMessage.getService() 
				+ "/" 
				+ restScampiMessage.getAppTag() 
				+ "/");
			
			return restScampiMessages;
	}

}
