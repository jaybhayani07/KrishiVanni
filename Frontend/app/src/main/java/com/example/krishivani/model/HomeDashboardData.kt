package com.example.krishivani.model

data class HomeDashboardData(
    val id: Long? = null,
    val location: String = "",
    val temperature: String = "",
    val conditionGu: String = "",
    val conditionEn: String = "",
    val highLow: String = "",
    val soilMoisture: String = "",
    val soilTemperature: String = "",
    val cropHealthStatusGu: String = "",
    val cropHealthStatusEn: String = "",
    val ndviValue: String = "",
    val lastScanDate: String = "",
    val nextScanDate: String = "",
    val satelliteImageUrl: String? = null,
    val warningTitleGu: String = "",
    val warningTitleEn: String = "",
    val warningDescGu: String = "",
    val irrigationRecommendationGu: String = "",
    val irrigationDays: String = "",
    val expertFarmingTips: List<String>? = null,
    val youtubeLastUpdated: String? = null,
    val pareshForecast: String? = null,
    val ambalalForecast: String? = null,
    val agreementPercentage: String? = null,
    val pareshIsLive: Boolean? = null,
    val ambalalIsLive: Boolean? = null,
    val youtubeIsRunning: Boolean? = false,
    val youtubeProgress: Int? = 0,
    val youtubeProgressMessage: String? = null,
    val forecast: List<DailyForecast>? = null
)

data class DailyForecast(
    val dayNameGu: String = "",
    val maxTemp: String = "",
    val iconName: String = ""
)
