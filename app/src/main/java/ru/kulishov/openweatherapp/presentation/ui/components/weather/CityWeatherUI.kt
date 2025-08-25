package ru.kulishov.openweatherapp.presentation.ui.components.weather

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.kulishov.openweatherapp.R
import ru.kulishov.openweatherapp.data.remote.model.Clouds
import ru.kulishov.openweatherapp.data.remote.model.Forecast
import ru.kulishov.openweatherapp.data.remote.model.MainForecast
import ru.kulishov.openweatherapp.data.remote.model.Sys
import ru.kulishov.openweatherapp.data.remote.model.Wind
import ru.kulishov.openweatherapp.domain.model.UiState
import ru.kulishov.openweatherapp.presentation.ui.components.app.ErrorMessageBoxUI
import ru.kulishov.openweatherapp.presentation.viewmodel.weather.CityWeatherViewModel
import java.time.LocalDateTime
import java.util.Collections

@Composable
fun CityWeatherUI(
    viewModel: CityWeatherViewModel,
) {
    val currentForecast = viewModel.currentForecast.observeAsState(
        Forecast(
            0,
            MainForecast(
                temp = 0.0,
                feels_like = 0.0,
                temp_min = 0.0,
                temp_max = 0.0,
                pressure = 0,
                sea_level = null,
                grnd_level = null,
                humidity = 0,
                temp_kf = 0.0
            ),
            weather = Collections.emptyList(),
            clouds = Clouds(0),
            wind = Wind(
                speed = 0.0,
                deg = 0,
                gust = null
            ),
            visibility = 0,
            pop = 0.0,
            sys = Sys(""),
            dt_txt = ""
        )
    )
    val curentForecastList = viewModel.weatherListCurrentDayWithDate.observeAsState(emptyList())
    val forecastList = viewModel.weatherListWithDate.observeAsState(emptyList())
    val selectedHour = viewModel.selectedTime.observeAsState(LocalDateTime.now().hour)
    val selectedDay = viewModel.selectedDay.observeAsState(LocalDateTime.now().dayOfMonth)
    val paramsState = viewModel.paramState.observeAsState(0)
    val uiState = viewModel.uiState.observeAsState(UiState.Loading)
    when (uiState.value) {
        is UiState.Loading -> {
            CircularProgressIndicator()
        }

        is UiState.InternetError -> {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(25.dp)
            ) {
                ErrorMessageBoxUI(
                    message = stringResource(R.string.there_is_no_internet_connection) + "\n" + stringResource(
                        R.string.the_data_is_current_on
                    ) + ":${currentForecast.value.dt_txt}",
                )
                CurrentWeatherBlockUI(
                    weather = currentForecast.value,
                )
            }

        }

        is UiState.Error -> {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ErrorMessageBoxUI(
                    message = stringResource(R.string.data_is_missing),
                )
            }
        }

        is UiState.Success -> {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(25.dp)
            ) {
                CurrentWeatherBlockUI(
                    weather = currentForecast.value,
                )
                WeatherParamsBlockUI(
                    state = paramsState.value,
                    onClick = { state ->
                        viewModel.setParamState(state)
                    },
                    weather = currentForecast.value,
                )
                HoursWeatherBlock(
                    state = paramsState.value,
                    onClick = { forecast ->
                        viewModel.updateCurrentForecast(forecast)
                    },
                    listForecast = curentForecastList.value,
                    selectedHour = selectedHour.value,
                )
                DaysWeatherBlockUI(
                    onClick = { day ->
                        viewModel.setSelectedDay(day)
                    },
                    listForecast = forecastList.value,
                    selectedDay = selectedDay.value,
                )
            }
        }

        is UiState.NotPermission -> {}
        is UiState.locationEnabled -> {}
    }


}