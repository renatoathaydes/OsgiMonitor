package com.athaydes.osgimonitor.fx

import com.athaydes.osgimonitor.api.BundleData
import com.athaydes.osgimonitor.api.MonitorRegister
import com.athaydes.osgimonitor.api.OsgiMonitor
import com.athaydes.osgimonitor.automaton.FXAutomaton
import org.junit.Test

/**
 *
 * User: Renato
 */
class FxOsgiMonitorTest {

	@Test
	void test1( ) {
		List<OsgiMonitor> monitors = [ ]
		def register = [
				register: { OsgiMonitor osgiMonitor ->
					monitors << osgiMonitor
					return true
				}
		] as MonitorRegister

		def monitor = new FxOsgiMonitor( register )

		sleep 500

		assert monitors.size() == 1

		monitors[ 0 ].updateBundle new BundleData( 'Some test bundle', 'Active' )
		monitors[ 0 ].updateBundle new BundleData( 'Another bundle', 'Stopped' )
		monitors[ 0 ].updateBundle new BundleData( 'This bundle', 'Resolved' )

		sleep 500

		FXAutomaton.user.moveTo( 0, 0 )

		monitors[ 0 ].updateBundle new BundleData( 'Another bundle', 'Active' )

		sleep 5000

	}

}
