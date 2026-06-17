package com.example.krishivani.model

data class IrrigationAdviceResponse(
    val adviceGu: String,
    val adviceEn: String,
    val daysToNextIrrigation: Int,
    val deficitMm: Double
)
