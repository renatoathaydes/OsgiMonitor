package com.athaydes.osgimonitor.automaton

import com.athaydes.osgimonitor.internal.Mouse

import java.awt.*
import java.awt.event.KeyEvent

import static com.athaydes.osgimonitor.automaton.Speed.MEDIUM
import static com.athaydes.osgimonitor.automaton.Speed.VERY_FAST
import static com.athaydes.osgimonitor.internal.RobotTypingUtil.robotCode

/**
 *
 * User: Renato
 */
class Automaton {

	private final robot = new Robot()
	static final user = new Automaton()
	static final DEFAULT = VERY_FAST

	private Automaton( ) {

	}

	Automaton moveTo( int x, int y, Speed speed = DEFAULT ) {
		def currPos = MouseInfo.pointerInfo.location
		def target = new Point( x, y )
		move( currPos, target, speed )
	}

	Automaton moveTo( Component component, Speed speed = DEFAULT ) {
		def currPos = MouseInfo.pointerInfo.location
		def target = centerOf component
		move( currPos, target, speed )
	}

	Automaton moveBy( int x, int y, Speed speed = DEFAULT ) {
		def currPos = MouseInfo.pointerInfo.location
		def target = new Point( currPos.x + x as int, currPos.y + y as int )
		move( currPos, target, speed )
	}

	private move( currPos, target, Speed speed ) {
		while ( currPos.x != target.x || currPos.y != target.y ) {
			robot.mouseMove delta( currPos.x, target.x ), delta( currPos.y, target.y )
			robot.delay speed.delay
			currPos = MouseInfo.pointerInfo.location
		}
		this
	}

	private delta( curr, target ) {
		def comp = curr.compareTo target
		curr + ( comp > 0 ? -1 : comp == 0 ? 0 : 1 ) as int
	}

	Automaton dragBy( int x, int y, Speed speed = DEFAULT ) {
		robot.mousePress Mouse.LEFT
		moveBy x, y, speed
		robot.mouseRelease Mouse.LEFT
		this
	}

	Automaton click( ) {
		robot.mousePress Mouse.LEFT
		robot.mouseRelease Mouse.LEFT
		this
	}

	Automaton clickOn( Component component ) {
		println "Clicking on component ${component}"
		if ( !component ) return this
		def center = centerOf component
		moveTo( center.x as int, center.y as int ).click()
	}

	Automaton rightClick( ) {
		robot.mousePress Mouse.RIGHT
		robot.mouseRelease Mouse.RIGHT
		this
	}

	Point centerOf( Component component ) {
		def center = component.locationOnScreen
		center.x += component.width / 2
		center.y += component.height / 2
		return center
	}

	Automaton type( int keyCode ) {
		typeCode( [ c: keyCode, shift: false ] )
		this
	}

	Automaton pressSimultaneously( int ... keyCodes ) {
		try {
			keyCodes.each { robot.keyPress it }
		} finally {
			robot.delay 50
			try {
				keyCodes.each { robot.keyRelease it }
			} catch ( e ) {}
		}
		this
	}

	Automaton type( String text, Speed speed = MEDIUM ) {
		text.each { c ->
			println "Typing '$c'"
			typeCode( robotCode( c ) )
		}
		this
	}

	private void typeCode( Map<String, String> key ) {
		if ( key.shift ) robot.keyPress KeyEvent.VK_SHIFT
		try {
			robot.keyPress key.c
			robot.delay 50
			robot.keyRelease key.c
		} finally {
			if ( key.shift ) robot.keyRelease KeyEvent.VK_SHIFT
		}
	}

	Automaton pause( long millis ) {
		sleep millis
		this
	}

}

enum Speed {
	SLOW( 10 ), MEDIUM( 7 ), FAST( 4 ), VERY_FAST( 1 )
	final int delay

	private Speed( int delay ) {
		this.delay = delay
	}
}
