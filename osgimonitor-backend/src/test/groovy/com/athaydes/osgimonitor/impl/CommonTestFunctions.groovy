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

	/**
	 * Creates and returns an executable jar with the given name.
	 * A single class is created and compiled, then included in the jar
	 * with a small manifest file.
	 * @param jarName name of the jar to create
	 * @param args optional arguments are: <ul>
	 * <li>classNames: name of classes to create into the jar</li>
	 * <li>mainClass: name of the main class (must be contained in classNames)</li>
	 * </ul>
	 * @return JarFile
	 */
	static JarFile createExecutableJar( String jarName, Map args = [ : ] ) {
		if ( !jarName.endsWith( '.jar' ) )
			throw new RuntimeException( "Executable jar must have .jar " +
					"extension but jarName has not: $jarName" )

		def tempDir = File.createTempDir().absolutePath
		println "Executable Jar Creator using tempDir: $tempDir"

		def ant = new AntBuilder()
		try {
			def classNames = args.get( 'classNames', [ 'Temp' ] ) as List<String>
			if ( classNames.isEmpty() )
				throw new RuntimeException( 'classNames cannot be empty' )
			def mainClass = args.get( 'mainClass', classNames[ 0 ] )

			createJavaFiles( classNames, tempDir )

			ant.javac( srcdir: tempDir, includes: '**/*.java', fork: 'true' )
			ant.jar( destfile: jarName, compress: true, index: true ) {
				fileset( dir: tempDir, includes: '**/*.class' )
				manifest {
					attribute( name: 'Main-Class', value: mainClass )
				}
			}
		} catch ( e ) {
			throw e
		} finally {
			ant.delete( dir: tempDir )
		}
		return new JarFile( jarName, false )
	}

	private static void createJavaFiles( List<String> classNames, String rootDir ) {
		classNames.each { className ->
			def pkgAndClassName = splitPackageAndClassName( className )
			def javaFile = pkgAndClassName.simpleClassName + '.java'
			def pkgDirs = pkgAndClassName.packages.join( File.separator )
			assert createFile( [ rootDir, pkgDirs, javaFile ].join( File.separator ),
					getSimplestCompilingJavaCode( className ) )
		}
	}

	/**
	 * @param className qualified name of class in the returned java code
	 * @return simplest possible compiling class which actually does something
	 */
	static getSimplestCompilingJavaCode( String className ) {
		def pkgAndClassName = splitPackageAndClassName( className )
		def packageDeclaration = pkgAndClassName.packages ?
			"package ${pkgAndClassName.packages.join( '.' )};" :
			'// default package'

		"""$packageDeclaration
		|class ${pkgAndClassName.simpleClassName} {
		|	public static void main( String[] args ) {
		|		System.out.println( \"$className\" );
		|	}
		|} """.stripMargin()
	}

	/**
	 * @param qualifiedClassName
	 * @return a map containing the <code>packages</code> in a list
	 * and the <code>simpleClassName</code> as a String from the given
	 * qualifiedClassName
	 */
	static splitPackageAndClassName( String qualifiedClassName ) {
		if ( !qualifiedClassName || qualifiedClassName.isEmpty() )
			throw new RuntimeException( 'No class name given' )
		def parts = qualifiedClassName.split( /\./ )
		[ packages: parts.size() > 1 ? parts[ 0..-2 ] : [ ],
				simpleClassName: parts[ -1 ] ]
	}

	/**
	 * Creates a file with the given fileName. Optionally, the contents
	 * of the file can be given.
	 * @param fileName name of the file. Can be a relative or absolute path
	 * @param contents optional contents of the file
	 * @return true if the file was created correctly
	 */
	static boolean createFile( String fileName, contents = '' ) {
		println "Creating file $fileName"
		def ant = new AntBuilder()
		ant.echo( file: fileName, contents )
		return new File( fileName ).exists()
	}

	/**
	 * Safely deletes the given file.
	 * If it's a directory, all of its contents will be deleted too.
	 * @param file can be a file or directory. Will work with instances of
	 * File or String
	 */
	static void safeDelete( file ) {
		def ant = new AntBuilder()
		ant.delete( file: file, dir: file )
	}

	/**
	 * @param text candidate version
	 * @return true iff the given text looks like an artifact version
	 */
	static boolean looksLikeArtifactVersion( String text ) {
		if ( text == null || text.isEmpty() )
			false
		else
			text.split( /\./ ).grep { it[ 0 ].isInteger() }.size() > 1
	}

}
