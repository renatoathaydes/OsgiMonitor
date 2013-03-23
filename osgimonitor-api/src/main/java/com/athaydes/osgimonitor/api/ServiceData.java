package com.athaydes.osgimonitor.api;

import java.util.Collections;
import java.util.Map;

/**
 * User: Renato
 */
public class ServiceData {

	private final String bundleName;
	private final String[] bundlesUsing;
	private final String state;
	private final Map<String, Object> properties;

	public ServiceData( String bundleName, String[] bundlesUsing,
	                    String state, Map<String, Object> properties ) {
		this.bundleName = bundleName;
		this.bundlesUsing = bundlesUsing;
		this.state = state;
		this.properties = Collections.unmodifiableMap( properties );
	}

	public String getBundleName() {
		return bundleName;
	}

	public String[] getBundlesUsing() {
		return bundlesUsing;
	}

	public String getState() {
		return state;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

}
