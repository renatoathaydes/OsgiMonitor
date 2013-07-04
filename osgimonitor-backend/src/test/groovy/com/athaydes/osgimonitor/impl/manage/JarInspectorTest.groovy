package com.athaydes.osgimonitor.impl.manage

import spock.lang.Specification

import java.nio.file.Paths

import static com.athaydes.osgimonitor.impl.CommonTestFunctions.*

/**
 *
 * User: Renato
 */
class JarInspectorTest extends Specification {

	def "A Collection of Paths can be turned into an array of JarFile instances"( ) {
		given:
		"A Collection of Paths"
		def paths = createFileTreeWith( files, 'target', this.class.simpleName )

		and:
		"All JAR files are actually valid Java JARs"
		paths.findAll { it.fileName.toString().endsWith( '.jar' ) }
				.each { createExecutableJar( it.toAbsolutePath().toString() ) }

		and:
		"A JarInspector"
		def jarInspector = new JarInspector()

		when:
		"I ask the JarInspector to filter the jars from the collection"
		def result = jarInspector.filterJars( paths )

		then:
		"I get an array containing only the actual jars"
		result.collect { Paths.get( it.name ) } as Set ==
				expected.collect {
					list2path( [ 'target', this.class.simpleName ] + it )
				} as Set

		cleanup:
		result?.each { it.close() }
		paths?.reverse()?.each {
			safeDelete it.toFile()
		}

		where:
		files                                     | expected
		[ ]                                       | [ ]
		[ [ d: [ 'test' ] ] ]                     | [ ]
		[ [ d: [ 'test' ] ], [ f: [ 'a.txt' ] ] ] | [ ]
		[ [ d: [ 'test' ] ], [ f: [ 'a.jar' ] ] ] | [ 'a.jar' ]
		[ [ d: [ 'notajar' ] ] ]                  | [ ]
		[ [ d: [ 'notajar' ] ],
				[ f: [ 'notajar', 'a.jar' ] ] ]   | [ [ 'notajar', 'a.jar' ] ]
		[ [ d: [ 'a' ] ], [ d: [ 'b' ] ],
				[ d: [ 'a', 's' ] ],
				[ f: [ 'b', 'b.jar' ] ],
				[ f: [ 'a', 's', 's.jar' ] ] ]    | [ [ 'b', 'b.jar' ], [ 'a', 's', 's.jar' ] ]
	}

	def "All classes inside a JAR can be discovered"( ) {
		given:
		"A JAR file containing a known set of classes"
		def targetPath = Paths.get( 'target', JarInspectorTest.class.simpleName )
		def pathToJar = targetPath.resolve( 'the.jar' )
				.toAbsolutePath().toString()
		def jar = createExecutableJar( pathToJar, [ classNames: classes ] )

		when:
		"I ask for all classes inside the JAR"
		def result = new JarInspector().findAllClassNamesIn( jar )

		then:
		"I get the expected set of classes"
		assert result as Set == classes as Set

		cleanup:
		jar?.close()
		safeDelete targetPath.toFile()

		where:
		classes << [
				[ 'SomeClass' ],
				[ 'com.eviware.Example', 'org.apache.Test' ],
				[ 'Class1', 'Class2', 'Class3' ]
		]

	}

	def "The name of the class in a .class file can be found from the path to the file"( ) {
		when:
		def result = new JarInspector().fromClassFileToClassName( classFile )

		then:
		result == expected

		where:
		classFile                           | expected
		'Abcdefghijkl.class'                | 'Abcdefghijkl'
		'A.class'                           | 'A'
		'h.class'                           | 'h'
		'path/to/a/Class1.class'            | 'path.to.a.Class1'
		"some${File.separator}Class2.class" | "some.Class2"
	}

	def "The groupId of a Maven artifact can be found from the location of a Jar file"( ) {
		given:
		"A Jar Inspector and the location of a Jar file relative to the Maven Repo Home"
		def jarInspector = new JarInspector()

		when:
		"I ask for the groupId of the artifact"
		def result = jarInspector.groupIdFrom( locationParts as String[] )

		then:
		"The groupId of the artifact is determined correctly"
		result == expectedGroupId

		where:
		locationParts                                  | expectedGroupId
		[ 'org', 'example', 'v1.0', 'the.jar' ]        | 'org'
		[ 'org', 'com', 'example', 'v1.0', 'the.jar' ] | 'org.com'
		[ 'a', 'b', 'c', 'a', 'v1.0', 'the.jar' ]      | 'a.b.c'

	}

	def "A VersionedArtifact can be created given just a Jar file based on its location"( ) {
		given:
		"A JarInspector"
		def jarInspector = new JarInspector()

		and:
		"The Maven repository location is known"
		def targetPath = Paths.get( 'target', this.class.simpleName )
		jarInspector.filesHelper = new FilesHelper() {
			@Override
			String getMavenRepoHome( ) { targetPath.toAbsolutePath().toString() }
		}

		and:
		"The default location of a given Jar in a Maven repository is known"
		def props = expectedProperties
		def jarLocation = targetPath.resolve( list2path(
				props.g.split( /\./ ) + props.a + props.v ) )

		and:
		"A Jar File with known contents"
		def pathToJar = jarLocation.resolve( 'the.jar' )
				.toAbsolutePath().toString()
		def jar = createExecutableJar( pathToJar,
				[ classNames: [ 'some.ClassInJar' ] ] )

		when:
		"A Versioned artifact is created from the Jar"
		def artifact = jarInspector.jar2artifact( jar )

		then:
		"The Versioned artifact has the expected properties"
		artifact != null
		artifact.groupId == expectedProperties.g
		artifact.artifactId == expectedProperties.a
		artifact.version == expectedProperties.v

		cleanup:
		jar?.close()
		safeDelete targetPath.toFile()

		where:
		expectedProperties << [
				[ g: 'group', a: 'd', v: '2.5.0' ],
				[ g: 'com.athaydes.osgimonitor', a: 'osgimonitor-backend', v: '0.1-SNAPSHOT' ]
		]
	}

}
