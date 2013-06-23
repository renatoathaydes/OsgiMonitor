package com.athaydes.osgimonitor.tests

import com.athaydes.automaton.FXAutomaton
import com.athaydes.osgimonitor.api.MonitorRegister
import com.athaydes.osgimonitor.fx.export.SceneProvider
import javafx.scene.Scene
import org.junit.Test
import org.junit.runner.RunWith
import org.ops4j.pax.exam.Configuration
import org.ops4j.pax.exam.Option
import org.ops4j.pax.exam.junit.PaxExam
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy
import org.ops4j.pax.exam.spi.reactors.PerClass
import org.osgi.framework.BundleContext

import javax.inject.Inject

import static com.athaydes.osgimonitor.tests.Util.*
import static org.ops4j.pax.exam.CoreOptions.mavenBundle
import static org.ops4j.pax.exam.CoreOptions.options

/**
 *
 * User: Renato
 */
@RunWith( PaxExam )
@ExamReactorStrategy( PerClass )
class FxOsgiMonitorIntegrationTest {

	@Inject BundleContext context
	@Inject MonitorRegister monitorRegister
	@Inject SceneProvider sceneProvider

	final osgiMonitorVersion = "0.0.1-SNAPSHOT"

	@Configuration
	Option[] config( ) {
		options(
				javaFxPackages(),
				systemBundles(),
				mavenBundle( "org.codehaus.groovy", "groovy-all", "2.1.1" ),
				mavenBundle( "com.athaydes.automaton", "automaton", osgiMonitorVersion ),
				mavenBundle( "com.athaydes.osgimonitor", "osgimonitor-api", osgiMonitorVersion ),
				mavenBundle( "com.athaydes.osgimonitor", "osgimonitor-backend", osgiMonitorVersion ),
				mavenBundle( "com.athaydes.osgimonitor", "fx-osgimonitor", osgiMonitorVersion )
		)
	}

	@Test
	void allBundlesAreActive( ) {
		assertAllBundlesActive context
	}

	@Test
	void fxMonitorDetectsEvents( ) {
		Scene scene = sceneProvider.scene
		def mainTabPane = scene.lookup( '#main-tab-pane' )
		def tabLabels = mainTabPane.lookupAll( 'Label' )
		assert tabLabels.isEmpty() == false
		def servicesLabel = tabLabels.find { it.text == 'Services' }
		assert servicesLabel
		FXAutomaton.user.clickOn( servicesLabel )

		sleep 5000
	}

}
