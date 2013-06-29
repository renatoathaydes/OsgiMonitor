package com.athaydes.osgimonitor.impl

import java.nio.file.Path
import java.nio.file.Paths
import java.util.jar.JarFile

/**
 *
 * User: Renato
 */
class CommonTestFunctions {

	static Path list2path( list ) {
		Paths.get( 'target', ( list as String[] ) )
	}

	static createFileTreeWith( files, Class testClass ) {
		final targetPath = Paths.get( 'target' )
		targetPath.toFile().mkdir()
		final rootPath = targetPath.resolve( testClass.simpleName )
		rootPath.toFile().delete()
		assert rootPath.toFile().mkdir()

		[ rootPath ] + files.collect {
			def path = list2path( [ testClass.simpleName ] + ( it.d ?: it.f ) )
			println "Created path ${path.toAbsolutePath()}"
			if ( it.d ) assert path.toFile().mkdir()
			else if ( it.f.last().endsWith( '.jar' ) )
				assert createExecutableJar( path.toAbsolutePath().toString() )
			else assert createEmptyFileFrom( path )
			return path
		}
	}

	static createEmptyFileFrom( Path path ) {
		path.toFile().createNewFile()
	}
	static final ant = new AntBuilder()
	static JarFile createExecutableJar( String jarName ) {
		if ( !jarName.endsWith( '.jar' ) ) jarName += '.jar'

		def tempDir = File.createTempDir().absolutePath
		println "Executable Jar Creator using tempDir: $tempDir"
		def javaFile = 'Temp.java'
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
		//final ant = new AntBuilder()
		ant.delete( file: file, dir: file )
	}

}
