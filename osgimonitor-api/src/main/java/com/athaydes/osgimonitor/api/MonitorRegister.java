package com.athaydes.osgimonitor.api;

/**
 * User: Renato
 */
public interface MonitorRegister {

	boolean register( OsgiMonitor osgiMonitor );

	boolean unregister( OsgiMonitor osgiMonitor );

}
