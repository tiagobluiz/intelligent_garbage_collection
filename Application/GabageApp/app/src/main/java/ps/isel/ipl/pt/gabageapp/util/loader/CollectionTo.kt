package ps.isel.ipl.pt.gabageapp.util.loader

import android.view.View
import com.google.android.gms.maps.model.LatLng
import ps.isel.ipl.pt.gabageapp.model.*
import ps.isel.ipl.pt.gabageapp.util.loader.collection.CollectionJson
import java.text.SimpleDateFormat


/**
 * Created by goncalo on 25/05/2018.
 */

private val COLLECT_REL = "/rels/collect-collect-zone-containers"
private val WAHES_REL = "/rels/wash-collect-zone-containers"

var formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm")

fun CollectionJson.toRoutes():ArrayList<RouteItem>{
    var ret: ArrayList<RouteItem> = arrayListOf()
    this.collection.items.forEach {
        var routeId = it.data.find{it.name.equals("routeId")}?.value?.toInt()
        if(routeId==null)
            routeId = 0
        var active = it.data.find{it.name.equals("active")}?.value?.toBoolean()
        if(active==null)
            active=false
        var startLat = it.data.find{it.name.equals("startPointLatitude")}?.value?.toDouble()
        if(startLat==null)
            startLat=0.0
        var startLong = it.data.find{it.name.equals("startPointLongitude")}?.value?.toDouble()
        if(startLong==null)
            startLong=0.0
        var startName = it.data.find{it.name.equals("startPointStationName")}?.value
        if(startName==null)
            startName=""

        val startPointLALO = LatLng(startLat,startLong)
        val startPoint = Location(startPointLALO, startName)

        var endLat = it.data.find{it.name.equals("finishPointLatitude")}?.value?.toDouble()
        if(endLat==null)
            endLat=0.0
        var endLong =  it.data.find{it.name.equals("finishPointLongitude")}?.value?.toDouble()
        if(endLong==null)
            endLong=0.0
        var endName = it.data.find{it.name.equals("finishPointStationName")}?.value
        if(endName==null)
            endName=""

        val endPointLALO = LatLng(endLat,endLong)
        val endPoint = Location(endPointLALO, endName)

        ret.add(RouteItem(routeId,startPoint, endPoint, active, it.href))
    }
    return ret
}

fun CollectionJson.toCollectZones():ArrayList<CollectZoneItem>{
    var ret: ArrayList<CollectZoneItem> = arrayListOf()
    this.collection.items.forEach {
        var collectZoneId = it.data.find{it.name.equals("collectZoneId")}?.value?.toInt()
        if(collectZoneId==null)
            collectZoneId = 0
        var routeId = it.data.find{it.name.equals("routeId")}?.value?.toInt()
        if(routeId==null)
            routeId = 0
        var active = it.data.find{it.name.equals("active")}?.value?.toBoolean()
        if(active==null)
            active=false
        var Lat = it.data.find{it.name.equals("latitude")}?.value?.toDouble()
        if(Lat==null)
            Lat=0.0
        var Long = it.data.find{it.name.equals("longitude")}?.value?.toDouble()
        if(Long==null)
            Long=0.0
        val Point = LatLng(Lat,Long)

        var collectLink = it.links.find{it.rel.equals(COLLECT_REL)}
        var collectURI: String? = null
        if(collectLink!=null)
            collectURI = collectLink.href

        var washLink = it.links.find{it.rel.equals(WAHES_REL)}
        var washURI: String? = null
        if(washLink!=null)
            washURI = washLink.href

        ret.add(CollectZoneItem(collectZoneId,active, Point, 0.0F, it.href, collectURI, washURI))
    }
    return ret
}

fun CollectionJson.toContainers(): ArrayList<ContainerItem>{
    var ret: ArrayList<ContainerItem> = arrayListOf()
    this.collection.items.forEach{
        var containerId = it.data.find{it.name.equals("containerId")}?.value?.toInt()
        if(containerId==null)
            containerId = 0

        var collectZoneId = it.data.find{it.name.equals("collectZoneId")}?.value?.toInt()
        if(collectZoneId==null)
            collectZoneId = 0

        var iotId = it.data.find{it.name.equals("iotId")}?.value
        if(iotId==null)
            iotId = ""

        var active = it.data.find{it.name.equals("active")}?.value?.toBoolean()
        if(active==null)
            active=false

        var Lat = it.data.find{it.name.equals("latitude")}?.value?.toDouble()
        if(Lat==null)
            Lat=0.0

        var Long = it.data.find{it.name.equals("longitude")}?.value?.toDouble()
        if(Long==null)
            Long=0.0

        val Point = LatLng(Lat,Long)

        var height = it.data.find{it.name.equals("height")}?.value?.toInt()
        if(height==null)
            height=0

        var battery = it.data.find{it.name.equals("battery")}?.value?.toInt()
        if(battery==null)
            battery=0

        var occupation = it.data.find{it.name.equals("occupation")}?.value?.toInt()
        if(occupation==null)
            occupation=0

        var temperature = it.data.find{it.name.equals("temperature")}?.value?.toInt()
        if(temperature==null)
            temperature=0

        var configurationId = it.data.find{it.name.equals("configurationId")}?.value?.toInt()
        if(configurationId==null)
            configurationId = 0

        var containerType = it.data.find{it.name.equals("containerType")}?.value
        if(containerType==null)
            containerType=""
        ret.add(ContainerItem(containerId, temperature, occupation, battery, Point, collectZoneId, active, it.href))
    }
    return ret
}

fun CollectionJson.toStations(): ArrayList<Station>{
    val ret = arrayListOf<Station>()
    this.collection.items.forEach{
        var stationId = it.data.find{it.name.equals("stationId")}?.value?.toInt()
        if(stationId==null)
            stationId = 0

        var stationName = it.data.find{it.name.equals("stationName")}?.value
        if(stationName==null)
            stationName = ""

        var latitude = it.data.find{it.name.equals("latitude")}?.value?.toDouble()
        if(latitude==null)
            latitude=0.0

        var longitude = it.data.find{it.name.equals("longitude")}?.value?.toDouble()
        if(longitude==null)
            longitude=0.0

        val Point = LatLng(latitude,longitude)

        var stationType = it.data.find{it.name.equals("stationType")}?.value
        if(stationType==null)
            stationType = ""
        ret.add(Station(stationId,Point, stationType, stationName, it.href))
    }
    return ret
}

fun CollectionJson.toWashes(): ArrayList<Wash>{
    val ret = arrayListOf<Wash>()

    this.collection.items.forEach {
        var containerId = it.data.find{it.name.equals("containerId")}?.value?.toInt()
        if(containerId==null)
            containerId = 0

        var washDate = formatter.parse(it.data.find{it.name.equals("washDate")}?.value).toString()
        if(washDate==null)
            washDate = ""

        ret.add(Wash(containerId, washDate))
    }
    return ret
}

fun CollectionJson.toRouteCollects(): ArrayList<RouteCollectItem>{
    val ret = arrayListOf<RouteCollectItem>()
    this.collection.items.forEach {
        var routeId = it.data.find{it.name.equals("routeId")}?.value?.toInt()
        if(routeId==null)
            routeId = 0

        var startDate = it.data.find{it.name.equals("startDate")}?.value
        if(startDate==null)
            startDate = ""
        else
            startDate = formatter.parse(startDate).toString()

        var finishDate = it.data.find{it.name.equals("finishDate")}?.value
        if(finishDate==null)
            finishDate = ""
        else
            finishDate = formatter.parse(finishDate).toString()

        ret.add(RouteCollectItem(finishDate, startDate, routeId))
    }
    return ret
}

fun CollectionJson.toContainerCollects(): ArrayList<ContainerCollect>{
    val ret = arrayListOf<ContainerCollect>()
    this.collection.items.forEach {
        var containerId = it.data.find{it.name.equals("containerId")}?.value?.toInt()
        if(containerId==null)
            containerId = 0

        var collectDate = formatter.parse(it.data.find{it.name.equals("collectDate")}?.value).toString()
        if(collectDate==null)
            collectDate = ""

        ret.add(ContainerCollect(containerId, collectDate))
    }
    return ret
}

fun CollectionJson.toTrucks() : ArrayList<Truck>{
    val ret = arrayListOf<Truck>()
    this.collection.items.forEach {
        var truckPlate = it.data.find{it.name.equals("truckPlate")}?.value
        if(truckPlate==null)
            truckPlate = ""

        var active = it.data.find{it.name.equals("active")}?.value?.toBoolean()
        if(active==null)
            active = false

        ret.add(Truck(truckPlate, active, it.href))
    }
    return ret
}

fun CollectionJson.toLandFills(): ArrayList<Landfill>{
    val ret = arrayListOf<Landfill>()
    this.collection.items.forEach {
        var routeId = it.data.find{it.name.equals("routeId")}?.value?.toInt()
        if(routeId==null)
            routeId = 0

        var dropZoneId = it.data.find{it.name.equals("dropZoneId")}?.value?.toInt()
        if(dropZoneId==null)
            dropZoneId = 0

        var latitude = it.data.find{it.name.equals("latitude")}?.value?.toDouble()
        if(latitude==null)
            latitude=0.0

        var longitude = it.data.find{it.name.equals("longitude")}?.value?.toDouble()
        if(longitude==null)
            longitude=0.0

        val Point = LatLng(latitude,longitude)

        ret.add(Landfill(dropZoneId, Point))
    }
    return ret
}

fun CollectionJson.toCommunications(): ArrayList<CommunicationDetails>{
    val ret = arrayListOf<CommunicationDetails>()
    this.collection.items.forEach {
        var configurationId = it.data.find{it.name.equals("configurationId")}?.value?.toInt()
        if(configurationId==null)
            configurationId = 0

        var communicationId = it.data.find{it.name.equals("communicationId")}?.value?.toInt()
        if(communicationId==null)
            communicationId = 0

        var communicationDesignation = it.data.find{it.name.equals("communicationDesignation")}?.value
        if(communicationDesignation==null)
            communicationDesignation = ""

        var value = it.data.find{it.name.equals("value")}?.value
        if(value==null)
            value = ""

        ret.add(CommunicationDetails(configurationId, communicationId, communicationDesignation, value, it.href, object :View.OnClickListener{
            override fun onClick(v: View?) {

            }
        }))
    }
    return ret
}

fun CollectionJson.toConfigurations() : ArrayList<ConfigurationItem>{
    val ret = arrayListOf<ConfigurationItem>()
    this.collection.items.forEach {
        var configurationId = it.data.find{it.name.equals("configurationId")}?.value?.toInt()
        if(configurationId==null)
            configurationId = 0

        var configurationName = it.data.find{it.name.equals("configurationName")}?.value
        if(configurationName==null)
            configurationName = ""

        ret.add(ConfigurationItem(configurationId, configurationName))
    }
    return ret
}