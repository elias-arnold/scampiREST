package de.scampiRest.applib;

import fi.tkk.netlab.dtn.scampi.applib.AppLibLifecycleListener;

class ScampiLifeCyclePrinter implements AppLibLifecycleListener {

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
