package com.athaydes.osgimonitor.api;

public interface OsgiMonitor {

	void updateBundle( BundleData bundleData );

	void updateService( ServiceData serviceData );

}