package com.athaydes.osgimonitor.fx.tab

import javafx.scene.control.Tab

/**
 *
 * User: Renato
 */
abstract class AsTab {

	final Tab tab = new Tab( tabName() )

	def asType( Class cls ) { if ( cls == Tab ) tab }

	abstract String tabName( )

}
