package ru.kulishov.openweatherapp.presentation.ui.components.weather

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.kulishov.openweatherapp.R
import ru.kulishov.openweatherapp.domain.model.UiState
import ru.kulishov.openweatherapp.presentation.ui.components.app.ErrorMessageBoxUI
import ru.kulishov.openweatherapp.presentation.viewmodel.weather.CityWeatherViewModel
import java.time.LocalDateTime

@Composable
fun CityWeatherUI(
    viewModel: CityWeatherViewModel,
) {
    val currentForecast = viewModel.currentForecast.collectAsState()
    val curentForecastList = viewModel.weatherListCurrentDayWithDate.collectAsState()
    val forecastList = viewModel.weatherListWithDate.collectAsState()
    val selectedHour = viewModel.selectedTime.observeAsState(LocalDateTime.now().hour)
    val selectedDay = viewModel.selecteDay.collectAsState()
    val paramsState = viewModel.paramState.observeAsState(0)
    val uiState = viewModel.uiState.collectAsState()
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