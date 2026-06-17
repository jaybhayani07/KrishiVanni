package com.example.krishivani.model

data class IrrigationRequest(
    val crop: String,
    val stage: String,
    val soilType: String,
    val lastWateringDate: String,
    val lat: Double = 21.5222,
    val lon: Double = 70.4579
)
