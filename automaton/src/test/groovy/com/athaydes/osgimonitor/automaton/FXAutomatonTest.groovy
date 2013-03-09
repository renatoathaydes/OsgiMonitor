package com.athaydes.osgimonitor.automaton

import javafx.application.Application
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.TextArea
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.stage.Stage
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.TimeUnit

/**
 *
 * User: Renato
 */
class FXAutomatonTest {

	static Stage stage

	@BeforeClass
	static void setup( ) {
		Thread.start {
			Application.launch TestApp
		}
		stage = TestApp.stageFuture.poll( 5, TimeUnit.SECONDS )
		assert stage
	}

	@AfterClass
	static void cleanup() {
		Platform.runLater{ stage?.close() }
	}

	@Test
	void testMoveTo( ) {
		def future = new LinkedBlockingDeque( 1 )

		def rect = new Rectangle( fill: Color.RED, width: 20, height: 20 )
		rect.onMouseEntered = [ handle: { rect.fill = Color.BLUE } ] as EventHandler
		rect.onMouseExited = [ handle: { rect.fill = Color.YELLOW } ] as EventHandler

		Platform.runLater {
			def hbox = new HBox( padding: [ 40 ] as Insets )
			def scene = new Scene( hbox, 200, 100, Color.BLACK )
			hbox.children.add rect
			stage.scene = scene
			stage.show()
			future << true
		}

		assert future.poll( 4, TimeUnit.SECONDS )

		FXAutomaton.user.moveTo( rect )

		assert rect.fill == Color.BLUE

		FXAutomaton.user.moveBy( 0, rect.height as int )
		assert rect.fill == Color.YELLOW

		FXAutomaton.user.moveBy( 0, -rect.height as int )
		assert rect.fill == Color.BLUE

		FXAutomaton.user.moveBy( rect.width as int, 0 )
		assert rect.fill == Color.YELLOW
	}

	@Test
	void testCenterOf( ) {
		def future = new LinkedBlockingDeque( 1 )

		def rect = new Rectangle( fill: Color.RED, width: 20, height: 20 )
		rect.onMouseEntered = [ handle: { rect.fill = Color.BLUE } ] as EventHandler
		rect.onMouseExited = [ handle: { rect.fill = Color.YELLOW } ] as EventHandler

		Platform.runLater {
			def hbox = new HBox( padding: [ 40 ] as Insets )
			def scene = new Scene( hbox, 200, 100, Color.BLACK )
			hbox.children.add rect
			stage.scene = scene
			stage.show()
			future << true
		}

		assert future.poll( 4, TimeUnit.SECONDS )

		def center = FXAutomaton.centerOf rect

		FXAutomaton.user.moveTo( center.x as int, center.y as int )
		assert rect.fill == Color.BLUE

		FXAutomaton.user.moveBy( 0, rect.height as int )
		assert rect.fill == Color.YELLOW

		FXAutomaton.user.moveBy( 0, -rect.height as int )
		assert rect.fill == Color.BLUE

		FXAutomaton.user.moveBy( rect.width as int, 0 )
		assert rect.fill == Color.YELLOW
	}

	@Test
	void testClickOn( ) {
		def future = new LinkedBlockingDeque( 1 )
		def buttonToClick = new Button( text: 'Click Me' )
		buttonToClick.onAction = [ handle: { future << it } ] as EventHandler

		Platform.runLater {
			def scene = new Scene( buttonToClick, 100, 50 )
			stage.scene = scene
			stage.show()
			future << true
		}

		assert future.poll( 4, TimeUnit.SECONDS )

		FXAutomaton.user.clickOn( buttonToClick ).pause 250

		assert future.size() == 1
		assert future.poll() instanceof ActionEvent
	}

	@Test
	void testType( ) {
		def future = new LinkedBlockingDeque( 1 )
		def textArea = new TextArea()

		Platform.runLater {
			def scene = new Scene( textArea, 100, 50 )
			stage.scene = scene
			stage.show()
			future << true
		}

		assert future.poll( 4, TimeUnit.SECONDS )

		FXAutomaton.user.clickOn( textArea ).type( 'I can type here' ).pause( 100 )

		assert textArea.text == 'I can type here'
	}

}

class TestApp extends Application {

	static stageFuture = new LinkedBlockingDeque<Stage>( 1 )

	@Override
	void start( Stage stage ) throws Exception {
		stage.title = 'FXAutomaton Tests'
		stageFuture.push stage
	}

}
