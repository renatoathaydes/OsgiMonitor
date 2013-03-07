package com.athaydes.osgimonitor.automaton

import com.athaydes.osgimonitor.internal.Mouse

import java.awt.*

import static com.athaydes.osgimonitor.automaton.Speed.VERY_FAST

/**
 *
 * User: Renato
 */
class Automaton<T extends Automaton> {

	protected final robot = new Robot()
	static final DEFAULT = VERY_FAST
	private static T instance

	static synchronized T getUser( ) {
		if ( !instance ) instance = new Automaton<Automaton>()
		instance
	}

	protected Automaton( ) {}

	T moveTo( int x, int y, Speed speed = DEFAULT ) {
		def currPos = MouseInfo.pointerInfo.location
		def target = new Point( x, y )
		move( currPos, target, speed )
	}

	T moveBy( int x, int y, Speed speed = DEFAULT ) {
		def currPos = MouseInfo.pointerInfo.location
		def target = new Point( currPos.x + x as int, currPos.y + y as int )
		move( currPos, target, speed )
	}

	protected T move( currPos, target, Speed speed ) {
		while ( currPos.x != target.x || currPos.y != target.y ) {
			robot.mouseMove delta( currPos.x, target.x ), delta( currPos.y, target.y )
			robot.delay speed.delay
			currPos = MouseInfo.pointerInfo.location
		}
		this
	}

	protected static int delta( curr, target ) {
		def comp = curr.compareTo target
		curr + ( comp > 0 ? -1 : comp == 0 ? 0 : 1 ) as int
	}

	T dragBy( int x, int y, Speed speed = DEFAULT ) {
		robot.mousePress Mouse.LEFT
		moveBy x, y, speed
		robot.mouseRelease Mouse.LEFT
		this
	}

	T click( ) {
		robot.mousePress Mouse.LEFT
		robot.mouseRelease Mouse.LEFT
		this
	}

	T rightClick( ) {
		robot.mousePress Mouse.RIGHT
		robot.mouseRelease Mouse.RIGHT
		this
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
