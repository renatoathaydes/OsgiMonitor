package com.athaydes.osgimonitor.api.manage;

import java.util.Set;

/**
 * An artifact locator.
 * User: Renato
 */
public interface ArtifactLocator {


	/**
	 * Find all artifacts matching the given keyword(s) in any field.
	 *
	 * @param keywords separated by white-spaces or commas
	 * @return artifacts
	 */
	Set<Artifact> findArtifacts( String keywords );

	/**
	 * Find all artifacts matching the given parameters
	 *
	 * @param groupId    the group ID
	 * @param artifactId the artifact ID
	 * @param option     to search with
	 * @return artifacts
	 */
	Set<Artifact> findArtifacts( String groupId, String artifactId, SearchOption option );

	/**
	 * @param artifact the artifact
	 * @return all versions available for the given artifact
	 */
	Set<String> getVersionsOf( Artifact artifact );

}
