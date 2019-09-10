package ps.isel.ipl.pt.gabageapp.util.loader

import android.view.View
import com.google.android.gms.maps.model.LatLng
import ps.isel.ipl.pt.gabageapp.model.*
import ps.isel.ipl.pt.gabageapp.service_web_api.dto.*
import ps.isel.ipl.pt.gabageapp.util.loader.siren.Siren
/**
 * Created by goncalo on 28/05/2018.
 */
private val SELF_REL = "self"
private val UP_REL = "up"

private val ROUTE_CONTAINERS_REL = "/rels/route-containers"
private val ROUTE_COLLECT_ZONES_REL = "/rels/route-collect-zones"
private val ROUTE_COLLECTS_REL = "/rels/route-collections"
private val ROUTE_PLAN_REL = "/rels/route-collection-plan"
private val ROUTE_DROP_ZONES_REL = "/rels/route-drop-zone"

private val COLLECT_ZONE_CONTAINERS_REL = "/rels/collect-zone-containers"

private val CONTAINER_WASHES_REL = "/rels/wash"
private val CONTAINER_COLLECTS_REL = "/rels/collect"
private val CONTAINER_CONFIGURATION_REL = "/rels/configuration"

private val ROUTE_COLLECT_PLAN_REL = "/rels/route-collection-plan"

private val CONFIGUARTION_COMMUNICTIONS_REL = "/rels/configuration-communication-list"


fun Siren<RouteDto>.toRoutes():Route{
    val routeDto = this.properties

    val routeId = routeDto.routeId
    val stratLatLng = LatLng(routeDto.startPointLatitude, routeDto.startPointLongitude)
    val startPoint = Location(stratLatLng, routeDto.startPointStationName)
    val endLatLng = LatLng(routeDto.finishPointLatitude, routeDto.finishPointLongitude)
    val endPoint = Location(endLatLng, routeDto.finishPointStationName)
    val active = routeDto.active.toBoolean()
    val numContainers = routeDto.numContainers
    val numCollectZones = routeDto.numCollectZones
    val numCollects = routeDto.numCollects

    var containers = this.entities.find { !it.rel.find { it.equals(ROUTE_CONTAINERS_REL) }.isNullOrEmpty() }?.href
    if(containers==null)
        containers=""
    var collectZones = this.entities.find { !it.rel.find { it.equals(ROUTE_COLLECT_ZONES_REL) }.isNullOrEmpty() }?.href
    if(collectZones==null)
        collectZones=""
    var collects = this.entities.find { !it.rel.find { it.equals(ROUTE_COLLECTS_REL) }.isNullOrEmpty() }?.href
    if(collects==null)
        collects=""
    var plan = this.entities.find { !it.rel.find { it.equals(ROUTE_PLAN_REL) }.isNullOrEmpty() }?.href
    if(plan==null)
        plan=""
    var dropZones = this.entities.find { !it.rel.find { it.equals(ROUTE_DROP_ZONES_REL) }.isNullOrEmpty() }?.href
    if(dropZones==null)
        dropZones=""
    return Route(routeId, startPoint, endPoint, active, numContainers, numCollectZones, numCollects, containers, collectZones, collects, plan, dropZones )
}

fun Siren<CollectZoneDto>.toCollectZone(): CollectZone {
    val collectZoneDto = this.properties

    val routeId = collectZoneDto.routeId
    val collectZoneId = collectZoneDto.collectZoneId
    val numContainers = collectZoneDto.numContainers
    val active = collectZoneDto.active.toBoolean()
    val lat = collectZoneDto.latitude
    val long = collectZoneDto.longitude
    val location = LatLng(lat, long)
    val generalOccupation = collectZoneDto.generalOccupation
    val glassOccupation = collectZoneDto.glassOccupation
    val paperOccupation = collectZoneDto.paperOccupation
    val plasticOccupation = collectZoneDto.plasticOccupation

    var containers = this.entities.find { !it.rel.find { it.equals(COLLECT_ZONE_CONTAINERS_REL) }.isNullOrEmpty() }?.href
    if(containers==null)
        containers=""


    return CollectZone(collectZoneId, routeId, numContainers, active, location, generalOccupation, plasticOccupation, paperOccupation, glassOccupation, containers)
}

fun Siren<ContainerDto>.toContainer(): ContainerDetails {

    var containerDto = this.properties

    val collectZoneId = containerDto.collectZoneId
    val batery = containerDto.battery
    val configurationId = containerDto.configurationId
    val containerId = containerDto.containerId
    val active = containerDto.active.toBoolean()
    val containerType = containerDto.containerType
    val heigth = containerDto.height
    val iot = containerDto.iotId
    val lastReadDate = containerDto.lastReadDate
    val latitude = containerDto.latitude
    val longitude = containerDto.longitude
    val numCollects = containerDto.numCollects
    val numWashes = containerDto.numWashes
    val location = LatLng(latitude, longitude)
    val occupation = containerDto.occupation
    val temperature= containerDto.temperature

    var washesURL = this.entities.find { !it.rel.find { it.equals(CONTAINER_WASHES_REL) }.isNullOrEmpty() }?.href
    if(washesURL==null)
        washesURL=""

    var collectsURL = this.entities.find { !it.rel.find { it.equals(CONTAINER_COLLECTS_REL) }.isNullOrEmpty() }?.href
    if(collectsURL==null)
        collectsURL=""

    var configurationsURL = this.entities.find { !it.rel.find { it.equals(CONTAINER_CONFIGURATION_REL) }.isNullOrEmpty() }?.href
    if(configurationsURL==null)
        configurationsURL=""

    return ContainerDetails(containerId, temperature, occupation,
            batery, heigth, location, collectZoneId, numWashes, numCollects,
            containerType, iot, configurationId, active,  washesURL, collectsURL, configurationsURL)
}

fun Siren<UserDto>.toUser(): User {
    var userDto = this.properties
    val username = userDto.username
    val post = userDto.job
    val email = userDto.email
    val phoneNumber = userDto.phoneNumber

    return User(username,post, email, phoneNumber)
}

fun Siren<RouteCollectDto>.toRouteCollect(): RouteCollect{
    var routeCollect = this.properties
    val routeId = routeCollect.routeId
    val truckPlate = routeCollect.truckPlate
    var upUrl = this.links.find { it.rel.contains(UP_REL) }?.href
    var planUrl = this.entities.find { !it.rel.find { it.equals(ROUTE_COLLECT_PLAN_REL) }.isNullOrEmpty() }?.href

    if(upUrl==null)
        upUrl = ""

    if(planUrl == null)
        planUrl = ""

    return RouteCollect(routeId, truckPlate, planUrl, upUrl)
}

fun Siren<ConfigurationDto>.toConfiguration(): Configuration{
    var configuration = this.properties
    val configurationId = configuration.configurationId
    val configurationName = configuration.configurationName

    var communicationsUrl = this.entities.find { !it.rel.find { it.equals(CONFIGUARTION_COMMUNICTIONS_REL) }.isNullOrEmpty() }?.href
    if(communicationsUrl == null)
        communicationsUrl = ""

    return Configuration(configurationId, configurationName, communicationsUrl)
}

fun Siren<CommunicationDto>.toCommunication():CommunicationDetails{
    val communication = this.properties
    val configurationId = communication.configurationId
    val communicationId = communication.communicationId
    val communicationDesignation = communication.communicationDesignation
    val value = "${communication.value}"
    val selfLink = this.links.find { it.rel.contains(SELF_REL) }
    var self = ""
    if(selfLink != null)
        self = selfLink.href
    return CommunicationDetails(configurationId, communicationId, communicationDesignation, value, self, object : View.OnClickListener{
        override fun onClick(v: View?) {

        }
    })
}
