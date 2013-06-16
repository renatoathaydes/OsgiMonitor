package com.athaydes.osgimonitor.impl.manage

import com.athaydes.osgimonitor.api.manage.RemoteArtifactLocator
import com.athaydes.osgimonitor.api.manage.VersionedArtifact
import spock.lang.Specification

import static com.athaydes.osgimonitor.api.manage.SearchOption.EXACT

/**
 *
 * User: Renato
 */
class RemoteArtifactLocatorImplTest extends Specification {

	def "Maven Repository XML responses can be parsed correctly"( ) {
		given:
		"An example Maven XML response"
		def xml = '''<?xml version="1.0" encoding="UTF-8"?>
		<response>
			<lst name="responseHeader">
				<int name="status">0</int>
				<int name="QTime">1</int>
				<lst name="params">
					<str name="spellcheck">true</str>
					<str name="fl">id,g,a,latestVersion,p,ec,repositoryId,text,timestamp,versionCount</str>
					<str name="sort">score desc,timestamp desc,g asc,a asc</str>
					<str name="indent">off</str>
					<str name="q">g:junit AND a:junit</str>
					<str name="spellcheck.count">5</str>
					<str name="wt">xml</str>
					<str name="rows">5</str>
					<str name="version">2.2</str>
				</lst>
			</lst>
			<result name="response" numFound="1" start="0">
				<doc>
					<str name="a">junit</str>
					<arr name="ec">
						<str>-sources.jar</str>
						<str>-javadoc.jar</str>
						<str>.jar</str>
						<str>.pom</str>
					</arr>
					<str name="g">org.junit</str>
					<str name="id">org.junit:junit</str>
					<str name="latestVersion">4.11</str>
					<str name="p">jar</str>
					<str name="repositoryId">central</str>
					<arr name="text">
						<str>junit</str>
						<str>junit</str>
						<str>-sources.jar</str>
						<str>-javadoc.jar</str>
						<str>.jar</str>
						<str>.pom</str>
					</arr>
					<long name="timestamp">1352920907000</long>
					<int name="versionCount">20</int>
				</doc>
			</result>
			<lst name="spellcheck">
				<lst name="suggestions"/>
			</lst>
		</response>
		'''

		when:
		List<VersionedArtifact> artifacts = new RemoteArtifactLocatorImpl().parseMavenXmlResponse( xml );

		then:
		artifacts != null
		artifacts.size() == 1
		artifacts[ 0 ].groupId == 'org.junit'
		artifacts[ 0 ].artifactId == 'junit'
		artifacts[ 0 ].version == '4.11'
	}

	def "A list of dependencies can be fetched by entering exact groupId and artifactId"( ) {
		given:
		RemoteArtifactLocator locator = new RemoteArtifactLocatorImpl()

		when:
		def artifacts = locator.findArtifacts( groupId, artifactId, EXACT )

		then:
		artifacts != null
		artifacts.each {
			assert it.groupId == groupId
			assert it.artifactId == artifactId
		}

		where:
		groupId               | artifactId
		"junit"               | "junit"
		"org.codehaus.groovy" | "groovy-all"

	}

}
