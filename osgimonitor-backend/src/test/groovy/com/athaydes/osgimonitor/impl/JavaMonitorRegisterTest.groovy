package com.athaydes.osgimonitor.impl

import com.athaydes.osgimonitor.api.BundleData
import com.athaydes.osgimonitor.api.OsgiMonitor
import com.athaydes.osgimonitor.api.ServiceData
import org.junit.Test
import org.osgi.framework.*
import org.osgi.framework.BundleEvent as BE
import org.osgi.framework.ServiceEvent as SE

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
				[
						getBundle: { [ getSymbolicName: { '' } ] as Bundle },
						getUsingBundles: { [ ] as Bundle[] },
						getPropertyKeys: { [ ] as String[] }
				] as ServiceReference,
		)


		10.times { registry.serviceChanged( serviceEvent ) }
	}


	@Test
	void testBundleChanged( ) {
		def registry = new JavaMonitorRegister( [
				addBundleListener: {},
				addServiceListener: {},
				getBundles: { [ ] as Bundle[] },
				getAllServiceReferences: { _1, _2 -> [ ] as ServiceReference[] }
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
				},
				getAllServiceReferences: { _1, _2 -> [ ] as ServiceReference[] }
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

	@Test
	void testServiceChanged( ) {
		def registry = new JavaMonitorRegister( [
				addBundleListener: {},
				addServiceListener: {},
				getBundles: { [ ] as Bundle[] },
				getAllServiceReferences: { _1, _2 -> [ ] as ServiceReference[] }
		] as BundleContext )

		def data = [ ] as List<ServiceData>
		def monitor = [ updateService: { data << it } ] as OsgiMonitor

		registry.register( monitor )

		def states = [ SE.MODIFIED, SE.MODIFIED_ENDMATCH, SE.REGISTERED, SE.UNREGISTERING ]
		( 0..3 ).each { n ->
			def serviceReference = [
					getBundle: { [ getSymbolicName: { 'bundleS' + n } ] as Bundle },
					getUsingBundles: {
						[
								[ getSymbolicName: { 'bundleA' + n } ] as Bundle,
								[ getSymbolicName: { 'bundleB' + n } ] as Bundle
						] as Bundle[]
					},
					getPropertyKeys: n == 0 ? { [ 'p1', 'p2' ] as String[] } : { [ ] as String[] },
					getProperty: n == 0 ? { key -> if ( key == 'p1' ) 'v1' else if ( key == 'p2' ) 2 } : {}
			] as ServiceReference
			registry.serviceChanged new ServiceEvent( states[ n ], serviceReference )
		}

		assert data.size() == 4
		assert data*.state == states.collect { n -> JavaMonitorRegister.serviceState n }
		( 0..3 ).each { n ->
			assert data[ n ].bundleName == 'bundleS' + n
			assert data[ n ].bundlesUsing == [ 'bundleA' + n, 'bundleB' + n ]
		}
		assert data[ 0 ].properties.size() == 2
		assert data[ 0 ].properties[ 'p1' ] == 'v1'
		assert data[ 0 ].properties[ 'p2' ] == 2

		assert data[ 1 ].properties.isEmpty()
		assert data[ 2 ].properties.isEmpty()
		assert data[ 3 ].properties.isEmpty()

	}

	@Test
	void testCurrentServiceDataProvidedOnRegistration( ) {
		def registry = new JavaMonitorRegister( [
				addBundleListener: {},
				addServiceListener: {},
				getBundles: { [ ] as Bundle[] },
				getAllServiceReferences: { _1, _2 ->
					[
							[
									getBundle: { [ getSymbolicName: { 'bundle 1' } ] as Bundle },
									getUsingBundles: {
										[
												[ getSymbolicName: { 'bundleA' } ] as Bundle,
												[ getSymbolicName: { 'bundleB' } ] as Bundle
										] as Bundle[]
									},
									getPropertyKeys: { [ ] as String[] }
							] as ServiceReference,
							[
									getBundle: { [ getSymbolicName: { 'bundle 2' } ] as Bundle },
									getUsingBundles: {
										[
												[ getSymbolicName: { 'bundleC' } ] as Bundle
										] as Bundle[]
									},
									getPropertyKeys: { [ ] as String[] }
							] as ServiceReference
					] as ServiceReference[]
				}
		] as BundleContext )

		def data = [ ] as List<ServiceData>
		def monitor = [ updateService: { data << it } ] as OsgiMonitor

		registry.register( monitor )

		assert data.size() == 2
		assert data[ 0 ].bundlesUsing == [ 'bundleA', 'bundleB' ]
		assert data[ 0 ].bundleName == 'bundle 1'
		assert data[ 0 ].state == JavaMonitorRegister.serviceState( SE.REGISTERED )

		assert data[ 1 ].bundlesUsing == [ 'bundleC' ]
		assert data[ 1 ].bundleName == 'bundle 2'
		assert data[ 1 ].state == JavaMonitorRegister.serviceState( SE.REGISTERED )

	}

}
