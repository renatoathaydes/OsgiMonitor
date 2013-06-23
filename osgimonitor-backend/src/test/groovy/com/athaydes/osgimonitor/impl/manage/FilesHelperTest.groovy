package com.athaydes.osgimonitor.impl.manage

import spock.lang.Specification

import java.nio.file.Paths

/**
 *
 * User: Renato
 */
class FilesHelperTest extends Specification {

	def "All files with a certain extension can be found in a directory tree"( ) {
		given:
		"A file tree of known contents"
		final targetPath = Paths.get( 'target' )
		targetPath.toFile().mkdir() // should be already made
		final rootDir = [ this.class.simpleName ]
		final rootPath = targetPath.resolve( rootDir[ 0 ] )
		assert rootPath.toFile().mkdir()

		def list2path = { list ->
			Paths.get( 'target', ( ( rootDir + list ) as String[] ) )
		}

		def paths = files.collect {
			def path = list2path( it.d ?: it.f )
			println "Created path ${path.toAbsolutePath()}"
			if ( it.d ) assert path.toFile().mkdir()
			else assert path.toFile().createNewFile()
			return path
		}

		when:
		"I ask for all files with an extension"
		def result = FilesHelper.findAllFilesWithExtension( extension, rootPath )

		then:
		"I get all expected files with that extension"
		result as Set == expected.collect { list2path( it ) } as Set

		cleanup:
		paths?.reverse()?.each {
			try { it.toFile().delete() } catch ( e ) { e.printStackTrace() }
		}
		rootPath.toFile().delete()

		where:
		extension | files /* d is dir, f is file */                                                       | expected
		"jar"     | [ ]                                                                                   | [ ]
		"jar"     | [ [ d: [ 'a' ] ], [ f: [ 'a.jar' ] ] ]                                                | [ [ 'a.jar' ] ]
		"jar"     | [ [ d: [ 'a' ] ], [ f: [ 'a', 'a.jar' ] ] ]                                           | [ [ 'a', 'a.jar' ] ]
		"m"       | [ [ f: [ 'm' ] ] ]                                                                    | [ ]
		"m"       | [ [ f: [ 'm.m' ] ], [ f: [ 'm' ] ] ]                                                  | [ [ 'm.m' ] ]
		"m"       | [ [ f: [ 'm.m' ] ], [ d: [ 'n.m' ] ] ]                                                | [ [ 'm.m' ] ]
		"m"       | [ [ f: [ 'm.m' ] ], [ d: [ 'm' ] ], [ d: [ 'm', 'n' ] ], [ f: [ 'm', 'n', 'o.m' ] ] ] | [ [ 'm.m' ], [ 'm', 'n', 'o.m' ] ]
	}


}
