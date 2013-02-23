package com.athaydes.osgimonitor.api;

/**
 * User: Renato
 */
public class ServiceData {

	private final String serviceId;
	private final String bundleSymbolicName;
	private final String serviceClassName;

	public ServiceData( String serviceId, String bundleSymbolicName, String serviceClassName ) {
		this.serviceId = serviceId;
		this.bundleSymbolicName = bundleSymbolicName;
		this.serviceClassName = serviceClassName;
	}

	public String getServiceId() {
		return serviceId;
	}

	public String getBundleSymbolicName() {
		return bundleSymbolicName;
	}

	public String getServiceClassName() {
		return serviceClassName;
	}
}
