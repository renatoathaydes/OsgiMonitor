package com.athaydes.osgimonitor.impl.manage;

import com.athaydes.osgimonitor.api.manage.Artifact;
import com.athaydes.osgimonitor.api.manage.RemoteArtifactLocator;
import com.athaydes.osgimonitor.api.manage.SearchOption;
import com.athaydes.osgimonitor.api.manage.VersionedArtifact;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
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
public class RemoteArtifactLocatorImpl implements RemoteArtifactLocator {

	private static final String MAVEN_URL = "http://search.maven.org/solrsearch/select?q=";

	private enum QueryType {
		GENERAL( "" ), CLASS_NAME( "c" ), GROUP_ID( "g" ), ARTIFACT_ID( "a" );

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
	public boolean installInLocal( VersionedArtifact artifact ) {
		return false;
	}

	@Override
	public Set<Artifact> findArtifacts( String keywords ) {
		return null;
	}

	@Override
	public Set<Artifact> findArtifacts( String groupId, String artifactId, SearchOption option ) {
		String xmlResponse = query( new QueryPart( QueryType.GROUP_ID, groupId ),
				new QueryPart( QueryType.ARTIFACT_ID, artifactId ) );

		try {
			return new HashSet<Artifact>( parseMavenXmlResponse( xmlResponse ) );
		} catch ( Exception e ) {
			throw new RuntimeException( "Could not parse Maven response", e );
		}
	}

	List<VersionedArtifact> parseMavenXmlResponse( String xmlResponse ) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Document doc = factory.newDocumentBuilder().parse( new ByteArrayInputStream( xmlResponse.getBytes( "UTF-8" ) ) );
		XPath xpath = XPathFactory.newInstance().newXPath();

		XPathExpression expr = xpath.compile( "//doc" );
		NodeList nodes = ( NodeList ) expr.evaluate( doc, XPathConstants.NODESET );

		List<VersionedArtifact> artifacts = new ArrayList<>( nodes.getLength() );
		for ( int i = 0; i < nodes.getLength(); i++ ) {
			String artifactId = xpath.evaluate( "str[@name='a']", nodes.item( i ) );
			String groupId = xpath.evaluate( "str[@name='g']", nodes.item( i ) );
			String latestVersion = xpath.evaluate( "str[@name='latestVersion']", nodes.item( i ) );
			Artifact artifact = new Artifact( groupId, artifactId );
			artifacts.add( new VersionedArtifact( artifact, latestVersion ) );
		}

		return artifacts;
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
