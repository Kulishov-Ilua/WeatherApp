package ru.kulishov.openweatherapp

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Retrofit
import ru.kulishov.openweatherapp.domain.model.UiState
import ru.kulishov.openweatherapp.domain.usecase.weather.GetCityWeatherByNameUseCase
import ru.kulishov.openweatherapp.domain.usecase.weather.InsertCityWeatherUseCase
import ru.kulishov.openweatherapp.domain.usecase.weather.UpdateCityWeatherUseCase
import ru.kulishov.openweatherapp.presentation.ui.auth.OTPScreen
import ru.kulishov.openweatherapp.presentation.ui.auth.PhoneInputScreen
import ru.kulishov.openweatherapp.presentation.ui.cities.SelectedCityScreen
import ru.kulishov.openweatherapp.presentation.ui.components.app.ErrorMessageBoxUI
import ru.kulishov.openweatherapp.presentation.ui.weather.WeatherScreenUi
import ru.kulishov.openweatherapp.presentation.viewmodel.auth.AuthScreenViewModel
import ru.kulishov.openweatherapp.presentation.viewmodel.cities.CitiesScreenViewModel
import ru.kulishov.openweatherapp.presentation.viewmodel.cities.CitySearchViewModel
import ru.kulishov.openweatherapp.presentation.viewmodel.weather.CityWeatherViewModel
import ru.kulishov.openweatherapp.presentation.viewmodel.weather.GeoWeatherViewModel
import ru.kulishov.openweatherapp.presentation.viewmodel.weather.WeatherNavigationViewModel
import ru.kulishov.openweatherapp.ui.theme.OpenWeatherAppTheme
import javax.inject.Inject

@AndroidEntryPoint

class MainActivity : ComponentActivity() {
    @Inject
    lateinit var retrofit: Retrofit

    @Inject
    lateinit var getCityWeatherByNameUseCase: GetCityWeatherByNameUseCase

    @Inject
    lateinit var updateCityWeatherUseCase: UpdateCityWeatherUseCase

    @Inject
    lateinit var insertWeatherByNameUseCase: InsertCityWeatherUseCase

    @SuppressLint("ViewModelConstructorInComposable")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            var navState by remember { mutableStateOf(true) }


            val weatherScreenWeatherViewModel: CityWeatherViewModel = hiltViewModel()
            val searchViewModel: CitySearchViewModel = hiltViewModel()
            val selectedCityScreenViewModel: CitiesScreenViewModel = hiltViewModel()
            val weatherNavigationViewModel: WeatherNavigationViewModel = hiltViewModel()
            val authScreenViewModel: AuthScreenViewModel = hiltViewModel()

            val cities by selectedCityScreenViewModel.selectedCities.observeAsState(emptyList())
            val citiesVM = cities.mapIndexed { index, city ->
                Log.d("Flow", city.localName)
                val cityViewModel: CityWeatherViewModel = hiltViewModel(
                    key = "city_${city.id}_$index"
                )
                cityViewModel.loadWeather(city)
                Log.d(
                    "VM_data",
                    cityViewModel.weatherListCurrentDayWithDate.observeAsState(emptyList())
                        .toString()
                )
                cityViewModel
            }

            val authState = authScreenViewModel.uiState.observeAsState(UiState.locationEnabled)
            val otpState = authScreenViewModel.otpState.observeAsState(false)

            val geoWeatherViewModel = GeoWeatherViewModel(
                retrofit = retrofit,
                context = this,
                locationManager = locationManager
            )

            OpenWeatherAppTheme {
                Scaffold(
                    containerColor = MaterialTheme.colorScheme.surface,
                    content = { paddingValues ->
                        Box(
                            Modifier
                                .padding(paddingValues)
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier.padding(
                                    top = 50.dp,
                                    start = 25.dp,
                                    end = 25.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(25.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                when (authState.value) {
                                    is UiState.Loading -> {
                                        CircularProgressIndicator()
                                    }

                                    is UiState.locationEnabled -> {

                                        if (!otpState.value) {
                                            PhoneInputScreen(authScreenViewModel)
                                        } else {
                                            OTPScreen(authScreenViewModel)
                                        }

                                    }

                                    is UiState.NotPermission -> {
                                        ErrorMessageBoxUI(stringResource(R.string.invalid_otp))
                                        if (!otpState.value) {
                                            PhoneInputScreen(authScreenViewModel)
                                        } else {
                                            OTPScreen(authScreenViewModel)
                                        }
                                    }

                                    is UiState.Success -> {
                                        if (navState) {
                                            WeatherScreenUi(
                                                geoWeatherViewModel = geoWeatherViewModel,
                                                weatherNavigationViewModel = weatherNavigationViewModel,
                                                cityWeatherViewModel = weatherScreenWeatherViewModel,
                                            )
                                        } else {
                                            SelectedCityScreen(
                                                citiesVM,
                                                selectedCityScreenViewModel,
                                                searchViewModel,
                                                onExit = { navState = true })
                                        }
                                    }

                                    is UiState.Error -> {
                                    }

                                    is UiState.InternetError -> {
                                    }
                                }
                            }

                        }
                        when (authState.value) {
                            is UiState.Loading -> {

                            }

                            is UiState.Success -> {
                                if (navState) {
                                    Box(
                                        Modifier
                                            .padding(top = 50.dp, start = 25.dp)
                                            .clickable {
                                                navState = false
                                            }) {
                                        Icon(
                                            painter = painterResource(R.drawable.menu),
                                            contentDescription = "Menu",
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            is UiState.Error -> {
                            }

                            is UiState.NotPermission -> {
                            }

                            is UiState.InternetError -> {
                            }

                            is UiState.locationEnabled -> {
                            }
                        }

                    }
                )


            }


        }
    }
}
