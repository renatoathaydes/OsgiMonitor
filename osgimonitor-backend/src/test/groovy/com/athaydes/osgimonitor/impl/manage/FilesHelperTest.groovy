package com.athaydes.osgimonitor.impl.manage

import spock.lang.Specification

import static com.athaydes.osgimonitor.impl.CommonTestFunctions.*

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

}
