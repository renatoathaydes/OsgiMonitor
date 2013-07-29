package com.athaydes.osgimonitor.impl.manage.maven

import com.athaydes.osgimonitor.api.manage.Artifact
import com.athaydes.osgimonitor.api.manage.ArtifactLocator
import spock.lang.Specification

import java.nio.file.Paths

import static com.athaydes.osgimonitor.impl.CommonTestFunctions.*

/**
 *
 * User: Renato
 */
class LocalArtifactLocatorTest extends Specification {

	final LIST_FOR_TEST_DIR = [ 'target', this.class.simpleName ].asImmutable()

	def "A single dependency can be fetched by entering a groupId and artifactId"( ) {
		given:
		"A LocalArtifactLocator pointing to the REAL Maven Repo Home" //TODO use fake repo
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
		def locator = locatorWithMavenRepoHomePointingToTestDir()

		and:
		"The fake Maven repo contains a number of jars with known classes"
		def jars = numberOfJars < 1 ? [ ] : ( 1..numberOfJars ).collect {
			def jarPath = list2path( LIST_FOR_TEST_DIR ).resolve(
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
		safeDelete list2path( LIST_FOR_TEST_DIR )

		where:
		className                    | numberOfJars | indexesContainingClass
		"hello.World"                | 0            | [ ]
		"org.codehaus.groovy.Groovy" | 1            | [ 1 ]
		"org.junit.After"            | 3            | [ 2, 3 ]
	}

	def "A set of artifacts can be found by entering a groupId"( ) {
		given:
		"A LocalArtifactLocator pointing to a REAL Maven Repo Home" //TODO use fake repo
		def locator = new LocalArtifactLocator()

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
		"A LocalArtifactLocator pointing to a fake Maven repo home"
		def locator = locatorWithMavenRepoHomePointingToTestDir()

		and:
		"A fake Maven repo with known contents"
		createFileTreeWith( files, LIST_FOR_TEST_DIR as String[] )

		when:
		"A search by artifactId is made"
		def artifacts = locator.findByArtifactId( artifactId )

		then:
		"A number of artifacts are found (note that uniqueness requires also groupId)"
		artifacts != null
		artifacts.collect { "${it.groupId}:${it.artifactId}" } as Set == expected as Set

		cleanup:
		safeDelete list2path( LIST_FOR_TEST_DIR )

		where:
		files                                                       | artifactId | expected
		[ ]                                                         | 'a'        | [ ]
		[ [ d: 'a' ] ]                                              | 'a'        | [ ]
		[ [ d: 'a' ],
				[ d: [ 'a', 'a' ] ] ]                               | 'a'        | [ ]
		[ [ d: 'a' ],
				[ d: [ 'a', 'b' ] ],
				[ d: [ 'a', 'b', '1.0' ] ] ]                        | 'b'        | [ ]
		[ [ d: 'a' ],
				[ d: [ 'a', 'b' ] ],
				[ d: [ 'a', 'b', '1.0' ] ],
				[ f: [ 'a', 'b', '1.0', 'hi.txt' ] ] ]              | 'b'        | [ ]
		[ [ d: 'a' ],
				[ d: [ 'a', 'b' ] ],
				[ d: [ 'a', 'b', '1.0' ] ],
				[ f: [ 'a', 'b', '1.0', 'a.jar' ] ] ]               | 'b'        | [ 'a:b' ]
		[ [ d: 'a' ],
				[ d: [ 'a', 'b' ] ],
				[ d: [ 'a', 'b', '1.0' ] ],
				[ f: [ 'a', 'b', '1.0', 'a.jar' ] ],
				[ d: [ 'a', 'b', '2.0' ] ],
				[ f: [ 'a', 'b', '2.0', 'b.jar' ] ] ]               | 'b'        | [ 'a:b' ]
		[ [ d: 'a' ],
				[ d: [ 'a', 'b' ] ],
				[ d: [ 'a', 'b', '1.0' ] ],
				[ f: [ 'a', 'b', '1.0', 'a.jar' ] ],
				[ d: [ 'a', 'b', 'b' ] ],
				[ d: [ 'a', 'b', 'b', '3.0' ] ],
				[ f: [ 'a', 'b', 'b', '3.0', 'b.jar' ] ] ]          | 'b'        | [ 'a:b', 'a.b:b' ]
		[ [ d: 'a' ],
				[ d: [ 'a', 'ba' ] ],
				[ d: [ 'a', 'ba', '1.0' ] ],
				[ f: [ 'a', 'ba', '1.0', 'a.jar' ] ],
				[ d: 'f' ],
				[ d: [ 'f', 'g' ] ],
				[ d: [ 'f', 'g', 'h' ] ],
				[ d: [ 'f', 'g', 'h', 'ba' ] ],
				[ d: [ 'f', 'g', 'h', 'ba', '3.0' ] ],
				[ f: [ 'f', 'g', 'h', 'ba', '3.0', 'ba-1.jar' ] ] ] | 'ba'       | [ 'a:ba', 'f.g.h:ba' ]
	}

	def "All versions of an artifact can be found"( ) {
		given:
		"A LocalArtifactLocator pointing to a fake Maven repo home"
		def locator = locatorWithMavenRepoHomePointingToTestDir()

		and:
		"A fake Maven repo with known contents"
		createFileTreeWith( files, LIST_FOR_TEST_DIR as String[] )

		when:
		"All versions of a certain artifact are requested"
		def result = locator.getVersionsOf( new Artifact( artifact.g, artifact.a ) )

		then:
		"All versions of the artifact are returned"
		result == expected as Set

		cleanup:
		safeDelete list2path( LIST_FOR_TEST_DIR )

		where:
		files                                            | artifact           | expected
		[ ]                                              | [ g: 'a', a: 'b' ] | [ ]
		[ [ d: 'a' ],
				[ d: [ 'a', 'b' ] ] ]                    | [ g: 'a', a: 'b' ] | [ ]
		[ [ d: 'a' ],
				[ d: [ 'a', 'b' ] ],
				[ d: [ 'a', 'b', 'no_ver' ] ] ]          | [ g: 'a', a: 'b' ] | [ ]
		[ [ d: 'a' ],
				[ d: [ 'a', 'b' ] ],
				[ d: [ 'a', 'b', 'no_ver' ] ],
				[ f: [ 'a', 'b', 'no_ver', 'a.jar' ] ] ] | [ g: 'a', a: 'b' ] | [ ]
		[ [ d: 'a' ],
				[ d: [ 'a', 'b' ] ],
				[ d: [ 'a', 'b', '1.0' ] ] ]             | [ g: 'a', a: 'b' ] | [ ]
		[ [ d: 'a' ],
				[ d: [ 'a', 'b' ] ],
				[ d: [ 'a', 'b', '1.0' ] ],
				[ f: [ 'a', 'b', '1.0', 'a.jar' ] ] ]    | [ g: 'a', a: 'b' ] | [ '1.0' ]
		[ [ d: 'a' ], [ d: 'b' ],
				[ d: [ 'a', 'b' ] ],
				[ d: [ 'a', 'b', '0.0' ] ],
				[ f: [ 'a', 'b', '0.0', 'a.jar' ] ],
				[ d: [ 'a', 'b', 'no_ver' ] ],
				[ f: [ 'a', 'b', 'no_ver', 'a.jar' ] ],
				[ d: [ 'a', 'b', '1.0' ] ],
				[ f: [ 'a', 'b', '1.0', 'a.jar' ] ],
				[ d: [ 'a', 'b', '0.4' ] ],
				[ f: [ 'a', 'b', '0.4', 'a.jar' ] ],
				[ d: [ 'b', 'a' ] ],
				[ d: [ 'b', 'a', '5.0' ] ],
				[ f: [ 'b', 'a', '5.0', 'b.jar' ] ] ]    | [ g: 'a', a: 'b' ] | [ '1.0', '0.4', '0.0' ]
	}

	private locatorWithMavenRepoHomePointingToTestDir( ) {
		final FAKE_MAVEN_REPO_HOME = list2path( LIST_FOR_TEST_DIR )
		ArtifactLocator locator = new LocalArtifactLocator()
		locator.mavenHelper = new MavenHelper() {
			@Override
			String getMavenRepoHome( ) { FAKE_MAVEN_REPO_HOME }
		}
		return locator
	}

}
