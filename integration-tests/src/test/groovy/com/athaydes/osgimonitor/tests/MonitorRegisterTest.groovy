package com.athaydes.osgimonitor.tests

import com.athaydes.osgimonitor.api.BundleData
import com.athaydes.osgimonitor.api.MonitorRegister
import com.athaydes.osgimonitor.api.OsgiMonitor
import com.athaydes.osgimonitor.api.ServiceData
import org.junit.Test
import org.junit.runner.RunWith
import org.ops4j.pax.exam.Configuration
import org.ops4j.pax.exam.Option
import org.ops4j.pax.exam.junit.PaxExam
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy
import org.ops4j.pax.exam.spi.reactors.PerClass
import org.osgi.framework.Bundle
import org.osgi.framework.BundleContext

import javax.inject.Inject

import static org.ops4j.pax.exam.CoreOptions.*

/**
 *
 * User: Renato
 */
@RunWith( PaxExam )
@ExamReactorStrategy( PerClass )
class MonitorRegisterTest {

	@Inject BundleContext context
	@Inject MonitorRegister monitorRegister

	@Configuration
	Option[] config( ) {
		final osgiMonitorVersion = "0.0.1-SNAPSHOT"
		options(
				systemBundles(),
				mavenBundle( "com.google.guava", "guava", "13.0.1" ),
				mavenBundle( "com.athaydes.osgimonitor", "osgimonitor-api", osgiMonitorVersion ),
				mavenBundle( "com.athaydes.osgimonitor", "osgimonitor-backend", osgiMonitorVersion )
		)
	}

	@Test
	void allBundlesAreActive( ) {
		assert context != null
		context.bundles.each { Bundle bundle ->
			assert bundle.state == Bundle.ACTIVE
		}
	}

	@Test
	void shouldDetectAllBundleEvents( ) {
		runCapturingEvents { updates ->
			def guava = context.bundles.find { it.symbolicName.contains( 'guava' ) }
			assert guava != null

			try {
				guava.stop()
				guava.start()
				guava.stop()
				guava.start()

				// events are asynchronous, so let's wait a little
				sleep 500

				updates*.class.each { assert it == BundleData }
				updates*.symbolicName.each { assert it.contains( 'guava' ) }
				assert updates*.state == [ 'Stopped', 'Started', 'Stopped', 'Started' ]

				updates.clear()

				guava.update()

				// events are asynchronous, so let's wait a little
				sleep 500

				updates*.class.each { assert it == BundleData }
				updates*.symbolicName.each { assert it.contains( 'guava' ) }
				assert updates*.state == [ 'Stopped', 'Unresolved',
						'Updated', 'Resolved', 'Started' ]

			} finally {
				guava.start() // ensure we don't break other tests
			}
		}
	}

	def runCapturingEvents( Closure tests ) {
		assert monitorRegister != null
		def updates = [ ]
		def monitor = [
				updateBundle: { BundleData bundleData ->
					updates << bundleData
				},
				updateService: { ServiceData serviceData ->
					updates << serviceData
				}
		] as OsgiMonitor

		monitorRegister.register monitor
		try {
			tests.call updates
		} finally {
			monitorRegister.unregister monitor
		}
	}

	def systemBundles( ) {
		composite(
				mavenBundle( "org.apache.aries.blueprint", "org.apache.aries.blueprint", "1.0.0" ),
				mavenBundle( "org.apache.aries", "org.apache.aries.util", "1.0.0" ),
				mavenBundle( "org.apache.aries.proxy", "org.apache.aries.proxy", "1.0.0" ),
				mavenBundle( "org.codehaus.groovy", "groovy-all", "2.0.6" ),
				junitBundles(),
				cleanCaches( true )
		)
	}

}

