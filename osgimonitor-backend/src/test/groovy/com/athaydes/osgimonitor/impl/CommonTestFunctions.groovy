package com.athaydes.osgimonitor.impl

import java.nio.file.Path
import java.nio.file.Paths
import java.util.jar.JarFile

/**
 *
 * User: Renato
 */
class CommonTestFunctions {

	/**
	 * Converts a list or an array of Strings to a Path
	 * @param list or array of Strings
	 * @return Path
	 */
	static Path list2path( list ) {
		if ( list.size() == 0 ) return null
		Paths.get( list.first(), ( list.tail() as String[] ) )
	}

	/**
	 * Creates a file tree with the given files.
	 * <p/>
	 * Usage Example:
	 * <p/>
	 * <code>
	 * createFileTreeWith( [ <br/>
	 * [ d: [ 'dir-1' ] ], <br/>
	 * [ f: [ 'dir-1', 'file.txt' ] ] ] )
	 * </code>
	 * @param files a list of map entries, each with a single key, either 'd'
	 * for directory or 'f' for file. The entry value is a sequence of Strings
	 * representing the path.
	 * @param target to be used as the root of the tree created
	 * @return a list of Paths
	 */
	static createFileTreeWith( List files, String... target ) {
		def targetPath = list2path( target )
		safeDelete targetPath
		targetPath.toFile().mkdirs()

		[ targetPath ] + files.collect {
			def path = list2path( target + ( it.d ?: it.f ) )
			println "Created path ${path.toAbsolutePath()}"
			if ( it.d ) assert path.toFile().mkdir()
			else assert path.toFile().createNewFile()
			return path
		}
	}

	static JarFile createExecutableJar( String jarName ) {
		if ( !jarName.endsWith( '.jar' ) )
			throw new RuntimeException( "Executable jar must have .jar " +
					"extension but jarName has not: $jarName" )

		def tempDir = File.createTempDir().absolutePath
		println "Executable Jar Creator using tempDir: $tempDir"
		def javaFile = 'Temp.java'

		def ant = new AntBuilder()
		ant.echo( file: tempDir + File.separator + javaFile, '''
			class Temp {
				public static void main( String[] args ) {
					System.out.println( "Hello" );
				}
			} ''' )
		ant.javac( srcdir: tempDir, includes: javaFile, fork: 'true' )
		ant.jar( destfile: jarName, compress: true, index: true ) {
			fileset( dir: tempDir, includes: '*.class' )
			manifest {
				attribute( name: 'Main-Class', value: javaFile - '.java' )
			}
		}
		ant.delete( dir: tempDir )
		return new JarFile( jarName, false )
	}

	static safeDelete( file ) {
		def ant = new AntBuilder()
		ant.delete( file: file, dir: file )
	}

}
