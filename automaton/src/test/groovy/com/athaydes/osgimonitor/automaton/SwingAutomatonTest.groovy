package com.athaydes.osgimonitor.automaton

import groovy.swing.SwingBuilder
import org.junit.After
import org.junit.Test

import javax.swing.*
import java.awt.*
import java.util.concurrent.BlockingDeque
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.TimeUnit

import static com.athaydes.osgimonitor.automaton.SwingUtil.lookup
import static java.awt.event.KeyEvent.*

/**
 *
 * User: Renato
 */
class SwingAutomatonTest {

	JFrame jFrame

	@After
	void cleanup( ) {
		jFrame?.dispose()
	}

	@Test
	void testMoveToComponent( ) {
		JButton btn
		new SwingBuilder().edt {
			jFrame = frame( title: 'Frame', size: [ 300, 300 ], show: true ) {
				btn = button( text: 'Click Me', name: 'the-button' )
			}
		}

		sleep 500
		assert btn != null
		SwingAutomaton.user.moveTo( btn )
		def mouseLocation = MouseInfo.pointerInfo.location
		def btnLocation = btn.locationOnScreen

		def assertMouseOnComponent = {
			assert mouseLocation.x > btnLocation.x
			assert mouseLocation.x < btnLocation.x + btn.width
			assert mouseLocation.y > btnLocation.y
			assert mouseLocation.y < btnLocation.y + btn.height
		}

		assertMouseOnComponent()

		// test method chaining
		SwingAutomaton.user.moveTo( jFrame ).moveTo( 500, 500 ).moveTo( btn )

		assertMouseOnComponent()
	}

	@Test
	void testClickOnComponent( ) {
		BlockingDeque future = new LinkedBlockingDeque( 1 )
		new SwingBuilder().edt {
			jFrame = frame( title: 'Frame', size: [ 300, 300 ], show: true ) {
				menuBar() {
					menu( name: 'menu-button', text: "File", mnemonic: 'F' ) {
						menuItem( name: 'item-exit', text: "Exit", mnemonic: 'X',
								actionPerformed: { future.add true } )
					}
				}
			}
		}

		sleep 500

		// this tests function and method chaining
		SwingAutomaton.user.clickOn( lookup( 'menu-button', jFrame ) ).pause( 250 )
				.clickOn( lookup( 'item-exit', jFrame ) )

		// wait up to 1 sec for the button to be clicked
		assert future.poll( 1, TimeUnit.SECONDS )

	}

	@Test
	void testType( ) {
		JTextArea jta
		new SwingBuilder().edt {
			jFrame = frame( title: 'Frame', size: [ 300, 300 ], show: true ) {
				jta = textArea()
			}
		}

		sleep 500
		assert jta != null
		assert jta.text == ''

		SwingAutomaton.user.moveTo( jta ).click().type( 'I can type here' ).pause( 100 )
		assert jta.text == 'I can type here'

		5.times { SwingAutomaton.user.type( VK_BACK_SPACE ) }
		SwingAutomaton.user.type( VK_ENTER ).type( VK_TAB ).type( '1234567890' ).pause( 100 )
		assert jta.text == 'I can type\n\t1234567890'

	}

	@Test
	void testPressSimultaneously( ) {
		JTextArea jta
		new SwingBuilder().edt {
			jFrame = frame( title: 'Frame', size: [ 300, 300 ], show: true ) {
				jta = textArea()
			}
		}

		sleep 500
		assert jta != null
		assert jta.text == ''

		SwingAutomaton.user.type( 'a' ).pressSimultaneously( VK_SHIFT, VK_A ).pause( 100 )
		assert jta.text == 'aA'
	}

}
