package com.athaydes.osgimonitor.fx

import com.athaydes.osgimonitor.api.BundleData
import com.athaydes.osgimonitor.api.MonitorRegister
import com.athaydes.osgimonitor.api.OsgiMonitor
import com.athaydes.osgimonitor.api.ServiceData
import javafx.application.Application
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.VBox
import javafx.stage.Stage

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit

/**
 *
 * User: Renato
 */
class FxOsgiMonitor {

	final MonitorRegister monitorRegister

	FxOsgiMonitor( MonitorRegister monitorRegister ) {
		this.monitorRegister = monitorRegister
		Thread.start {
			Application.launch OsgiMonitorApp
		}
		OsgiMonitor app = OsgiMonitorApp.appFuture.poll( 5, TimeUnit.SECONDS )
		monitorRegister.register app
	}

}

class OsgiMonitorApp extends Application implements OsgiMonitor {

	static appFuture = new ArrayBlockingQueue<OsgiMonitorApp>( 1 )

	def table = new TableView()
	def bundlesData = FXCollections.observableArrayList()

	@Override
	void start( Stage stage ) {
		appFuture.add this

		def nameCol = new TableColumn( 'Bundle Symbolic Name' )
		nameCol.minWidth = 200
		nameCol.cellValueFactory = new PropertyValueFactory( 'symbolicName' )

		def stateCol = new TableColumn( 'State' )
		stateCol.minWidth = 100
		stateCol.cellValueFactory = new PropertyValueFactory( 'state' )

		table.columns.addAll( nameCol, stateCol )
		table.items = bundlesData

		Button btn = new Button()
		btn.text = "Test"
		btn.onAction = [
				handle: { ActionEvent event ->
					println "Hello World!"
				}
		] as EventHandler

		VBox root = new VBox()
		root.spacing = 20
		root.children.addAll( table, btn )
		stage.scene = new Scene( root, 300, 250 )
		stage.show()

	}

	@Override
	void updateBundle( BundleData bundleData ) {
		bundlesData << bundleData
	}

	@Override
	void updateService( ServiceData serviceData ) {
		//TODO implement services monitor
	}
}
