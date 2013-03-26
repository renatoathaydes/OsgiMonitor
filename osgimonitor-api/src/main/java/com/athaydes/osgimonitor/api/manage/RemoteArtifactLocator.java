package com.athaydes.osgimonitor.api.manage;

/**
 * User: Renato
 */
public interface RemoteArtifactLocator extends ArtifactLocator {

	/**
	 * Installs the given artifact in the user local repository.
	 *
	 * @param artifact
	 * @return true if installation was successful, false otherwise
	 */
	boolean installInLocal( VersionedArtifact artifact );

}
