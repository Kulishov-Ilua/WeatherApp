package ru.kulishov.openweatherapp.presentation.ui.components.weather

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.kulishov.openweatherapp.R
import ru.kulishov.openweatherapp.data.remote.model.Clouds
import ru.kulishov.openweatherapp.data.remote.model.Forecast
import ru.kulishov.openweatherapp.data.remote.model.MainForecast
import ru.kulishov.openweatherapp.data.remote.model.Sys
import ru.kulishov.openweatherapp.data.remote.model.Wind
import ru.kulishov.openweatherapp.domain.model.UiState
import ru.kulishov.openweatherapp.presentation.ui.components.app.ErrorMessageBoxUI
import ru.kulishov.openweatherapp.presentation.viewmodel.weather.GeoWeatherViewModel
import java.time.LocalDateTime
import java.util.Collections

@Composable
fun GeoWeatherUi(
    viewModel: GeoWeatherViewModel
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
        is UiState.locationEnabled -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ErrorMessageBoxUI(
                    stringResource(R.string.geo_error),
                )
                Box(
                    Modifier
                        .padding(bottom = 25.dp)
                        .fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Button(
                        onClick = { viewModel.getForecast() },
                        modifier = Modifier
                            .padding(top = 25.dp)
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(10),
                    ) {
                        Text(
                            stringResource(R.string.update),
                            style = TextStyle(
                                fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                                color = MaterialTheme.colorScheme.surface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                fontStyle = MaterialTheme.typography.bodyMedium.fontStyle
                            )
                        )
                    }
                }
            }
        }

        is UiState.NotPermission -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ErrorMessageBoxUI(
                    message = stringResource(R.string.perm_geo_error),
                )
                Box(
                    Modifier
                        .padding(bottom = 15.dp)
                        .fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Button(
                        onClick = { viewModel.getForecast() },
                        modifier = Modifier
                            .padding(top = 25.dp)
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(10),
                    ) {
                        Text(
                            stringResource(R.string.update),
                            style = TextStyle(
                                fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                                color = MaterialTheme.colorScheme.surface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                fontStyle = MaterialTheme.typography.bodyMedium.fontStyle
                            )
                        )
                    }
                }
            }
        }

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
                    message = stringResource(R.string.there_is_no_internet_connection),
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
    }
}