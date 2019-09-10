package ps.isel.ipl.pt.gabageapp.util.loader.siren.dto

import ps.isel.ipl.pt.gabageapp.service_web_api.dto.ContainerDto
import ps.isel.ipl.pt.gabageapp.util.loader.siren.Action
import ps.isel.ipl.pt.gabageapp.util.loader.siren.Link
import ps.isel.ipl.pt.gabageapp.util.loader.siren.Siren
import ps.isel.ipl.pt.gabageapp.util.loader.siren.SubEntity

/**
 * Created by goncalo on 01/06/2018.
 */
class SirenContainerDto(properties: ContainerDto, entities: Array<SubEntity>, actions: Array<Action>, links: Array<Link>) : Siren<ContainerDto>(properties, entities, actions, links) {
}