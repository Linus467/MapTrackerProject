
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.griffith.maptrackerproject.DB.Locations
import com.griffith.maptrackerproject.DB.LocationsDAO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import java.util.Calendar
import java.util.Date

//Viewmodel used to get the Locations for the DayStatistics page
class DayStatisticsViewModel(private val locationsDAO: LocationsDAO) : ViewModel() {

    private val _hourlyDistances = MutableStateFlow<Map<Int, Float>?>(null)
    val hourlyDistances: StateFlow<Map<Int, Float>?> = _hourlyDistances

    private val _hourlyHeight = MutableStateFlow<Map<Int, Float>?>(null)
    val hourlyHeight: StateFlow<Map<Int, Float>?> = _hourlyHeight

    private val _liveLocations = MutableStateFlow<List<Locations>>(emptyList())
    val liveLocations: StateFlow<List<Locations>> = _liveLocations

    private val _locationsAverage = MutableStateFlow(GeoPoint(37.00,-121.96, 0.0))
    val locationsAverage: StateFlow<GeoPoint> = _locationsAverage



    fun loadHourlyDistances(date: Date) {
        viewModelScope.launch {
            // Assuming 'date' is a Java 'long' representing the date in milliseconds
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = date.time

            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val startOfDay = calendar.timeInMillis

            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)

            val endOfDay = calendar.timeInMillis

            locationsDAO.getLocationsForDay(startOfDay,endOfDay).collect { locations ->
                _hourlyDistances.value = calculateHourlyDistance(locations)
                _hourlyHeight.value = calculateHourlyHeightDistance(locations)
                _liveLocations.value = locations
                calculateAveragePosition(locations)
            }
        }
    }

    private fun calculateAveragePosition(locations: List<Locations>){
        var averageLocationLat: Double = 0.0
        var averageLocationLong: Double = 0.0
        var averageLocationAlt: Double = 0.0
        for(loc in locations){
            averageLocationLat += loc.latitude
            averageLocationLong += loc.longitude
            averageLocationAlt += loc.altitude
        }
        if(averageLocationLat != 0.0 && averageLocationLong != 0.0){
            averageLocationLat /= locations.size
            averageLocationLong /= locations.size
        }
        if(averageLocationAlt != 0.0){
            averageLocationAlt /= locations.size
        }
        _locationsAverage.value = GeoPoint(averageLocationLong, averageLocationLat, averageLocationAlt)
    }

    fun calculateDistance(locations: List<Locations>): Float {
        var totalDistance: Float = 0.0F

        for(i in 0 .. locations.size -2){
            val start = locations[i]
            val end = locations[i+1]

            val result = FloatArray(1)
            Location.distanceBetween(
                start.latitude, start.longitude,
                end.latitude, end.longitude,
                result
            )
            totalDistance += result[0]
        }
        return totalDistance
    }

    //Calculates the total height traveled by the locations given
    private fun calculateHourlyHeightDistance(locations: List<Locations>) : Map<Int, Float>{
        //totalHeight travels by a user for the locations list given
        var hourlyHeight = mutableMapOf<Int, Float>()

        for( hour in 0..23){
            hourlyHeight[hour] = 0.0f
        }

        //Get all the locations by Hour
        val locationsByHour = locations.groupBy {
            val calendar = Calendar.getInstance()
            calendar.time = it.date
            calendar.get(Calendar.HOUR_OF_DAY)
        }

        //set the location into
        for ((hour, locationsInHour) in locationsByHour) {
            var totalDistance = 0f
            for (i in 0 until locationsInHour.size - 1) {
                val startLocation = locationsInHour[i]
                val endLocation = locationsInHour[i + 1]
                val heightDifference =  endLocation.altitude - startLocation.altitude
                if(heightDifference > 0){
                    totalDistance += heightDifference.toFloat()
                }
            }
            hourlyHeight[hour] = totalDistance
        }
        return hourlyHeight
    }
    //Calculates the Hourly path taken
    private fun calculateHourlyDistance(locations: List<Locations>):  Map<Int,Float>{
        val hourlyDistances = mutableMapOf<Int, Float>()

        for (hour in 0..23){
            hourlyDistances[hour] = 0.0f
        }

        //Get all the locations by Hour
        val locationsByHour = locations.groupBy {
            val calendar = Calendar.getInstance()
            calendar.time = it.date
            calendar.get(Calendar.HOUR_OF_DAY)
        }

        //set the location into
        for ((hour, locationsInHour) in locationsByHour) {
            var totalDistance = 0f
            for (i in 0 until locationsInHour.size - 1) {
                val startLocation = locationsInHour[i]
                val endLocation = locationsInHour[i + 1]

                val results = FloatArray(1)
                Location.distanceBetween(
                    startLocation.latitude, startLocation.longitude,
                    endLocation.latitude, endLocation.longitude,
                    results
                )
                totalDistance += results[0]
            }
            hourlyDistances[hour] = totalDistance
        }
        return hourlyDistances
    }
}
