package com.athaydes.osgimonitor.fx.export

import com.athaydes.osgimonitor.fx.OsgiMonitorApp
import javafx.scene.Scene

/**
 *
 * User: Renato
 */
class SceneProvider {

	Scene getScene( ) {
		OsgiMonitorApp.instance.scene
	}

}
