package ru.kulishov.openweatherapp.presentation.viewmodel.weather

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import retrofit2.Retrofit
import ru.kulishov.openweatherapp.data.local.data.mapper.WeatherForecastMapper
import ru.kulishov.openweatherapp.data.remote.api.cityRequest
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
    private val _selectedTime = MutableLiveData<Int>(LocalDateTime.now().hour)
    val selectedTime: LiveData<Int> = _selectedTime

    private val _weatherForecast = MutableLiveData<WeatherForecastResponceWithDateTime>()
    val weatherForecast: LiveData<WeatherForecastResponceWithDateTime> = _weatherForecast

    private val _weatherListWithDate = MutableLiveData<List<Pair<Int, List<Forecast>>>>()
    val weatherListWithDate: LiveData<List<Pair<Int, List<Forecast>>>> = _weatherListWithDate

    private val _weatherListCurrentDayWithDate = MutableLiveData<List<Forecast>>()
    val weatherListCurrentDayWithDate: LiveData<List<Forecast>> = _weatherListCurrentDayWithDate

    private val _selectedDay = MutableLiveData<Int>(LocalDateTime.now().dayOfMonth)
    val selectedDay: LiveData<Int> = _selectedDay

    fun loadWeather(city: SelectedCity) {
        launch {
            _cityName.postValue(city)
            _uiState.postValue(UiState.Loading)
            try {
                val weatherFromDb = getCityWeatherByNameUseCase(city.enName).firstOrNull()
                if (weatherFromDb != null && weatherFromDb.isNotEmpty()) {
                    _weatherForecast.value = weatherFromDb.first()
                    val fForecast = findTodayCurrentHourForecast(weatherForecast.value!!.list)
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
                            _weatherForecast.value = forecast
                            updateDatabase(weather, weatherFromDb.isNotEmpty())
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

        _weatherListCurrentDayWithDate.value = groupedByDay[selectedDayValue] ?: emptyList()
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