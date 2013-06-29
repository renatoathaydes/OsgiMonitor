package com.athaydes.osgimonitor.impl.manage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.jar.JarFile;

/**
 * User: Renato
 */
public class JarInspector {

	public String[] findAllClassNamesIn( JarFile jar ) {

		return null;
	}

	public JarFile[] filterJars( Collection<? extends Path> paths ) {
		List<JarFile> result = new ArrayList<>();
		for ( Path path : paths ) {
			if ( path.getFileName().toString().endsWith( "jar" ) ) {
				try {
					result.add( new JarFile( path.toFile(), false ) );
				} catch ( IOException e ) {
					e.printStackTrace();
				}
			}
		}
		return result.toArray( new JarFile[result.size()] );
	}

}
