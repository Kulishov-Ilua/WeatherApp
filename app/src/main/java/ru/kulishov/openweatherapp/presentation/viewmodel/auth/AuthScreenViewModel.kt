package ru.kulishov.openweatherapp.presentation.viewmodel.auth

import android.accounts.Account
import android.accounts.AccountManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.kulishov.openweatherapp.domain.model.UiState
import ru.kulishov.openweatherapp.presentation.viewmodel.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class AuthScreenViewModel @Inject constructor(
    private val accountManager: AccountManager
) : BaseViewModel() {
    private val otpKey = "1234"

    private val _uiState = MutableLiveData<UiState>()
    val uiState: LiveData<UiState> = _uiState
    private val _otpState = MutableLiveData<Boolean>()
    val otpState: LiveData<Boolean> = _otpState


    private val _phone = MutableLiveData<String>()
    val phone: LiveData<String> = _phone

    private val _otp = MutableLiveData<String>()
    val otp: LiveData<String> = _otp

    init {
        val currentState = checkUserAuthState()
        launch { if (currentState) _uiState.postValue(UiState.Success) }
    }

    fun setPhone(inp: String) {
        launch {
            val numericRegex = Regex("[^0-9]")
            val stripped = numericRegex.replace(inp, "")
            _phone.postValue(
                if (stripped.length >= 10) {
                    stripped.substring(0..9)
                } else {
                    stripped
                }
            )
        }
    }

    fun checkUserAuthState(): Boolean =
        accountManager.getAccountsByType("openweatherapp").isNotEmpty()

    fun sendCod() {
        launch {
            _otpState.postValue(true)
        }

    }

    fun handleOTPVerification(otpCode: String) {

        launch {
            _uiState.postValue(UiState.Loading)
            if (otpCode == otpKey) {
                val account = Account("+7" + phone.value, "openweatherapp")
                val authToken = "OpenWeather Auth Token"

                accountManager.addAccountExplicitly(account, null, null)
                accountManager.setAuthToken(account, "OpenWeather Auth Token", authToken)

                _uiState.postValue(UiState.Success)
            } else {
                _phone.postValue("")
                _otpState.postValue(false)
                _uiState.postValue(UiState.NotPermission)
            }
        }


    }

}