package com.athaydes.osgimonitor.impl.manage;

import com.athaydes.osgimonitor.api.manage.VersionedArtifact;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * User: Renato
 */
public class JarInspector {

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
		//TODO implement
		return null;
	}
}
