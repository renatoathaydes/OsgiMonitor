package com.athaydes.osgimonitor.fx.views

import javafx.scene.effect.DropShadow
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.LinearGradient
import javafx.scene.paint.Stop
import javafx.scene.text.Font
import javafx.scene.text.Text

/**
 *
 * User: Renato
 */
class MainHeader extends Region {

	MainHeader( ) {
		this.prefHeight = 150
		def text = new Text( fill: textFill(), text: 'Osgi Monitor', x: 20, y: 28 )
		text.font = new Font( 'Arial Black', 20 )
		text.effect = new DropShadow( offsetY: 3.0f, color: Color.WHEAT )
		children.setAll text
	}

	static textFill( ) {
		def stops = [
				new Stop( 0, Color.MAROON ),
				new Stop( 1, Color.TOMATO ) ] as Stop[]
		new LinearGradient( 55, 0, 225, 0, false, CycleMethod.REFLECT, stops )
	}

}
