package com.athaydes.osgimonitor.impl.manage.maven;

import com.athaydes.osgimonitor.api.manage.VersionedArtifact;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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
		String[] locationParts = mavenHelper.locationParts( jarFile.getName() );
		String version = mavenHelper.versionFrom( locationParts );
		String artifactId = mavenHelper.artifactIdFrom( locationParts );
		String groupId = mavenHelper.groupIdFrom( locationParts );
		return VersionedArtifact.from( groupId, artifactId, version );
	}

}
