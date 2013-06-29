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

}