package com.athaydes.osgimonitor.automaton

import javafx.scene.Node

import java.awt.*

/**
 *
 * User: Renato
 */
class FXAutomaton extends Automaton<FXAutomaton> {

	private static instance

	static synchronized FXAutomaton getUser( ) {
		if ( !instance ) instance = new FXAutomaton()
		instance
	}

	private FXAutomaton( ) {}

	FXAutomaton clickOn( Node node ) {
		if ( !node ) return this
		def center = centerOf node
		moveTo( center.x as int, center.y as int ).click()
	}

	FXAutomaton moveTo( Node node, Speed speed = DEFAULT ) {
		def currPos = MouseInfo.pointerInfo.location
		def target = centerOf node
		move( currPos, target, speed )
	}

	static Point centerOf( Node node ) {
		use( ListAsPoint ) {
			def windowPos = [ node.scene.window.x, node.scene.window.y ]
			def scenePos = [ node.scene.x, node.scene.y ]
			def boundsInScene = node.localToScene node.boundsInLocal
			def absX = windowPos.x + scenePos.x + boundsInScene.minX
			def absY = windowPos.y + scenePos.y + boundsInScene.minY
			[ ( absX + boundsInScene.width / 2 ) as int,
					( absY + boundsInScene.height / 2 ) as int ] as Point
		}
	}

}

@Category( ArrayList )
class ListAsPoint {
	def getX( ) { this[ 0 ] as int }

	def getY( ) { this[ 1 ] as int }
}