package com.athaydes.osgimonitor.fx.tab

import com.athaydes.osgimonitor.api.ServiceData
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.ObservableMap
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
class ServicesTab extends AsTab {

	def table = new TableView( id: 'services-table' )
	ObservableList<ObservableServiceData> servicesData = FXCollections.observableArrayList()

	String tabName( ) { 'Services' }

	ServicesTab( ) {
		tab.id = 'services-tab'
		tab.closable = false
		def nameCol = new TableColumn( 'Publishing Bundle' )
		nameCol.minWidth = 200
		nameCol.sortType = ASCENDING
		nameCol.cellValueFactory = new PropertyValueFactory( 'bundleName' )

		def stateCol = new TableColumn( 'State' )
		stateCol.minWidth = 100
		stateCol.cellValueFactory = { CellDataFeatures<ObservableServiceData, String> p ->
			p.value.stateProp
		} as Callback

		table.columns.addAll nameCol, stateCol
		table.items = servicesData

		def root = new VBox()
		root.children.add table

		tab.content = root

	}

	void update( ServiceData serviceData ) {
		//def existing = servicesData.find { it.name == serviceData.get }
		//if ( existing ) {
		//	existing.stateProp.value = bundleData.state
		//} else {
		servicesData << new ObservableServiceData(
				bundleName: serviceData.bundleName,
				bundlesUsingProp: FXCollections.observableArrayList( serviceData.bundlesUsing ),
				stateProp: new SimpleStringProperty( serviceData.state ),
				propertiesProp: FXCollections.observableMap( serviceData.properties )
		)
		servicesData.sort { b1, b2 -> b1.bundleName.compareTo b2.bundleName }
		//}
	}

	class ObservableServiceData {
		String bundleName
		ObservableList<String> bundlesUsingProp
		StringProperty stateProp
		ObservableMap propertiesProp
	}

}
