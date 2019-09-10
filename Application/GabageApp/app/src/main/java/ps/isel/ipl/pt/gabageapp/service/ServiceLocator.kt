package ps.isel.ipl.pt.gabageapp.service

import ps.isel.ipl.pt.gabageapp.service_web_api.service.HttpRequestService
import ps.isel.ipl.pt.gabageapp.service_web_api.service.RequestService

/**
 * Created by goncalo on 10/07/2018.
 */
class ServiceLocator {
    companion object {
        fun getMakeRequest(): RequestService{
            return HttpRequestService
        }
    }
}