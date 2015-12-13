package de.scampiRest.applib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import de.scampiRest.data.RestScampiMessage;
import de.scampiRest.data.RestScampiMessageRepository;
import fi.tkk.netlab.dtn.scampi.applib.MessageReceivedCallback;
import fi.tkk.netlab.dtn.scampi.applib.SCAMPIMessage;

public class ScampiMessageHandler implements MessageReceivedCallback {
	private static final Logger logger = LoggerFactory.getLogger(ScampiMessageHandler.class);
	
	@Override
	public void messageReceived(SCAMPIMessage message, String service) {
		try {
			if (!ScampiCommunicator.getSelf().getRestScampiMessageRepository().exists(message.getAppTag())){
				RestScampiMessage restScampiMessage = new RestScampiMessage(message, service);
				ScampiCommunicator.getSelf().saveInDatabase(restScampiMessage);
			}
		} finally {
			message.close();
		}
	}
}