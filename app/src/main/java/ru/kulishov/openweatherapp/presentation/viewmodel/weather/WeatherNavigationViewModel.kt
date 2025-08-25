package ru.kulishov.openweatherapp.presentation.viewmodel.weather

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import ru.kulishov.openweatherapp.domain.model.SelectedCity
import ru.kulishov.openweatherapp.domain.model.UiState
import ru.kulishov.openweatherapp.domain.usecase.cities.GetSelectedCityUseCase
import ru.kulishov.openweatherapp.presentation.viewmodel.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class WeatherNavigationViewModel @Inject constructor(
    private val getSelectedCityUseCase: GetSelectedCityUseCase
) : BaseViewModel() {
    private val _uiState =
        MutableLiveData<UiState>()
    val uiState: LiveData<UiState> = _uiState
    private val _selectedCities = MutableLiveData<List<SelectedCity>>()
    val selectedCities: LiveData<List<SelectedCity>> = _selectedCities

    private val _currentPage = MutableLiveData<Int>()
    val currentPage: LiveData<Int> = _currentPage

    private val _isSwipeBlocked = MutableLiveData<Boolean>()
    val isSwipeBlocked: LiveData<Boolean> = _isSwipeBlocked


    init {
        loadSelectedCities()
    }

    private fun loadSelectedCities() {
        launch {
            getSelectedCityUseCase()
                .catch { e ->
                    _uiState.postValue(
                        UiState.Error(e.message ?: "Unknow error")
                    )
                }
                .collect { cities ->
                    _selectedCities.postValue(cities)
                    _uiState.postValue(UiState.Success)
                }
        }
    }

    fun pageChanged(id: Int) {
        _currentPage.postValue(id)

    }

    fun blockedSwipe() {
        launch {
            _isSwipeBlocked.postValue(true)
            delay(300)
            _isSwipeBlocked.postValue(false)
        }
    }

}