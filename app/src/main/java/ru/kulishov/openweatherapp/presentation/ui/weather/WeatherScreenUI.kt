package ru.kulishov.openweatherapp.presentation.ui.weather

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.kulishov.openweatherapp.R
import ru.kulishov.openweatherapp.presentation.ui.components.app.PagerIndicator
import ru.kulishov.openweatherapp.presentation.ui.components.weather.CityWeatherUI
import ru.kulishov.openweatherapp.presentation.ui.components.weather.GeoWeatherUi
import ru.kulishov.openweatherapp.presentation.viewmodel.weather.CityWeatherViewModel
import ru.kulishov.openweatherapp.presentation.viewmodel.weather.GeoWeatherViewModel
import ru.kulishov.openweatherapp.presentation.viewmodel.weather.WeatherNavigationViewModel

@Composable
fun WeatherScreenUi(
    geoWeatherViewModel: GeoWeatherViewModel,
    weatherNavigationViewModel: WeatherNavigationViewModel,
    cityWeatherViewModel: CityWeatherViewModel
) {
    val selectedCities = weatherNavigationViewModel.selectedCities.observeAsState(emptyList())
    val currentPage = weatherNavigationViewModel.currentPage.observeAsState(0)
    val isSwipeBlocked = weatherNavigationViewModel.isSwipeBlocked.observeAsState(false)
    Box(
        Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, dragAmount ->
                    if (isSwipeBlocked.value) return@detectHorizontalDragGestures
                    weatherNavigationViewModel.blockedSwipe()
                    change.consume()
                    if (dragAmount > 0) {
                        if (currentPage.value > 0) {
                            if (currentPage.value != 1) {
                                cityWeatherViewModel.loadWeather(selectedCities.value[currentPage.value - 2])
                            }
                            weatherNavigationViewModel.pageChanged(currentPage.value - 1)
                        }
                    } else {
                        if (currentPage.value < selectedCities.value.size) {
                            cityWeatherViewModel.loadWeather(selectedCities.value[currentPage.value])
                            weatherNavigationViewModel.pageChanged(currentPage.value + 1)
                        }
                    }
                }
            },
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(25.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    if (currentPage.value == 0) stringResource(R.string.Your_location)
                    else selectedCities.value[currentPage.value - 1].localName,
                    style = TextStyle(
                        fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        fontStyle = MaterialTheme.typography.bodyMedium.fontStyle
                    )
                )
                PagerIndicator(
                    pageCount = selectedCities.value.size + 1,
                    currentPageIndex = currentPage.value,
                )
                LazyColumn() {
                    item {
                        if (currentPage.value == 0) {
                            GeoWeatherUi(
                                viewModel = geoWeatherViewModel,
                            )
                        } else {
                            CityWeatherUI(
                                viewModel = cityWeatherViewModel,
                            )
                        }
                    }
                }

            }


        }
    }

}