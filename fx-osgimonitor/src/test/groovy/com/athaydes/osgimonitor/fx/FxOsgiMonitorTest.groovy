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
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 *
 * User: Renato
 */
class FxOsgiMonitorTest {

	def monitor
	List<OsgiMonitor> monitors = [ ]

	@Before
	void setup( ) {
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

		monitor = new FxOsgiMonitor( register )
	}

	@After
	void end( ) {
		sleep 500
		FXApp.close()
		sleep 500

	}

	@Test
	void testMainScreenContainsAllItems( ) {
		println "Running testMainScreenContainsAllItems"

		// just ensure everything is on the screen
		def mainTabPane = monitor.scene.lookup( '#main-tab-pane' ) as TabPane
		assert mainTabPane
		assert monitor.scene.lookup( '#osgimonitor-root' )
		assert monitor.scene.lookup( '#main-header' )
		assert monitor.scene.lookup( '#bundles-tab' )
		assert monitor.scene.lookup( '#services-tab' )
		assert monitor.scene.lookup( '#manage-tab' )

		assert mainTabPane.tabs.size() == 3
		assert mainTabPane.tabs[ 0 ].text == 'Bundles'
		assert mainTabPane.tabs[ 1 ].text == 'Services'
		assert mainTabPane.tabs[ 2 ].text == 'Manage'

		def bundlesTable = monitor.scene.lookup( '#bundles-table' ) as TableView
		assert bundlesTable
		assert bundlesTable.columns.size() == 2

		def nameColumn = bundlesTable.columns[ 0 ] as TableColumn
		assert nameColumn.text == 'Bundle Symbolic Name'

		def stateColumn = bundlesTable.columns[ 1 ] as TableColumn
		assert stateColumn.text == 'State'

		def servicesTable = monitor.scene.lookup( '#services-table' ) as TableView
		assert servicesTable
		assert servicesTable.columns.size() == 4

		def publishingColumn = servicesTable.columns[ 0 ] as TableColumn
		assert publishingColumn.text == 'Publishing Bundle'

		def serviceStateColumn = servicesTable.columns[ 1 ] as TableColumn
		assert serviceStateColumn.text == 'State'

		def bundlesUsingColumn = servicesTable.columns[ 2 ] as TableColumn
		assert bundlesUsingColumn.text == 'Bundles using'

		def propsColumn = servicesTable.columns[ 3 ] as TableColumn
		assert propsColumn.text == 'Service classes'
	}

	@Test
	@Newify( BundleData )
	void testBundleDataIsUpdated( ) {
		println "Running testBundleDataIsUpdated"
		assert monitors.size() == 1

		monitors[ 0 ].updateBundle BundleData( 'Some test bundle', 'Active' )
		monitors[ 0 ].updateBundle BundleData( 'Another bundle', 'Stopped' )
		monitors[ 0 ].updateBundle BundleData( 'This bundle', 'Resolved' )
		sleep 50

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
		println "Running testServiceDataIsUpdated"
		assert monitors.size() == 1

		def serviceAProps = [ 'service.id': '1', 'objectClass': [ 'String' ] as String[] ]
		def serviceBProps = [ 'service.id': '44', 'objectClass': [ 'Boolean' ] as String[] ]
		def serviceCProps = [ 'service.id': '22', 'objectClass': [ 'A', 'B', 'C' ] as String[] ]

		monitors[ 0 ].updateService ServiceData( 'A',
				[ 'A', 'B' ] as String[], 'OK',
				serviceAProps )
		monitors[ 0 ].updateService ServiceData( 'B',
				[ 'C' ] as String[], 'BAD',
				serviceBProps )
		monitors[ 0 ].updateService ServiceData( 'C',
				[ 'D' ] as String[], 'FINE',
				serviceCProps )
		sleep 50

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

		def usingColumn = servicesTable.columns[ 2 ] as TableColumn

		data0 = usingColumn.getCellData( 0 )
		data1 = usingColumn.getCellData( 1 )
		data2 = usingColumn.getCellData( 2 )

		assert [ data0, data1, data2 ] == [ [ 'A', 'B' ], [ 'C' ], [ 'D' ] ]
				.collect { it.toString() }

		def classesColumn = servicesTable.columns[ 3 ] as TableColumn

		data0 = classesColumn.getCellData( 0 )
		data1 = classesColumn.getCellData( 1 )
		data2 = classesColumn.getCellData( 2 )

		assert [ data0, data1, data2 ] == [
				serviceAProps, serviceBProps, serviceCProps ]
				.collect { Arrays.toString( it[ 'objectClass' ] ) }

	}

}
