package com.athaydes.osgimonitor.impl.manage.maven;

import com.athaydes.osgimonitor.impl.manage.FilesHelper;
import com.athaydes.osgimonitor.impl.manage.XmlHelper;
import org.w3c.dom.Document;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;

import static java.util.regex.Pattern.quote;

/**
 * User: Renato
 */
public class MavenHelper extends FilesHelper {

	private static final String SETTINGS_FILE_NAME = "settings.xml";

	private XmlHelper xmlHelper = new XmlHelper();

	public void setXmlHelper( XmlHelper xmlHelper ) {
		this.xmlHelper = xmlHelper;
	}

	public String getMavenHome() {
		String m2Home = getMavenHomeEnvVariable();
		if ( dirExists( m2Home ) ) {
			return m2Home;
		} else {
			String userHome = getUserHome();
			String mavenHome = userHome + File.separator + ".m2";
			if ( dirExists( mavenHome ) ) {
				return mavenHome;
			} else {
				throw new RuntimeException( "Cannot find the Maven Home" );
			}
		}
	}

	public String getMavenRepoHome() {
		File settingsFile = Paths.get( getMavenHome(), SETTINGS_FILE_NAME ).toFile();
		if ( settingsFile.exists() ) {
			try {
				Document doc = xmlHelper.parseFile( settingsFile );
				String repoLocation = xmlHelper.evalXPath( doc, "/settings/localRepository/text()" );
				if ( repoLocation != null ) {
					return repoLocation;
				}
			} catch ( Exception e ) {
				e.printStackTrace();
			}
		}
		return getDefaultMavenRepoHome();
	}

	public String pathFromMavenRepoHome( String fullPath ) {
		String mavenRepoHome = getMavenRepoHome();
		String[] parts = fullPath.split( quote( mavenRepoHome ) );
		if ( parts.length != 2 )
			throw new RuntimeException( "Full path to jar is not" +
					" under Maven repo home: " + fullPath );
		return parts[1].substring( 1 );
	}

	public String groupIdFrom( String[] locationParts ) {
		String[] parts = Arrays.copyOfRange( locationParts,
				0, locationParts.length - 3 );
		String result = parts[0];
		for ( int i = 1; i < parts.length; i++ ) {
			result += "." + parts[i];
		}
		return result;
	}

	protected String getMavenHomeEnvVariable() {
		return System.getenv( "M2_HOME" );
	}

	private String getDefaultMavenRepoHome() {
		return Paths.get( getMavenHome(), "repository" ).toString();
	}

}
