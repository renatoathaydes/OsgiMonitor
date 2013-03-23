package com.athaydes.osgimonitor.api;

/**
 * User: Renato
 */
public class BundleData {

	private final String symbolicName;
	private final String state;

	public BundleData( String symbolicName, String state ) {
		this.symbolicName = symbolicName;
		this.state = state;
	}

	public String getSymbolicName() {
		return symbolicName;
	}

	public String getState() {
		return state;
	}

	@Override
	public String toString() {
		return symbolicName + ":" + state;
	}

}
