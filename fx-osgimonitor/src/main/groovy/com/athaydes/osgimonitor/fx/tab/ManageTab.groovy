package com.athaydes.osgimonitor.fx.tab

import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.layout.VBox
import javafx.stage.FileChooser

/**
 *
 * User: Renato
 */
class ManageTab extends AsTab {

	String tabName( ) { 'Manage' }

	ManageTab( ) {
		tab.id = 'manage-tab'
		tab.closable = false

		def root = new VBox( spacing: 20 )
		def bundleChooserGroup = createBundleChooserGroup( root )

		root.children.addAll( bundleChooserGroup as Node[] )

		tab.content = root
	}

	def createBundleChooserGroup( Node root ) {
		final chooseFileButton = new Button(
				id: 'select-bundle-button',
				text: 'Select a bundle to be installed into the OSGi environment' )
		final fileChooser = createJarFileChooser()

		chooseFileButton.onAction = [
				handle: { ActionEvent event ->
					File f = fileChooser.showOpenDialog( root.scene.window )
					println "You selected file ${f?.absolutePath}"
				}
		] as EventHandler

		return [ chooseFileButton ]
	}

	protected createJarFileChooser( ) {
		final fileChooser = new FileChooser()
		def extFilter = new FileChooser.ExtensionFilter( "Jar Files", "*.jar" )
		fileChooser.extensionFilters.add extFilter
		return fileChooser
	}

}
