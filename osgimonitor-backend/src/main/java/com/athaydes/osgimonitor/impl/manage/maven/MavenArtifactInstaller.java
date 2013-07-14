package com.athaydes.osgimonitor.impl.manage.maven;

import com.athaydes.osgimonitor.api.manage.ArtifactInstaller;
import com.athaydes.osgimonitor.api.manage.VersionedArtifact;

/**
 * User: Renato
 */
public class MavenArtifactInstaller implements ArtifactInstaller {


	@Override
	public boolean install( VersionedArtifact versionedArtifact ) {
		return false;
	}

	@Override
	public boolean uninstall( VersionedArtifact versionedArtifact ) {
		return false;
	}

}
