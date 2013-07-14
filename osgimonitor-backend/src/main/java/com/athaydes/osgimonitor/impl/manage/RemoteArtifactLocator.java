package com.athaydes.osgimonitor.impl.manage;

import com.athaydes.osgimonitor.api.manage.Artifact;
import com.athaydes.osgimonitor.api.manage.ArtifactLocator;
import com.athaydes.osgimonitor.api.manage.VersionedArtifact;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.*;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * User: Renato
 */
public class RemoteArtifactLocator implements ArtifactLocator {

	private static final String MAVEN_URL = "http://search.maven.org/solrsearch/select?q=";
	private static final DocumentBuilderFactory DOC_BUILDER_FACTORY =
			DocumentBuilderFactory.newInstance();
	private static final XPath X_PATH = XPathFactory.newInstance().newXPath();

	private enum QueryType {
		CLASS_NAME( "c" ), GROUP_ID( "g" ), ARTIFACT_ID( "a" );

		final String key;

		QueryType( String key ) {
			this.key = key;
		}
	}

	private class QueryPart {
		final QueryType type;
		final String queryStr;

		QueryPart( QueryType type, String queryStr ) {
			this.type = type;
			this.queryStr = queryStr;
		}
	}

	@Override
	public Set<? extends VersionedArtifact> findByClassName( String className ) {
		return findBy( QueryType.CLASS_NAME, className );
	}

	@Override
	public Set<? extends Artifact> findByGroupId( String groupId ) {
		return findBy( QueryType.GROUP_ID, groupId );
	}

	@Override
	public Set<? extends Artifact> findByArtifactId( String artifactId ) {
		return findBy( QueryType.ARTIFACT_ID, artifactId );
	}

	private Set<? extends VersionedArtifact> findBy( QueryType queryType, String value ) {
		String xmlResponse = query( new QueryPart( queryType, value ) );
		return new HashSet<>( toVersionedArtifacts( xmlResponse ) );
	}

	@Override
	public Artifact findArtifact( String groupId, String artifactId ) {
		String xmlResponse = query( new QueryPart( QueryType.GROUP_ID, groupId ),
				new QueryPart( QueryType.ARTIFACT_ID, artifactId ) );
		List<VersionedArtifact> artifacts = toVersionedArtifacts( xmlResponse );
		switch ( artifacts.size() ) {
			case 0:
				return null;
			case 1:
				return artifacts.get( 0 );
			default:
				throw new RuntimeException( "Search by groupId and artifactId should return a " +
						"single result but returned " + artifacts.size() );
		}
	}

	private List<VersionedArtifact> toVersionedArtifacts( String xmlResponse ) {
		List<VersionedArtifact> artifacts;
		try {
			artifacts = parseMavenXmlResponse( xmlResponse );
		} catch ( Exception e ) {
			throw new RuntimeException( "Could not parse Maven response", e );
		}
		return artifacts;
	}

	List<VersionedArtifact> parseMavenXmlResponse( String xmlResponse ) throws Exception {
		Document doc = DOC_BUILDER_FACTORY.newDocumentBuilder().parse( new ByteArrayInputStream( xmlResponse.getBytes( "UTF-8" ) ) );

		XPathExpression expr = X_PATH.compile( "//doc" );
		NodeList nodes = ( NodeList ) expr.evaluate( doc, XPathConstants.NODESET );

		List<VersionedArtifact> artifacts = new ArrayList<>( nodes.getLength() );
		for ( int i = 0; i < nodes.getLength(); i++ ) {
			artifacts.add( toVersionedArtifact( nodes.item( i ) ) );
		}

		return artifacts;
	}

	private VersionedArtifact toVersionedArtifact( Node node ) {
		try {
			String groupId = X_PATH.evaluate( "str[@name='g']", node );
			String artifactId = X_PATH.evaluate( "str[@name='a']", node );
			String latestVersion = X_PATH.evaluate( "str[@name='latestVersion']", node );
			Artifact artifact = new Artifact( groupId, artifactId );
			return new VersionedArtifact( artifact, latestVersion );
		} catch ( XPathExpressionException e ) {
			throw new RuntimeException( e );
		}
	}

	@Override
	public Set<String> getVersionsOf( Artifact artifact ) {
		return null;
	}

	private String query( QueryPart... queryParts ) {
		return readAsString( MAVEN_URL + groupQueryParts( queryParts ) + "&rows=5&wt=xml" );
	}

	private String groupQueryParts( QueryPart... queryParts ) {
		String result = "";
		for ( QueryPart part : queryParts ) {
			if ( !result.isEmpty() ) result += "+AND+";
			result += part.type.key + ":" + part.queryStr;
		}
		return result;
	}

	private static String readAsString( String uri ) {
		System.out.println( "Connecting to " + uri );
		return extractResponse( connectTo( uri ) );
	}

	private static HttpURLConnection connectTo( String uri ) {
		URL url;
		try {
			url = new URL( uri );
		} catch ( MalformedURLException e ) {
			throw new RuntimeException( "URI provided is not valid: " + uri, e );
		}

		HttpURLConnection connection;
		try {
			connection = ( HttpURLConnection ) url.openConnection();
			connection.connect();
			if ( connection.getResponseCode() != HttpURLConnection.HTTP_OK ) {
				throw new RuntimeException( "Maven server responde code was " + connection.getResponseCode() );
			}
		} catch ( IOException e ) {
			throw new RuntimeException( "Could not connect to Maven" );
		}
		return connection;
	}

	private static String extractResponse( HttpURLConnection connection ) {
		try ( BufferedReader in = new BufferedReader(
				new InputStreamReader(
						connection.getInputStream() ) ) ) {
			StringBuilder sb = new StringBuilder();

			String inputLine;
			while ( ( inputLine = in.readLine() ) != null )
				sb.append( inputLine ).append( "\n" );

			return sb.toString();
		} catch ( IOException e ) {
			throw new RuntimeException( "Problem reading Maven repo response", e );
		}
	}

}
