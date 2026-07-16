package com.example.krishivani.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.krishivani.ui.theme.*
import com.example.krishivani.viewmodel.MarketViewModel

val regions = listOf("સૌરાષ્ટ્ર", "ઉત્તર ગુજરાત", "મધ્ય ગુજરાત", "દક્ષિણ ગુજરાત", "કચ્છ")
val districtMap = mapOf(
    "સૌરાષ્ટ્ર" to listOf("રાજકોટ (Rajkot)", "જુનાગઢ (Junagadh)", "અમરેલી (Amreli)", "ભાવનગર (Bhavnagar)", "સુરેન્દ્રનગર (Surendranagar)", "જામનગર (Jamnagar)", "પોરબંદર (Porbandar)", "મોરબી (Morbi)", "બોટાદ (Botad)", "ગીર સોમનાથ (Gir Somnath)", "દેવભૂમિ દ્વારકા (Devbhumi Dwarka)"),
    "ઉત્તર ગુજરાત" to listOf("ગાંધીનગર (Gandhinagar)", "મહેસાણા (Mehsana)", "પાટણ (Patan)", "બનાસકાંઠા (Banaskantha)", "સાબરકાંઠા (Sabarkantha)", "અરવલ્લી (Aravalli)"),
    "મધ્ય ગુજરાત" to listOf("અમદાવાદ (Ahmedabad)", "ખેડા (Kheda)", "આણંદ (Anand)", "પંચમહાલ (Panchmahal)", "મહીસાગર (Mahisagar)", "દાહોદ (Dahod)", "વડોદરા (Vadodara)", "છોટા ઉદેપુર (Chhota Udaipur)"),
    "દક્ષિણ ગુજરાત" to listOf("સુરત (Surat)", "નવસારી (Navsari)", "વલસાડ (Valsad)", "ડાંગ (Dang)", "તાપી (Tapi)", "ભરૂચ (Bharuch)", "નર્મદા (Narmada)"),
    "કચ્છ" to listOf("કચ્છ (Kutch)")
)
val yardMap = mapOf(

    "રાજકોટ (Rajkot)" to listOf("રાજકોટ બેડી APMC", "ગોંડલ APMC", "જસદણ APMC", "ધોરાજી APMC", "જેતપુર APMC", "ઉપલેટા APMC"),
    "જુનાગઢ (Junagadh)" to listOf("જુનાગઢ APMC", "વિસાવદર APMC", "માણાવદર APMC"),
    "અમરેલી (Amreli)" to listOf("અમરેલી APMC", "સાવરકુંડલા APMC", "બાબરા APMC"),
    "ભાવનગર (Bhavnagar)" to listOf("ભાવનગર APMC", "મહુવા APMC", "પાલીતાણા APMC"),
    "સુરેન્દ્રનગર (Surendranagar)" to listOf("સુરેન્દ્રનગર APMC", "વઢવાણ APMC", "હળવદ APMC"),
    "જામનગર (Jamnagar)" to listOf("જામનગર APMC", "ધ્રોલ APMC", "જામજોધપુર APMC"),
    "પોરબંદર (Porbandar)" to listOf("પોરબંદર APMC"),
    "મોરબી (Morbi)" to listOf("મોરબી APMC", "વાંકાનેર APMC"),
    "બોટાદ (Botad)" to listOf("બોટાદ APMC"),
    "ગીર સોમનાથ (Gir Somnath)" to listOf("ઉના APMC", "વેરાવળ APMC", "તાલાલા APMC"),
    "દેવભૂમિ દ્વારકા (Devbhumi Dwarka)" to listOf("ખંભાળિયા APMC", "ભાણવડ APMC"),


    "ગાંધીનગર (Gandhinagar)" to listOf("દહેગામ APMC", "માણસા APMC", "કલોલ APMC"),
    "મહેસાણા (Mehsana)" to listOf("ઊંઝા APMC", "મહેસાણા APMC", "વિસનગર APMC", "કડી APMC"),
    "પાટણ (Patan)" to listOf("પાટણ APMC", "હારીજ APMC", "રાધનપુર APMC"),
    "બનાસકાંઠા (Banaskantha)" to listOf("ડીસા APMC", "પાલનપુર APMC", "થરાદ APMC", "ધાનેરા APMC"),
    "સાબરકાંઠા (Sabarkantha)" to listOf("હિંમતનગર APMC", "ઈડર APMC", "પ્રાંતિજ APMC"),
    "અરવલ્લી (Aravalli)" to listOf("મોડાસા APMC", "બાયડ APMC"),


    "અમદાવાદ (Ahmedabad)" to listOf("અમદાવાદ APMC", "બાવળા APMC", "સાણંદ APMC", "ધોળકા APMC"),
    "ખેડા (Kheda)" to listOf("નડીયાદ APMC", "કપડવંજ APMC", "મહેમદાવાદ APMC"),
    "આણંદ (Anand)" to listOf("આણંદ APMC", "ઉમરેઠ APMC", "પેટલાદ APMC"),
    "પંચમહાલ (Panchmahal)" to listOf("ગોધરા APMC", "હાલોલ APMC"),
    "મહીસાગર (Mahisagar)" to listOf("લુણાવાડા APMC", "બાલાસિનોર APMC"),
    "દાહોદ (Dahod)" to listOf("દાહોદ APMC", "ઝાલોદ APMC"),
    "વડોદરા (Vadodara)" to listOf("વડોદરા APMC", "સાવલી APMC", "પાદરા APMC"),
    "છોટા ઉદેપુર (Chhota Udaipur)" to listOf("છોટા ઉદેપુર APMC", "બોડેલી APMC"),


    "સુરત (Surat)" to listOf("સુરત APMC", "બારડોલી APMC", "માંડવી (સુરત) APMC"),
    "નવસારી (Navsari)" to listOf("નવસારી APMC", "બીલીમોરા APMC"),
    "વલસાડ (Valsad)" to listOf("વલસાડ APMC", "ધરમપુર APMC"),
    "ડાંગ (Dang)" to listOf("આહવા APMC"),
    "તાપી (Tapi)" to listOf("વ્યારા APMC", "નિઝર APMC"),
    "ભરૂચ (Bharuch)" to listOf("ભરૂચ APMC", "અંકલેશ્વર APMC", "જંબુસર APMC"),
    "નર્મદા (Narmada)" to listOf("રાજપીપળા APMC"),


    "કચ્છ (Kutch)" to listOf("ભુજ APMC", "અંજાર APMC", "રાપર APMC", "ભચાઉ APMC", "માંડવી (કચ્છ) APMC")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketScreen(onNavigate: (String) -> Unit, viewModel: MarketViewModel = viewModel()) {
    var selectedRegion by remember { mutableStateOf("સૌરાષ્ટ્ર") }
    val districts = districtMap[selectedRegion] ?: listOf()
    var selectedDistrict by remember { mutableStateOf(if (districts.isNotEmpty()) districts[0] else "Unknown") }


    LaunchedEffect(selectedRegion) {
        val newDistricts = districtMap[selectedRegion] ?: listOf()
        if (newDistricts.isNotEmpty() && !newDistricts.contains(selectedDistrict)) {
            selectedDistrict = newDistricts[0]
        }
    }

    val yards = yardMap[selectedDistrict] ?: listOf("તમામ યાર્ડ (All Yards)")
    var selectedYard by remember { mutableStateOf(if (yards.isNotEmpty()) yards[0] else "Unknown") }


    LaunchedEffect(selectedDistrict) {
        val newYards = yardMap[selectedDistrict] ?: listOf("તમામ યાર્ડ (All Yards)")
        if (newYards.isNotEmpty() && !newYards.contains(selectedYard)) {
            selectedYard = newYards[0]
        }
    }


    LaunchedEffect(selectedDistrict, selectedYard) {
        viewModel.fetchMarketData(selectedDistrict, selectedYard)
    }

    val marketData by viewModel.marketData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var searchQuery by remember { mutableStateOf("") }


    Scaffold(
        topBar = { HomeTopBar() },
        bottomBar = { AppBottomNavigation(currentRoute = "market", onNavigate = onNavigate) },
        containerColor = Color(0xFFF9F9F9)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("પાક અથવા માર્કેટ શોધો... (Search Crop or Mandi)", color = Color.Gray, fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.LightGray,
                        focusedBorderColor = PrimaryGreen
                    )
                )
            }


            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MarketDropdown(
                        label = "જીલ્લો (District)",
                        options = districts,
                        selected = selectedDistrict,
                        onSelected = { selectedDistrict = it },
                        modifier = Modifier.weight(1f)
                    )
                    MarketDropdown(
                        label = "યાર્ડ (Yard)",
                        options = yards,
                        selected = selectedYard,
                        onSelected = { selectedYard = it },
                        modifier = Modifier.weight(1f)
                    )
                }
            }


            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(regions) { region ->
                        val isSelected = region == selectedRegion
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .background(if (isSelected) PrimaryGreen else Color.White)
                                .border(1.dp, if (isSelected) PrimaryGreen else Color.LightGray, RoundedCornerShape(24.dp))
                                .clickable { selectedRegion = region }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(region, color = if (isSelected) Color.White else Color.DarkGray, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }


            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryGreen)
                ) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.BarChart, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("આજના બજાર ભાવ", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                                Text("All Gujarat APMC Live Rates", color = Color.White, fontSize = 12.sp)
                            }
                        }
                        Box(modifier = Modifier.background(Color.Red, RoundedCornerShape(16.dp)).padding(horizontal = 12.dp, vertical = 4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.White))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("LIVE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }


            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryGreen)
                    }
                }
            } else if (errorMessage != null) {
                item {
                    Text(text = errorMessage ?: "", color = Color.Red, modifier = Modifier.padding(16.dp))
                }
            } else if (marketData.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("કોઈ ડેટા ઉપલબ્ધ નથી", color = Color.Gray)
                    }
                }
            } else {
                val filteredData = if (searchQuery.isBlank()) {
                    marketData
                } else {
                    val query = searchQuery.lowercase().trim()
                    marketData.filter {
                        val cropEn = it.cropName.substringAfter("(").removeSuffix(")").lowercase().trim()
                        val cropGu = it.cropName.substringBefore("(").trim()

                        it.cropName.lowercase().contains(query) ||
                        it.yard.lowercase().contains(query) ||
                        it.district.lowercase().contains(query) ||

                        (query.length >= 3 && levenshteinDistance(cropEn, query) <= 2) ||

                        cropEn.startsWith(query) ||
                        cropGu.startsWith(query)
                    }
                }

                if (filteredData.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("કોઈ પરિણામ મળ્યું નથી (No results found)", color = Color.Gray)
                        }
                    }
                } else {
                    val groupedData = filteredData.groupBy { it.cropName }
                    groupedData.forEach { (cropName, prices) ->
                    val themeColor = when {
                        cropName.contains("કપાસ") || cropName.contains("Cotton") -> CottonRed
                        cropName.contains("જીરું") || cropName.contains("Cumin") -> CuminYellow
                        else -> PrimaryGreen
                    }
                    val icon = when {
                        cropName.contains("કપાસ") || cropName.contains("Cotton") -> Icons.Default.FilterVintage
                        cropName.contains("જીરું") || cropName.contains("Cumin") -> Icons.Default.Grass
                        else -> Icons.Default.Eco
                    }

                    item {
                        CropMarketSection(cropName, "", themeColor, icon) {
                            prices.forEach { price ->
                                val rateStr = "${price.minPrice} - ${price.maxPrice}"
                                val isUp = price.priceTrend.startsWith("+")
                                val trendColor = if (isUp) TrendGreen else if (price.priceTrend.startsWith("-")) TrendRed else Color.Gray

                                MarketRateCard(
                                    yardGu = price.yard,
                                    yardEn = price.district,
                                    rate = rateStr,
                                    arrival = price.arrivals,
                                    time = "Live",
                                    trend = price.priceTrend,
                                    trendColor = trendColor,
                                    isUp = isUp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun MarketDropdown(label: String, options: List<String>, selected: String, onSelected: (String) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .border(1.dp, PrimaryGreen, RoundedCornerShape(12.dp))
                    .clickable { expanded = true }
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(selected, fontWeight = FontWeight.Bold, color = PrimaryGreen, fontSize = 12.sp, maxLines = 1)
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = PrimaryGreen)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White)
            ) {
                options.forEach { opt ->
                    DropdownMenuItem(
                        text = { Text(opt, color = if (opt == selected) WeatherBlue else Color.Black) },
                        onClick = {
                            onSelected(opt)
                            expanded = false
                        }
                    )
                }
            }
        }
        Text(label, color = Color.Gray, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp, start = 4.dp))
    }
}

@Composable
fun FavoriteYardItem(titleGu: String, titleEn: String) {
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = FavoriteYellow, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(titleGu, fontWeight = FontWeight.Bold, color = Color.DarkGray, fontSize = 14.sp)
                    Text(titleEn, color = Color.Gray, fontSize = 12.sp)
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}

@Composable
fun CropMarketSection(titleGu: String, titleEn: String, themeColor: Color, icon: androidx.compose.ui.graphics.vector.ImageVector, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier.fillMaxWidth().background(themeColor).padding(16.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(titleGu, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                            Text("($titleEn)", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                content()
            }
        }
    }
}

@Composable
fun MarketRateCard(yardGu: String, yardEn: String, rate: String, arrival: String, time: String, trend: String, trendColor: Color, isUp: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(yardGu, fontWeight = FontWeight.Bold, color = Color.DarkGray, fontSize = 14.sp)
                }
            }
            Text("($yardEn)", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("ભાવ (Rate)", color = Color.Gray, fontSize = 10.sp)
                    Text(rate, fontWeight = FontWeight.Bold, color = Color(0xFFB71C1C), fontSize = 14.sp)
                }
                Column {
                    Text("આવક (Arrival)", color = Color.Gray, fontSize = 10.sp)
                    Text(arrival, fontWeight = FontWeight.Bold, color = Color.DarkGray, fontSize = 14.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("અપડેટ", color = Color.Gray, fontSize = 10.sp)
                    Text(time, fontWeight = FontWeight.Bold, color = Color.DarkGray, fontSize = 12.sp)
                }
            }
        }
    }
}

fun levenshteinDistance(a: String, b: String): Int {
    if (a == b) return 0
    if (a.isEmpty()) return b.length
    if (b.isEmpty()) return a.length

    var v0 = IntArray(b.length + 1) { it }
    var v1 = IntArray(b.length + 1)

    for (i in 0 until a.length) {
        v1[0] = i + 1
        for (j in 0 until b.length) {
            val cost = if (a[i] == b[j]) 0 else 1
            v1[j + 1] = minOf(v1[j] + 1, v0[j + 1] + 1, v0[j] + cost)
        }
        val temp = v0
        v0 = v1
        v1 = temp
    }
    return v0[b.length]
}
