package com.athaydes.osgimonitor.impl.manage.maven

import spock.lang.Specification

import java.nio.file.Paths

import static com.athaydes.osgimonitor.impl.CommonTestFunctions.createFileTreeWith
import static com.athaydes.osgimonitor.impl.CommonTestFunctions.safeDelete

/**
 *
 * User: Renato
 */
class MavenHelperTest extends Specification {

	def "Can find all existing Maven settings files"( ) {
		given:
		"A MavenHelper with mocked out getUserHome() and getM2_HOME methods"
		def mavenHelper = Spy( MavenHelper )

		final FAKE_M2_HOME = File.createTempDir().absolutePath
		final FAKE_USER_HOME = File.createTempDir().absolutePath
		mavenHelper.getUserHome() >> FAKE_USER_HOME
		mavenHelper.getM2_HOME() >> FAKE_M2_HOME

		and:
		"The example files exist"
		if ( globalFileExists ) {
			Paths.get( FAKE_M2_HOME, 'conf' ).toFile().mkdirs()
			Paths.get( FAKE_M2_HOME, 'conf', 'settings.xml' ).toFile().createNewFile()
		}
		if ( userFileExists ) {
			Paths.get( FAKE_USER_HOME, '.m2' ).toFile().mkdirs()
			Paths.get( FAKE_USER_HOME, '.m2', 'settings.xml' ).toFile().createNewFile()
		}

		when:
		"I get the settings files"
		def result = mavenHelper.getSettingsFiles()

		then:
		"All existing files are found"
		result.size() == expectedFilesFound

		cleanup:
		safeDelete FAKE_M2_HOME
		safeDelete FAKE_USER_HOME

		where:
		globalFileExists | userFileExists | expectedFilesFound
		false            | false          | 0
		false            | true           | 1
		true             | false          | 1
		true             | true           | 2
	}

	def "Default Maven Repo home is used when no Maven settings file exists"( ) {
		given:
		"A FilesHelper in an environment where no Maven settings file exists"
		final FAKE_M2_HOME = File.createTempDir().absolutePath
		final FAKE_USER_HOME = File.createTempDir().absolutePath

		def mavenHelper = Spy( MavenHelper )
		mavenHelper.getM2_HOME() >> FAKE_M2_HOME
		mavenHelper.getUserHome() >> FAKE_USER_HOME

		and:
		"The default Maven Repo home"
		final String DEFAULT_REPO_HOME = Paths.get(
				FAKE_USER_HOME, '.m2', 'repository' ).toFile().absolutePath

		when:
		"I try to get the Maven Repo home"
		def result = mavenHelper.getMavenRepoHome()

		then:
		"I get the default Maven Repo home"
		result == DEFAULT_REPO_HOME

		cleanup:
		safeDelete FAKE_M2_HOME
		safeDelete FAKE_USER_HOME
	}

	def "Maven Repo home can be determined correctly when there is a Maven settings file which sets its location"( ) {
		given:
		"A FilesHelper in an environment where a Maven settings file exists"
		final FAKE_M2_HOME = File.createTempDir().absolutePath
		def mavenHelper = Spy( MavenHelper )
		mavenHelper.getM2_HOME() >> FAKE_M2_HOME

		Paths.get( FAKE_M2_HOME, 'conf' ).toFile().mkdirs()
		def settingsFile = Paths.get( FAKE_M2_HOME, 'conf', 'settings.xml' ).toFile()

		and:
		"The settings file points to a known repository location"
		final REPO_LOCATION = Paths.get( FAKE_M2_HOME, 'mavenRepo' ).toFile()
		REPO_LOCATION.mkdirs()
		settingsFile << """
		|<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
		|  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		| xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
		|                      http://maven.apache.org/xsd/settings-1.0.0.xsd">
		|  <localRepository>${REPO_LOCATION.absolutePath}</localRepository>
		|  <interactiveMode>true</interactiveMode>
		|</settings>
		|""".stripMargin()

		when:
		"I try to get the Maven Repo home"
		def result = mavenHelper.getMavenRepoHome()

		then:
		"I get the location pointed at by the settings file"
		result == REPO_LOCATION.absolutePath

		cleanup:
		safeDelete FAKE_M2_HOME
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
		final FAKE_MAVEN_REPO_HOME = "."
		def mavenHelper = Spy( MavenHelper )
		mavenHelper.getMavenRepoHome() >> FAKE_MAVEN_REPO_HOME

		when:
		"I ask for the location of the artifact in the examples"
		def result = mavenHelper.locationOfArtifact( groupId, artifactId )

		then:
		"The location of the artifact is determined correctly"
		result == Paths.get( FAKE_MAVEN_REPO_HOME, expected as String[] )

		where:
		groupId     | artifactId | expected
		'org.a'     | 'artifact' | [ 'org', 'a', 'artifact' ]
		'org'       | 'artifact' | [ 'org', 'artifact' ]
		'org.a.b.c' | 'd'        | [ 'org', 'a', 'b', 'c', 'd' ]
	}

	def "An Exception is thrown when trying to find the location of artifacts with invalid arguments"( ) {
		given:
		"A MavenHelper and invalid artifact's groupId or artifactId"
		final FAKE_MAVEN_REPO_HOME = "."
		def mavenHelper = Spy( MavenHelper )
		mavenHelper.getMavenRepoHome() >> FAKE_MAVEN_REPO_HOME

		when:
		"I ask for the location of the artifact in the examples"
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

	def "All artifactIds under a certain groupId can be found"( ) {
		given:
		"A MavenHelper and an artifact's groupId"
		final FAKE_MAVEN_REPO_HOME = Paths.get( 'target', this.class.simpleName )
				.toFile().absolutePath
		def mavenHelper = Spy( MavenHelper )
		mavenHelper.getMavenRepoHome() >> FAKE_MAVEN_REPO_HOME

		and:
		"A number of artifacts under the groupId"
		createFileTreeWith( files, 'target', this.class.simpleName )

		when:
		"I ask for the artifactIds under the groupId"
		def result = mavenHelper.findArtifactIdsUnder( groupId )

		then:
		"All the artifactIds are found"
		result == expected

		cleanup:
		safeDelete FAKE_MAVEN_REPO_HOME

		where:
		groupId | files                                   | expected
		'org'   | [ [ d: 'org' ] ]                        | [ ]
		'org'   | [ [ d: 'org' ], [ d: [ 'org', 'a' ] ] ] | [ 'a' ]
		'com'   | [ [ d: 'org' ], [ d: [ 'org', 'z' ] ],
				[ d: 'com' ],
				[ d: [ 'com', 'a' ] ],
				[ f: [ 'com', 'some.info' ] ],
				[ f: [ 'com', 'a', 'pom.xml' ] ],
				[ d: [ 'com', 'a', '1.0' ] ],
				[ d: [ 'com', 'b' ] ] ]                   | [ 'a', 'b' ]

	}

	def "It can be determined whether a String refers to a Maven version number"( ) {
		given:
		"A MavenHelper and a String"
		def mavenHelper = new MavenHelper()

		when:
		"I ask if the String is a Maven version"
		def result = mavenHelper.isMavenVersion( string )

		then:
		"I get the expected answer"
		result == expected

		where:
		string            | expected
		'1'               | false
		'a'               | false
		'a1'              | false
		'1a'              | false
		'NO.V.1.0'        | false
		'1.0'             | true
		'1.0-A'           | true
		'2.0'             | true
		'2.3.4'           | true
		'5.4.3.2.1.ALPHA' | true
		'8.2-BETA'        | true
		'8.2.1-ALPHA'     | true

	}

	def "It can be determined whether a Path refers to an artifactId"( ) {
		given:
		"A MavenHelper and an artifactId"
		def mavenHelper = new MavenHelper()

		and:
		"A file tree with known contents"
		createFileTreeWith( files, 'target', this.class.simpleName )

		when:
		"I ask whether a folder in the file tree refers to an artifactId's folder"
		def result = mavenHelper.isArtifactId(
				Paths.get( 'target', this.class.simpleName ) )

		then:
		"I get the expected answer"
		result == expected

		cleanup:
		safeDelete new File( 'target', this.class.simpleName )

		where:
		files                                     | expected
		[ ]                                       | false
		[ [ d: [ '1.0' ] ],
				[ f: [ '1.0', 'a.jar' ] ] ]       | true
		[ [ d: [ '1.0' ] ] ]                      | false
		[ [ d: [ '1.0' ] ],
				[ d: [ '1.0', 'no.jar' ] ] ]      | false
		[ [ d: [ '1.0' ] ],
				[ f: [ '1.0', 'no_jar' ] ] ]      | false
		[ [ d: [ 'no_ver' ] ],
				[ f: [ 'no_ver', 'a.jar' ] ] ]    | false
		[ [ d: 'a' ],
				[ d: [ 'a', '1.0' ] ],
				[ f: [ 'a', '1.0', 'a.jar' ] ] ]  | false
		[ [ d: 'a' ],
				[ d: [ 'a', 'b' ] ],
				[ d: [ '5.1-BETA' ] ],
				[ f: [ '5.1-BETA', 'my.jar' ] ] ] | true
		[ [ d: [ '5.1-BETA' ] ],
				[ f: [ '5.1-BETA', 'my.jar' ] ],
				[ d: [ '5.1' ] ],
				[ d: [ '5.1', 'no.jar' ] ] ]      | true
		[ [ d: [ '0.1' ] ],
				[ f: [ '0.1', 'my.jar' ] ],
				[ f: [ '0.1', 'my-sources.jar' ] ],
				[ f: [ '0.1', 'info.txt' ] ] ]    | true

	}

}
