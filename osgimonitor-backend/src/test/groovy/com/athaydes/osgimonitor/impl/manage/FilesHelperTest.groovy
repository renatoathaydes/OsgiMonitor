package com.athaydes.osgimonitor.impl.manage

import spock.lang.Specification

import static com.athaydes.osgimonitor.impl.CommonTestFunctions.createFileTreeWith
import static com.athaydes.osgimonitor.impl.CommonTestFunctions.list2path
import static com.athaydes.osgimonitor.impl.CommonTestFunctions.safeDelete

/**
 *
 * User: Renato
 */
class FilesHelperTest extends Specification {

	def "The FilesHelper can determine if a file has a certain extension"( ) {
		given:
		"A certain file (instance of Path)"
		def path = list2path( [ file ] )

		when:
		"I ask if a file has a certain extension"
		println "Asking if $file has extension $extension"
		def result = new FilesHelper().hasExtension( path, extension )

		then:
		"I get the expected answer"
		result == expected

		where:
		file              | extension | expected
		'someFile'        | 'ext'     | false
		'anotherFile.ext' | 'ext'     | true
		'notext'          | 'ext'     | false
		'jar'             | 'jar'     | false
		'jarjarjar'       | 'jar'     | false
		'a.jar'           | 'jar'     | true
		'a.jar.jar'       | 'jar'     | true
		'not.a.jar.'      | 'jar.'    | false
		'.jar'            | 'jar'     | false
		'.no.jar'         | 'jar'     | false
	}

	def "All files can be found in a directory tree"( ) {
		given:
		"A file tree of known contents"
		def paths = createFileTreeWith( files, 'target', this.class.simpleName )

		when:
		"I ask for all files in the tree"
		def result = new FilesHelper()
				.findAllFilesIn( paths.first() )

		then:
		"I get all expected files with that extension"
		result as Set == expected.collect {
			list2path( [ 'target', this.class.simpleName ] + it )
		} as Set

		cleanup:
		paths?.reverse()?.each {
			safeDelete it.toFile()
		}

		where:
		files /* d is dir, f is file */             | expected
		[ ]                                         | [ ]
		[ [ d: [ 'a' ] ], [ f: [ 'a.jar' ] ] ]      | [ [ 'a.jar' ] ]
		[ [ d: [ 'a' ] ], [ f: [ 'a', 'a.jar' ] ] ] | [ [ 'a', 'a.jar' ] ]
		[ [ f: [ 'm' ] ] ]                          | [ [ 'm' ] ]
		[ [ f: [ 'm.m' ] ], [ d: [ 'n.m' ] ] ]      | [ [ 'm.m' ] ]
		[ [ f: [ 'm.m' ] ],
				[ d: [ 'm' ] ],
				[ d: [ 'm', 'n' ] ],
				[ f: [ 'm', 'n', 'o.m' ] ] ]        | [ [ 'm.m' ], [ 'm', 'n', 'o.m' ] ]
	}

	def "Maven Home can be determined correctly when M2_HOME is defined"( ) {
		given:
		"A filesHelper in an environment with a known M2_HOME variable"
		final KNOWN_M2_HOME_VALUE = File.createTempDir().absolutePath
		def filesHelper = new FilesHelper() {
			String getMavenHomeEnvVariable( ) { KNOWN_M2_HOME_VALUE }
		}

		when:
		"I try to get the Maven Home"
		def result = filesHelper.getMavenHome()

		then:
		"I get the value of M2_HOME"
		result == KNOWN_M2_HOME_VALUE

		cleanup:
		safeDelete KNOWN_M2_HOME_VALUE
	}

	def "Maven Home can be determined correctly when M2_HOME is NOT defined"( ) {
		given:
		"An environment with no M2_HOME variable but existing {user.home}/.m2 folder"
		final USER_HOME = File.createTempDir().absolutePath
		final USER_HOME_M2 = USER_HOME + File.separator + '.m2'
		new File( USER_HOME_M2 ).mkdir()

		and:
		"A FilesHelper"
		def filesHelper = new FilesHelper() {
			String getMavenHomeEnvVariable( ) { null }

			String getUserHome( ) { USER_HOME }
		}

		when:
		"I try to get the Maven Home"
		def result = filesHelper.getMavenHome()

		then:
		"I get the value of {user.home}/.m2"
		result == USER_HOME_M2

		cleanup:
		safeDelete USER_HOME_M2
		safeDelete USER_HOME
	}

	def "Maven Home cannot be determined if no M2_HOME is defined and {user.home}/.m2 does not exist"( ) {
		given:
		"A FilesHelper in an environment where M2_HOME is not defined and {user.home}/.m2 does not exist"
		def filesHelper = new FilesHelper() {
			String getMavenHomeEnvVariable( ) { null }

			String getUserHome( ) { "__NON_EXISTING_LOCATION__" }
		}

		when:
		"I try to get the Maven Home"
		filesHelper.getMavenHome()

		then:
		"A RuntimeException is thrown with a nice message to the user"
		def e = thrown( RuntimeException )
		!e.message.empty
	}
}
