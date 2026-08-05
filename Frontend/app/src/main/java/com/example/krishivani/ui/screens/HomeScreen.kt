package com.example.krishivani.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import java.util.Calendar
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.krishivani.viewmodel.HomeViewModel
import com.example.krishivani.viewmodel.HomeUiState
import com.example.krishivani.model.HomeDashboardData
import androidx.compose.ui.Alignment
import android.Manifest
import android.location.Location
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.krishivani.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)


    LaunchedEffect(Unit) {
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        }
    }

    LaunchedEffect(locationPermissionState.status.isGranted) {
        if (locationPermissionState.status.isGranted && !viewModel.hasFetchedGpsLocation) {
            viewModel.hasFetchedGpsLocation = true
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    location?.let {
                        viewModel.fetchDashboardDataByCoordinates(it.latitude, it.longitude)
                    }
                }
            } catch (e: SecurityException) {

            }
        }
    }

    val currentLocation = when (val state = uiState) {
        is HomeUiState.Success -> state.data.location
        else -> "જુનાગઢ"
    }

    Scaffold(
        topBar = {
            HomeTopBar(
                currentLocation = currentLocation,
                onDistrictSelected = { district ->
                    viewModel.fetchDashboardData(district)
                },
                onGpsClicked = {
                    if (locationPermissionState.status.isGranted) {
                        try {
                            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                                location?.let {
                                    viewModel.fetchDashboardDataByCoordinates(it.latitude, it.longitude)
                                }
                            }
                        } catch (e: SecurityException) {}
                    } else {
                        locationPermissionState.launchPermissionRequest()
                    }
                }
            )
        },
        bottomBar = { AppBottomNavigation(currentRoute = "home", onNavigate = onNavigate) },
        containerColor = Color(0xFFF9F9F9)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    item {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = PrimaryGreen)
                        }
                    }
                }
                is HomeUiState.Success -> {
                    item { WeatherAndConsensusCard(state.data) }
                    item { CropHealthCard(state.data) }
                    item { IrrigationAdvisorCard() }
                    item { OfficialAdvisoriesCard() }
                }
                is HomeUiState.Error -> {
                    item { Text("Error: ${state.message}", color = Color.Red) }
                }
            }
        }
    }
}

@Composable
fun HomeTopBar(
    currentLocation: String = "જુનાગઢ",
    onDistrictSelected: (String) -> Unit = {},
    onGpsClicked: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    val districts = listOf(
        "અમદાવાદ", "અમરેલી", "અરવલ્લી", "આણંદ", "કચ્છ", "ખેડા", "ગાંધીનગર",
        "ગીર સોમનાથ", "છોટા ઉદેપુર", "જામનગર", "જુનાગઢ", "ડાંગ", "તાપી",
        "દાહોદ", "દેવભૂમિ દ્વારકા", "નર્મદા", "નવસારી", "પંચમહાલ", "પાટણ",
        "પોરબંદર", "બનાસકાંઠા", "બોટાદ", "ભરૂચ", "ભાવનગર", "મહીસાગર",
        "મહેસાણા", "મોરબી", "રાજકોટ", "વડોદરા", "વલસાડ", "સાબરકાંઠા",
        "સુરત", "સુરેન્દ્રનગર"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PrimaryGreen)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(WarningOrange),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "ખ", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = "નમસ્તે (Namaste)", color = Color.White, fontSize = 12.sp)
                Text(text = "ખેડૂત", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF4CAF50), RoundedCornerShape(16.dp))
                        .clickable { expanded = true }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(currentLocation, color = Color.White, fontSize = 14.sp)
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color.White).heightIn(max = 300.dp)
                ) {
                    districts.forEach { district ->
                        DropdownMenuItem(
                            text = { Text(district, color = Color.Black) },
                            onClick = {
                                expanded = false
                                onDistrictSelected(district)
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF4CAF50), CircleShape)
                    .clickable { onGpsClicked() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = "Use GPS", tint = Color.White)
            }

        }
    }
}

@Composable
fun AppBottomNavigation(currentRoute: String, onNavigate: (String) -> Unit) {
    NavigationBar(containerColor = Color.White) {
        NavigationBarItem(
            selected = currentRoute == "home",
            onClick = { onNavigate("home") },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("હોમ"); Text("(Home)", fontSize = 10.sp) } },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = PrimaryGreen, selectedTextColor = PrimaryGreen)
        )
        NavigationBarItem(
            selected = currentRoute == "market",
            onClick = { onNavigate("market") },
            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Market") },
            label = { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("બજાર"); Text("(Market)", fontSize = 10.sp) } },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = PrimaryGreen, selectedTextColor = PrimaryGreen)
        )

        NavigationBarItem(
            selected = currentRoute == "camera",
            onClick = { onNavigate("camera") },
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(if (currentRoute == "camera") PrimaryGreen else Color(0xFFE0E0E0), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Camera", tint = if (currentRoute == "camera") Color.White else Color.DarkGray)
                }
            },
            label = { Text("કેમેરા", fontSize = 10.sp, color = if (currentRoute == "camera") PrimaryGreen else Color.DarkGray) }
        )
        NavigationBarItem(
            selected = currentRoute == "advisory",
            onClick = { onNavigate("advisory") },
            icon = { Icon(Icons.Default.Info, contentDescription = "Advisory", tint = if (currentRoute == "advisory") PrimaryGreen else Color.DarkGray) },
            label = { Text("સલાહ", fontSize = 10.sp, color = if (currentRoute == "advisory") PrimaryGreen else Color.DarkGray) }
        )
    }
}

@Composable
fun WeatherAndConsensusCard(data: HomeDashboardData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WeatherBlue)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("આજે (Today) - ${data.location}", color = Color.White, fontWeight = FontWeight.Bold)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Icon(Icons.Default.Cloud, contentDescription = null, tint = Color.White, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text(data.temperature.replace("°C", ""), color = Color.White, fontSize = 64.sp, fontWeight = FontWeight.Bold)
                Text("°C", color = Color.White, fontSize = 24.sp, modifier = Modifier.padding(top = 16.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF60A5FA), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(data.conditionGu, color = Color.White, fontSize = 12.sp)
                }
                Box(
                    modifier = Modifier
                        .background(Color(0xFF8B4513).copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("જમીન ભેજ: ${data.soilMoisture}", color = Color.White, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .background(Color(0xFFD84315).copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("જમીન તાપમાન: ${data.soilTemperature}", color = Color.White, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier
                .fillMaxWidth()
                .background(ForecastLightBlue, RoundedCornerShape(12.dp))
                .padding(12.dp)
            ) {
                Column {
                    Text("આગામી ૭ દિવસ (7-Day Forecast)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (!data.forecast.isNullOrEmpty()) {
                            data.forecast.take(7).forEach { daily ->
                                val icon = when(daily.iconName) {
                                    "Sunny" -> Icons.Default.WbSunny
                                    "Rain" -> Icons.Default.WaterDrop
                                    "Thunder" -> Icons.Default.FlashOn
                                    "Cloud" -> Icons.Default.Cloud
                                    else -> Icons.Default.WbCloudy
                                }
                                ForecastDay(daily.dayNameGu, daily.maxTemp, icon, isToday = daily.dayNameGu == "આજે")
                            }
                        } else {
                            ForecastDay("સોમ", "32°", Icons.Default.WbSunny)
                            ForecastDay("મંગળ", "31°", Icons.Default.Cloud)
                            ForecastDay("બુધ", "33°", Icons.Default.WaterDrop)
                            ForecastDay("ગુરુ", "35°", Icons.Default.WbSunny)
                            ForecastDay("શુક્ર", "34°", Icons.Default.WbCloudy)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))


            if (data.warningDescGu.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = WarningOrange),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.Videocam, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(data.warningTitleGu.ifEmpty { "ખેડૂત સંમતિ (Farmers Consensus)" }, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                            }
                            Text(
                                text = data.warningDescGu,
                                color = Color.Black,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            if (data.youtubeLastUpdated != null) {
                                Text(
                                    text = "છેલ્લે અપડેટ: ${data.youtubeLastUpdated}",
                                    color = Color.DarkGray,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ForecastDay(day: String, temp: String, icon: ImageVector, isToday: Boolean = false) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = if (isToday) Modifier.background(Color(0x33FFFFFF), RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 8.dp) else Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Text(day, color = if (isToday) Color.Yellow else Color.White, fontSize = 14.sp, fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Normal)
        Icon(icon, contentDescription = null, tint = if (isToday) Color.Yellow else Color.White, modifier = Modifier.size(32.dp).padding(vertical = 4.dp))
        Text(temp, color = if (isToday) Color.Yellow else Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CropHealthCard(data: HomeDashboardData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().background(CardBrown).padding(16.dp)) {
                Column {
                    Text("પાક સ્વાસ્થ્ય નિરીક્ષણ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("(Satellite Crop Health Monitor)", color = Color.LightGray, fontSize = 12.sp)
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clipToBounds(),
                contentAlignment = Alignment.Center
            ) {
                if (data.satelliteImageUrl != null) {
                    coil.compose.AsyncImage(
                        model = data.satelliteImageUrl,
                        contentDescription = "Real Satellite NDVI Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        filterQuality = FilterQuality.None
                    )
                } else {

                    PixelatedHeatmapBackground()
                }

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(HealthGreen))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("LIVE SCAN", fontWeight = FontWeight.Bold, color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("પાક સ્વાસ્થ્ય: ${data.cropHealthStatusGu}", color = PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                        Text("(Crop Health: ${data.cropHealthStatusEn})", color = Color.Gray, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .background(LightGreenBg, RoundedCornerShape(16.dp))
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text("NDVI: ${data.ndviValue}", color = PrimaryGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().background(Color(0xFF00C853)).padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("છેલ્લું સ્કેન (Last Scan)", color = Color.White, fontSize = 12.sp)
                    Text(data.lastScanDate, color = Color.White, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("આગળું સ્કેન (Next Scan)", color = Color.White, fontSize = 12.sp)
                    Text(data.nextScanDate, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun IrrigationAdvisorCard(viewModel: com.example.krishivani.viewmodel.HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val irrigationState by viewModel.irrigationState.collectAsState()

    var selectedSoilType by rememberSaveable { mutableStateOf("Black Soil") }
    var selectedCropStage by rememberSaveable { mutableStateOf("Sowing") }
    var expanded by remember { mutableStateOf(false) }
    var lastWateringDate by rememberSaveable { mutableStateOf("") }

    val crops = listOf(
        "મગફળી (Groundnut)", "જીરું (Cumin)", "દિવેલા/એરંડા (Castor)",
        "ઘઉં (Wheat)", "કપાસ (Cotton)", "ડુંગળી (Onion)",
        "ધાણા (Coriander)", "સોયાબીન (Soyabean)", "તલ (Sesame)"
    )
    var selectedCrop by rememberSaveable { mutableStateOf(crops[0]) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val calendar = java.util.Calendar.getInstance()
    val year = calendar.get(java.util.Calendar.YEAR)
    val month = calendar.get(java.util.Calendar.MONTH)
    val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

    val datePickerDialog = android.app.DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDay ->
            lastWateringDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
        }, year, month, day
    )

    LaunchedEffect(selectedCrop, selectedSoilType, selectedCropStage, lastWateringDate) {
        viewModel.fetchIrrigationAdvice(selectedCrop, selectedCropStage, selectedSoilType, lastWateringDate)
    }

    val days = irrigationState?.daysToNextIrrigation ?: 4
    val adviceGu = irrigationState?.adviceGu ?: "માહિતી મેળવી રહ્યા છીએ..."
    val adviceEn = irrigationState?.adviceEn ?: "Fetching data..."

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {

            Box(modifier = Modifier.fillMaxWidth().background(WeatherBlue).padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WaterDrop, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("ચાસ પિયત સલાહકાર", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("(Furrow Irrigation Advisor)", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text("જમીનનો પ્રકાર (Soil Type)", fontWeight = FontWeight.Bold, color = Color.DarkGray, modifier = Modifier.padding(bottom = 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val soils = listOf(
                        "Black Soil" to "કાળી જમીન",
                        "Sandy Soil" to "ગોરાડુ જમીન",
                        "Alluvial Soil" to "કાંપવાળી જમીન",
                        "Saline Soil" to "ખારી જમીન"
                    )
                    soils.forEach { (en, gu) ->
                        val isSelected = selectedSoilType == en
                        Box(
                            modifier = Modifier
                                .background(if (isSelected) SoilBrown else SandyBg, RoundedCornerShape(12.dp))
                                .clickable { selectedSoilType = en }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(gu, color = if (isSelected) Color.White else Color.DarkGray, fontWeight = FontWeight.Bold)
                                Text("($en)", color = if (isSelected) Color.LightGray else Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("પાકનો તબક્કો (Crop Stage)", fontWeight = FontWeight.Bold, color = Color.DarkGray, modifier = Modifier.padding(bottom = 8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val stages = listOf("Sowing" to "વાવણી", "Growth" to "વૃદ્ધિ", "Flowering" to "ફૂલ")
                    stages.forEach { (en, gu) ->
                        val isSelected = selectedCropStage == en
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (isSelected) Color(0xFF00C853) else SandyBg, RoundedCornerShape(12.dp))
                                .clickable { selectedCropStage = en }
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(gu, color = if (isSelected) Color.White else Color.DarkGray, fontWeight = FontWeight.Bold)
                                Text("($en)", color = if (isSelected) Color.White else Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))


                Card(
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFFFCC80), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(0.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFDF2E3))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(56.dp).background(Color(0xFFFF6D00), RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Water, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("આગામી ચાસ પિયત", fontWeight = FontWeight.Bold, color = Color.DarkGray, fontSize = 16.sp)
                                Text("(Next Furrow Irrigation Turn)", color = Color.Gray, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))


                        Text("તમારો પાક પસંદ કરો (Select Your Crop)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.DarkGray, modifier = Modifier.padding(bottom = 8.dp))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White, RoundedCornerShape(12.dp))
                                    .border(1.dp, Color(0xFFFF9800), RoundedCornerShape(12.dp))
                                    .clickable { expanded = true }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(selectedCrop, fontWeight = FontWeight.Bold, color = Color.Black)
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color(0xFFFF6D00))
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.fillMaxWidth(0.85f).background(Color.White)
                            ) {
                                crops.forEach { crop ->
                                    DropdownMenuItem(
                                        text = { Text(crop, color = if (crop == selectedCrop) WeatherBlue else Color.Black) },
                                        onClick = {
                                            selectedCrop = crop
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("છેલ્લું પિયત ક્યારે આપ્યું? (Last Watering Date)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.DarkGray, modifier = Modifier.padding(bottom = 8.dp))
                        OutlinedTextField(
                            value = lastWateringDate,
                            onValueChange = {},
                            readOnly = true,
                            placeholder = { Text("તારીખ પસંદ કરો (Select Date)") },
                            modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.WaterDrop, contentDescription = null, tint = Color(0xFFFF9800)) },
                            trailingIcon = {
                                IconButton(onClick = { datePickerDialog.show() }) {
                                    Icon(Icons.Default.CalendarToday, contentDescription = "Select Date", tint = Color(0xFFFF9800))
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))


                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(vertical = 24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(days.toString(), color = Color(0xFFD84315), fontWeight = FontWeight.Bold, fontSize = 48.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("દિવસ પછી", color = Color(0xFFD84315), fontWeight = FontWeight.Bold, fontSize = 36.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(if (days == 0) "આજે જ પિયત આપો" else "પિયત આપો", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF37474F))
                                Text(if (days == 0) "(Irrigate today)" else "(Irrigate after $days days)", color = Color.Gray, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))


                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE8F5E9), RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFF4CAF50), RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.Top) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF4CAF50)).padding(top = 6.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(adviceGu, color = Color(0xFF1B5E20), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("($adviceEn)", color = Color(0xFF2E7D32), fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("છેલ્લું ચાસ પિયત:", color = Color.Gray, fontSize = 12.sp)
                            val displayDate = if (lastWateringDate.isNotEmpty()) lastWateringDate else "માહિતી નથી (N/A)"
                            Text(displayDate, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.DarkGray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OfficialAdvisoriesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().background(PrimaryGreen).padding(16.dp)) {
                Column {
                    Text("JAU સલાહ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("જુનાગઢ કૃષિ યુનિવર્સિટી (Official Advisories)", color = Color.LightGray, fontSize = 12.sp)
                }
            }
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AdvisoryItem("કપાસમાં ગુલાબી ઈયળ નિયંત્રણ", "Pink Bollworm Control in Cotton", "જંતુ નિયંત્રણ", "૨૫ મે ૨૦૨૬")
                AdvisoryItem("મગફળીમાં ટીકા રોગ વ્યવસ્થાપન", "Tikka Disease Management in Groundnut", "રોગ વ્યવસ્થાપન", "૨૩ મે ૨૦૨૬")
            }
        }
    }
}

@Composable
fun AdvisoryItem(titleGu: String, titleEn: String, categoryGu: String, dateGu: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).background(PrimaryGreen, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Article, contentDescription = null, tint = Color.White)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(titleGu, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(titleEn, color = Color.Gray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.background(Color(0xFFFFCDD2), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text(categoryGu, color = Color.Red, fontSize = 10.sp)
                    }
                    Text(dateGu, color = Color.Gray, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(WarningOrange, RoundedCornerShape(8.dp))
                    .clickable {  },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Download, contentDescription = "Download", tint = Color.White)
            }
        }
    }
}

@Composable
fun PixelatedHeatmapBackground() {
    val colors = listOf(
        Color(0xFF4CAF50),
        Color(0xFF81C784),
        Color(0xFF388E3C),
        Color(0xFFAED581),
        Color(0xFFFFD54F)
    )


    Column(modifier = Modifier.fillMaxSize()) {
        for (i in 0 until 10) {
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                for (j in 0 until 20) {

                    val colorIndex = (i * 7 + j * 13) % colors.size
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().background(colors[colorIndex]))
                }
            }
        }
    }
}

@Composable
fun YoutubeAdvisoryCard(tips: List<String>) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFE53935)).padding(16.dp)) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Videocam, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("પરેશ ગોસ્વામીની ખેતી સલાહ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Text("YouTube એનાલિસિસ (Farming Tips)", color = Color.LightGray, fontSize = 12.sp)
                }
            }
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                tips.forEach { tip ->
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(16.dp).padding(top = 4.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(tip, fontSize = 14.sp, color = Color.DarkGray)
                    }
                }
            }
        }
    }
}
