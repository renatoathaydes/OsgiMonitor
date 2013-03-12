package com.athaydes.osgimonitor.fx

import com.athaydes.osgimonitor.api.BundleData
import com.athaydes.osgimonitor.api.MonitorRegister
import com.athaydes.osgimonitor.api.OsgiMonitor
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import org.junit.Test

/**
 *
 * User: Renato
 */
class FxOsgiMonitorTest {

	@Test
	@Newify( BundleData )
	void testMainScreenContainsAllItems( ) {
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

		monitors[ 0 ].updateBundle BundleData( 'Some test bundle', 'Active' )
		monitors[ 0 ].updateBundle BundleData( 'Another bundle', 'Stopped' )
		monitors[ 0 ].updateBundle BundleData( 'This bundle', 'Resolved' )

		while ( !monitor.scene ) sleep 500

		// just ensure everything is on the screen
		assert monitor.scene.lookup( '#osgimonitor-root' )
		assert monitor.scene.lookup( '#main-header' )
		assert monitor.scene.lookup( '#bundles-tab' )
		assert monitor.scene.lookup( '#services-tab' )

		def bundlesTable = monitor.scene.lookup( '#bundles-table' ) as TableView
		assert bundlesTable
		assert bundlesTable.columns.size() == 2

		def nameColumn = bundlesTable.columns[ 0 ] as TableColumn

		assert nameColumn.text == 'Bundle Symbolic Name'

		def data0 = nameColumn.getCellData( 0 )
		def data1 = nameColumn.getCellData( 1 )
		def data2 = nameColumn.getCellData( 2 )
		assert [ data0, data1, data2 ] == [ 'Another bundle', 'Some test bundle', 'This bundle' ]

		def stateColumn = bundlesTable.columns[ 1 ] as TableColumn

		assert stateColumn.text == 'State'

		data0 = stateColumn.getCellData( 0 )
		data1 = stateColumn.getCellData( 1 )
		data2 = stateColumn.getCellData( 2 )
		assert [ data0, data1, data2 ] == [ 'Stopped', 'Active', 'Resolved' ]

		monitors[ 0 ].updateBundle BundleData( 'Another bundle', 'Active' )

		data0 = stateColumn.getCellData( 0 )
		data1 = stateColumn.getCellData( 1 )
		data2 = stateColumn.getCellData( 2 )
		assert [ data0, data1, data2 ] == [ 'Active', 'Active', 'Resolved' ]

		sleep 5000

	}

}
