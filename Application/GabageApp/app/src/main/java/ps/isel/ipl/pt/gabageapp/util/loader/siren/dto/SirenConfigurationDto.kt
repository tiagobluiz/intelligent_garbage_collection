package ps.isel.ipl.pt.gabageapp.util.loader.siren.dto

import ps.isel.ipl.pt.gabageapp.service_web_api.dto.ConfigurationDto
import ps.isel.ipl.pt.gabageapp.util.loader.siren.Action
import ps.isel.ipl.pt.gabageapp.util.loader.siren.Link
import ps.isel.ipl.pt.gabageapp.util.loader.siren.Siren
import ps.isel.ipl.pt.gabageapp.util.loader.siren.SubEntity

/**
 * Created by goncalo on 10/07/2018.
 */
class SirenConfigurationDto(properties: ConfigurationDto, entities: Array<SubEntity>, actions: Array<Action>, links: Array<Link>) : Siren<ConfigurationDto>(properties, entities, actions, links) {
}