package com.athaydes.osgimonitor.api.manage;

/**
 * User: Renato
 */
public interface ArtifactInstaller {

	/**
	 * Install the given artifact into the OSGi container
	 *
	 * @param versionedArtifact to be installed
	 * @return true if the artifact was not previously installed and it was
	 *         installed successfully, false if the artifact was already installed
	 * @throws RuntimeException if a problem arises when trying to install
	 *                          the artifact
	 */
	boolean install( VersionedArtifact versionedArtifact );

	/**
	 * Uninstall the given artifact from the OSGi container
	 *
	 * @param versionedArtifact to be uninstalled
	 * @return true if the artifact was previously installed and it was
	 *         successfully uninstalled, false if the artifact was not previously
	 *         installed
	 * @throws RuntimeException if a problem arises when trying to uninstall
	 *                          the artifact
	 */
	boolean uninstall( VersionedArtifact versionedArtifact );

}
