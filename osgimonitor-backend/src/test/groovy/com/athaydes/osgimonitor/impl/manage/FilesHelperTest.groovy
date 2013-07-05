package com.athaydes.osgimonitor.impl.manage

import spock.lang.Specification

import java.nio.file.Paths

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

	def "Maven Repo home can be determined correctly when no Maven settings file exists"( ) {
		given:
		"A FilesHelper in an environment where no Maven settings file exists"
		final FAKE_MAVEN_HOME = File.createTempDir().absolutePath
		def filesHelper = new FilesHelper() {
			String getMavenHome( ) { FAKE_MAVEN_HOME }
		}

		and:
		"The default Maven Repo home"
		final String DEFAULT_REPO_HOME = list2path(
				[ FAKE_MAVEN_HOME, 'repository' ] ).toAbsolutePath().toString()

		when:
		"I try to get the Maven Repo home"
		def result = filesHelper.getMavenRepoHome()

		then:
		"I get the default Maven Repo home"
		result == DEFAULT_REPO_HOME

		cleanup:
		safeDelete FAKE_MAVEN_HOME
	}

	def "Maven Repo home can be determined correctly when there is a Maven settings file"( ) {
		given:
		"A FilesHelper in an environment where a Maven settings file exists"
		final FAKE_MAVEN_HOME = File.createTempDir().absolutePath
		def filesHelper = new FilesHelper() {
			String getMavenHome( ) { FAKE_MAVEN_HOME }
		}
		def settingsFile = Paths.get( FAKE_MAVEN_HOME, 'settings.xml' ).toFile()

		and:
		"The settings file points to a known repository location"
		final REPO_LOCATION = FAKE_MAVEN_HOME + File.separator + 'mavenRepo'
		final SETTINGS_FILE_TEXT = """
		|<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
		|  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		| xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
		|                      http://maven.apache.org/xsd/settings-1.0.0.xsd">
		|  <localRepository>${REPO_LOCATION}</localRepository>
		|  <interactiveMode>true</interactiveMode>
		|</settings>
		|""".stripMargin()
		settingsFile << SETTINGS_FILE_TEXT

		when:
		"I try to get the Maven Repo home"
		def result = filesHelper.getMavenRepoHome()

		then:
		"I get the location pointed at by the settings file"
		result == REPO_LOCATION

		cleanup:
		safeDelete FAKE_MAVEN_HOME
	}
}
