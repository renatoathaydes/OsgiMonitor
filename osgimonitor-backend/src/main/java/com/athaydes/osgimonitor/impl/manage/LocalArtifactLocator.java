package com.athaydes.osgimonitor.impl.manage;

import com.athaydes.osgimonitor.api.manage.Artifact;
import com.athaydes.osgimonitor.api.manage.ArtifactLocator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * User: Renato
 */
public class LocalArtifactLocator implements ArtifactLocator {

	private FilesHelper filesHelper = new FilesHelper();

	public void setFilesHelper( FilesHelper filesHelper ) {
		this.filesHelper = filesHelper;
	}

	@Override
	public Set<Artifact> findByClassName( String className ) {
		Set<Artifact> result = new HashSet<>();
		String mavenHome = filesHelper.getMavenHome();
		Path repositoryDir = Paths.get( mavenHome, "repository" );
		try {
			List<Path> jars = filesHelper.findAllFilesWithExtension( "jar", repositoryDir );

		} catch ( IOException e ) {

		}

		return result;
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
