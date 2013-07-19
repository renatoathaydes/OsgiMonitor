package com.athaydes.osgimonitor.impl.manage.maven

import spock.lang.Specification

import java.nio.file.Paths

import static com.athaydes.osgimonitor.impl.CommonTestFunctions.list2path
import static com.athaydes.osgimonitor.impl.CommonTestFunctions.safeDelete

/**
 *
 * User: Renato
 */
class MavenHelperTest extends Specification {

	def "Maven Home can be determined correctly when M2_HOME is defined"( ) {
		given:
		"A mavenHelper in an environment with a known M2_HOME variable"
		final KNOWN_M2_HOME_VALUE = File.createTempDir().absolutePath
		def mavenHelper = new MavenHelper() {
			String getMavenHomeEnvVariable( ) { KNOWN_M2_HOME_VALUE }
		}

		when:
		"I try to get the Maven Home"
		def result = mavenHelper.getMavenHome()

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
		def mavenHelper = new MavenHelper() {
			String getMavenHomeEnvVariable( ) { null }

			String getUserHome( ) { USER_HOME }
		}

		when:
		"I try to get the Maven Home"
		def result = mavenHelper.getMavenHome()

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
		def mavenHelper = new MavenHelper() {
			String getMavenHomeEnvVariable( ) { null }

			String getUserHome( ) { "__NON_EXISTING_LOCATION__" }
		}

		when:
		"I try to get the Maven Home"
		mavenHelper.getMavenHome()

		then:
		"A RuntimeException is thrown with a nice message to the user"
		def e = thrown( RuntimeException )
		!e.message.empty
	}

	def "Maven Repo home can be determined correctly when no Maven settings file exists"( ) {
		given:
		"A FilesHelper in an environment where no Maven settings file exists"
		final FAKE_MAVEN_HOME = File.createTempDir().absolutePath
		def mavenHelper = new MavenHelper() {
			String getMavenHome( ) { FAKE_MAVEN_HOME }
		}

		and:
		"The default Maven Repo home"
		final String DEFAULT_REPO_HOME = list2path(
				[ FAKE_MAVEN_HOME, 'repository' ] ).toAbsolutePath().toString()

		when:
		"I try to get the Maven Repo home"
		def result = mavenHelper.getMavenRepoHome()

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
		def mavenHelper = new MavenHelper() {
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
		def result = mavenHelper.getMavenRepoHome()

		then:
		"I get the location pointed at by the settings file"
		result == REPO_LOCATION

		cleanup:
		safeDelete FAKE_MAVEN_HOME
	}

	def "The groupId of a Maven artifact can be found from the location of a Jar file"( ) {
		given:
		"A MavenHelper and the location of a Jar file relative to the Maven Repo Home"
		def mavenHelper = new MavenHelper()

		when:
		"I ask for the groupId of the artifact"
		def result = mavenHelper.groupIdFrom( locationParts as String[] )

		then:
		"The groupId of the artifact is determined correctly"
		result == expectedGroupId

		where:
		locationParts                                  | expectedGroupId
		[ 'org', 'example', 'v1.0', 'the.jar' ]        | 'org'
		[ 'org', 'com', 'example', 'v1.0', 'the.jar' ] | 'org.com'
		[ 'a', 'b', 'c', 'a', 'v1.0', 'the.jar' ]      | 'a.b.c'

	}

	def "The location of an artifact can be determined by its groupId and artifactId"( ) {
		given:
		"A MavenHelper and an artifact's groupId and artifactId"
		final FAKE_MAVEN_HOME = "."
		def mavenHelper = new MavenHelper() {
			String getMavenRepoHome( ) { FAKE_MAVEN_HOME }
		}

		when:
		"I ask for the location of the artifact"
		def result = mavenHelper.locationOfArtifact( groupId, artifactId )

		then:
		"The location of the artifact is determined correctly"
		result == Paths.get( FAKE_MAVEN_HOME, expected as String[] )

		where:
		groupId     | artifactId | expected
		'org.a'     | 'artifact' | [ 'org', 'a', 'artifact' ]
		'org'       | 'artifact' | [ 'org', 'artifact' ]
		'org.a.b.c' | 'd'        | [ 'org', 'a', 'b', 'c', 'd' ]
	}

	def "An Exception is thrown when trying to find the location of artifacts with invalid arguments"( ) {
		given:
		"A MavenHelper and invalid artifact's groupId or artifactId"
		final FAKE_MAVEN_HOME = "."
		def mavenHelper = new MavenHelper() {
			String getMavenRepoHome( ) { FAKE_MAVEN_HOME }
		}

		when:
		"I ask for the location of the artifact"
		mavenHelper.locationOfArtifact( groupId, artifactId )

		then:
		"A IllegalArgumentException is thrown"
		thrown IllegalArgumentException

		where:
		groupId | artifactId
		''      | 'artifact'
		'org'   | ''
		null    | 'artifact'
		'org'   | null
		''      | ''
		null    | null
	}

}
