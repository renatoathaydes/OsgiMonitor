package com.athaydes.osgimonitor.fx.tab

import com.athaydes.osgimonitor.api.ServiceData
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

		def usingCol = new TableColumn( 'Bundles using' )
		usingCol.minWidth = 200
		usingCol.cellValueFactory = {
			CellDataFeatures<ObservableServiceData, String> p ->
				p.value.bundlesUsingProp
		} as Callback

		def classesCol = new TableColumn( 'Service classes' )
		classesCol.minWidth = 200
		classesCol.cellValueFactory = {
			CellDataFeatures<ObservableServiceData, String> p ->
				p.value.serviceClassesProp
		} as Callback

		table.columns.addAll nameCol, stateCol, usingCol, classesCol
		table.items = servicesData

		def root = new VBox()
		root.children.add table

		tab.content = root

	}

	void update( ServiceData serviceData ) {
		println "Updating Service: $serviceData"
		def serviceClasses = Arrays.toString( serviceData.properties[ 'objectClass' ] )
		def id = serviceData.properties[ 'service.id' ]
		def existing = servicesData.find { it.serviceId == id }

		if ( existing ) {
			existing.stateProp.value = serviceData.state
			existing.bundlesUsingProp.value = Arrays.toString( serviceData.bundlesUsing )
			existing.stateProp.value = serviceData.state
			existing.serviceClassesProp.value = serviceClasses
		} else {
			servicesData << new ObservableServiceData(
					serviceId: id as int,
					bundleName: serviceData.bundleName,
					bundlesUsingProp: new SimpleStringProperty( Arrays.toString( serviceData.bundlesUsing ) ),
					stateProp: new SimpleStringProperty( serviceData.state ),
					serviceClassesProp: new SimpleStringProperty( serviceClasses )
			)
			servicesData.sort { b1, b2 -> b1.bundleName.compareTo b2.bundleName }
		}
	}

	class ObservableServiceData {
		final int serviceId
		final String bundleName
		final StringProperty bundlesUsingProp
		final StringProperty stateProp
		final StringProperty serviceClassesProp

		ObservableServiceData( Map args ) {
			serviceId = args.serviceId
			bundleName = args.bundleName
			bundlesUsingProp = args.bundlesUsingProp
			stateProp = args.stateProp
			serviceClassesProp = args.serviceClassesProp
		}
	}

}
