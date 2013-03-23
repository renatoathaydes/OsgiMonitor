package com.athaydes.osgimonitor.fx

import com.athaydes.automaton.FXApp
import com.athaydes.automaton.FXAutomaton
import com.athaydes.osgimonitor.api.BundleData
import com.athaydes.osgimonitor.api.MonitorRegister
import com.athaydes.osgimonitor.api.OsgiMonitor
import com.athaydes.osgimonitor.api.ServiceData
import javafx.scene.control.Label
import javafx.scene.control.TabPane
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import org.junit.Test

/**
 *
 * User: Renato
 */
class FxOsgiMonitorTest {

	@Test
	void testMainScreenContainsAllItems( ) {
		def register = [ register: { true } ] as MonitorRegister

		def app = new OsgiMonitorApp()

		// mock Launcher static method
		Launcher.metaClass.'static'.launchApplication = {
			FXApp.startApp app
		}

		def monitor = new FxOsgiMonitor( register )

		// just ensure everything is on the screen
		def mainTabPane = monitor.scene.lookup( '#main-tab-pane' ) as TabPane
		assert mainTabPane
		assert monitor.scene.lookup( '#osgimonitor-root' )
		assert monitor.scene.lookup( '#main-header' )
		assert monitor.scene.lookup( '#bundles-tab' )
		assert monitor.scene.lookup( '#services-tab' )

		assert mainTabPane.tabs.size() == 2
		assert mainTabPane.tabs[ 0 ].text == 'Bundles'
		assert mainTabPane.tabs[ 1 ].text == 'Services'

		def bundlesTable = monitor.scene.lookup( '#bundles-table' ) as TableView
		assert bundlesTable
		assert bundlesTable.columns.size() == 2

		def nameColumn = bundlesTable.columns[ 0 ] as TableColumn
		assert nameColumn.text == 'Bundle Symbolic Name'

		def stateColumn = bundlesTable.columns[ 1 ] as TableColumn
		assert stateColumn.text == 'State'

		def servicesTable = monitor.scene.lookup( '#services-table' ) as TableView
		assert servicesTable
		assert servicesTable.columns.size() == 2

		def publishingColumn = servicesTable.columns[ 0 ] as TableColumn
		assert publishingColumn.text == 'Publishing Bundle'

		def serviceStateColumn = servicesTable.columns[ 1 ] as TableColumn
		assert serviceStateColumn.text == 'State'

	}

	@Test
	@Newify( BundleData )
	void testBundleDataIsUpdated( ) {
		List<OsgiMonitor> monitors = [ ]
		def register = [
				register: { OsgiMonitor osgiMonitor ->
					monitors << osgiMonitor
					return true
				}
		] as MonitorRegister

		// mock Launcher static method
		Launcher.metaClass.'static'.launchApplication = {
			FXApp.startApp new OsgiMonitorApp()
		}

		def monitor = new FxOsgiMonitor( register )

		assert monitors.size() == 1

		monitors[ 0 ].updateBundle BundleData( 'Some test bundle', 'Active' )
		monitors[ 0 ].updateBundle BundleData( 'Another bundle', 'Stopped' )
		monitors[ 0 ].updateBundle BundleData( 'This bundle', 'Resolved' )

		def bundlesTable = monitor.scene.lookup( '#bundles-table' ) as TableView
		def nameColumn = bundlesTable.columns[ 0 ] as TableColumn

		def data0 = nameColumn.getCellData( 0 )
		def data1 = nameColumn.getCellData( 1 )
		def data2 = nameColumn.getCellData( 2 )
		assert [ data0, data1, data2 ] == [ 'Another bundle', 'Some test bundle', 'This bundle' ]

		def stateColumn = bundlesTable.columns[ 1 ] as TableColumn

		data0 = stateColumn.getCellData( 0 )
		data1 = stateColumn.getCellData( 1 )
		data2 = stateColumn.getCellData( 2 )
		assert [ data0, data1, data2 ] == [ 'Stopped', 'Active', 'Resolved' ]

		monitors[ 0 ].updateBundle BundleData( 'Another bundle', 'Active' )

		data0 = stateColumn.getCellData( 0 )
		data1 = stateColumn.getCellData( 1 )
		data2 = stateColumn.getCellData( 2 )
		assert [ data0, data1, data2 ] == [ 'Active', 'Active', 'Resolved' ]
	}

	@Test
	@Newify( ServiceData )
	void testServiceDataIsUpdated( ) {
		List<OsgiMonitor> monitors = [ ]
		def register = [
				register: { OsgiMonitor osgiMonitor ->
					monitors << osgiMonitor
					return true
				}
		] as MonitorRegister

		// mock Launcher static method
		Launcher.metaClass.'static'.launchApplication = {
			FXApp.startApp new OsgiMonitorApp()
		}

		def monitor = new FxOsgiMonitor( register )

		assert monitors.size() == 1

		monitors[ 0 ].updateService ServiceData( 'A', [ ] as String[], 'OK', [ : ] )
		monitors[ 0 ].updateService ServiceData( 'B', [ ] as String[], 'BAD', [ : ] )
		monitors[ 0 ].updateService ServiceData( 'C', [ ] as String[], 'FINE', [ : ] )

		def mainTabPane = monitor.scene.lookup( '#main-tab-pane' ) as TabPane

		FXAutomaton.user.clickOn mainTabPane.lookupAll( '.tab-label' ).find {
			it instanceof Label && it.text == 'Services'
		}

		def servicesTable = monitor.scene.lookup( '#services-table' ) as TableView

		def bundleColumn = servicesTable.columns[ 0 ] as TableColumn

		def data0 = bundleColumn.getCellData( 0 )
		def data1 = bundleColumn.getCellData( 1 )
		def data2 = bundleColumn.getCellData( 2 )
		assert [ data0, data1, data2 ] == [ 'A', 'B', 'C' ]

		def stateColumn = servicesTable.columns[ 1 ] as TableColumn

		data0 = stateColumn.getCellData( 0 )
		data1 = stateColumn.getCellData( 1 )
		data2 = stateColumn.getCellData( 2 )
		assert [ data0, data1, data2 ] == [ 'OK', 'BAD', 'FINE' ]

	}

}
