package com.example.krishivani.network

import com.example.krishivani.model.HomeDashboardData
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("api/home/dashboard")
    suspend fun getHomeDashboardData(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): HomeDashboardData

    @retrofit2.http.POST("api/irrigation/advice")
    suspend fun getIrrigationAdvice(
        @retrofit2.http.Body request: com.example.krishivani.model.IrrigationRequest
    ): com.example.krishivani.model.IrrigationAdviceResponse

    @GET("api/market/prices")
    suspend fun getMarketPrices(
        @Query("district") district: String? = null,
        @Query("yard") yard: String? = null
    ): List<com.example.krishivani.model.CropPrice>
}
