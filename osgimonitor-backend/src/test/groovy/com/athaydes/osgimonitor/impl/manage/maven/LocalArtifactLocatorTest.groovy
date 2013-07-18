package com.athaydes.osgimonitor.impl.manage.maven

import com.athaydes.osgimonitor.api.manage.ArtifactLocator
import spock.lang.Specification

import java.nio.file.Paths

import static com.athaydes.osgimonitor.impl.CommonTestFunctions.createExecutableJar
import static com.athaydes.osgimonitor.impl.CommonTestFunctions.safeDelete

/**
 *
 * User: Renato
 */
class LocalArtifactLocatorTest extends Specification {

	def "A single dependency can be fetched by entering a groupId and artifactId"( ) {
		given:
		"A LocalArtifactLocator"
		def locator = new LocalArtifactLocator()

		when:
		"An exact match is requested for a groupId and artifactId"
		def artifact = locator.findArtifact( groupId, artifactId )

		then:
		"The artifact is correctly located"
		artifact != null
		artifact.groupId == groupId
		artifact.artifactId == artifactId

		where:
		groupId               | artifactId
		"junit"               | "junit"
		"org.codehaus.groovy" | "groovy-all"

	}

	def "A set of artifacts can be found by entering a class name"( ) {
		given:
		"A LocalArtifactLocator pointing to a fake Maven repo home"
		final FAKE_MAVEN_REPO_HOME = Paths.get( 'target', LocalArtifactLocatorTest.class.simpleName )
		ArtifactLocator locator = new LocalArtifactLocator()
		locator.mavenHelper = new MavenHelper() {
			@Override
			String getMavenRepoHome( ) { FAKE_MAVEN_REPO_HOME }
		}

		and:
		"The fake Maven repo contains a number of jars with known classes"
		def jars = numberOfJars < 1 ? [ ] : ( 1..numberOfJars ).collect {
			def jarPath = FAKE_MAVEN_REPO_HOME.resolve(
					Paths.get( "g$it", "a$it", "v$it", "a${it}.jar" ) )
			createExecutableJar( jarPath.toAbsolutePath().toString(),
					[ classNames: [
							"some.NoiseClass$it",
							"and.AnotherOne$it" ] + (
					it in indexesContainingClass ?
						[ className ] : [ ] ) ]
			)
		}

		when:
		"A search by class name is made"
		def artifacts = locator.findByClassName( className )

		then:
		"The artifacts which contain the class name are found"
		artifacts != null
		artifacts.collect {
			"${it.groupId}:${it.artifactId}:${it.version}"
		} as Set == indexesContainingClass.collect {
			"g$it:a$it:v$it"
		} as Set

		cleanup:
		jars?.each { it?.close() }
		safeDelete FAKE_MAVEN_REPO_HOME

		where:
		className                    | numberOfJars | indexesContainingClass
		"hello.World"                | 0            | [ ]
		"org.codehaus.groovy.Groovy" | 1            | [ 1 ]
		"org.junit.After"            | 3            | [ 2, 3 ]
	}

	def "A set of artifacts can be found by entering a groupId"( ) {
		given:
		"A LocalArtifactLocator"
		ArtifactLocator locator = new LocalArtifactLocator()

		when:
		"A search by groupId is made"
		def artifacts = locator.findByGroupId( groupId )

		then:
		"A number of artifacts are found"
		artifacts != null
		!artifacts.isEmpty()
		artifacts.each { assert it.groupId == groupId }
		artifacts.collect { it.artifactId }.unique().size() == artifacts.size()

		where:
		groupId << [ "com.google.inject", "junit" ]
	}

	def "A set of artifacts can be found by entering an artifactId"( ) {
		given:
		"A LocalArtifactLocator"
		ArtifactLocator locator = new LocalArtifactLocator()

		when:
		"A search by artifactId is made"
		def artifacts = locator.findByArtifactId( artifactId )

		then:
		"A number of artifacts are found (note that uniqueness requires also groupId)"
		artifacts != null
		!artifacts.isEmpty()
		//println artifacts.collect { "${it.groupId}:${it.artifactId}" }
		artifacts.each { assert it.artifactId == artifactId }
		artifacts.collect { it.groupId }.unique().size() == artifacts.size()

		where:
		artifactId << [ "guice", "junit" ]
	}
}
