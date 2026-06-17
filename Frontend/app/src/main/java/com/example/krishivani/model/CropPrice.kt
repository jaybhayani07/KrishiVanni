package com.example.krishivani.model

data class CropPrice(
    val id: Long? = null,
    val cropName: String,
    val district: String,
    val yard: String,
    val minPrice: String,
    val maxPrice: String,
    val modalPrice: String,
    val arrivals: String,
    val priceTrend: String,
    val arrivalTrend: String,
    val cropType: String
)
