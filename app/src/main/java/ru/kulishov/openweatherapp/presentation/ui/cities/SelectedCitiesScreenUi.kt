package ru.kulishov.openweatherapp.presentation.ui.cities

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import ru.kulishov.openweatherapp.domain.model.SelectedCity
import ru.kulishov.openweatherapp.presentation.ui.components.city.CityCardUI
import ru.kulishov.openweatherapp.presentation.ui.components.city.FindCitiesField
import ru.kulishov.openweatherapp.presentation.viewmodel.cities.CitiesScreenViewModel
import ru.kulishov.openweatherapp.presentation.viewmodel.cities.CitySearchViewModel
import ru.kulishov.openweatherapp.presentation.viewmodel.weather.CityWeatherViewModel

@Composable
fun SelectedCityScreen(
    citiesVM: List<CityWeatherViewModel>,
    selectedCityViewModel: CitiesScreenViewModel,
    searchViewModel: CitySearchViewModel,
    onExit: () -> Unit

) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(25.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FindCitiesField(
            viewModel = searchViewModel,
            onTap = { city ->
                selectedCityViewModel.insertSelectedCities(city)
            })
        LazyColumn(
            modifier = Modifier
                .padding(bottom = 150.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            items(citiesVM) { VM ->
                val name = VM.cityName.observeAsState(SelectedCity(0, "", ""))
                Log.d("VM_data", VM.weatherListCurrentDayWithDate.observeAsState().toString())
                CityCardUI(
                    viewModel = VM,
                    onTap = {
                        selectedCityViewModel.deleteSelectedCities(name.value)
                    })
            }
        }
    }
    Box(
        Modifier
            .padding(bottom = 25.dp)
            .fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Button(
            onClick = { onExit() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(10),
        ) {
            Text(
                stringResource(R.string.ok),
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