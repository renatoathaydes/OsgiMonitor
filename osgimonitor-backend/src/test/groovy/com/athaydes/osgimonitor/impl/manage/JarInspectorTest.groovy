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
		def paths = createFileTreeWith( files, JarInspectorTest )

		when:
		"I filter the jars from the collection"
		def result = new JarInspector().filterJars( paths )

		then:
		"I get an array containing only the actual jars"
		result.collect { Paths.get( it.name ) } as Set ==
				expected.collect { list2path( [ this.class.simpleName ] + it ) } as Set

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
		[ [ d: [ 'test' ] ] ]                     | [ ]
		[ [ d: [ 'test' ] ] ]                     | [ ]
	}

}
