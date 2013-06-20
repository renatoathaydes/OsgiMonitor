package com.athaydes.osgimonitor.api.manage;

import java.util.Set;

/**
 * An artifact locator.
 * User: Renato
 */
public interface ArtifactLocator {


	/**
	 * Find all artifacts containing a class with the given name.
	 *
	 * @param className name of the class
	 * @return artifacts
	 */
	Set<Artifact> findByClassName( String className );

	/**
	 * Find all artifacts matching the given groupdId.
	 *
	 * @param groupId the group ID
	 * @return artifacts
	 */
	Set<Artifact> findByGroupId( String groupId );

	/**
	 * Find all artifacts matching the given artifactId.
	 *
	 * @param artifactId the artifact ID
	 * @return artifacts
	 */
	Set<Artifact> findByArtifactId( String artifactId );

	/**
	 * Find a single artifact matching the given parameters
	 *
	 * @param groupId    the group ID
	 * @param artifactId the artifact ID
	 * @return artifact if any, or null
	 */
	Artifact findArtifact( String groupId, String artifactId );

	/**
	 * @param artifact the artifact
	 * @return all versions available for the given artifact
	 */
	Set<String> getVersionsOf( Artifact artifact );

}
