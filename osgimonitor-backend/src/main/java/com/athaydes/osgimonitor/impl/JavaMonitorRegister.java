package com.athaydes.osgimonitor.impl;

import com.athaydes.osgimonitor.api.BundleData;
import com.athaydes.osgimonitor.api.MonitorRegister;
import com.athaydes.osgimonitor.api.OsgiMonitor;
import org.osgi.framework.*;

import java.util.ArrayList;
import java.util.List;

public class JavaMonitorRegister implements MonitorRegister,
		BundleListener, ServiceListener {

	private final List<OsgiMonitor> monitors = new ArrayList<>( 2 );

	public JavaMonitorRegister( BundleContext context ) {
		context.addBundleListener( this );
		context.addServiceListener( this );
	}

	@Override
	public void bundleChanged( BundleEvent bundleEvent ) {
		BundleData data = new BundleData(
				bundleEvent.getBundle().getSymbolicName(),
				toStateString( bundleEvent.getType() ) );
		synchronized ( monitors ) {
			for ( OsgiMonitor monitor : monitors ) {
				monitor.updateBundle( data );
			}
		}
	}


	@Override
	public void serviceChanged( ServiceEvent serviceEvent ) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public boolean register( OsgiMonitor osgiMonitor ) {
		synchronized ( monitors ) {
			return monitors.add( osgiMonitor );
		}
	}

	@Override
	public boolean unregister( OsgiMonitor osgiMonitor ) {
		synchronized ( monitors ) {
			return monitors.remove( osgiMonitor );
		}
	}

	public static String toStateString( int state ) {
		switch ( state ) {
			case BundleEvent.INSTALLED: return "Installed";
			case BundleEvent.UNINSTALLED: return "Uninstalled";
			case BundleEvent.RESOLVED: return "Resolved";
			case BundleEvent.UNRESOLVED: return "Unresolved";
			case BundleEvent.STARTING: return "Starting";
			case BundleEvent.STARTED: return "Started";
			case BundleEvent.STOPPED: return "Stopped";
			case BundleEvent.STOPPING: return "Stopping";
			case BundleEvent.UPDATED: return "Updated";
			default: return "Unknown";
		}
	}

}