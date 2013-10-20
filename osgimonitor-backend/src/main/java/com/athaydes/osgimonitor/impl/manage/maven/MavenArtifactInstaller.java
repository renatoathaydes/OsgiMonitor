package com.athaydes.osgimonitor.impl.manage.maven;

import com.athaydes.osgimonitor.api.manage.ArtifactInstaller;
import com.athaydes.osgimonitor.api.manage.VersionedArtifact;
import javax.xml.XMLConstants;

/**
 * User: Renato
 */
public class MavenArtifactInstaller implements ArtifactInstaller {

	private MavenHelper helper = new MavenHelper();

	@Override
	public boolean install( VersionedArtifact versionedArtifact ) {

		return false;

	}

	@Override
	public boolean uninstall( VersionedArtifact versionedArtifact ) {
		return false;
	}

	public void setMavenHelper( MavenHelper helper ) {
		this.helper = helper;
	}

}
