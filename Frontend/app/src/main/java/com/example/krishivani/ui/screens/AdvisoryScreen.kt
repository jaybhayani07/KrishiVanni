package com.example.krishivani.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.krishivani.ui.theme.*

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.krishivani.viewmodel.HomeViewModel
import com.example.krishivani.viewmodel.HomeUiState

@Composable
fun AdvisoryScreen(
    onNavigate: (String) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { HomeTopBar() },
        bottomBar = { AppBottomNavigation(currentRoute = "advisory", onNavigate = onNavigate) },
        containerColor = Color(0xFFF3F4F6)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChipCustom("બધા (All)", isSelected = true)
                FilterChipCustom("યુનિવર્સિટી (University)", isSelected = false)
                FilterChipCustom("હવામાન (Weather)", isSelected = false)
            }


            if (uiState is HomeUiState.Success) {
                val data = (uiState as HomeUiState.Success).data

                if (data.youtubeIsRunning == true) {
                    val progress = (data.youtubeProgress ?: 0) / 100f
                    val progressMessage = data.youtubeProgressMessage ?: "નવા વિડીયોનું પૃથ્થકરણ ચાલુ છે..."

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .background(Color(0xFFEF4444), RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Live Analyzing Video",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                progressMessage,
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth().height(6.dp),
                                color = Color(0xFF10B981),
                                trackColor = Color.White.copy(alpha = 0.3f),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                ExpertConsensusCard(
                    pareshForecast = data.pareshForecast ?: "આગામી ૨૪ કલાકમાં ભારે વરસાદની સંભાવના. પિયત અને જંતુનાશક છંટકાવ ટાળો.",
                    ambalalForecast = data.ambalalForecast ?: "તાપમાનમાં અચાનક ઘટાડો થવાની શક્યતા. કપાસ અને મગફળીમાં રોગનું જોખમ વધી શકે.",
                    agreementPercentage = data.agreementPercentage ?: "85%",
                    pareshIsLive = data.pareshIsLive ?: false,
                    ambalalIsLive = data.ambalalIsLive ?: false
                )
            } else {
                ExpertConsensusCard(
                    pareshForecast = "આગામી ૨૪ કલાકમાં ભારે વરસાદની સંભાવના. પિયત અને જંતુનાશક છંટકાવ ટાળો.",
                    ambalalForecast = "તાપમાનમાં અચાનક ઘટાડો થવાની શક્યતા. કપાસ અને મગફળીમાં રોગનું જોખમ વધી શકે.",
                    agreementPercentage = "95%",
                    pareshIsLive = false,
                    ambalalIsLive = false
                )
            }

            Spacer(modifier = Modifier.height(16.dp))


            if (uiState is HomeUiState.Success) {
                val alert = (uiState as HomeUiState.Success).data.warningDescGu
                if (alert.isNotEmpty()) {
                    WeatherAlertCard(warningDescGu = alert)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }


            if (uiState is HomeUiState.Success) {
                val tips = (uiState as HomeUiState.Success).data.expertFarmingTips
                if (!tips.isNullOrEmpty()) {
                    YoutubeAdvisoryCard(tips = tips)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }


            JauBulletinsSection()

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun FilterChipCustom(text: String, isSelected: Boolean) {
    Row(
        modifier = Modifier
            .background(
                if (isSelected) PrimaryGreen else Color.White,
                RoundedCornerShape(24.dp)
            )
            .border(
                1.dp,
                if (isSelected) PrimaryGreen else Color.LightGray,
                RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelected) {
            Icon(Icons.Default.List, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text,
            color = if (isSelected) Color.White else Color.DarkGray,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun ExpertConsensusCard(
    pareshForecast: String,
    ambalalForecast: String,
    agreementPercentage: String,
    pareshIsLive: Boolean,
    ambalalIsLive: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WarningOrange)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFFFFB74D).copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("નિષ્ણાત પૂર્વાનુમાન", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("(AI-Powered Expert Consensus)", color = Color.White, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))


            ExpertItem(
                avatarChar = "પ",
                avatarColor = Color(0xFFD4AF37),
                name = "પરેશ ગોસ્વામી",
                sub = "YouTube Expert • 2.5L Subscribers",
                gujText = pareshForecast,
                engText = "(Generated from latest YouTube video)",
                isLive = pareshIsLive
            )

            Spacer(modifier = Modifier.height(12.dp))


            ExpertItem(
                avatarChar = "અ",
                avatarColor = Color(0xFF2563EB),
                name = "અંબાલાલ પટેલ",
                sub = "Agricultural Specialist • 1.8L Subscribers",
                gujText = ambalalForecast,
                engText = "(Generated from latest YouTube video)",
                isLive = ambalalIsLive
            )

            Spacer(modifier = Modifier.height(16.dp))


            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE65100), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("સંમતિ સ્તર: $agreementPercentage (Both experts agree)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun ExpertItem(
    avatarChar: String,
    avatarColor: Color,
    name: String,
    sub: String,
    gujText: String,
    engText: String,
    isLive: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(avatarColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(avatarChar, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        Text(sub, color = Color.White, fontSize = 11.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier.background(if (isLive) Color(0xFF00E676) else Color.Gray, RoundedCornerShape(12.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(if (isLive) "Live" else "Old", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(gujText, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(engText, color = Color.White, fontSize = 12.sp)
        }
    }
}

@Composable
fun JauBulletinsSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {

            Box(modifier = Modifier.fillMaxWidth().background(PrimaryGreen).padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.List, contentDescription = null, tint = Color.LightGray)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("JAU સત્તાવાર સલાહ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("જુનાગઢ કૃષિ યુનિવર્સિટી (Official Bulletins)", color = Color.White, fontSize = 12.sp)
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                BulletinItem(
                    titleGu = "કપાસમાં ગુલાબી ઈયળ નિયંત્રણ",
                    titleEn = "Pink Bollworm Control in Cotton",
                    tagText = "જંતુ નિયંત્રણ",
                    tagColor = Color(0xFFE53935),
                    tagBg = Color(0xFFFFEBEE),
                    dateStr = "૨૫ મે ૨૦૨૬ • 2.4 MB"
                )
                Spacer(modifier = Modifier.height(12.dp))
                BulletinItem(
                    titleGu = "મગફળીમાં ટીકા રોગ વ્યવસ્થાપન",
                    titleEn = "Tikka Disease Management in Groundnut",
                    tagText = "રોગ વ્યવસ્થાપન",
                    tagColor = Color(0xFFE65100),
                    tagBg = Color(0xFFFFF3E0),
                    dateStr = "૨૩ મે ૨૦૨૬ • 1.8 MB"
                )
                Spacer(modifier = Modifier.height(12.dp))
                BulletinItem(
                    titleGu = "ખરીફ ૨૦૨૬ ની તૈયારી માર્ગદર્શિકા",
                    titleEn = "Kharif 2026 Preparation Guidelines",
                    tagText = "સામાન્ય સલાહ",
                    tagColor = Color(0xFF2E7D32),
                    tagBg = Color(0xFFE8F5E9),
                    dateStr = "૨૦ મે ૨૦૨૬ • 3.2 MB"
                )
                Spacer(modifier = Modifier.height(12.dp))
                BulletinItem(
                    titleGu = "કૃત્રિમ વરસાદની પરિસ્થિતિમાં પાક સંરક્ષણ",
                    titleEn = "Crop Protection During Heavy Rainfall",
                    tagText = "હવામાન સલાહ",
                    tagColor = Color(0xFF1976D2),
                    tagBg = Color(0xFFE3F2FD),
                    dateStr = "૧૮ મે ૨૦૨૬ • 1.5 MB"
                )
                Spacer(modifier = Modifier.height(12.dp))
                BulletinItem(
                    titleGu = "જૈવિક ખાતર ઉપયોગની સલાહ",
                    titleEn = "Organic Fertilizer Usage Guidelines",
                    tagText = "પોષક તત્વ વ્યવસ્થાપન",
                    tagColor = Color(0xFF2E7D32),
                    tagBg = Color(0xFFE8F5E9),
                    dateStr = "૧૫ મે ૨૦૨૬ • 2.1 MB"
                )
                Spacer(modifier = Modifier.height(12.dp))
                BulletinItem(
                    titleGu = "સૂકા વિસ્તારોમાં જળ સંરક્ષણ તકનીકો",
                    titleEn = "Water Conservation Techniques for Drought Areas",
                    tagText = "જળ વ્યવસ્થાપન",
                    tagColor = Color(0xFF1976D2),
                    tagBg = Color(0xFFE3F2FD),
                    dateStr = "૧૨ મે ૨૦૨૬ • 2.8 MB"
                )
            }
        }
    }
}

@Composable
fun BulletinItem(titleGu: String, titleEn: String, tagText: String, tagColor: Color, tagBg: Color, dateStr: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier.size(48.dp).background(PrimaryGreen, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(titleGu, fontWeight = FontWeight.Bold, color = Color.DarkGray, fontSize = 14.sp)
                    Text(titleEn, color = Color.Gray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.background(tagBg, RoundedCornerShape(12.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Text(tagText, color = tagColor, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(dateStr, color = Color.Gray, fontSize = 10.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier.size(40.dp).background(FavoriteYellow, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.DarkGray)
            }
        }
    }
}

@Composable
fun WeatherAlertCard(warningDescGu: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2563EB))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("હવામાન ચેતવણી (Weather Alert)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(warningDescGu, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("(AI-Generated Combined Weather Alert)", color = Color.White, fontSize = 12.sp)
        }
    }
}
