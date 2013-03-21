package com.athaydes.osgimonitor.impl

import com.athaydes.osgimonitor.api.BundleData
import com.athaydes.osgimonitor.api.OsgiMonitor
import org.junit.Test
import org.osgi.framework.*
import org.osgi.framework.BundleEvent as BE

/**
 *
 * User: Renato
 */
class JavaMonitorRegisterTest {

	@Test
	void testConstruction( ) {
		def bundleListeners = [ ]
		def serviceListeners = [ ]
		def ctx = [
				addBundleListener: { bundleListeners << it },
				addServiceListener: { serviceListeners << it }
		] as BundleContext

		def register = new JavaMonitorRegister( ctx )
		assert register

		assert bundleListeners.size() == 1
		assert bundleListeners[ 0 ] instanceof BundleListener

		assert serviceListeners.size() == 1
		assert serviceListeners[ 0 ] instanceof ServiceListener
	}

	@Test
	void testRegisterWithoutMonitors( ) {
		def registry = new JavaMonitorRegister( [
				addBundleListener: {}, addServiceListener: {}
		] as BundleContext )

		def bundleEvent = new BundleEvent( BundleEvent.INSTALLED,
				[ getSymbolicName: { 'bundle name' } ] as Bundle )

		10.times { registry.bundleChanged bundleEvent }

		def serviceEvent = new ServiceEvent( ServiceEvent.REGISTERED,
				[ : ] as ServiceReference )

		10.times { registry.serviceChanged( serviceEvent ) }
	}


	@Test
	void testBundleChanged( ) {
		def registry = new JavaMonitorRegister( [
				addBundleListener: {},
				addServiceListener: {},
				getBundles: { [ ] as Bundle[] }
		] as BundleContext )

		def data = [ ] as List<BundleData>
		def monitor = [ updateBundle: { data << it } ] as OsgiMonitor

		registry.register( monitor )

		def states = [ BE.INSTALLED, BE.LAZY_ACTIVATION, BE.RESOLVED, BE.STARTED, BE.STARTING,
				BE.STOPPED, BE.STOPPING, BE.UNINSTALLED, BE.UNRESOLVED, BE.UPDATED ]
		( 0..9 ).each { n ->
			registry.bundleChanged new BundleEvent( states[ n ],
					[ getSymbolicName: { 'Bundle ' + n } ] as Bundle )
		}

		assert data.size() == 10
		assert data*.state == states.collect { n -> JavaMonitorRegister.toStateString n }
		( 0..9 ).each { n -> assert data[ n ].symbolicName == 'Bundle ' + n }

	}

	@Test
	void testCurrentBundleDataProvidedOnRegistration( ) {
		def registry = new JavaMonitorRegister( [
				addBundleListener: {},
				addServiceListener: {},
				getBundles: {
					[
							[ getSymbolicName: { 'BundleA' }, getState: { BE.INSTALLED } ] as Bundle,
							[ getSymbolicName: { 'BundleB' }, getState: { BE.RESOLVED } ] as Bundle,
					] as Bundle[]
				}
		] as BundleContext )

		def data = [ ] as List<BundleData>
		def monitor = [ updateBundle: { data << it } ] as OsgiMonitor

		registry.register( monitor )

		assert data.size() == 2
		assert data[ 0 ].symbolicName == 'BundleA'
		assert data[ 0 ].state == JavaMonitorRegister.toStateString( BE.INSTALLED )
		assert data[ 1 ].symbolicName == 'BundleB'
		assert data[ 1 ].state == JavaMonitorRegister.toStateString( BE.RESOLVED )

	}

}
