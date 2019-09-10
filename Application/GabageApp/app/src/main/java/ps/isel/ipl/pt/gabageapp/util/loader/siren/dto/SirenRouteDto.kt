package ps.isel.ipl.pt.gabageapp.util.loader.siren.dto

import ps.isel.ipl.pt.gabageapp.service_web_api.dto.RouteDto
import ps.isel.ipl.pt.gabageapp.util.loader.siren.Action
import ps.isel.ipl.pt.gabageapp.util.loader.siren.Link
import ps.isel.ipl.pt.gabageapp.util.loader.siren.Siren
import ps.isel.ipl.pt.gabageapp.util.loader.siren.SubEntity

/**
 * Created by goncalo on 28/05/2018.
 */
class SirenRouteDto(properties: RouteDto, entities: Array<SubEntity>, actions: Array<Action>, links: Array<Link>) : Siren<RouteDto>(properties, entities, actions, links) {
}