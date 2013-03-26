package com.athaydes.osgimonitor.api.manage;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * User: Renato
 */
public class Artifact {

	private final String groupId;
	private final String artifactId;
	private final Set<VersionedArtifact> requiredDependencies = new HashSet<>();

	public Artifact( String groupId, String artifactId ) {
		this.groupId = groupId;
		this.artifactId = artifactId;
	}

	public Artifact( Artifact artifact ) {
		this( artifact.getGroupId(), artifact.getArtifactId() );
	}

	public void setRequiredDependencies( Collection<VersionedArtifact> dependencies ) {
		requiredDependencies.clear();
		requiredDependencies.addAll( dependencies );
	}

	public void addRequiredDependency( VersionedArtifact dependency ) {
		requiredDependencies.add( dependency );
	}

	public String getGroupId() {
		return groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public Set<VersionedArtifact> getRequiredDependencies() {
		return Collections.unmodifiableSet( requiredDependencies );
	}

	@Override
	public boolean equals( Object o ) {
		if ( this == o ) return true;
		if ( o == null || getClass() != o.getClass() ) return false;

		Artifact that = ( Artifact ) o;

		if ( !artifactId.equals( that.artifactId ) ) return false;
		if ( !groupId.equals( that.groupId ) ) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = groupId.hashCode();
		result = 31 * result + artifactId.hashCode();
		return result;
	}

}
