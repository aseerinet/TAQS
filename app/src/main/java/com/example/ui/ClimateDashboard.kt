package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ForecastDay
import com.example.data.ForecastMonth
import com.example.data.LocationData
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClimateDashboard(viewModel: ClimateAppViewModel) {
    val selectedLocation by viewModel.selectedLocation.collectAsState()
    val activeLayer by viewModel.activeLayer.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val allLocations by viewModel.allLocations.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val aiReportState by viewModel.aiReportState.collectAsState()
    val updateNonce by viewModel.updateNonce.collectAsState()

    val latInput by viewModel.customLat.collectAsState()
    val lonInput by viewModel.customLon.collectAsState()

    val focusManager = LocalFocusManager.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .windowInsetsPadding(WindowInsets.statusBars)
            .testTag("climate_dashboard"),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // --- 1. Top Integrated Hero Bar (Arabic Branding & Search) ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, BorderSlate)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "أطلس المناخ العربي",
                                color = TextPrimary,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.End)
                            )
                            Text(
                                "خرائط حية للأرصاد وتقارير المناخ الذكية",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                        IconButton(
                            onClick = { viewModel.triggerRadarRefresh() },
                            modifier = Modifier
                                .background(SlateMedium, CircleShape)
                                .size(44.dp)
                                .testTag("refresh_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "تحديث رادار المناخ",
                                tint = if (isRefreshing) ClimatePrimary else ClimateSecondary,
                                modifier = if (isRefreshing) {
                                    Modifier.graphicsLayer {
                                        rotationZ += 360f
                                    }
                                } else Modifier
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Localization search field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        placeholder = {
                            Text(
                                "ابحث عن مدينة أو محطة مناخية (أبها، الرياض، دبي...)",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.End
                            )
                        },
                        trailingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = ClimatePrimary)
                        },
                        leadingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "مسح التصفية", tint = TextSecondary)
                                }
                            }
                        },
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End, color = TextPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("location_search"),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = ClimatePrimary,
                            unfocusedBorderColor = BorderSlate,
                            focusedContainerColor = SlateMedium,
                            unfocusedContainerColor = SlateMedium
                        )
                    )
                }
            }
        }

        // --- 2. Advanced Dynamic Climate Radar Map (Custom Vector Elements) ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    "خريطة المناخ التفاعلية الحية",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(bottom = 8.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateDark),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.5.dp, BorderSlate)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Live vector weather map component
                        InteractiveMiddleEastRadar(
                            locations = allLocations,
                            selectedId = selectedLocation.id,
                            activeLayer = activeLayer,
                            updateNonce = updateNonce,
                            onLocationSelected = { location ->
                                viewModel.selectLocation(location)
                            }
                        )

                        // Top Active Radar Layer Tag overlay
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .border(1.dp, BorderSlate, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "طبقة الرادار: " + when(activeLayer) {
                                    ClimateLayer.RAIN -> "رادار الأمطار والبرق 🌧️"
                                    ClimateLayer.TEMP -> "خريطة الحرارة والحمل 🌡️"
                                    ClimateLayer.CLOUDS -> "تتبع الغطاء السحابي ☁️"
                                    ClimateLayer.WIND -> "مسارات الرياح واتجاهاتها 💨"
                                    ClimateLayer.HUMIDITY -> "الرطوبة النسبية والضباب 🌫️"
                                },
                                color = ClimatePrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Bottom layer tool controller buttons ("استخدم اسلوب الايقونات لادوات المناخ")
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(12.dp)
                                .background(CardSlate.copy(alpha = 0.92f), RoundedCornerShape(16.dp))
                                .border(1.dp, BorderSlate, RoundedCornerShape(16.dp))
                                .padding(6.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ClimateToolButton(
                                emoji = "🌫️",
                                label = "رطوبة",
                                isSelected = activeLayer == ClimateLayer.HUMIDITY,
                                tintColor = HumidityTeal,
                                onClick = { viewModel.setClimateLayer(ClimateLayer.HUMIDITY) }
                            )
                            ClimateToolButton(
                                emoji = "💨",
                                label = "رياح",
                                isSelected = activeLayer == ClimateLayer.WIND,
                                tintColor = WindCyan,
                                onClick = { viewModel.setClimateLayer(ClimateLayer.WIND) }
                            )
                            ClimateToolButton(
                                emoji = "☁️",
                                label = "سحب",
                                isSelected = activeLayer == ClimateLayer.CLOUDS,
                                tintColor = CloudWhite,
                                onClick = { viewModel.setClimateLayer(ClimateLayer.CLOUDS) }
                            )
                            ClimateToolButton(
                                emoji = "🌡️",
                                label = "حرارة",
                                isSelected = activeLayer == ClimateLayer.TEMP,
                                tintColor = TempWarm,
                                onClick = { viewModel.setClimateLayer(ClimateLayer.TEMP) }
                            )
                            ClimateToolButton(
                                emoji = "🌧️",
                                label = "أمطار",
                                isSelected = activeLayer == ClimateLayer.RAIN,
                                tintColor = RainBlue,
                                onClick = { viewModel.setClimateLayer(ClimateLayer.RAIN) }
                            )
                        }
                    }
                }
            }
        }

        // --- 3. Elevation & Meteorological Metrics of Selected Location ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardSlate),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, BorderSlate)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Location metadata header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Elevation pill ("الارتفاع عن سطح البحر بالمتر")
                            Row(
                                modifier = Modifier
                                    .background(SlateMedium, RoundedCornerShape(12.dp))
                                    .border(1.dp, BorderSlate, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "${selectedLocation.elevationMeters} م",
                                    color = ClimateSecondary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "🏔️",
                                    fontSize = 14.sp
                                )
                            }

                            // Dynamic Title & Flag description
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    selectedLocation.nameAr,
                                    color = TextPrimary,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "${selectedLocation.countryAr} · الإحداثيات: ${selectedLocation.latitude}°N , ${selectedLocation.longitude}°E",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Divider(color = BorderSlate, modifier = Modifier.padding(vertical = 12.dp))

                        // Meteorological details with dynamic styling
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Rain expectation mm & clouds
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Text("🌧️", fontSize = 22.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("الهطول المتوقع", fontSize = 11.sp, color = TextSecondary)
                                Text("${selectedLocation.rainRateMm} ملم", fontSize = 15.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                            }

                            // Wind directions & vector speeds ("الرياح واتجاهاتها")
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowUp,
                                    contentDescription = null,
                                    tint = WindCyan,
                                    modifier = Modifier.size(24.dp).graphicsLayer { rotationZ = selectedLocation.windDirectionDeg.toFloat() }
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("الرياح واتجاهاتها", fontSize = 11.sp, color = TextSecondary)
                                Text("${selectedLocation.windSpeedKmh} كم/س", fontSize = 15.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                                Text(selectedLocation.windDirectionAr, fontSize = 9.sp, color = ClimatePrimary, textAlign = TextAlign.Center)
                            }

                            // Humidity %
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Text("💧", fontSize = 22.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("الرطوبة النسبية", fontSize = 11.sp, color = TextSecondary)
                                Text("${selectedLocation.humidityPercent}%", fontSize = 15.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                            }

                            // Dynamic temperature
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (selectedLocation.temperature > 30f) "🥵" else "😎",
                                    fontSize = 24.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("درجة الحرارة", fontSize = 11.sp, color = TextSecondary)
                                Text(
                                    String.format("%.1f°م", selectedLocation.temperature),
                                    fontSize = 16.sp,
                                    color = if (selectedLocation.temperature > 30f) TempWarm else TempCold,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 4. Interactive Simulation & Geographic Coordinates Override ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, BorderSlate)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "الاعتماد الجغرافي وتحديد الموقع",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.End)
                    )
                    Text(
                        "أدخل إحداثيات خط العقد والارتفاع أو لتحديد المحطة الأقرب وتغيير التحديث",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.align(Alignment.End),
                        textAlign = TextAlign.End
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = lonInput,
                            onValueChange = { viewModel.customLon.value = it },
                            label = { Text("خط الطول°E", fontSize = 10.sp, color = TextSecondary) },
                            textStyle = LocalTextStyle.current.copy(color = TextPrimary),
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ClimatePrimary,
                                unfocusedBorderColor = BorderSlate,
                                focusedContainerColor = SlateMedium,
                                unfocusedContainerColor = SlateMedium
                            )
                        )
                        OutlinedTextField(
                            value = latInput,
                            onValueChange = { viewModel.customLat.value = it },
                            label = { Text("خط العرض°N", fontSize = 10.sp, color = TextSecondary) },
                            textStyle = LocalTextStyle.current.copy(color = TextPrimary),
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ClimatePrimary,
                                unfocusedBorderColor = BorderSlate,
                                focusedContainerColor = SlateMedium,
                                unfocusedContainerColor = SlateMedium
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Buttons to search coordinates or simulate auto-localization
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                val lat = latInput.toDoubleOrNull() ?: 24.7136
                                val lon = lonInput.toDoubleOrNull() ?: 46.6753
                                viewModel.simulateGpsLocation(lat, lon)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SlateLight),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("محاكاة الأقرب 📍", color = TextPrimary, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                // Simulating auto detection of client coordinates (like Sarawat mountain peaks!)
                                val randomLat = 18.2 + (0..4).random() * 0.1
                                val randomLon = 42.4 + (0..4).random() * 0.1
                                viewModel.simulateGpsLocation(randomLat, randomLon)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ClimatePrimary),
                            modifier = Modifier.weight(1.2f).testTag("gps_locate_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = SlateDark, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("موقعي الجغرافي 🛰️", color = SlateDark, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // --- 5. Forecast Period Selector & Charts Layout ("أزرار الأيام والأزمنة") ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Day / period switcher heading
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${selectedLocation.nameAr} - الجدول الزمني",
                        color = ClimatePrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "التوقعات والفترات الزمنية",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Toggles: Daily, Weekly, Monthly ("أزرار الايام والتواريخ")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SlateMedium, RoundedCornerShape(16.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PeriodTabButton(
                        label = "اليومي (ساعات)",
                        isSelected = selectedPeriod == ForecastPeriod.DAILY,
                        onClick = { viewModel.setForecastPeriod(ForecastPeriod.DAILY) }
                    )
                    PeriodTabButton(
                        label = "خلال أسبوع",
                        isSelected = selectedPeriod == ForecastPeriod.WEEKLY,
                        onClick = { viewModel.setForecastPeriod(ForecastPeriod.WEEKLY) }
                    )
                    PeriodTabButton(
                        label = "خلال شهر",
                        isSelected = selectedPeriod == ForecastPeriod.MONTHLY,
                        onClick = { viewModel.setForecastPeriod(ForecastPeriod.MONTHLY) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Chart and list dynamically rendering based on the selected period
                AnimatedContent(
                    targetState = selectedPeriod,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                    },
                    label = "forecast_content"
                ) { period ->
                    when (period) {
                        ForecastPeriod.DAILY -> {
                            Column {
                                Text(
                                    "الحالة الساعيّة لليوم الثاني",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    modifier = Modifier.align(Alignment.End).padding(bottom = 8.dp)
                                )
                                // Scrollable hours list
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(selectedLocation.dailyForecast) { hour ->
                                        Card(
                                            modifier = Modifier.width(95.dp),
                                            colors = CardDefaults.cardColors(containerColor = CardSlate),
                                            border = BorderStroke(1.dp, BorderSlate)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(10.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(hour.time, color = TextSecondary, fontSize = 11.sp)
                                                Text(hour.iconAr, fontSize = 24.sp, modifier = Modifier.padding(vertical = 4.dp))
                                                Text("${hour.temp.toInt()}°م", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Text(hour.conditionAr, color = ClimatePrimary, fontSize = 9.sp, textAlign = TextAlign.Center)
                                                 Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(top = 4.dp)
                                                ) {
                                                    Text("💧", fontSize = 9.sp)
                                                    Spacer(modifier = Modifier.width(2.dp))
                                                    Text("${hour.rainProbability}%", color = RainBlue, fontSize = 9.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        ForecastPeriod.WEEKLY -> {
                            // Render a gorgeous temperature bar chart and lists side-by-side
                            Column {
                                Text(
                                    "مخطط درجات الحرارة المتوقعة الأسبوع القادم",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    modifier = Modifier.align(Alignment.End).padding(bottom = 8.dp)
                                )

                                CustomPrecipitationBarChart(forecastDays = selectedLocation.weeklyForecast)

                                Spacer(modifier = Modifier.height(12.dp))

                                // Days list
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    selectedLocation.weeklyForecast.forEach { day ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = CardSlate),
                                            border = BorderStroke(0.5.dp, BorderSlate)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Min / Max temperature curves
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text("${day.minTemp.toInt()}°م", color = TempCold, fontSize = 13.sp)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Box(modifier = Modifier.width(30.dp).height(4.dp).background(color = BorderSlate, shape = CircleShape))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("${day.maxTemp.toInt()}°م", color = TempWarm, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                                }

                                                // Condition description with rain chance
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Column(horizontalAlignment = Alignment.End) {
                                                        Text(day.conditionAr, color = TextPrimary, fontSize = 12.sp)
                                                        if (day.rainProbability > 0) {
                                                            Text("احتمال مطر: ${day.rainProbability}%", color = RainBlue, fontSize = 10.sp)
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(day.iconAr, fontSize = 22.sp)
                                                }

                                                // Day name
                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text(day.dayNameAr, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                                    Text(day.dateString, color = TextSecondary, fontSize = 10.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        ForecastPeriod.MONTHLY -> {
                            Column {
                                Text(
                                    "منظور المناخ الشهري والارتفاع عن المتوسط العام (30 سنة)",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    modifier = Modifier.align(Alignment.End).padding(bottom = 8.dp)
                                )

                                CustomMonthlyAnomalyChart(forecastMonths = selectedLocation.monthlyForecast)

                                Spacer(modifier = Modifier.height(12.dp))

                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    selectedLocation.monthlyForecast.forEach { month ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = CardSlate),
                                            border = BorderStroke(0.5.dp, BorderSlate)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Monthly anomalies status
                                                Box(
                                                    modifier = Modifier
                                                        .background(if (month.expectedRainDays > 2) RainBlue.copy(alpha = 0.15f) else TempWarm.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text(
                                                        "الانحراف: ${month.anomalyScore}",
                                                        color = if (month.expectedRainDays > 2) RainBlue else TempWarm,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }

                                                // Condition details
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text("معدل الرطوبة: ${month.avgHumidity}%", color = TextSecondary, fontSize = 11.sp)
                                                    Text("${month.expectedRainDays} أيام ممطرة", color = ClimatePrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                                }

                                                // Month/Week segment name
                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text(month.weekNameAr, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                                    Text("متوسط حرارة ${month.avgTemp}°م", color = ClimateSecondary, fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 6. Climate Change Anomaly Fingerprint Card ("مؤشر التغير") ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    "مؤشر وبصمة التغير المناخي للمنطقة",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(bottom = 8.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardSlate),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.2.dp, ClimateSecondary.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "عالي الخطورة الحرارية ⛈️",
                                color = TempWarm,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(TempWarm.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                            Text(
                                "تحديث نموذجي تراكمي دولي",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Render climate warning details
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    "${selectedLocation.climateChangeIndex.vulnerabilityScore}%",
                                    color = TempWarm,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    "معدل التعرض للموجات الطارئة",
                                    fontSize = 10.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }

                            // Divider
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(55.dp)
                                    .background(BorderSlate)
                                    .align(Alignment.CenterVertically)
                            )

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1.3f)
                            ) {
                                Text(
                                    "${if(selectedLocation.climateChangeIndex.tempAnomaly > 0) "+" else ""}${selectedLocation.climateChangeIndex.tempAnomaly}°م",
                                    color = ClimateSecondary,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    "الانحراف الحراري عن متوسط 30 سنة",
                                    fontSize = 10.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Advisory notice of drought risk
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SlateMedium, RoundedCornerShape(12.dp))
                                .border(0.5.dp, BorderSlate, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "تصنيف التهديد المناخي الإقليمي:",
                                    color = ClimateSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                                Text(
                                    selectedLocation.climateChangeIndex.desertificationRisk,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Right,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 7. Gemini AI Smart Climate Report Consultation ("مدعم بتحليلات الطقس الذكية") ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "مدعوم بـ Gemini AI",
                        color = ClimatePrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "خبير المناخ والتحليلات الجغرافية",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardSlate),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.2.dp, ClimatePrimary.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { viewModel.generateAiReport() },
                                colors = ButtonDefaults.buttonColors(containerColor = SlateMedium),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp).testTag("ai_reanalyze_button")
                            ) {
                                Text("إعادة التحليل 🔄", fontSize = 10.sp, color = ClimatePrimary, fontWeight = FontWeight.Bold)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("مستشار المناخ الذكي المباشر", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("✨", fontSize = 16.sp)
                            }
                        }

                        Divider(color = BorderSlate, modifier = Modifier.padding(vertical = 12.dp))

                        // Dynamic Chat state from Gemini
                        when (val state = aiReportState) {
                            is AiReportState.Idle -> {
                                Text(
                                    "اضغط على زر التحديث أو اختر مدينة للبدء في توليد تقرير مناخي ذكي ومفصل عبر نماذج الذكاء الاصطناعي.",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            is AiReportState.Loading -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        CircularProgressIndicator(color = ClimatePrimary, strokeWidth = 3.dp)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            "جارِ تحليل جغرافية الموقع وارتفاع البحر والكتل السحابية عبر جميني...",
                                            color = ClimatePrimary,
                                            fontSize = 11.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }

                            is AiReportState.Success -> {
                                Text(
                                    text = state.report,
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    lineHeight = 20.sp,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("ai_report_content"),
                                    fontWeight = FontWeight.Normal
                                )
                            }

                            is AiReportState.Error -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(TempWarm.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("⚠️ خطأ في معالجة الاستشارة:", color = TempWarm, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(state.message, color = TextPrimary, fontSize = 12.sp, textAlign = TextAlign.Right)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Custom animated interactive vector map of the Middle East region
@Composable
fun InteractiveMiddleEastRadar(
    locations: List<LocationData>,
    selectedId: String,
    activeLayer: ClimateLayer,
    updateNonce: Int,
    onLocationSelected: (LocationData) -> Unit
) {
    // Pulse animation for selected station node
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 24f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radius_pulse"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha_pulse"
    )

    // Cloud drift offset animation
    val cloudOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 500f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cloud"
    )

    // Rain drop animation
    val rainDropShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rain"
    )

    // Wind wave animation
    val windWaveShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wind"
    )

    // Map bounds mapping coordinates perfectly to Cartesian plane
    val lonMin = 30.0
    val lonMax = 62.0
    val latMin = 14.0
    val latMax = 33.0

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(locations, updateNonce) {
                detectTapGestures { offset ->
                    // Find closest weather node clicked
                    var minDistance = Float.MAX_VALUE
                    var closestLoc: LocationData? = null

                    locations.forEach { loc ->
                        // Calculate expected node pixel
                        val pctX = (loc.longitude - lonMin) / (lonMax - lonMin)
                        val pctY = 1.0 - (loc.latitude - latMin) / (latMax - latMin)
                        val nodeX = (pctX * size.width).toFloat()
                        val nodeY = (pctY * size.height).toFloat()

                        val dist = (offset.x - nodeX) * (offset.x - nodeX) + (offset.y - nodeY) * (offset.y - nodeY)
                        if (dist < minDistance && dist < 1200f) { // Touch search radius 35dp
                            minDistance = dist
                            closestLoc = loc
                        }
                    }

                    closestLoc?.let { onLocationSelected(it) }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Draw stylized Middle East Map Base Layout
            drawStylizedMiddleEastCoastLine(w, h)

            // 2. Draw meteorological overlay layers depending on choice
            when (activeLayer) {
                ClimateLayer.TEMP -> {
                    // Contoured radial heating glow simulating thermal dynamic currents
                    locations.forEach { loc ->
                        // Draw localized heater cores
                        val pctX = (loc.longitude - lonMin) / (lonMax - lonMin)
                        val pctY = 1.0 - (loc.latitude - latMin) / (latMax - latMin)
                        val px = (pctX * w).toFloat()
                        val py = (pctY * h).toFloat()

                        val tempSeverity = (loc.temperature - 15f) / 35f // Normalized temp factor
                        val glowSize = (45f + tempSeverity * 65f).dp.toPx()
                        
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    TempWarm.copy(alpha = 0.28f * tempSeverity.coerceIn(0.2f, 1f)),
                                    TempWarm.copy(alpha = 0.05f),
                                    Color.Transparent
                                ),
                                center = Offset(px, py),
                                radius = glowSize
                            ),
                            radius = glowSize,
                            center = Offset(px, py)
                        )
                    }
                }

                ClimateLayer.RAIN -> {
                    // Rain droplets flowing within localized clouds
                    locations.forEach { loc ->
                        if (loc.rainRateMm > 0 || loc.cloudsCoverage > 50) {
                            val pctX = (loc.longitude - lonMin) / (lonMax - lonMin)
                            val pctY = 1.0 - (loc.latitude - latMin) / (latMax - latMin)
                            val px = (pctX * w).toFloat()
                            val py = (pctY * h).toFloat()

                            // Draw rain overlay clouds
                            drawCircle(
                                color = RainBlue.copy(alpha = 0.12f),
                                radius = 40.dp.toPx(),
                                center = Offset(px, py)
                            )

                            // Falling dynamic raindrops within cloud coverage
                            val dropsCount = 5
                            for (i in 0 until dropsCount) {
                                val dx = px - 30f + (i * 15f)
                                val dy = py - 20f + ((rainDropShift + (i * 20)) % 60)
                                drawLine(
                                    color = RainBlue.copy(alpha = 0.7f),
                                    start = Offset(dx, dy),
                                    end = Offset(dx - 3f, dy + 10f),
                                    strokeWidth = 2f
                                )
                            }
                        }
                    }
                }

                ClimateLayer.CLOUDS -> {
                    // Draw flowing oblong soft cloud structures traveling West-to-East
                    val cloudScale = 60.dp.toPx()
                    val positions = listOf(
                        Offset(0.25f * w + cloudOffset, 0.35f * h),
                        Offset(-0.1f * w + cloudOffset, 0.55f * h),
                        Offset(0.45f * w + cloudOffset, 0.75f * h),
                        Offset(-0.5f * w + cloudOffset, 0.2f * h),
                        Offset(0.1f * w + cloudOffset, 0.15f * h)
                    )

                    positions.forEach { p ->
                        // Loop around the viewport bounds
                        val loopedX = p.x % (w + 120.dp.toPx()) - 60.dp.toPx()
                        
                        drawOval(
                            color = CloudWhite.copy(alpha = 0.16f),
                            topLeft = Offset(loopedX, p.y),
                            size = Size(cloudScale * 2f, cloudScale * 0.8f)
                        )
                    }
                }

                ClimateLayer.WIND -> {
                    // Animated blowing wind streamlines and direction arrows ("الرياح واتجاهاتها")
                    locations.forEach { loc ->
                        val pctX = (loc.longitude - lonMin) / (lonMax - lonMin)
                        val pctY = 1.0 - (loc.latitude - latMin) / (latMax - latMin)
                        val px = (pctX * w).toFloat()
                        val py = (pctY * h).toFloat()

                        // Calculate polar vectors for wind flow
                        val radian = Math.toRadians(loc.windDirectionDeg.toDouble() - 180.0) // Flowing forward
                        val length = 35.dp.toPx()
                        
                        // Flow simulation moving point
                        val flowOffsetFactor = (windWaveShift / 100f) * length
                        val fx = px + cos(radian).toFloat() * flowOffsetFactor
                        val fy = py + sin(radian).toFloat() * flowOffsetFactor

                        // Draw wind stream backing line
                        drawLine(
                            color = WindCyan.copy(alpha = 0.25f),
                            start = Offset(px - cos(radian).toFloat() * length, py - sin(radian).toFloat() * length),
                            end = Offset(px + cos(radian).toFloat() * length, py + sin(radian).toFloat() * length),
                            strokeWidth = 2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 15f), 0f)
                        )

                        // Draw moving wind particle ring
                        drawCircle(
                            color = WindCyan,
                            radius = 4f,
                            center = Offset(fx, fy)
                        )
                    }
                }

                ClimateLayer.HUMIDITY -> {
                    // Soft mist / dew glows centering water coasts
                    locations.forEach { loc ->
                        if (loc.humidityPercent > 65) {
                            val pctX = (loc.longitude - lonMin) / (lonMax - lonMin)
                            val pctY = 1.0 - (loc.latitude - latMin) / (latMax - latMin)
                            val px = (pctX * w).toFloat()
                            val py = (pctY * h).toFloat()

                            val humidityFactor = (loc.humidityPercent - 50f) / 50f
                            val radius = (35f + humidityFactor * 40f).dp.toPx()

                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        HumidityTeal.copy(alpha = 0.25f * humidityFactor),
                                        HumidityTeal.copy(alpha = 0.02f),
                                        Color.Transparent
                                    ),
                                    center = Offset(px, py),
                                    radius = radius
                                ),
                                radius = radius,
                                center = Offset(px, py)
                            )
                        }
                    }
                }
            }

            // 3. Draw Station Beacon Beacons & Text tags
            locations.forEach { loc ->
                val pctX = (loc.longitude - lonMin) / (lonMax - lonMin)
                val pctY = 1.0 - (loc.latitude - latMin) / (latMax - latMin)
                val px = (pctX * w).toFloat()
                val py = (pctY * h).toFloat()

                val isSelected = loc.id == selectedId

                if (isSelected) {
                    // Pulse ring on focused spot
                    drawCircle(
                        color = ClimatePrimary.copy(alpha = pulseAlpha),
                        radius = pulseRadius * 1.5f,
                        center = Offset(px, py),
                        style = Stroke(width = 3f)
                    )
                }

                // Node core circle representation
                drawCircle(
                    color = if (isSelected) ClimatePrimary else BorderSlate,
                    radius = 7f,
                    center = Offset(px, py)
                )

                drawCircle(
                    color = if (isSelected) Color.White else TextSecondary,
                    radius = 3.5f,
                    center = Offset(px, py)
                )

                // Render tiny text marker with city label
                drawContext.canvas.nativeCanvas.drawText(
                    loc.nameAr,
                    px - 15f,
                    py - 12f,
                    android.graphics.Paint().apply {
                        color = if (isSelected) android.graphics.Color.WHITE else android.graphics.Color.GRAY
                        textSize = 24f
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                        textAlign = android.graphics.Paint.Align.RIGHT
                    }
                )
            }
        }
    }
}

// Draw professional stylized simplified continental maps of the Middle East region
fun DrawScope.drawStylizedMiddleEastCoastLine(w: Float, h: Float) {
    // Elegant high-fidelity geometric continent representation using gradients
    // Drawing a nice landmass background
    drawRect(
        color = CardSlate,
        size = Size(w, h)
    )

    // Red Sea coastal path (diagonal water stripe dividing Saudi shield from Africa)
    val redSeaPath = Path().apply {
        moveTo(0.04f * w, 0.08f * h)
        lineTo(0.12f * w, 0.02f * h)
        lineTo(0.42f * w, 0.85f * h)
        lineTo(0.38f * w, 0.95f * h)
        lineTo(0.28f * w, 0.98f * h)
        close()
    }

    // Arabian Persian Gulf coastal path (curvy northeast corner water body)
    val arabianGulfPath = Path().apply {
        moveTo(0.55f * w, 0.12f * h)
        quadraticTo(0.68f * w, 0.15f * h, 0.72f * w, 0.32f * h)
        quadraticTo(0.85f * w, 0.44f * h, 0.92f * w, 0.52f * h)
        lineTo(0.98f * w, 0.46f * h)
        lineTo(0.98f * w, 0.05f * h)
        close()
    }

    // Mediterranean coast
    val medPath = Path().apply {
        moveTo(0f, 0f)
        lineTo(0.24f * w, 0f)
        lineTo(0.21f * w, 0.15f * h)
        lineTo(0f, 0.18f * h)
        close()
    }

    // Render waterways
    drawPath(
        path = redSeaPath,
        color = BackgroundDark,
        alpha = 0.8f
    )
    drawPath(
        path = arabianGulfPath,
        color = BackgroundDark,
        alpha = 0.8f
    )
    drawPath(
        path = medPath,
        color = BackgroundDark,
        alpha = 0.7f
    )

    // Highlight coastal borders
    drawPath(
        path = redSeaPath,
        color = BorderSlate,
        style = Stroke(width = 1.5f)
    )
    drawPath(
        path = arabianGulfPath,
        color = BorderSlate,
        style = Stroke(width = 1.5f)
    )
}

// Custom Climate Selector Tools button styling
@Composable
fun ClimateToolButton(
    emoji: String,
    label: String,
    isSelected: Boolean,
    tintColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) SlateMedium else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("tool_btn_$label")
    ) {
        Text(
            text = emoji,
            fontSize = 18.sp,
            modifier = Modifier.size(22.dp),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            label,
            color = if (isSelected) TextPrimary else TextSecondary,
            fontSize = 9.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// Custom period selector tab button row
@Composable
fun RowScope.PeriodTabButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) SlateDark else Color.Transparent
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .weight(1f)
            .height(36.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            label,
            color = if (isSelected) ClimatePrimary else TextSecondary,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

// Beautiful customized temperature weekly bar visual chart
@Composable
fun CustomPrecipitationBarChart(forecastDays: List<ForecastDay>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp),
        colors = CardDefaults.cardColors(containerColor = SlateMedium),
        border = BorderStroke(0.5.dp, BorderSlate)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            forecastDays.forEach { day ->
                val maxTempNormalized = (day.maxTemp / 50f).coerceIn(0.1f, 1f)
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(36.dp)
                ) {
                    Text(
                        "${day.maxTemp.toInt()}°",
                        color = TempWarm,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Native Box Bar representation
                    Box(
                        modifier = Modifier
                            .width(8.dp)
                            .height(55.dp * maxTempNormalized)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(TempWarm, TempCold.copy(alpha = 0.5f))
                                ),
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        day.dayNameAr,
                        color = TextSecondary,
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}

// Custom monthly anomaly/change representation indicators bar charts
@Composable
fun CustomMonthlyAnomalyChart(forecastMonths: List<ForecastMonth>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp),
        colors = CardDefaults.cardColors(containerColor = SlateMedium),
        border = BorderStroke(0.5.dp, BorderSlate)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.Bottom
        ) {
            forecastMonths.forEach { week ->
                val scoreClean = week.anomalyScore.replace("°C", "").toFloatOrNull() ?: +1f
                val heightPct = (kotlin.math.abs(scoreClean) / 3f).coerceIn(0.1f, 1f)
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(55.dp)
                ) {
                    Text(
                        week.anomalyScore,
                        color = if (scoreClean > 0) TempWarm else TempCold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Native Box Bar representation
                    Box(
                        modifier = Modifier
                            .width(14.dp)
                            .height(55.dp * heightPct)
                            .background(
                                color = if (scoreClean > 0) TempWarm else TempCold,
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        week.weekNameAr,
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
