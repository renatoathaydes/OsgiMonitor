package com.athaydes.osgimonitor.impl.manage;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * User: Renato
 */
public class FilesHelper {

	private static final int MAX_DEPTH_TO_VISIT = 6;

	public boolean hasExtension( Path path, String extension ) {
		String[] parts = path.getFileName().toString().split( "\\." );
		return parts.length > 1 &&
				!parts[0].isEmpty() &&
				parts[parts.length - 1].equals( extension );
	}

	public List<Path> findAllFilesIn( final Path start )
			throws IOException {
		final List<Path> result = new ArrayList<>();

		Files.walkFileTree(
				start,
				EnumSet.noneOf( FileVisitOption.class ),
				MAX_DEPTH_TO_VISIT,
				new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult visitFile( Path file, BasicFileAttributes attrs ) throws IOException {
						result.add( file );
						return FileVisitResult.CONTINUE;
					}
				} );

		return result;
	}

	public String getMavenHome() {
		String m2Home = getMavenHomeEnvVariable();
		System.out.println( "M2_HOME: " + m2Home );
		if ( dirExists( m2Home ) ) {
			return m2Home;
		} else {
			String userHome = getUserHome();
			System.out.println( "User HOME: " + userHome );
			String mavenHome = userHome + File.separator + ".m2";
			if ( dirExists( mavenHome ) ) {
				return mavenHome;
			} else {
				throw new RuntimeException( "Cannot find the Maven Home" );
			}
		}
	}

	protected String getMavenHomeEnvVariable() {
		return System.getenv( "M2_HOME" );
	}

	protected String getUserHome() {
		return System.getProperty( "user.home" );
	}

	private static boolean dirExists( String m2Home ) {
		return m2Home != null && new File( m2Home ).exists();
	}

}
