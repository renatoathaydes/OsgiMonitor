package com.athaydes.osgimonitor.fx.tab

import com.athaydes.osgimonitor.api.BundleData
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.TableColumn
import javafx.scene.control.TableColumn.CellDataFeatures
import javafx.scene.control.TableView
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.VBox
import javafx.util.Callback

import static javafx.scene.control.TableColumn.SortType.ASCENDING

/**
 *
 * User: Renato
 */
class BundlesTab extends AsTab {

	def table = new TableView( id: 'bundles-table' )
	ObservableList<ObservableBundleData> bundlesData = FXCollections.observableArrayList()

	String tabName( ) { 'Bundles' }


	BundlesTab( ) {
		tab.id = 'bundles-tab'
		tab.closable = false
		def nameCol = new TableColumn( 'Bundle Symbolic Name' )
		nameCol.minWidth = 200
		nameCol.sortType = ASCENDING
		nameCol.cellValueFactory = new PropertyValueFactory( 'name' )

		def stateCol = new TableColumn( 'State' )
		stateCol.minWidth = 100
		stateCol.cellValueFactory = { CellDataFeatures<ObservableBundleData, String> p ->
			p.value.stateProp
		} as Callback

		table.columns.addAll nameCol, stateCol
		table.items = bundlesData

		def root = new VBox()
		root.children.add table

		tab.content = root

	}

	void update( BundleData bundleData ) {
		def existing = bundlesData.find { it.name == bundleData.symbolicName }
		if ( existing ) {
			existing.stateProp.value = bundleData.state
		} else {
			bundlesData << new ObservableBundleData(
					name: bundleData.symbolicName,
					stateProp: new SimpleStringProperty( bundleData.state ) )
			bundlesData.sort()
		}
	}

	class ObservableBundleData implements Comparable<ObservableBundleData> {
		String name
		StringProperty stateProp

		@Override
		int compareTo( ObservableBundleData other ) {
			return this.name.compareTo( other.name )
		}
	}


}
