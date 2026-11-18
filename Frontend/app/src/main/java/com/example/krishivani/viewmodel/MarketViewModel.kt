package com.example.krishivani.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.krishivani.model.CropPrice
import com.example.krishivani.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MarketViewModel : ViewModel() {

    private val _marketData = MutableStateFlow<List<CropPrice>>(emptyList())
    val marketData: StateFlow<List<CropPrice>> = _marketData

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun fetchMarketData(district: String?, yard: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val englishDistrict = if (district?.contains("(") == true) {
                    district.substringAfter("(").substringBefore(")").trim()
                } else {
                    district ?: ""
                }

                var response = RetrofitInstance.api.getMarketPrices(district = englishDistrict, yard = null)

                if (response.isEmpty() && englishDistrict.equals("Mahisagar", ignoreCase = true)) {
                    response = RetrofitInstance.api.getMarketPrices(district = "Panchmahal", yard = null)
                }

                if (yard != null && !yard.contains("તમામ યાર્ડ") && !yard.equals("All Yards", ignoreCase = true)) {
                    val cleanYard = yard.replace(" APMC", "").trim()
                    response = response.filter { cp ->
                        cp.yard.contains(cleanYard, ignoreCase = true) ||
                        cp.yard.equals(yard, ignoreCase = true)
                    }
                }

                _marketData.value = response
            } catch (e: Exception) {
                _errorMessage.value = "ડેટા લાવવામાં ભૂલ: ${e.localizedMessage}"
                _marketData.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
