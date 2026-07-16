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
                var englishDistrict = if (district?.contains("(") == true) {
                    district.substringAfter("(").substringBefore(")").trim()
                } else {
                    district ?: ""
                }


                englishDistrict = when (englishDistrict) {
                    "Junagadh" -> "Junagarh"
                    "Banaskantha" -> "Banaskanth"
                    "Kutch" -> "Kachchh"
                    "Vadodara" -> "Vadodara(Baroda)"
                    "Panchmahal" -> "Panchmahals"
                    else -> englishDistrict
                }

                var response = RetrofitInstance.api.getMarketPrices(district = englishDistrict, yard = null)


                if (yard != null && !yard.contains("તમામ યાર્ડ")) {

                    val yardPrefixGu = yard.replace(" APMC", "").trim()


                    val yardMapGuToEn = mapOf(

                        "રાજકોટ" to "Rajkot", "રાજકોટ બેડી" to "Rajkot", "ગોંડલ" to "Gondal", "જસદણ" to "Jasdan", "ધોરાજી" to "Dhoraji", "જેતપુર" to "Jetpur", "ઉપલેટા" to "Upleta",
                        "જુનાગઢ" to "Junagadh", "વિસાવદર" to "Visavadar", "માણાવદર" to "Manavdar",
                        "અમરેલી" to "Amreli", "સાવરકુંડલા" to "Savarkundla", "બાબરા" to "Babra",
                        "ભાવનગર" to "Bhavnagar", "મહુવા" to "Mahuva", "પાલીતાણા" to "Palitana",
                        "સુરેન્દ્રનગર" to "Surendranagar", "વઢવાણ" to "Vadhvan", "હળવદ" to "Halvad",
                        "જામનગર" to "Jamnagar", "ધ્રોલ" to "Dhrol", "જામજોધપુર" to "Jamjodhpur",
                        "પોરબંદર" to "Porbandar",
                        "મોરબી" to "Morbi", "વાંકાનેર" to "Vankaner",
                        "બોટાદ" to "Botad",
                        "ઉના" to "Una", "વેરાવળ" to "Veraval", "તાલાલા" to "Talala",
                        "ખંભાળિયા" to "Khambhalia", "ભાણવડ" to "Bhanvad",


                        "દહેગામ" to "Dehgam", "માણસા" to "Mansa", "કલોલ" to "Kalol",
                        "ઊંઝા" to "Unjha", "મહેસાણા" to "Mehsana", "વિસનગર" to "Visnagar", "કડી" to "Kadi",
                        "પાટણ" to "Patan", "હારીજ" to "Harij", "રાધનપુર" to "Radhanpur",
                        "ડીસા" to "Deesa", "પાલનપુર" to "Palanpur", "થરાદ" to "Tharad", "ધાનેરા" to "Dhanera",
                        "હિંમતનગર" to "Himmat", "ઈડર" to "Idar", "પ્રાંતિજ" to "Prantij",
                        "મોડાસા" to "Modasa", "બાયડ" to "Bayad",


                        "અમદાવાદ" to "Ahmedabad", "બાવળા" to "Bavla", "સાણંદ" to "Sanand", "ધોળકા" to "Dholka",
                        "નડીયાદ" to "Nadiad", "કપડવંજ" to "Kapadvanj", "મહેમદાવાદ" to "Mahemdavad",
                        "આણંદ" to "Anand", "ઉમરેઠ" to "Umreth", "પેટલાદ" to "Petlad",
                        "ગોધરા" to "Godhra", "હાલોલ" to "Halol",
                        "લુણાવાડા" to "Lunawada", "બાલાસિનોર" to "Balasinor",
                        "દાહોદ" to "Dahod", "ઝાલોદ" to "Zalod",
                        "વડોદરા" to "Vadodara", "સાવલી" to "Savli", "પાદરા" to "Padra",
                        "છોટા ઉદેપુર" to "Chhota", "બોડેલી" to "Bodeli",


                        "સુરત" to "Surat", "બારડોલી" to "Bardoli", "માંડવી (સુરત)" to "Mandvi",
                        "નવસારી" to "Navsari", "બીલીમોરા" to "Bilimora",
                        "વલસાડ" to "Valsad", "ધરમપુર" to "Dharampur",
                        "આહવા" to "Ahwa",
                        "વ્યારા" to "Vyara", "નિઝર" to "Nizar",
                        "ભરૂચ" to "Bharuch", "અંકલેશ્વર" to "Ankleshwar", "જંબુસર" to "Jambusar",
                        "રાજપીપળા" to "Rajpipla",


                        "ભુજ" to "Bhuj", "અંજાર" to "Anjar", "રાપર" to "Rapar", "ભચાઉ" to "Bhachau", "માંડવી (કચ્છ)" to "Mandvi"
                    )

                    val yardPrefixEn = yardMapGuToEn[yardPrefixGu] ?: ""


                    response = response.filter {
                        (yardPrefixEn.isNotEmpty() && it.yard.startsWith(yardPrefixEn, ignoreCase = true)) ||
                        it.yard.startsWith(yardPrefixGu, ignoreCase = true)
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
