package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.ClimateAiExpert
import com.example.data.ClimateRepository
import com.example.data.LocationData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ClimateLayer {
    RAIN,      // الأمطار والبرق
    TEMP,      // درجات الحرارة
    CLOUDS,    // حركة السحب
    WIND,      // اتجاهات وقوة الرياح
    HUMIDITY   // الرطوبة الضبابية
}

enum class ForecastPeriod {
    DAILY,
    WEEKLY,
    MONTHLY
}

sealed class AiReportState {
    object Idle : AiReportState()
    object Loading : AiReportState()
    data class Success(val report: String) : AiReportState()
    data class Error(val message: String) : AiReportState()
}

class ClimateAppViewModel : ViewModel() {

    // Selected Weather Station / Location
    private val _selectedLocation = MutableStateFlow<LocationData>(ClimateRepository.locations[0]) // Riyadh default
    val selectedLocation: StateFlow<LocationData> = _selectedLocation.asStateFlow()

    // Current active Climate Radar Overlay Layer
    private val _activeLayer = MutableStateFlow(ClimateLayer.RAIN)
    val activeLayer: StateFlow<ClimateLayer> = _activeLayer.asStateFlow()

    // Current active forecast period (Daily, Weekly, Monthly)
    private val _selectedPeriod = MutableStateFlow(ForecastPeriod.DAILY)
    val selectedPeriod: StateFlow<ForecastPeriod> = _selectedPeriod.asStateFlow()

    // List of searchable and plotted stations
    private val _allLocations = MutableStateFlow<List<LocationData>>(ClimateRepository.locations)
    val allLocations: StateFlow<List<LocationData>> = _allLocations.asStateFlow()

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Refresh simulation sweep state
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Dedicated climate data modification count (just to force UI visual recalculations, "يتغير التحديث")
    private val _updateNonce = MutableStateFlow(0)
    val updateNonce: StateFlow<Int> = _updateNonce.asStateFlow()

    // Gemini AI climate assessment report
    private val _aiReportState = MutableStateFlow<AiReportState>(AiReportState.Idle)
    val aiReportState: StateFlow<AiReportState> = _aiReportState.asStateFlow()

    // Selected custom manual coordinates input state
    val customLat = MutableStateFlow("24.7136")
    val customLon = MutableStateFlow("46.6753")

    init {
        // Trigger default initial report
        generateAiReport()
    }

    fun selectLocation(location: LocationData) {
        _selectedLocation.value = location
        // Automatically fetch AI climate analysis of this newly selected region
        generateAiReport()
    }

    fun setClimateLayer(layer: ClimateLayer) {
        _activeLayer.value = layer
    }

    fun setForecastPeriod(period: ForecastPeriod) {
        _selectedPeriod.value = period
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _allLocations.value = ClimateRepository.locations
        } else {
            _allLocations.value = ClimateRepository.locations.filter {
                it.nameAr.contains(query, ignoreCase = true) || 
                it.nameEn.contains(query, ignoreCase = true) ||
                it.countryAr.contains(query, ignoreCase = true)
            }
        }
    }

    // Rely on Geographic Location / coordinates ("الاعتماد على الموقع الجغرافي")
    // When triggered, simulates a real GPS lookup and triggers nearest weather monitoring post calculation
    fun simulateGpsLocation(lat: Double, lon: Double) {
        customLat.value = String.format("%.4f", lat)
        customLon.value = String.format("%.4f", lon)
        
        viewModelScope.launch {
            _isRefreshing.value = true
            delay(1000) // Simulating GPS hardware delay
            val nearest = ClimateRepository.findNearestLocation(lat, lon)
            _selectedLocation.value = nearest
            _updateNonce.value += 1
            _isRefreshing.value = false
            generateAiReport()
        }
    }

    // Refresh Sweep
    fun triggerRadarRefresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            delay(1200) // Aesthetic radar sweep animation latency
            
            // Generate tiny fluctuates in meteorological properties so that values visually update perfectly ("يتغير التحديث")
            val current = _selectedLocation.value
            val tempShift = ((-15..15).random() / 10f)
            val humShift = (-5..5).random()
            val newWind = (current.windSpeedKmh + (-3..3).random()).coerceIn(5, 45)
            
            _selectedLocation.value = current.copy(
                temperature = (current.temperature + tempShift).coerceIn(10f, 50f),
                humidityPercent = (current.humidityPercent + humShift).coerceIn(5, 98),
                windSpeedKmh = newWind
            )
            
            _updateNonce.value += 1
            _isRefreshing.value = false
            
            // Optionally refresh the AI analysis under new atmospheric values!
            generateAiReport()
        }
    }

    fun generateAiReport() {
        val location = _selectedLocation.value
        val periodName = when(_selectedPeriod.value) {
            ForecastPeriod.DAILY -> "اليومية"
            ForecastPeriod.WEEKLY -> "الأسبوعية"
            ForecastPeriod.MONTHLY -> "الشهرية"
        }
        
        viewModelScope.launch {
            _aiReportState.value = AiReportState.Loading
            try {
                val report = ClimateAiExpert.generateClimateReport(
                    cityName = location.nameAr,
                    countryName = location.countryAr,
                    elevation = location.elevationMeters,
                    temperature = location.temperature,
                    humidity = location.humidityPercent,
                    windAr = location.windDirectionAr,
                    tempAnomaly = location.climateChangeIndex.tempAnomaly,
                    vulnerability = location.climateChangeIndex.vulnerabilityScore,
                    selectedPeriod = periodName
                )
                _aiReportState.value = AiReportState.Success(report)
            } catch (e: Exception) {
                _aiReportState.value = AiReportState.Error(e.localizedMessage ?: "فشل غير معروف في الاتصال بخادم المناخ")
            }
        }
    }
}
