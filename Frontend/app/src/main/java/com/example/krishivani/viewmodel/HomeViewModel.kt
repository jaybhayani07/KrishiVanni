package com.example.krishivani.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.krishivani.model.HomeDashboardData
import com.example.krishivani.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val data: HomeDashboardData) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState

    private val _irrigationState = MutableStateFlow<com.example.krishivani.model.IrrigationAdviceResponse?>(null)
    val irrigationState: StateFlow<com.example.krishivani.model.IrrigationAdviceResponse?> = _irrigationState

    var hasFetchedGpsLocation = false


    private val districtCoordinates = mapOf(
        "અમદાવાદ" to Pair(23.0225, 72.5714),
        "અમરેલી" to Pair(21.6032, 71.2158),
        "અરવલ્લી" to Pair(23.3303, 73.3300),
        "આણંદ" to Pair(22.5645, 72.9289),
        "કચ્છ" to Pair(23.7337, 69.8597),
        "ખેડા" to Pair(22.7506, 72.6828),
        "ગાંધીનગર" to Pair(23.2156, 72.6369),
        "ગીર સોમનાથ" to Pair(20.8880, 70.4012),
        "છોટા ઉદેપુર" to Pair(22.3023, 74.0135),
        "જામનગર" to Pair(22.4707, 70.0577),
        "જુનાગઢ" to Pair(21.5222, 70.4579),
        "ડાંગ" to Pair(20.8143, 73.7022),
        "તાપી" to Pair(21.1712, 73.6664),
        "દાહોદ" to Pair(22.8335, 74.2541),
        "દેવભૂમિ દ્વારકા" to Pair(22.2592, 69.0069),
        "નર્મદા" to Pair(21.8700, 73.5414),
        "નવસારી" to Pair(20.9467, 72.9322),
        "પંચમહાલ" to Pair(22.7666, 73.6167),
        "પાટણ" to Pair(23.8493, 72.1266),
        "પોરબંદર" to Pair(21.6417, 69.6293),
        "બનાસકાંઠા" to Pair(24.2691, 71.7456),
        "બોટાદ" to Pair(22.1706, 71.6684),
        "ભરૂચ" to Pair(21.7051, 72.9959),
        "ભાવનગર" to Pair(21.7645, 72.1519),
        "મહીસાગર" to Pair(23.2201, 73.4905),
        "મહેસાણા" to Pair(23.5880, 72.3693),
        "મોરબી" to Pair(22.8120, 70.8320),
        "રાજકોટ" to Pair(22.3039, 70.8022),
        "વડોદરા" to Pair(22.3072, 73.1812),
        "વલસાડ" to Pair(20.5992, 72.9342),
        "સાબરકાંઠા" to Pair(23.8312, 73.0043),
        "સુરત" to Pair(21.1702, 72.8311),
        "સુરેન્દ્રનગર" to Pair(22.7276, 71.6371)
    )

    init {

        fetchDashboardData("જુનાગઢ")
    }

    private var pollingJob: kotlinx.coroutines.Job? = null

    fun fetchDashboardData(district: String) {
        val coords = districtCoordinates[district] ?: Pair(21.5222, 70.4579)
        if (_uiState.value !is HomeUiState.Success) {
            _uiState.value = HomeUiState.Loading
        }

        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                try {
                    val response = RetrofitInstance.api.getHomeDashboardData(coords.first, coords.second)
                    _uiState.value = HomeUiState.Success(response.copy(location = district))

                    if (response.youtubeIsRunning != true) {
                        break
                    }
                } catch (e: Exception) {
                    if (_uiState.value !is HomeUiState.Success) {
                        _uiState.value = HomeUiState.Error("માહિતી લાવવામાં નિષ્ફળ: ${e.localizedMessage}")
                    }
                    break
                }
                kotlinx.coroutines.delay(3000)
            }
        }
    }

    fun fetchDashboardDataByCoordinates(lat: Double, lon: Double) {
        if (_uiState.value !is HomeUiState.Success) {
            _uiState.value = HomeUiState.Loading
        }

        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                try {
                    val response = RetrofitInstance.api.getHomeDashboardData(lat, lon)

                    _uiState.value = HomeUiState.Success(response.copy(location = "તમારું વર્તમાન સ્થાન (GPS)"))

                    if (response.youtubeIsRunning != true) {
                        break
                    }
                } catch (e: Exception) {
                    if (_uiState.value !is HomeUiState.Success) {
                        _uiState.value = HomeUiState.Error("માહિતી લાવવામાં નિષ્ફળ: ${e.localizedMessage}")
                    }
                    break
                }
                kotlinx.coroutines.delay(3000)
            }
        }
    }

    fun fetchIrrigationAdvice(crop: String, stage: String, soilType: String, lastWateringDate: String) {
        viewModelScope.launch {
            try {
                var lat = 21.5222
                var lon = 70.4579
                val currentState = _uiState.value
                if (currentState is HomeUiState.Success) {
                    val coords = districtCoordinates[currentState.data.location]
                    if (coords != null) {
                        lat = coords.first
                        lon = coords.second
                    }
                }

                val request = com.example.krishivani.model.IrrigationRequest(
                    lat = lat,
                    lon = lon,
                    crop = crop,
                    stage = stage,
                    soilType = soilType,
                    lastWateringDate = lastWateringDate
                )
                val response = RetrofitInstance.api.getIrrigationAdvice(request)
                _irrigationState.value = response
            } catch (e: Exception) {

                println("Irrigation Error: ${e.message}")
            }
        }
    }
}
