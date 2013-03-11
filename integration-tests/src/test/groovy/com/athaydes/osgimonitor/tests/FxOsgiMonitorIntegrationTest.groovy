package com.athaydes.osgimonitor.tests

import com.athaydes.osgimonitor.api.MonitorRegister
import com.athaydes.automaton.Automaton
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

	final osgiMonitorVersion = "0.0.1-SNAPSHOT"

	@Configuration
	Option[] config( ) {
		options(
				javaFxPackages(),
				systemBundles(),
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
		Automaton.user.pause( 1000 ).moveTo( 500, 500 )
		sleep 10000
	}

}
