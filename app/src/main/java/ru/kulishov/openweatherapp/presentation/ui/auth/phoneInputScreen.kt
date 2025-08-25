package ru.kulishov.openweatherapp.presentation.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.kulishov.openweatherapp.R
import ru.kulishov.openweatherapp.presentation.ui.components.app.ErrorMessageBoxUI
import ru.kulishov.openweatherapp.presentation.ui.components.app.TextFieldCustom
import ru.kulishov.openweatherapp.presentation.viewmodel.auth.AuthScreenViewModel

@Composable
fun PhoneInputScreen(
    viewModel: AuthScreenViewModel
) {
    val phoneNumber = viewModel.phone.observeAsState("")
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    var isEror by remember { mutableStateOf(false) }
    if (isEror) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            ErrorMessageBoxUI(stringResource(R.string.incorrect_number))
        }
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(25.dp)
        ) {

            Icon(
                painter = painterResource(R.drawable.outline_home_24),
                contentDescription = "home",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(100.dp)
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    stringResource(R.string.log_phone),
                    style = TextStyle(
                        fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        fontStyle = MaterialTheme.typography.bodyMedium.fontStyle
                    )
                )
                Text(
                    stringResource(R.string.log_log),
                    style = TextStyle(
                        fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        fontStyle = MaterialTheme.typography.bodyMedium.fontStyle
                    )
                )

            }
            Box(
                Modifier
                    .width(353.dp)
                    .height(70.dp)
                    .clip(RoundedCornerShape(10)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painterResource(R.drawable.city_card_bacground),
                    contentDescription = "background",
                    modifier = Modifier.fillMaxHeight(),
                    contentScale = ContentScale.FillHeight
                )
                TextFieldCustom(
                    inpText = phoneNumber.value,
                    onUpdate = { it ->
                        viewModel.setPhone(it)
                    },
                    label = { if (phoneNumber.value.isEmpty() && !isFocused) Text(stringResource(R.string.number_example)) },
                    visualTransformation = NanpVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    prefix = { Text("+7") },
                    interactionSource = interactionSource,
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        if (phoneNumber.value.length == 10) {
                            isEror = false
                            viewModel.sendCod()
                        } else {
                            viewModel.setPhone("")
                            isEror = true
                        }

                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(10),
                ) {
                    Text(
                        stringResource(R.string.send_code),
                        style = TextStyle(
                            fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                            color = MaterialTheme.colorScheme.surface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            fontStyle = MaterialTheme.typography.bodyMedium.fontStyle
                        )
                    )
                }
                Text(
                    stringResource(R.string.accept_terms),
                    style = TextStyle(
                        fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        fontStyle = MaterialTheme.typography.bodyMedium.fontStyle
                    ),
                    textAlign = TextAlign.Center
                )
            }


        }
    }

}