package com.athaydes.osgimonitor.impl.manage.osgi

import spock.lang.Specification

import java.nio.file.Paths

import static com.athaydes.osgimonitor.impl.CommonTestFunctions.createExecutableJar
import static com.athaydes.osgimonitor.impl.CommonTestFunctions.safeDelete

/**
 *
 * User: Renato
 */
class OsgiHelperTest extends Specification {

	OsgiHelper helper = new OsgiHelper()

	def "Can wrap a Jar file into an OSGi bundle"( ) {
		given:
		"A Jar file containing a class"
		def jarLocation = Paths.get( 'target', this.class.name, 'the.jar' ).toAbsolutePath().toString()
		def jar = createExecutableJar( jarLocation, [ classNames: [ 'TheClass' ],
				codeCreator: { String _ -> codeForTheClass() } ] )

		and:
		"A location where the bundle should be saved"
		def bundle = Paths.get( 'target', this.class.name, 'bundle.jar' ).toFile()

		when:
		"I ask it to be wrapped into ans OSGi bundle"
		helper.wrapJarIntoBundle( jar, bundle )

		then:
		"A valid OSGi bundle is created in the given destination"
		bundle.exists()

		cleanup:
		try { jar?.close() } catch ( e ) { e.printStackTrace() }
		safeDelete jarLocation
		safeDelete bundle
	}

	String codeForTheClass( ) {
		'''
		|import javax.xml.XMLConstants;
		|public class TheClass {
		|	public static void main(String[] args) {
		|		System.out.println( XMLConstants.DEFAULT_NS_PREFIX );
		|	}
		|}
		|'''.stripMargin()
	}

}
