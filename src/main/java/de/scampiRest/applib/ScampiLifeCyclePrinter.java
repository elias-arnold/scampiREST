package de.scampiRest.applib;

import fi.tkk.netlab.dtn.scampi.applib.AppLibLifecycleListener;

class ScampiLifeCyclePrinter implements AppLibLifecycleListener {

	Integer restartCounter = 0;
	
	@Override
	public void onConnected(String scampiId) {
		restartCounter = 0;
		System.out.println("> onConnected: " + scampiId);
	}

	@Override
	public void onDisconnected() {
		System.out.println("> onDisconnected");
	}

	@Override
	public void onConnectFailed() {
		restartCounter++;
		if (restartCounter < 3){
			ScampiCommunicator.getSelf().tryReconnect();
		}
		System.out.println("> onConnectFailed");
	}

	@Override
	public void onStopped() {
		System.out.println("> onStopped");
	}
}
