package ru.kulishov.openweatherapp.presentation.viewmodel.cities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import ru.kulishov.openweatherapp.domain.model.SelectedCity
import ru.kulishov.openweatherapp.domain.model.UiState
import ru.kulishov.openweatherapp.domain.usecase.cities.FindCityUseCase
import ru.kulishov.openweatherapp.presentation.viewmodel.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class CitySearchViewModel @Inject constructor(
    val findCityUseCase: FindCityUseCase
) : BaseViewModel() {
    private val _uiState = MutableLiveData<UiState>(UiState.Loading)
    val uiState: LiveData<UiState> = _uiState

    private val debouncePeriod = 300L
    private val _findName = MutableLiveData<String>("")
    val findName: LiveData<String> = _findName

    private val _findCities = MutableLiveData<List<SelectedCity>>(emptyList())
    val findCities: LiveData<List<SelectedCity>> = _findCities

    fun setName(name: String) {
        _findName.postValue(name)
        launch {
            delay(300)
            if (_findName.value == name) {
                findCityUseCase(name).catch { e ->
                    _uiState.postValue(UiState.Error(e.message ?: "Unknow error"))
                }
                    .collect { cities ->
                        _findCities.postValue(cities)
                        _uiState.postValue(UiState.Success)
                    }
            }
        }


    }

    fun search(name: String) {
        launch {
            delay(300)
            if (_findName.value == name) {
                findCityUseCase(name).catch { e ->
                    _uiState.postValue(UiState.Error(e.message ?: "Unknow error"))
                }
                    .collect { cities ->

                        _findCities.postValue(cities)
                        _uiState.postValue(UiState.Success)
                    }
            }
        }
    }


}