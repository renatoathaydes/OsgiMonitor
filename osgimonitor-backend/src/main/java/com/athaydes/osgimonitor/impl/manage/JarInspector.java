package com.athaydes.osgimonitor.impl.manage;

import com.athaydes.osgimonitor.api.manage.VersionedArtifact;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static java.util.regex.Pattern.quote;

/**
 * User: Renato
 */
public class JarInspector {

	private FilesHelper filesHelper = new FilesHelper();

	public void setFilesHelper( FilesHelper filesHelper ) {
		this.filesHelper = filesHelper;
	}

	public String[] findAllClassNamesIn( JarFile jar ) {
		List<String> result = new ArrayList<>();
		Enumeration<JarEntry> entries = jar.entries();
		while ( entries.hasMoreElements() ) {
			JarEntry entry = entries.nextElement();
			if ( !entry.isDirectory() && entry.getName().endsWith( ".class" ) ) {
				result.add( fromClassFileToClassName( entry.getName() ) );
			}
		}
		return result.toArray( new String[result.size()] );
	}

	protected String fromClassFileToClassName( final String path ) {
		return path.replace( "/", "." ).replace( File.separator, "." )
				.substring( 0, path.length() - ".class".length() );
	}

	public JarFile[] filterJars( Collection<? extends Path> paths ) {
		List<JarFile> result = new ArrayList<>();
		for ( Path path : paths ) {
			if ( path.getFileName().toString().endsWith( ".jar" ) ) {
				try {
					result.add( new JarFile( path.toFile(), false ) );
				} catch ( IOException e ) {
					e.printStackTrace();
				}
			}
		}
		return result.toArray( new JarFile[result.size()] );
	}

	public VersionedArtifact jar2artifact( JarFile jarFile ) {
		String location = jarFile.getName();
		String pathFromMavenRepoHome = pathFromMavenRepoHome( location );
		String[] locationParts = pathFromMavenRepoHome.split( quote( File.separator ) );
		if ( locationParts.length < 4 ) {
			throw new RuntimeException( "Cannot recognize path as being" +
					" in a Maven Repo: " + location );
		}
		int len = locationParts.length;
		String version = locationParts[len - 2];
		String artifactId = locationParts[len - 3];
		String groupId = groupIdFrom( locationParts );
		return VersionedArtifact.from( groupId, artifactId, version );
	}

	protected String pathFromMavenRepoHome( String fullPath ) {
		String mavenRepoHome = filesHelper.getMavenRepoHome();
		String[] parts = fullPath.split( quote( mavenRepoHome ) );
		if ( parts.length != 2 )
			throw new RuntimeException( "Full path to jar is not" +
					" under Maven repo home" + fullPath );
		return parts[1].substring( 1 );
	}

	protected String groupIdFrom( String[] locationParts ) {
		String[] parts = Arrays.copyOfRange( locationParts,
				0, locationParts.length - 3 );
		String result = parts[0];
		for ( int i = 1; i < parts.length; i++ ) {
			result += "." + parts[i];
		}
		return result;
	}
}
