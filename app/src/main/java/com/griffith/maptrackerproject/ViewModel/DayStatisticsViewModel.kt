
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.griffith.maptrackerproject.DB.Locations
import com.griffith.maptrackerproject.DB.LocationsDAO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class DayStatisticsViewModel(private val locationsDAO: LocationsDAO) : ViewModel() {

    private val _hourlyDistances = MutableStateFlow<Map<Int, Float>?>(null)
    val hourlyDistances: StateFlow<Map<Int, Float>?> = _hourlyDistances

    fun loadHourlyDistances(date: Date) {
        viewModelScope.launch {
            locationsDAO.getAllLocations().collect { locations ->
                _hourlyDistances.value = calculateHourlyDistance(locations)
            }
        }
    }
    fun calculateDistance(locations: List<Locations>): Float {
        var totalDistance: Float = 0.0F

        for(i in 0 until locations.size -2){
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

    fun calculateHourlyDistance(locations: List<Locations>):  Map<Int,Float>{
        val hourlyDistances = mutableMapOf<Int, Float>()

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
