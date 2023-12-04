
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.griffith.maptrackerproject.DB.Locations
import com.griffith.maptrackerproject.DB.LocationsDAO
import com.griffith.maptrackerproject.DB.calculateAveragePosition
import com.griffith.maptrackerproject.DB.calculateHourlyDistance
import com.griffith.maptrackerproject.DB.calculateHourlyHeightDistance
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
            // Getting the long values for the current day for extracting from DB
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = date.time
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            //Start of the Day in long
            val startOfDay = calendar.timeInMillis

            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            //End of the Day in long
            val endOfDay = calendar.timeInMillis

            //DB Data extraction
            locationsDAO.getLocationsForDay(startOfDay,endOfDay).collect { locations ->
                _hourlyDistances.value = locations.calculateHourlyDistance()
                _hourlyHeight.value = locations.calculateHourlyHeightDistance()
                _liveLocations.value = locations
                _locationsAverage.value = locations.calculateAveragePosition()
            }
        }
    }
}
