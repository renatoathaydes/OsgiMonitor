package com.athaydes.osgimonitor.automaton

import java.awt.*
import java.awt.event.KeyEvent

import static com.athaydes.osgimonitor.internal.RobotTypingUtil.robotCode

/**
 *
 * User: Renato
 */
class SwingAutomaton extends Automaton<SwingAutomaton> {

	private static instance

	static synchronized SwingAutomaton getUser( ) {
		if ( !instance ) instance = new SwingAutomaton()
		instance
	}

	private SwingAutomaton( ) {}

	SwingAutomaton clickOn( Component component ) {
		println "Clicking on component ${component}"
		if ( !component ) return this
		def center = centerOf component
		moveTo( center.x as int, center.y as int ).click()
	}

	SwingAutomaton moveTo( Component component, Speed speed = DEFAULT ) {
		def currPos = MouseInfo.pointerInfo.location
		def target = centerOf component
		move( currPos, target, speed )
	}

	static Point centerOf( Component component ) {
		def center = component.locationOnScreen
		center.x += component.width / 2
		center.y += component.height / 2
		return center
	}

	SwingAutomaton type( int keyCode ) {
		typeCode( [ c: keyCode, shift: false ] )
		this
	}

	SwingAutomaton pressSimultaneously( int ... keyCodes ) {
		try {
			keyCodes.each { robot.keyPress it }
		} finally {
			robot.delay 50
			try {
				keyCodes.each { robot.keyRelease it }
			} catch ( ignored ) {}
		}
		this
	}

	SwingAutomaton type( String text, Speed speed = DEFAULT ) {
		text.each { c ->
			println "Typing '$c'"
			typeCode robotCode( c ), speed
		}
		this
	}

	private void typeCode( Map key, Speed speed = DEFAULT ) {
		if ( key.shift ) robot.keyPress KeyEvent.VK_SHIFT
		try {
			robot.keyPress key.c
			robot.delay speed.delay
			robot.keyRelease key.c
		} finally {
			if ( key.shift ) robot.keyRelease KeyEvent.VK_SHIFT
		}
	}

}
