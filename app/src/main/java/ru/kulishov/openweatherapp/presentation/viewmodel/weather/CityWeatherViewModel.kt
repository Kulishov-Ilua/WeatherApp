package ru.kulishov.openweatherapp.presentation.viewmodel.weather

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import retrofit2.Retrofit
import ru.kulishov.openweatherapp.data.local.data.mapper.WeatherForecastMapper
import ru.kulishov.openweatherapp.data.remote.api.cityRequest
import ru.kulishov.openweatherapp.data.remote.model.City
import ru.kulishov.openweatherapp.data.remote.model.Coord
import ru.kulishov.openweatherapp.data.remote.model.Forecast
import ru.kulishov.openweatherapp.data.remote.model.WeatherForecastResponse
import ru.kulishov.openweatherapp.domain.model.SelectedCity
import ru.kulishov.openweatherapp.domain.model.UiState
import ru.kulishov.openweatherapp.domain.model.WeatherForecastResponceWithDateTime
import ru.kulishov.openweatherapp.domain.usecase.weather.GetCityWeatherByNameUseCase
import ru.kulishov.openweatherapp.domain.usecase.weather.InsertCityWeatherUseCase
import ru.kulishov.openweatherapp.domain.usecase.weather.UpdateCityWeatherUseCase
import ru.kulishov.openweatherapp.presentation.viewmodel.BaseViewModel
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Collections.emptyList
import javax.inject.Inject

@HiltViewModel
class CityWeatherViewModel @Inject constructor(
    val getCityWeatherByNameUseCase: GetCityWeatherByNameUseCase,
    val updateCityWeatherUseCase: UpdateCityWeatherUseCase,
    val insertCityWeatherUseCase: InsertCityWeatherUseCase,
    val retrofit: Retrofit
) : BaseViewModel() {
    private val _uiState = MutableLiveData<UiState>()
    val uiState: LiveData<UiState> = _uiState

    private val _cityName = MutableLiveData<SelectedCity>()
    val cityName: LiveData<SelectedCity> = _cityName

    private val _paramState = MutableLiveData<Int>()
    val paramState: LiveData<Int> = _paramState


    private val _currentForecast = MutableLiveData<Forecast>()
    val currentForecast: LiveData<Forecast> = _currentForecast

    private val _weatherForecat = MutableStateFlow<WeatherForecastResponceWithDateTime>(
        WeatherForecastResponceWithDateTime(
            cod = "",
            message = 0,
            cnt = 0,
            list = emptyList(),
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
    val weatherForecast: StateFlow<WeatherForecastResponceWithDateTime> =
        _weatherForecat.asStateFlow()

    private val _weatherListWithDate =
        MutableStateFlow<List<Pair<Int, MutableList<Forecast>>>>(emptyList())
    val weatherListWithDate: StateFlow<List<Pair<Int, MutableList<Forecast>>>> =
        _weatherListWithDate.asStateFlow()

    private val _weatherListCurrentDayWithDate = MutableStateFlow<List<Forecast>>(emptyList())
    val weatherListCurrentDayWithDate: StateFlow<List<Forecast>> =
        _weatherListCurrentDayWithDate.asStateFlow()

    private val _selectedDay = MutableStateFlow<Int>(LocalDateTime.now().dayOfMonth)
    val selecteDay: StateFlow<Int> = _selectedDay.asStateFlow()


    private val _selectedTime = MutableLiveData<Int>(LocalDateTime.now().hour)
    val selectedTime: LiveData<Int> = _selectedTime

    fun loadWeather(city: SelectedCity) {
        launch {
            _cityName.postValue(city)
            _uiState.postValue(UiState.Loading)
            try {
                val weatherFromDb = getCityWeatherByNameUseCase(city.enName).firstOrNull()
                if (weatherFromDb != null && weatherFromDb.isNotEmpty()) {
                    _weatherForecat.value = weatherFromDb.first()
                    val fForecast = findTodayCurrentHourForecast(weatherForecast.value.list)
                    if (fForecast != null) {
                        _currentForecast.postValue(fForecast)
                        sortedForecastForDate()
                    } else {
                        _uiState.postValue(UiState.Error("Not data"))
                    }
                }
                val shouldUpdateFromApi = weatherFromDb!!.isEmpty() || shouldUpdateFromApi(
                    weatherFromDb.first().update
                )
                if (!shouldUpdateFromApi) {
                    _uiState.postValue(UiState.Success)
                    return@launch
                }

                try {
                    cityRequest(
                        retrofit = retrofit,
                        city = city.enName,
                        onSuccess = { weather ->
                            val forecast = WeatherForecastMapper.toForecastWithDate(weather)
                            _weatherForecat.value = forecast
                            updateDatabase(weather, weatherFromDb.isNotEmpty())
                            val fForecast = findTodayCurrentHourForecast(weatherForecast.value.list)
                            if (fForecast != null) {
                                _currentForecast.postValue(fForecast)
                                sortedForecastForDate()
                            } else {
                                _uiState.postValue(UiState.Error("Not data"))
                            }
                            _uiState.postValue(UiState.Success)
                        },
                        onFailure = { e ->
                            handleApiFailure(
                                if (weatherFromDb.isEmpty()) null
                                else
                                    weatherFromDb.first(), e
                            )
                        }
                    )

                } catch (e: Exception) {
                    handleApiFailure(weatherFromDb.first(), e.message!!)
                }

            } catch (e: Exception) {
                _uiState.postValue(UiState.Error("Database error: ${e.message}"))
            }
        }
    }


    private fun shouldUpdateFromApi(lastUpdate: Long?): Boolean {
        if (lastUpdate == null) return true
        val currentTime = System.currentTimeMillis()
        val threeHoursInMillis = 3 * 60 * 60 * 1000
        return (currentTime - lastUpdate) > threeHoursInMillis
    }

    private fun updateDatabase(weather: WeatherForecastResponse, hasExistingData: Boolean) {
        launch {
            try {
                if (hasExistingData) {
                    updateCityWeatherUseCase(weather)
                } else {
                    insertCityWeatherUseCase(weather)
                }
            } catch (e: Exception) {
                Log.e("Database update", "${e.message}")
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
        _weatherListWithDate.value = emptyList()
        _weatherListCurrentDayWithDate.value = emptyList()
        for (x in weatherForecast.value.list) {
            val date = Instant.ofEpochMilli(x.dt * 1000 - 10800000 + 60000)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
            val dayFind = _weatherListWithDate.value.find { it ->
                it.first == date.dayOfMonth
            }
            if (date.dayOfMonth == selecteDay.value) {
                _weatherListCurrentDayWithDate.value += x
            }

            if (dayFind == null) {
                _weatherListWithDate.value += Pair(
                    date.dayOfMonth, mutableListOf(
                        x
                    )
                )
            } else {
                dayFind.second += x
            }
        }
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