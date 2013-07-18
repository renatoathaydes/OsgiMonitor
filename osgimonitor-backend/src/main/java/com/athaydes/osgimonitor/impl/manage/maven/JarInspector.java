package com.athaydes.osgimonitor.impl.manage.maven;

import com.athaydes.osgimonitor.api.manage.VersionedArtifact;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static java.util.regex.Pattern.quote;

/**
 * User: Renato
 */
public class JarInspector {

	private MavenHelper mavenHelper = new MavenHelper();

	public void setMavenHelper( MavenHelper mavenHelper ) {
		this.mavenHelper = mavenHelper;
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

	public VersionedArtifact jar2artifact( JarFile jarFile ) {
		String location = jarFile.getName();
		String pathFromMavenRepoHome = mavenHelper.pathFromMavenRepoHome( location );
		String[] locationParts = pathFromMavenRepoHome.split( quote( File.separator ) );
		if ( locationParts.length < 4 ) {
			throw new RuntimeException( "Cannot recognize path as being" +
					" in a Maven Repo: " + location );
		}
		int len = locationParts.length;
		String version = locationParts[len - 2];
		String artifactId = locationParts[len - 3];
		String groupId = mavenHelper.groupIdFrom( locationParts );
		return VersionedArtifact.from( groupId, artifactId, version );
	}

}
