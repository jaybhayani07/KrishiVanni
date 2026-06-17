package com.example.krishivani.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

sealed class IrrigationUiState {
    object Loading : IrrigationUiState()
    data class WaterToday(val depletionMm: Double, val cropNameGu: String) : IrrigationUiState()
    data class DelayRainExpected(val rainfallMm: Double) : IrrigationUiState()
    data class WaterInXDays(val days: Int, val cropNameGu: String) : IrrigationUiState()
    data class Error(val message: String) : IrrigationUiState()
}

@Composable
fun FurrowIrrigationCard(uiState: IrrigationUiState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "ચાસ / ધોરિયા પિયત વ્યવસ્થાપન", // Furrow Irrigation Management
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            when (uiState) {
                is IrrigationUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is IrrigationUiState.WaterToday -> {
                    StatusBanner(
                        text = "આજે જ પિયત આપો (${uiState.cropNameGu})",
                        backgroundColor = Color(0xFFE8F5E9), // Light Green
                        contentColor = Color(0xFF2E7D32), // Dark Green
                        icon = Icons.Default.WaterDrop
                    )
                }
                is IrrigationUiState.DelayRainExpected -> {
                    StatusBanner(
                        text = "વરસાદની સંભાવના છે, પિયત લંબાવો (${uiState.rainfallMm} mm)",
                        backgroundColor = Color(0xFFFFF3E0), // Light Orange
                        contentColor = Color(0xFFE65100), // Dark Orange
                        icon = Icons.Default.Warning
                    )
                }
                is IrrigationUiState.WaterInXDays -> {
                    StatusBanner(
                        text = "${uiState.days} દિવસ પછી પિયત આપો",
                        backgroundColor = Color(0xFFE3F2FD), // Light Blue
                        contentColor = Color(0xFF1565C0), // Dark Blue
                        icon = Icons.Default.WaterDrop
                    )
                }
                is IrrigationUiState.Error -> {
                    Text(text = "Error: ${uiState.message}", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun StatusBanner(
    text: String,
    backgroundColor: Color,
    contentColor: Color,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = backgroundColor, shape = RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            ),
            color = contentColor
        )
    }
}
