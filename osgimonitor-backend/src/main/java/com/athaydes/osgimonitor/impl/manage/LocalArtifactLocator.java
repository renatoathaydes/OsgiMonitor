package com.athaydes.osgimonitor.impl.manage;

import com.athaydes.osgimonitor.api.manage.Artifact;
import com.athaydes.osgimonitor.api.manage.ArtifactLocator;

import java.util.Set;

/**
 * User: Renato
 */
public class LocalArtifactLocator implements ArtifactLocator {
	@Override
	public Set<Artifact> findByClassName( String className ) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public Set<Artifact> findByGroupId( String groupId ) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public Set<Artifact> findByArtifactId( String artifactId ) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public Artifact findArtifact( String groupId, String artifactId ) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public Set<String> getVersionsOf( Artifact artifact ) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}
}
