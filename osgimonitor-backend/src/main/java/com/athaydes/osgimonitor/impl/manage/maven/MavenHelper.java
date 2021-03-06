package com.athaydes.osgimonitor.impl.manage.maven;

import com.athaydes.osgimonitor.impl.manage.FilesHelper;
import com.athaydes.osgimonitor.impl.manage.XmlHelper;
import org.w3c.dom.Document;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.regex.Pattern.quote;

/**
 * User: Renato
 */
public class MavenHelper extends FilesHelper {

	private XmlHelper xmlHelper = new XmlHelper();

	public void setXmlHelper( XmlHelper xmlHelper ) {
		this.xmlHelper = xmlHelper;
	}

	protected List<File> getSettingsFiles() {
		final String settingsFileName = "settings.xml";
		List<File> candidateLocations = Arrays.asList(
				Paths.get( getUserHome(), ".m2", settingsFileName ).toFile(),
				Paths.get( getM2_HOME(), "conf", settingsFileName ).toFile() );

		List<File> result = new ArrayList<>( candidateLocations.size() );
		for ( File candidate : candidateLocations ) {
			if ( candidate.exists() )
				result.add( candidate );
		}
		return result;
	}

	public String getMavenRepoHome() {
		for ( File settingsFile : getSettingsFiles() ) {
			try {
				Document doc = xmlHelper.parseFile( settingsFile );
				String repoLocation = xmlHelper.evalXPath( doc, "/settings/localRepository/text()" );
				if ( dirExists( repoLocation ) ) {
					return repoLocation;
				}
			} catch ( Exception e ) {
				System.err.println( "Invalid settings file: " + settingsFile.getAbsolutePath() );
			}
		}
		return getDefaultMavenRepoHome();
	}

	public String[] locationParts( String fullPathToJar ) {
		verifyNotNullOrEmpty( fullPathToJar );
		String[] locationParts =
				pathFromMavenRepoHome( fullPathToJar )
						.split( quote( File.separator ) );
		verifyLocationParts( locationParts, "Cannot recognize path as being" +
				" in a Maven Repo: " + fullPathToJar );
		return locationParts;
	}

	private void verifyLocationParts( String[] locationParts, String... errorMessage ) {
		if ( locationParts.length < 4 ) {
			throw new RuntimeException( errorMessage.length == 0 ?
					"Maven location must contain at least 4 parts" : errorMessage[0] );
		}
	}

	private String pathFromMavenRepoHome( String fullPath ) {
		verifyNotNullOrEmpty( fullPath );
		String mavenRepoHome = getMavenRepoHome();
		String[] parts = fullPath.split( quote( mavenRepoHome ) );
		if ( parts.length != 2 )
			throw new RuntimeException( "Full path to jar is not" +
					" under Maven repo home: " + fullPath );
		return parts[1].substring( 1 );
	}

	public String groupIdFrom( String[] locationParts ) {
		verifyLocationParts( locationParts );
		String[] parts = Arrays.copyOfRange( locationParts,
				0, locationParts.length - 3 );
		String result = parts[0];
		for ( int i = 1; i < parts.length; i++ ) {
			result += "." + parts[i];
		}
		return result;
	}

	public String artifactIdFrom( String[] locationParts ) {
		verifyLocationParts( locationParts );
		return locationParts[locationParts.length - 3];
	}

	public String versionFrom( String[] locationParts ) {
		verifyLocationParts( locationParts );
		return locationParts[locationParts.length - 2];
	}

	public Path locationOfArtifact( String groupId, String artifactId ) {
		verifyNotNullOrEmpty( groupId, artifactId );
		List<String> pathList = getPathListFromGroupId( groupId );
		pathList.add( artifactId );
		return Paths.get( getMavenRepoHome(),
				pathList.toArray( new String[pathList.size()] ) );
	}

	public List<String> findArtifactIdsUnder( String groupId ) {
		verifyNotNullOrEmpty( groupId );
		List<String> pathList = getPathListFromGroupId( groupId );
		Path groupPath = Paths.get( getMavenRepoHome(),
				pathList.toArray( new String[pathList.size()] ) );
		return listFoldersUnder( groupPath );
	}

	public boolean isMavenVersion( String text ) {
		if ( text == null || text.isEmpty() )
			return false;
		String[] parts = text.split( quote( "." ) );
		int startsWithIntCount = 0;
		for ( int i = 0; i < Math.min( 2, parts.length ); i++ ) {
			String part = parts[i];
			if ( !part.isEmpty() && Character.isDigit( part.toCharArray()[0] ) )
				startsWithIntCount++;
		}
		return startsWithIntCount > 1;
	}

	public boolean isArtifactId( Path path ) {
		for ( File child : nullSafeListFiles( path.toFile() ) ) {
			if ( child.isDirectory()
					&& isMavenVersion( child.getName() )
					&& hasChildJar( child ) )
				return true;
		}
		return false;
	}

	private boolean hasChildJar( File file ) {
		for ( File child : nullSafeListFiles( file ) ) {
			if ( child.isFile() && hasExtension( child.toPath(), "jar" ) )
				return true;
		}
		return false;
	}

	private List<String> getPathListFromGroupId( String groupId ) {
		List<String> pathList = new ArrayList<>();
		pathList.addAll( Arrays.asList( groupId.split( quote( "." ) ) ) );
		return pathList;
	}

	private void verifyNotNullOrEmpty( String... args ) {
		for ( String arg : args ) {
			if ( arg == null || arg.isEmpty() ) {
				throw new IllegalArgumentException( "Argument cannot be null or empty" );
			}
		}
	}

	protected String getM2_HOME() {
		return System.getenv( "M2_HOME" );
	}

	private String getDefaultMavenRepoHome() {
		return Paths.get( getUserHome(), ".m2", "repository" ).toString();
	}

}
