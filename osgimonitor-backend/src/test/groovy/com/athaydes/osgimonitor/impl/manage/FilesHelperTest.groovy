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
		def result = new FilesHelper()
				.findAllFilesWithExtension( extension, rootPath )

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
		try { new File( KNOWN_M2_HOME_VALUE ).delete() }
		catch ( ignored ) {}
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
}
