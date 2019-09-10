package ps.isel.ipl.pt.gabageapp.util.direction_json.json_obj

/**
 * Created by goncalo on 19/04/2018.
 */
class LegItem(val duration: Duration,
              val distance: Duration,
              val end_location: LocationRoute,
              val start_location: LocationRoute,
              val steps: List<Step>,
              val via_waypoint: List<WayItem>) {
}