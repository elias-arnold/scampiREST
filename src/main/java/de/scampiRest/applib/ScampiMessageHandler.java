package de.scampiRest.applib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.tkk.netlab.dtn.scampi.applib.MessageReceivedCallback;
import fi.tkk.netlab.dtn.scampi.applib.SCAMPIMessage;

class ScampiMessageHandler implements MessageReceivedCallback {
	private static final Logger logger = LoggerFactory.getLogger(ScampiMessageHandler.class);
	
	@Override
	public void messageReceived(SCAMPIMessage message, String service) {
		try {
			if (message.hasString("text")) {
				logger.info("> messageReceived: " + message.getString("text"));
			}
		} finally {
			message.close();
		}
	}
}