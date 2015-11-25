package de.scampiRest.applib;

import fi.tkk.netlab.dtn.scampi.applib.*;

/**
 * Sample Scampi application that sends and receives Hello World! messages. For
 * explanation see the Scampi Application Developer Guide.
 *
 * @author Elias Arnold
 */
public class ScampiCommunicator {
	static private final AppLib APP_LIB = AppLib.builder().build();

	public ScampiCommunicator() {
		try {
			// Setup
			APP_LIB.start();
			APP_LIB.addLifecycleListener(new LifeCyclePrinter());
			APP_LIB.connect();

			// Subscribe to a service
			APP_LIB.addMessageReceivedCallback(new MessagePrinter());
			APP_LIB.subscribe("Hello Service");

			// Publish a message
			APP_LIB.publish(getMessage("Hello World!"), "Hello Service");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static SCAMPIMessage getMessage(String text) {
		SCAMPIMessage message = SCAMPIMessage.builder().appTag("Hello").build();
		message.putString("text", text);
		return message;
	}

	private static final class LifeCyclePrinter implements AppLibLifecycleListener {

		@Override
		public void onConnected(String scampiId) {
			System.out.println("> onConnected: " + scampiId);
		}

		@Override
		public void onDisconnected() {
			System.out.println("> onDisconnected");
		}

		@Override
		public void onConnectFailed() {
			System.out.println("> onConnectFailed");
		}

		@Override
		public void onStopped() {
			System.out.println("> onStopped");
		}
	}

	private static final class MessagePrinter implements MessageReceivedCallback {

		@Override
		public void messageReceived(SCAMPIMessage message, String service) {
			try {
				if (message.hasString("text")) {
					System.out.println("> messageReceived: " + message.getString("text"));
				}
			} finally {
				message.close();
			}
		}
	}
}
