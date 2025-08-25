package ru.kulishov.openweatherapp.presentation.viewmodel.cities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import ru.kulishov.openweatherapp.domain.model.SelectedCity
import ru.kulishov.openweatherapp.domain.model.UiState
import ru.kulishov.openweatherapp.domain.usecase.cities.DeleteSelectedCityUseCase
import ru.kulishov.openweatherapp.domain.usecase.cities.GetSelectedCityUseCase
import ru.kulishov.openweatherapp.domain.usecase.cities.InsertSelectedCityUseCase
import ru.kulishov.openweatherapp.presentation.viewmodel.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class CitiesScreenViewModel @Inject constructor(
    private val getSelectedCityUseCase: GetSelectedCityUseCase,
    private val insertSelectedCityUseCase: InsertSelectedCityUseCase,
    private val deleteSelectedCity: DeleteSelectedCityUseCase
) : BaseViewModel() {
    private val _uiState = MutableSharedFlow<UiState>()
    val uiState: Flow<UiState> = _uiState

    private val _selectedCities = MutableLiveData<List<SelectedCity>>()
    val selectedCities: LiveData<List<SelectedCity>> = _selectedCities

    init {
        loadSelectedCities()
    }

    private fun loadSelectedCities() {
        launch {
            getSelectedCityUseCase()
                .catch { e ->
                    _uiState.emit(UiState.Error(e.message ?: "Unknow error"))
                }
                .collect { cities ->
                    _selectedCities.postValue(cities)
                    _uiState.emit(UiState.Success)
                }
        }
    }

    fun deleteSelectedCities(city: SelectedCity) {
        launch {
            _uiState.emit(UiState.Loading)
            try {
                val currentCities = _selectedCities.value ?: emptyList()
                val containsCity = currentCities.any { it.enName == city.enName }
                if (containsCity) {
                    deleteSelectedCity(city)
                    _selectedCities.postValue(currentCities - city)
                }
                _uiState.emit(UiState.Success)
            } catch (e: Exception) {
                _uiState.emit(UiState.Error(e.message ?: "Failed to add event"))
            }
        }
    }

    fun insertSelectedCities(city: SelectedCity) {
        launch {
            _uiState.emit(UiState.Loading)
            try {
                val currentCities = _selectedCities.value ?: emptyList()
                val containsCity = currentCities.any { it.enName == city.enName }
                if (!containsCity) {
                    insertSelectedCityUseCase(city)
                    _selectedCities.postValue(currentCities + city)
                }
                _uiState.emit(UiState.Success)
            } catch (e: Exception) {
                _uiState.emit(UiState.Error(e.message ?: "Failed to add event"))
            }
        }
    }

}