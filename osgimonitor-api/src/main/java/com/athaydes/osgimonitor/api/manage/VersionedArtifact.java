package com.athaydes.osgimonitor.api.manage;

/**
 * User: Renato
 */
public class VersionedArtifact extends Artifact {

	private final String version;

	public VersionedArtifact( Artifact artifact, String version ) {
		super( artifact );
		this.version = version;
	}

	public String getVersion() {
		return version;
	}

	public static VersionedArtifact from( String groupId,
	                                      String artifactId,
	                                      String version ) {
		return new VersionedArtifact(
				new Artifact( groupId, artifactId ),
				version );
	}


}
