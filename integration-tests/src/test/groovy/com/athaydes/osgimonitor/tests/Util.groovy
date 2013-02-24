package com.athaydes.osgimonitor.tests

import org.osgi.framework.Bundle
import org.osgi.framework.BundleContext

import static org.ops4j.pax.exam.CoreOptions.*

/**
 *
 * User: Renato
 */
class Util {

	static systemBundles( ) {
		composite(
				mavenBundle( "org.apache.aries.blueprint", "org.apache.aries.blueprint", "1.0.0" ),
				mavenBundle( "org.apache.aries", "org.apache.aries.util", "1.0.0" ),
				mavenBundle( "org.apache.aries.proxy", "org.apache.aries.proxy", "1.0.0" ),
				mavenBundle( "org.codehaus.groovy", "groovy-all", "2.0.6" ),
				junitBundles(),
				cleanCaches( true )
		)
	}

	static assertAllBundlesActive( BundleContext context ) {
		assert context != null
		context.bundles.each { Bundle bundle ->
			assert bundle.state == Bundle.ACTIVE: "Bundle ${bundle.symbolicName} " +
					"in state ${bundle.state}"
		}
	}

	static javaFxPackages( ) {
		systemPackages( [
				'javafx.application',
				'com.sun.browser.plugin',
				'com.sun.deploy.uitoolkit.impl.fx',
				'com.sun.deploy.uitoolkit.impl.fx.ui',
				'com.sun.deploy.uitoolkit.impl.fx.ui.resources',
				'com.sun.deploy.uitoolkit.impl.fx.ui.resources.image',
				'com.sun.glass.events',
				'com.sun.glass.ui',
				'com.sun.glass.ui.delegate',
				'com.sun.glass.ui.gtk',
				'com.sun.glass.ui.mac',
				'com.sun.glass.ui.win',
				'com.sun.glass.ui.x11',
				'com.sun.glass.utils',
				'com.sun.javafx',
				'com.sun.javafx.animation',
				'com.sun.javafx.animation.transition',
				'com.sun.javafx.applet',
				'com.sun.javafx.application',
				'com.sun.javafx.beans',
				'com.sun.javafx.beans.annotations',
				'com.sun.javafx.beans.event',
				'com.sun.javafx.binding',
				'com.sun.javafx.charts',
				'com.sun.javafx.collections',
				'com.sun.javafx.collections.annotations',
				'com.sun.javafx.collections.transformation',
				'com.sun.javafx.css',
				'com.sun.javafx.css.converters',
				'com.sun.javafx.css.parser',
				'com.sun.javafx.cursor',
				'com.sun.javafx.effect',
				'com.sun.javafx.embed',
				'com.sun.javafx.event',
				'com.sun.javafx.font',
				'com.sun.javafx.fxml',
				'com.sun.javafx.fxml.builder',
				'com.sun.javafx.fxml.expression',
				'com.sun.javafx.geom',
				'com.sun.javafx.geom.transform',
				'com.sun.javafx.iio',
				'com.sun.javafx.iio.bmp',
				'com.sun.javafx.iio.common',
				'com.sun.javafx.iio.gif',
				'com.sun.javafx.iio.jpeg',
				'com.sun.javafx.iio.png',
				'com.sun.javafx.image',
				'com.sun.javafx.image.impl',
				'com.sun.javafx.jmx',
				'com.sun.javafx.logging',
				'com.sun.javafx.menu',
				'com.sun.javafx.perf',
				'com.sun.javafx.property',
				'com.sun.javafx.property.adapter',
				'com.sun.javafx.robot',
				'com.sun.javafx.robot.impl',
				'com.sun.javafx.runtime',
				'com.sun.javafx.runtime.async',
				'com.sun.javafx.runtime.eula',
				'com.sun.javafx.scene',
				'com.sun.javafx.scene.control',
				'com.sun.javafx.scene.control.behavior',
				'com.sun.javafx.scene.control.skin',
				'com.sun.javafx.scene.control.skin.caspian',
				'com.sun.javafx.scene.control.skin.resources',
				'com.sun.javafx.scene.input',
				'com.sun.javafx.scene.layout.region',
				'com.sun.javafx.scene.paint',
				'com.sun.javafx.scene.shape',
				'com.sun.javafx.scene.text',
				'com.sun.javafx.scene.transform',
				'com.sun.javafx.scene.traversal',
				'com.sun.javafx.scene.web',
				'com.sun.javafx.scene.web.behavior',
				'com.sun.javafx.scene.web.skin',
				'com.sun.javafx.sg',
				'com.sun.javafx.sg.prism',
				'com.sun.javafx.stage',
				'com.sun.javafx.tk',
				'com.sun.javafx.tk.desktop',
				'com.sun.javafx.tk.quantum',
				'com.sun.javafx.util',
				'com.sun.media.jfxmedia',
				'com.sun.media.jfxmedia.control',
				'com.sun.media.jfxmedia.effects',
				'com.sun.media.jfxmedia.events',
				'com.sun.media.jfxmedia.locator',
				'com.sun.media.jfxmedia.logging',
				'com.sun.media.jfxmedia.track',
				'com.sun.media.jfxmediaimpl',
				'com.sun.media.jfxmediaimpl.platform',
				'com.sun.media.jfxmediaimpl.platform.gstreamer',
				'com.sun.media.jfxmediaimpl.platform.java',
				'com.sun.media.jfxmediaimpl.platform.osx',
				'com.sun.openpisces',
				'com.sun.prism',
				'com.sun.prism.camera',
				'com.sun.prism.d3d',
				'com.sun.prism.d3d.hlsl',
				'com.sun.prism.image',
				'com.sun.prism.impl',
				'com.sun.prism.impl.packrect',
				'com.sun.prism.impl.paint',
				'com.sun.prism.impl.ps',
				'com.sun.prism.impl.shape',
				'com.sun.prism.j2d',
				'com.sun.prism.j2d.paint',
				'com.sun.prism.paint',
				'com.sun.prism.ps',
				'com.sun.prism.render',
				'com.sun.prism.shader',
				'com.sun.prism.shape',
				'com.sun.prism.tkal',
				'com.sun.prism.util.tess',
				'com.sun.prism.util.tess.impl.tess',
				'com.sun.scenario',
				'com.sun.scenario.animation',
				'com.sun.scenario.animation.shared',
				'com.sun.scenario.effect',
				'com.sun.scenario.effect.impl',
				'com.sun.scenario.effect.impl.hw',
				'com.sun.scenario.effect.impl.hw.d3d',
				'com.sun.scenario.effect.impl.hw.d3d.hlsl',
				'com.sun.scenario.effect.impl.prism',
				'com.sun.scenario.effect.impl.prism.ps',
				'com.sun.scenario.effect.impl.prism.sw',
				'com.sun.scenario.effect.impl.state',
				'com.sun.scenario.effect.impl.sw',
				'com.sun.scenario.effect.impl.sw.java',
				'com.sun.scenario.effect.impl.sw.sse',
				'com.sun.scenario.effect.light',
				'com.sun.t2k',
				'com.sun.webpane.perf',
				'com.sun.webpane.platform',
				'com.sun.webpane.platform.event',
				'com.sun.webpane.platform.graphics',
				'com.sun.webpane.sg',
				'com.sun.webpane.sg.prism',
				'com.sun.webpane.sg.prism.resources',
				'com.sun.webpane.sg.prism.theme',
				'com.sun.webpane.sg.theme',
				'com.sun.webpane.webkit',
				'com.sun.webpane.webkit.dom',
				'com.sun.webpane.webkit.network',
				'com.sun.webpane.webkit.network.about',
				'com.sun.webpane.webkit.network.data',
				'com.sun.webpane.webkit.unicode',
				'javafx.animation',
				'javafx.beans',
				'javafx.beans.binding',
				'javafx.beans.property',
				'javafx.beans.property.adapter',
				'javafx.beans.value',
				'javafx.collections',
				'javafx.concurrent',
				'javafx.embed.swing',
				'javafx.embed.swt',
				'javafx.event',
				'javafx.fxml',
				'javafx.geometry',
				'javafx.scene',
				'javafx.scene.canvas',
				'javafx.scene.chart',
				'javafx.scene.control',
				'javafx.scene.control.cell',
				'javafx.scene.effect',
				'javafx.scene.image',
				'javafx.scene.input',
				'javafx.scene.layout',
				'javafx.scene.media',
				'javafx.scene.paint',
				'javafx.scene.shape',
				'javafx.scene.text',
				'javafx.scene.transform',
				'javafx.scene.web',
				'javafx.stage',
				'javafx.util',
				'javafx.util.converter',
				'netscape.javascript; version=0.0.0'
		] as String[] )
	}

}
