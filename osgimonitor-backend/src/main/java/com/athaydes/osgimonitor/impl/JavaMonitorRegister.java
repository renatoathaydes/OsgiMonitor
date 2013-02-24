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
	private final BundleContext context;

	public JavaMonitorRegister( BundleContext context ) {
		this.context = context;
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
		//TODO implement services change
	}

	@Override
	public boolean register( OsgiMonitor osgiMonitor ) {
		provideCurrentDataFor( osgiMonitor );
		synchronized ( monitors ) {
			return monitors.add( osgiMonitor );
		}
	}

	private void provideCurrentDataFor( OsgiMonitor osgiMonitor ) {
		for ( Bundle bundle : context.getBundles() ) {
			BundleData data = new BundleData(
					bundle.getSymbolicName(),
					toStateString( bundle.getState() ) );
			osgiMonitor.updateBundle( data );
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
			case BundleEvent.INSTALLED:
				return "Installed";
			case BundleEvent.UNINSTALLED:
				return "Uninstalled";
			case BundleEvent.RESOLVED:
				return "Resolved";
			case BundleEvent.UNRESOLVED:
				return "Unresolved";
			case BundleEvent.STARTING:
				return "Starting";
			case BundleEvent.STARTED:
				return "Started";
			case BundleEvent.STOPPED:
				return "Stopped";
			case BundleEvent.STOPPING:
				return "Stopping";
			case BundleEvent.UPDATED:
				return "Updated";
			default:
				return "Unknown";
		}
	}

}