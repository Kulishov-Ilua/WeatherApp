package ru.kulishov.openweatherapp.presentation.ui.components.city

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.kulishov.openweatherapp.R
import ru.kulishov.openweatherapp.domain.model.SelectedCity
import ru.kulishov.openweatherapp.domain.model.UiState
import ru.kulishov.openweatherapp.presentation.viewmodel.weather.CityWeatherViewModel

@Composable
fun CityCardUI(
    viewModel: CityWeatherViewModel,
    onTap: () -> Unit
) {
    val curentForecastList = viewModel.weatherListCurrentDayWithDate.collectAsState()
    val uiState = viewModel.uiState.observeAsState(UiState.Loading)
    val cityName = viewModel.cityName.observeAsState(SelectedCity(0, "", ""))
    Box(
        Modifier
            .fillMaxWidth()
            .height(60.dp),
        contentAlignment = Alignment.Center
    ) {
        var showMinTemp by remember { mutableStateOf(true) }
        Image(
            painter = painterResource(R.drawable.city_card_bacground),
            contentDescription = "backgroung",
            Modifier.fillMaxSize()
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(start = 25.dp, end = 25.dp)
                .fillMaxSize(),
            horizontalArrangement = Arrangement.Start
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.exit),
                    contentDescription = "delete",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clickable {
                        onTap()
                    })
                Text(
                    cityName.value.localName,
                    style = TextStyle(
                        fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        fontStyle = MaterialTheme.typography.bodyMedium.fontStyle
                    ),
                    modifier = Modifier.padding(start = 15.dp),
                    overflow = TextOverflow.Ellipsis,
                    onTextLayout = { textLayoutResult ->
                        if (textLayoutResult.hasVisualOverflow) {
                            showMinTemp = false
                        }
                    }
                )
            }
            Row(
                //modifier = Modifier.wrapContentWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                when (uiState.value) {
                    is UiState.Loading -> {
                        CircularProgressIndicator()
                    }

                    is UiState.InternetError -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            if (showMinTemp) {
                                Text(
                                    "${curentForecastList.value[curentForecastList.value.size / 2].main.temp_min}°С",
                                    style = TextStyle(
                                        fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                                        color = Color(111, 121, 118),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        fontStyle = MaterialTheme.typography.bodyMedium.fontStyle
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Visible,
                                )
                                Text(
                                    "${curentForecastList.value[curentForecastList.value.size / 2].main.temp_max}°С",
                                    style = TextStyle(
                                        fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        fontStyle = MaterialTheme.typography.bodyMedium.fontStyle
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Visible,
                                    onTextLayout = { textLayoutResult ->
                                        if (textLayoutResult.hasVisualOverflow) {
                                            showMinTemp = false
                                        }
                                    }
                                )
                            }


                        }
                    }

                    is UiState.Success -> {
                        Row(
                            modifier = Modifier.wrapContentWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            if (showMinTemp && curentForecastList.value.isNotEmpty()) {
                                Log.d("Data", "nit empty")
                                Text(
                                    "${curentForecastList.value[curentForecastList.value.size / 2].main.temp_min}°С",
                                    style = TextStyle(
                                        fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                                        color = Color(111, 121, 118),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        fontStyle = MaterialTheme.typography.bodyMedium.fontStyle
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Visible,
                                )
                                Text(
                                    "${curentForecastList.value[curentForecastList.value.size / 2].main.temp_max}°С",
                                    style = TextStyle(
                                        fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        fontStyle = MaterialTheme.typography.bodyMedium.fontStyle
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Visible,
                                    onTextLayout = { textLayoutResult ->
                                        if (textLayoutResult.hasVisualOverflow) {
                                            showMinTemp = false
                                        }
                                    }
                                )
                            }


                        }
                    }

                    is UiState.Error -> {
                        Text(
                            stringResource(R.string.not_data),
                            style = TextStyle(
                                fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                fontStyle = MaterialTheme.typography.bodyMedium.fontStyle
                            )
                        )
                    }

                    is UiState.locationEnabled -> {}
                    is UiState.NotPermission -> {}
                }
            }

        }

    }
}