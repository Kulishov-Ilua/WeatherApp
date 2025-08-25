package ru.kulishov.openweatherapp.presentation.ui.components.city

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.kulishov.openweatherapp.R
import ru.kulishov.openweatherapp.domain.model.SelectedCity
import ru.kulishov.openweatherapp.presentation.ui.components.app.TextFieldCustom
import ru.kulishov.openweatherapp.presentation.viewmodel.cities.CitySearchViewModel

@Composable
fun FindCitiesField(
    viewModel: CitySearchViewModel,
    onTap: (SelectedCity) -> Unit
) {
    val text = viewModel.findName.observeAsState("")
    val cities = viewModel.findCities.observeAsState(emptyList())
    val boxHeight = animateDpAsState(
        targetValue = if (cities.value.isNotEmpty()) 200.dp
        else 70.dp
    )

    Box(
        Modifier
            .fillMaxWidth()
            .height(boxHeight.value)
            .clip(RoundedCornerShape(10)), contentAlignment = Alignment.Center
    ) {
        if (boxHeight.value == 200.dp) {
            Image(
                painter = painterResource(R.drawable.find_background),
                contentDescription = "backgroung",
                Modifier
                    .fillMaxWidth()
                    .height(boxHeight.value),

                )
        } else {
            Image(
                painter = painterResource(R.drawable.city_card_bacground),
                contentDescription = "backgroung",
                Modifier
                    .fillMaxWidth()
                    .height(boxHeight.value),
                contentScale = ContentScale.Crop
            )
        }
        Column(
            Modifier
                .padding(top = 5.dp, bottom = 15.dp, start = 25.dp, end = 25.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextFieldCustom(
                inpText = text.value,
                onUpdate = { inp -> viewModel.setName(inp) },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.outline_search_24),
                        contentDescription = "search",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            )

            LazyColumn {
                items(cities.value) { city ->
                    Column(
                        modifier = Modifier
                            .clickable {
                                onTap(city)
                            }
                            .padding(bottom = 5.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(25.dp)) {
                        Box(
                            modifier = Modifier
                                .padding(start = 5.dp, end = 5.dp)
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.onSurface, CircleShape)
                        )
                        Text(
                            city.localName,
                            style = TextStyle(
                                fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
                                fontStyle = MaterialTheme.typography.bodyMedium.fontStyle
                            )
                        )
                    }

                }
            }
        }

    }
}