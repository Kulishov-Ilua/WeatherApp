package ru.kulishov.openweatherapp.presentation.viewmodel.weather

import android.Manifest
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import retrofit2.Retrofit
import ru.kulishov.openweatherapp.data.local.data.mapper.WeatherForecastMapper
import ru.kulishov.openweatherapp.data.remote.api.geoRequest
import ru.kulishov.openweatherapp.data.remote.model.City
import ru.kulishov.openweatherapp.data.remote.model.Coord
import ru.kulishov.openweatherapp.data.remote.model.Forecast
import ru.kulishov.openweatherapp.domain.model.SelectedCity
import ru.kulishov.openweatherapp.domain.model.UiState
import ru.kulishov.openweatherapp.domain.model.WeatherForecastResponceWithDateTime
import ru.kulishov.openweatherapp.presentation.viewmodel.BaseViewModel
import ru.kulishov.openweatherapp.widget_feature.WeatherWidget
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Collections
import javax.inject.Inject

@HiltViewModel
class GeoWeatherViewModel @Inject constructor(
    private val retrofit: Retrofit,
    private val context: Context,
    private val locationManager: LocationManager
) : BaseViewModel() {

    private val _uiState = MutableLiveData<UiState>()
    val uiState: LiveData<UiState> = _uiState
    private val _cityName = MutableLiveData<SelectedCity>()
    val cityName: LiveData<SelectedCity> = _cityName

    private val _paramState = MutableLiveData<Int>()
    val paramState: LiveData<Int> = _paramState


    private val MY_PERMISSIONS_REQUEST_LOCATION = 98
    private val _currentForecast = MutableLiveData<Forecast>()
    val currentForecast: LiveData<Forecast> = _currentForecast
    private val _weatherForecat = MutableStateFlow<WeatherForecastResponceWithDateTime>(
        WeatherForecastResponceWithDateTime(
            cod = "",
            message = 0,
            cnt = 0,
            list = Collections.emptyList(),
            city = City(
                id = 0,
                name = "",
                coord = Coord(0.0, 0.0),
                country = "",
                population = 0,
                timezone = 0,
                sunrise = 0,
                sunset = 0
            ),
            update = 0
        )
    )
    private val _weatherForecast = MutableLiveData<WeatherForecastResponceWithDateTime>()
    val weatherForecast: LiveData<WeatherForecastResponceWithDateTime> = _weatherForecast

    private val _weatherListWithDate = MutableLiveData<List<Pair<Int, List<Forecast>>>>()
    val weatherListWithDate: LiveData<List<Pair<Int, List<Forecast>>>> = _weatherListWithDate

    private val _weatherListCurrentDayWithDate = MutableLiveData<List<Forecast>>()
    val weatherListCurrentDayWithDate: LiveData<List<Forecast>> = _weatherListCurrentDayWithDate

    private val _selectedDay = MutableLiveData<Int>(LocalDateTime.now().dayOfMonth)
    val selectedDay: LiveData<Int> = _selectedDay
    private val _selectedTime = MutableLiveData<Int>(LocalDateTime.now().hour)
    val selectedTime: LiveData<Int> = _selectedTime

    private val _isApiBlocked = mutableStateOf(false)


    private var fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private var cancellationTokenSource: CancellationTokenSource? = null


    init {
        getForecast()
    }

    fun getForecast() {
        if (!isLocationEnabled()) {
            _uiState.postValue(UiState.locationEnabled)
            return
        }
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    context as Activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {

            } else {
                ActivityCompat.requestPermissions(
                    context as Activity,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    MY_PERMISSIONS_REQUEST_LOCATION
                )
            }
            _uiState.postValue(UiState.NotPermission)
            return
        }
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource?.token
        )
            .addOnCompleteListener { data ->
                if (data.result == null) {
                    _uiState.postValue(UiState.locationEnabled)
                } else {
                    loadWeather(data.result.latitude, data.result.latitude)
                }
            }
    }

    private fun blockedApi() {
        launch {
            _isApiBlocked.value = true
            delay(1000)
            _isApiBlocked.value = false
        }
    }


    fun isLocationEnabled(): Boolean = locationManager.isLocationEnabled


    private fun loadWeather(lat: Double, lon: Double) {
        launch {
            _uiState.postValue(UiState.Loading)
            if (!_isApiBlocked.value) {
                try {
                    blockedApi()
                    geoRequest(
                        retrofit = retrofit,
                        lat = lat,
                        lon = lon,
                        onSuccess = { weather ->
                            val forecast = WeatherForecastMapper.toForecastWithDate(weather)
                            _weatherForecast.value = forecast
                            val fForecast =
                                findTodayCurrentHourForecast(weatherForecast.value!!.list)
                            if (fForecast != null) {
                                _currentForecast.postValue(fForecast)
                                sortedForecastForDate()
                            } else {
                                _uiState.postValue(UiState.Error("Not data"))
                            }
                            _uiState.postValue(UiState.Success)
                        },
                        onFailure = { e ->
                            _uiState.postValue(UiState.InternetError(""))
                        }
                    )

                } catch (e: Exception) {
                    _uiState.postValue(UiState.Error(e.message!!))
                }
            }
        }

    }


    fun findTodayCurrentHourForecast(forecasts: List<Forecast>): Forecast? {
        val currentTime = LocalDateTime.now()
        return forecasts.find { forecast ->
            val forecastDateTime = Instant.ofEpochMilli(forecast.dt * 1000 - 10800000 + 60000)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()

            forecastDateTime.year == currentTime.year &&
                    forecastDateTime.month == currentTime.month &&
                    forecastDateTime.dayOfMonth == currentTime.dayOfMonth &&
                    (currentTime.hour - forecastDateTime.hour < 3)
        }
    }

    fun sortedForecastForDate() {
        val forecastList = weatherForecast.value?.list ?: return
        val selectedDayValue = _selectedDay.value ?: LocalDateTime.now().dayOfMonth

        val groupedByDay = forecastList.groupBy { forecast ->
            Instant.ofEpochMilli(forecast.dt * 1000 - 10800000 + 60000)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .dayOfMonth
        }

        _weatherListWithDate.value = groupedByDay.entries.map { (day, forecasts) ->
            Pair(day, forecasts)
        }

        _weatherListCurrentDayWithDate.value =
            groupedByDay[selectedDayValue] ?: Collections.emptyList()
    }

    private fun handleApiFailure(
        cachedWeather: WeatherForecastResponceWithDateTime?,
        error: String
    ) {
        if (cachedWeather != null) {
            _uiState.postValue(UiState.InternetError(cachedWeather.update.toString()))
        } else {
            _uiState.postValue(UiState.Error("Network error: $error"))
        }
    }

    fun onWeatherUpdated(weather: Forecast) {
        launch {
            val prefs = context.getSharedPreferences("weather_widget", Context.MODE_PRIVATE)
            prefs.edit {
                putFloat("temp", weather.main.temp.toFloat())
                putInt(
                    "state", if (weather.main.humidity > 80) 2
                    else if (weather.clouds.all > 10) 1
                    else 0
                )

            }
            updateWidgetBroadcast()
        }
    }

    private fun updateWidgetBroadcast() {
        val intent = Intent(context, WeatherWidget::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        }
        val ids = AppWidgetManager.getInstance(context)
            .getAppWidgetIds(ComponentName(context, WeatherWidget::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        context.sendBroadcast(intent)
    }

    fun setParamState(state: Int) {
        _paramState.postValue(state)
    }

    fun setSelectedDay(day: Int) {
        _selectedDay.value = day
        sortedForecastForDate()
    }

    fun setSelectedTime(hour: Int) {
        _selectedTime.value = hour
    }

    fun updateCurrentForecast(forecast: Forecast) {
        val forecastTime = Instant.ofEpochMilli(forecast.dt * 1000 - 10800000 + 60000)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
        _currentForecast.postValue(forecast)
        setSelectedTime(forecastTime.hour)
    }


}
