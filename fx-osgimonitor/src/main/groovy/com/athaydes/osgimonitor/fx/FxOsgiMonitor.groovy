package com.athaydes.osgimonitor.fx

import com.athaydes.osgimonitor.api.BundleData
import com.athaydes.osgimonitor.api.MonitorRegister
import com.athaydes.osgimonitor.api.OsgiMonitor
import com.athaydes.osgimonitor.api.ServiceData
import com.athaydes.osgimonitor.fx.tab.BundlesTab
import com.athaydes.osgimonitor.fx.tab.ServicesTab
import javafx.application.Application
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
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

	final bundlesTab = new BundlesTab()
	final servicesTab = new ServicesTab()


	@Override
	void start( Stage stage ) {
		appFuture.add this

		Button btn = new Button()
		btn.text = "Test"
		btn.onAction = [
				handle: { ActionEvent event ->
					println "Hello World!"
				}
		] as EventHandler

		def tabPane = new TabPane()
		tabPane.tabs.addAll( bundlesTab as Tab, servicesTab as Tab )

		VBox root = new VBox()
		root.spacing = 20
		root.children.addAll( tabPane, btn )
		stage.scene = new Scene( root, 600, 400 )
		stage.show()

	}

	@Override
	void updateBundle( BundleData bundleData ) {
		bundlesTab.update bundleData
	}

	@Override
	void updateService( ServiceData serviceData ) {
		//TODO implement services monitor
	}
}
