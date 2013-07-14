package com.athaydes.osgimonitor.impl.manage.maven

import spock.lang.Specification

/**
 *
 * User: Renato
 */
class MavenArtifactInstallerTest extends Specification {

	def "A local Maven artifact's jar can be copied into the OSGi container bundles folder"( ) {
		given:
		"A local Maven artifact"

		when:
		"I ask to install the artifact into the OSGi container"

		then:
		"The artifact's Jar is copied into the OSGi container's bundle folder"

	}

	def "A remote Maven artifact's jar can be copied into the OSGi container bundles folder"( ) {
		given:
		"A remote Maven artifact"

		when:
		"I ask to install the artifact into the OSGi container"

		then:
		"The artifact's Jar is copied into the OSGi container's bundle folder"

	}

	def "Trying to install a non-existing artifact causes an exception to be thrown"() {
		given:
		"A non-existing Maven artifact"

		when:
		"I ask to install the artifact into the OSGi container"

		then:
		"A RuntimeException is thrown with an explanatory message"

	}

	def "Trying to install an artifact which is already installed just returns false"() {
		given:
		"An installed artifact"

		when:
		"I ask to install the artifact into the OSGi container"

		then:
		"The ArtifactInstaller just returns false"

	}

	def "An installed artifact can be uninstalled from the OSGi container"( ) {
		given:
		"An installed artifact"

		when:
		"I ask to uninstall the artifact from the OSGi container"

		then:
		"The artifact is uninstalled from the OSGi container"

		and:
		"The artifact's Jar is removed from the bundles folder"

	}

	def "Trying to uninstall an artifact which has not been installed just returns false"() {
		given:
		"An artifact which is not installed"

		when:
		"I ask to uninstall the artifact from the OSGi container"

		then:
		"The ArtifactInstaller just returns false"
	}

}
