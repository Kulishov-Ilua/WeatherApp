package ru.kulishov.openweatherapp.presentation.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.kulishov.openweatherapp.R
import ru.kulishov.openweatherapp.presentation.ui.components.app.OTPTextFields
import ru.kulishov.openweatherapp.presentation.viewmodel.auth.AuthScreenViewModel

@Composable
fun OTPScreen(
    viewModel: AuthScreenViewModel
) {
    val otp = viewModel.otp.observeAsState("")
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
                    stringResource(R.string.sms_cod),
                    style = TextStyle(
                        fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        fontStyle = MaterialTheme.typography.bodyMedium.fontStyle
                    )
                )
                Text(
                    stringResource(R.string.enter_code),
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
                OTPTextFields(
                    length = 4,
                    { otp ->
                        viewModel.handleOTPVerification(otp)
                    }
                )
            }


        }
    }
}