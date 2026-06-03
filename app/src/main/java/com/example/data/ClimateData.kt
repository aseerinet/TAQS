package com.example.data

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

// Weather and Climate Models
data class LocationData(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val countryAr: String,
    val countryEn: String,
    val latitude: Double,
    val longitude: Double,
    val elevationMeters: Int, // الارتفاع عن سطح البحر بالمتر
    val temperature: Float,    // Current Temperature in Celsius
    val minTemp: Float,
    val maxTemp: Float,
    val humidityPercent: Int,  // Humidity %
    val pressureHpa: Int,      // Atmospheric Pressure
    val windSpeedKmh: Int,     // Wind speed in km/h
    val windDirectionDeg: Int, // Wind direction angle (0 = North, 90 = East, etc.)
    val windDirectionAr: String, // Arabic Wind direction (شمالية غريبة، إلخ)
    val cloudsCoverage: Int,   // Cloud cover %
    val rainRateMm: Float,     // Expected Rain (mm)
    val climateChangeIndex: ClimateAnomalyIndex, // مؤشر التغير المناخي للمنطقة
    val dailyForecast: List<DailyWeather>, // اليومي
    val weeklyForecast: List<ForecastDay>, // التوقعات أسبوع
    val monthlyForecast: List<ForecastMonth> // التوقعات شهر
)

data class ClimateAnomalyIndex(
    val tempAnomaly: Float,      // Temperature anomaly (+/- relative to 30yr baseline)
    val rainAnomalyPercent: Int, // Precipitation anomaly % (+/- relative to baseline)
    val desertificationRisk: String, // Arabic hazard risk (مرتفع، متوسط)
    val vulnerabilityScore: Int // Out of 100
)

data class DailyWeather(
    val time: String, // e.g. "09:00", "12:00"
    val temp: Float,
    val iconAr: String,
    val conditionAr: String,
    val rainProbability: Int,
    val windSpeed: Int
)

data class ForecastDay(
    val dayNameAr: String,
    val dayNameEn: String,
    val dateString: String,
    val maxTemp: Float,
    val minTemp: Float,
    val rainProbability: Int,
    val conditionAr: String,
    val iconAr: String
)

data class ForecastMonth(
    val weekNameAr: String,
    val avgTemp: Float,
    val deviationType: String, // "ارتفاع" or "انخفاض" or "مستقر"
    val avgHumidity: Int,
    val expectedRainDays: Int,
    val anomalyScore: String
)

// List of professional climate stations / Arab cities
object ClimateRepository {
    val locations = listOf(
        LocationData(
            id = "RUH",
            nameAr = "الرياض",
            nameEn = "Riyadh",
            countryAr = "السعودية",
            countryEn = "Saudi Arabia",
            latitude = 24.7136,
            longitude = 46.6753,
            elevationMeters = 612, // الرياض هضبة نجد 612 م
            temperature = 39.5f,
            minTemp = 28.0f,
            maxTemp = 42.0f,
            humidityPercent = 12,
            pressureHpa = 1010,
            windSpeedKmh = 18,
            windDirectionDeg = 315, // NW
            windDirectionAr = "شمالية غربية جافة",
            cloudsCoverage = 5,
            rainRateMm = 0.0f,
            climateChangeIndex = ClimateAnomalyIndex(
                tempAnomaly = +1.4f,
                rainAnomalyPercent = -14,
                desertificationRisk = "مرتفع جداً (توسع الجفاف الحراري)",
                vulnerabilityScore = 78
            ),
            dailyForecast = listOf(
                DailyWeather("06:00 ص", 29f, "☀️", "صافٍ ولطيف نسبيًا", 0, 10),
                DailyWeather("12:00 م", 38f, "☀️", "حار وجاف جداً", 0, 18),
                DailyWeather("06:00 م", 37f, "⛅", "حار مع غبار خفيف", 0, 22),
                DailyWeather("12:00 ص", 32f, "🌙", "صافٍ ودافئ", 0, 12)
            ),
            weeklyForecast = listOf(
                ForecastDay("الأحد", "Sunday", "06/02", 41f, 27f, 0, "شديد الحرارة واهب", "☀️"),
                ForecastDay("الاثنين", "Monday", "06/03", 42f, 28f, 0, "شديد الحرارة وصافٍ", "☀️"),
                ForecastDay("الثلاثاء", "Tuesday", "06/04", 40f, 26f, 0, "حار وجاف", "☀️"),
                ForecastDay("الأربعاء", "Wednesday", "06/05", 39f, 25f, 5, "حار مع نشاط رياح مثيرة للتربة", "💨"),
                ForecastDay("الخميس", "Thursday", "06/06", 41f, 27f, 0, "حار وصافٍ ومستقر", "☀️"),
                ForecastDay("الجمعة", "Friday", "06/07", 42f, 28f, 0, "حار جداً وجاف", "☀️"),
                ForecastDay("السبت", "Saturday", "06/08", 43f, 29f, 0, "موجة جافة شديدة الحرارة", "☀️")
            ),
            monthlyForecast = listOf(
                ForecastMonth("الأسبوع 1", 39.8f, "ارتفاع ملموس", 11, 0, "+1.6°C"),
                ForecastMonth("الأسبوع 2", 40.5f, "ارتفاع طفيف", 13, 0, "+0.9°C"),
                ForecastMonth("الأسبوع 3", 41.2f, "ارتفاع ملموس", 12, 0, "+1.8°C"),
                ForecastMonth("الأسبوع 4", 42.0f, "مستقر حار للغاية", 10, 0, "+2.0°C")
            )
        ),
        LocationData(
            id = "AHB",
            nameAr = "أبها",
            nameEn = "Abha",
            countryAr = "السعودية",
            countryEn = "Saudi Arabia",
            latitude = 18.2164,
            longitude = 42.5053,
            elevationMeters = 2270, // أبها جبال السروات 2270 م (طقس فريد وبارد)
            temperature = 22.0f,
            minTemp = 14.0f,
            maxTemp = 25.0f,
            humidityPercent = 78,
            pressureHpa = 1014,
            windSpeedKmh = 24,
            windDirectionDeg = 240, // SW
            windDirectionAr = "جنوبية غربية رطبة من السروات",
            cloudsCoverage = 80,
            rainRateMm = 15.2f,
            climateChangeIndex = ClimateAnomalyIndex(
                tempAnomaly = +0.6f,
                rainAnomalyPercent = +18, // زيادة الأمطار الرعدية الموسمية بسب تغير الرياح الموسمية
                desertificationRisk = "منخفض (نشاط تضاريسي مطري)",
                vulnerabilityScore = 32
            ),
            dailyForecast = listOf(
                DailyWeather("06:00 ص", 15f, "🌫️", "ضباب كثيف بارد", 20, 8),
                DailyWeather("12:00 م", 23f, "🌦️", "غائم جزئي مع زخات مطر", 60, 16),
                DailyWeather("06:00 م", 18f, "⛈️", "عواصف رعدية ممطرة", 85, 28),
                DailyWeather("12:00 ص", 14f, "🌧️", "رذاذ مستمر وبارد", 50, 10)
            ),
            weeklyForecast = listOf(
                ForecastDay("الأحد", "Sunday", "06/02", 24f, 15f, 75, "أمطار رعدية ركامية", "⛈️"),
                ForecastDay("الاثنين", "Monday", "06/03", 23f, 14f, 80, "أمطار رعدية غزيرة عصراً", "⛈️"),
                ForecastDay("الثلاثاء", "Tuesday", "06/04", 22f, 13f, 60, " غائم مع زخات مطر خفيفة", "🌦️"),
                ForecastDay("الأربعاء", "Wednesday", "06/05", 24f, 15f, 40, "غائم جزئياً مع فرصة رذاذ", "⛅"),
                ForecastDay("الخميس", "Thursday", "06/06", 25f, 16f, 50, "رادار ومطر ركامي متفرق", "⛈️"),
                ForecastDay("الجمعة", "Friday", "06/07", 23f, 14f, 70, "عواصف رعدية ممطرة جبلية", "⛈️"),
                ForecastDay("السبت", "Saturday", "06/08", 22f, 13f, 85, "أمطار مستمرة وضباب على القمم", "🌧️")
            ),
            monthlyForecast = listOf(
                ForecastMonth("الأسبوع 1", 23.1f, "انخفاض طفيف", 82, 5, "-0.3°C"),
                ForecastMonth("الأسبوع 2", 22.8f, "انخفاض ملموس", 85, 6, "-0.8°C"),
                ForecastMonth("الأسبوع 3", 24.2f, "مستقر لطيف", 76, 4, "+0.2°C"),
                ForecastMonth("الأسبوع 4", 23.5f, "ارتفاع نسبي هادئ", 80, 5, "+0.4°C")
            )
        ),
        LocationData(
            id = "JED",
            nameAr = "جدة",
            nameEn = "Jeddah",
            countryAr = "السعودية",
            countryEn = "Saudi Arabia",
            latitude = 21.5433,
            longitude = 39.1728,
            elevationMeters = 12, // جدة ساحلية البحر الأحمر
            temperature = 34.0f,
            minTemp = 27.0f,
            maxTemp = 38.0f,
            humidityPercent = 82,
            pressureHpa = 1008,
            windSpeedKmh = 14,
            windDirectionDeg = 270, // West
            windDirectionAr = "غربية رطبة نسيم البحر",
            cloudsCoverage = 15,
            rainRateMm = 0.0f,
            climateChangeIndex = ClimateAnomalyIndex(
                tempAnomaly = +1.1f,
                rainAnomalyPercent = +5, // سيول وموجات أمطار عنيفة فجائية متطرفة
                desertificationRisk = "متوسط (خطورة رطوبة وتطرف حراري ساحلي)",
                vulnerabilityScore = 67
            ),
            dailyForecast = listOf(
                DailyWeather("06:00 ص", 28f, "☀️", "رطوبة خانقة مع شمس مشرقة", 0, 8),
                DailyWeather("12:00 م", 35f, "⛅", "حار ورطب جداً", 5, 14),
                DailyWeather("06:00 م", 33f, "⛅", "صافي مع نسيم البحر الرطب", 10, 16),
                DailyWeather("12:00 ص", 30f, "🌙", "رطب دافئ هادئ", 0, 7)
            ),
            weeklyForecast = listOf(
                ForecastDay("الأحد", "Sunday", "06/02", 37f, 28f, 10, "صافٍ ورطب طوال اليوم", "⛅"),
                ForecastDay("الاثنين", "Monday", "06/03", 38f, 29f, 5, "حار ورطوبة مرتفعة جداً", "☀️"),
                ForecastDay("الثلاثاء", "Tuesday", "06/04", 36f, 27f, 0, "مشمس ورطب نسبيًا", "☀️"),
                ForecastDay("الأربعاء", "Wednesday", "06/05", 35f, 27f, 0, "أجواء دافئة ورطوبة عالية", "⛅"),
                ForecastDay("الخميس", "Thursday", "06/06", 37f, 28f, 0, "حار رطب مع نسيم خفيف", "☀️"),
                ForecastDay("الجمعة", "Friday", "06/07", 38f, 29f, 12, "غائم جزئياً بسبب بخار الماء", "⛅"),
                ForecastDay("السبت", "Saturday", "06/08", 39f, 29f, 5, "رطوبة شديدة وشمس حارقة", "☀️")
            ),
            monthlyForecast = listOf(
                ForecastMonth("الأسبوع 1", 35.5f, "ارتفاع طفيف", 84, 0, "+0.7°C"),
                ForecastMonth("الأسبوع 2", 36.2f, "ارتفاع ملموس", 81, 0, "+1.2°C"),
                ForecastMonth("الأسبوع 3", 35.8f, "مستقر رطب", 83, 1, "+0.6°C"),
                ForecastMonth("الأسبوع 4", 36.9f, "ارتفاع كبير", 85, 0, "+1.5°C")
            )
        ),
        LocationData(
            id = "MAK",
            nameAr = "مكة المكرمة",
            nameEn = "Mecca",
            countryAr = "السعودية",
            countryEn = "Saudi Arabia",
            latitude = 21.3891,
            longitude = 39.8579,
            elevationMeters = 277, // مكة المكرمة 277 م وتحيطها الجبال الجرداء
            temperature = 42.0f,
            minTemp = 30.0f,
            maxTemp = 46.0f,
            humidityPercent = 22,
            pressureHpa = 1009,
            windSpeedKmh = 14,
            windDirectionDeg = 90, // East
            windDirectionAr = "شرقية جبلية جافة حارة جداً",
            cloudsCoverage = 10,
            rainRateMm = 0.0f,
            climateChangeIndex = ClimateAnomalyIndex(
                tempAnomaly = +1.6f,
                rainAnomalyPercent = -10,
                desertificationRisk = "مرتفع (تطرف حراري حاد)",
                vulnerabilityScore = 75
            ),
            dailyForecast = listOf(
                DailyWeather("06:00 ص", 32f, "☀️", "مشمس دافئ وجاف", 0, 7),
                DailyWeather("12:00 م", 44f, "☀️", "شديد الحرارة ولاهب", 0, 15),
                DailyWeather("06:00 م", 42f, "☀️", "أجواء نارية شديدة الجفاف", 5, 12),
                DailyWeather("12:00 ص", 34f, "🌙", "صافٍ وحار ليلاً", 0, 8)
            ),
            weeklyForecast = listOf(
                ForecastDay("الأحد", "Sunday", "06/02", 45f, 31f, 0, "طغيان الحرارة ومستقر", "☀️"),
                ForecastDay("الاثنين", "Monday", "06/03", 46f, 32f, 0, "موجة نارية جافة للغاية", "☀️"),
                ForecastDay("الثلاثاء", "Tuesday", "06/04", 45f, 30f, 10, "صاف مشمس شديد الحرارة", "☀️"),
                ForecastDay("الأربعاء", "Wednesday", "06/05", 43f, 29f, 20, "ظهور بعض السحب المتوسطة", "⛅"),
                ForecastDay("الخميس", "Thursday", "06/06", 44f, 31f, 0, "حار ولاهب جداً", "☀️"),
                ForecastDay("الجمعة", "Friday", "06/07", 45f, 32f, 0, "مستقر مشمس شديد الجفاف", "☀️"),
                ForecastDay("السبت", "Saturday", "06/08", 46f, 33f, 5, "طقس صيفي شديد القسوة", "☀️")
            ),
            monthlyForecast = listOf(
                ForecastMonth("الأسبوع 1", 44.5f, "ارتفاع حراري", 25, 0, "+1.8°C"),
                ForecastMonth("الأسبوع 2", 45.0f, "ارتفاع قياسي", 23, 0, "+2.1°C"),
                ForecastMonth("الأسبوع 3", 43.8f, "مستقر قياسي", 26, 0, "+1.2°C"),
                ForecastMonth("الأسبوع 4", 45.5f, "تطرف حراري", 20, 0, "+2.5°C")
            )
        ),
        LocationData(
            id = "MED",
            nameAr = "المدينة المنورة",
            nameEn = "Medina",
            countryAr = "السعودية",
            countryEn = "Saudi Arabia",
            latitude = 24.4673,
            longitude = 39.6111,
            elevationMeters = 608,
            temperature = 38.0f,
            minTemp = 26.0f,
            maxTemp = 41.0f,
            humidityPercent = 18,
            pressureHpa = 1011,
            windSpeedKmh = 19,
            windDirectionDeg = 180, // South
            windDirectionAr = "جنوبية حارة جافة (السموم)",
            cloudsCoverage = 8,
            rainRateMm = 0.0f,
            climateChangeIndex = ClimateAnomalyIndex(
                tempAnomaly = +1.3f,
                rainAnomalyPercent = -8,
                desertificationRisk = "مرتفع",
                vulnerabilityScore = 70
            ),
            dailyForecast = listOf(
                DailyWeather("06:00 ص", 28f, "☀️", "صافٍ ومعتدل نسبياً", 0, 9),
                DailyWeather("12:00 م", 39f, "☀️", "حار وجاف جداً", 0, 16),
                DailyWeather("06:00 م", 38f, "☀️", "رياح مثيرة لبعض الأتربة", 0, 21),
                DailyWeather("12:00 ص", 31f, "🌙", "لطيف وصافٍ", 0, 11)
            ),
            weeklyForecast = listOf(
                ForecastDay("الأحد", "Sunday", "06/02", 40f, 27f, 0, "مشمس حار ومستقر", "☀️"),
                ForecastDay("الاثنين", "Monday", "06/03", 41f, 28f, 0, "حار جداً وصافي", "☀️"),
                ForecastDay("الثلاثاء", "Tuesday", "06/04", 42f, 29f, 5, "رياح نشطة مثيفة للغبار", "💨"),
                ForecastDay("الأربعاء", "Wednesday", "06/05", 39f, 26f, 0, "صافٍ وجاف", "☀️"),
                ForecastDay("الخميس", "Thursday", "06/06", 41f, 28f, 0, "تأثير رياح السموم النهارية", "💨"),
                ForecastDay("الجمعة", "Friday", "06/07", 42f, 29f, 0, "شديد الحرارة وصافٍ", "☀️"),
                ForecastDay("السبت", "Saturday", "06/08", 43f, 30f, 0, "صافٍ تماماً جاف وحار للغاية", "☀️")
            ),
            monthlyForecast = listOf(
                ForecastMonth("الأسبوع 1", 39.5f, "ارتفاع ملموس", 19, 0, "+1.3°C"),
                ForecastMonth("الأسبوع 2", 40.2f, "مستقر حار", 17, 0, "+0.9°C"),
                ForecastMonth("الأسبوع 3", 41.5f, "ارتفاع حراري", 15, 0, "+1.7°C"),
                ForecastMonth("الأسبوع 4", 42.0f, "ارتفاع قياسي", 14, 0, "+2.0°C")
            )
        ),
        LocationData(
            id = "DXB",
            nameAr = "دبي",
            nameEn = "Dubai",
            countryAr = "الإمارات",
            countryEn = "UAE",
            latitude = 25.2048,
            longitude = 55.2708,
            elevationMeters = 5,
            temperature = 35.5f,
            minTemp = 28.0f,
            maxTemp = 39.0f,
            humidityPercent = 88, // رطوبة ساحلية هائلة في الخليج العربي
            pressureHpa = 1007,
            windSpeedKmh = 12,
            windDirectionDeg = 330, // NNW
            windDirectionAr = "شمال غربية بحرية مشبعة بالرطوبة",
            cloudsCoverage = 20,
            rainRateMm = 0.0f,
            climateChangeIndex = ClimateAnomalyIndex(
                tempAnomaly = +1.2f,
                rainAnomalyPercent = +28, // زيادة حدة العواصف المطيرة المتطرفة (مثل منخفض الهدير)
                desertificationRisk = "متوسط (خطورة سيول تيارية مفاجئة)",
                vulnerabilityScore = 69
            ),
            dailyForecast = listOf(
                DailyWeather("06:00 ص", 29f, "🌫️", "ضباب مائي رطب خانق", 5, 6),
                DailyWeather("12:00 م", 37f, "☀️", "حار جداً رطب ومجهد", 0, 11),
                DailyWeather("06:00 م", 34f, "⛅", "عالق رطب مع نسيم المليحة", 15, 14),
                DailyWeather("12:00 ص", 31f, "🌙", "رطب مغشى دافئ", 10, 8)
            ),
            weeklyForecast = listOf(
                ForecastDay("الأحد", "Sunday", "06/02", 38f, 29f, 15, "رطب وغائم بسحب رقيقة", "⛅"),
                ForecastDay("الاثنين", "Monday", "06/03", 39f, 30f, 10, "رطب جداً وشمس حارقة", "☀️"),
                ForecastDay("الثلاثاء", "Tuesday", "06/04", 37f, 28f, 25, "ضبابي صباحاً ثم غائم جزئياً", "🌫️"),
                ForecastDay("الأربعاء", "Wednesday", "06/05", 36f, 27f, 5, "رطب ومغبر نهاراً", "⛅"),
                ForecastDay("الخميس", "Thursday", "06/06", 38f, 29f, 0, "مشمس لاهب رطب", "☀️"),
                ForecastDay("الجمعة", "Friday", "06/07", 40f, 30f, 15, "سحب ركامية محلية في الجبال المجاورة", "⛅"),
                ForecastDay("السبت", "Saturday", "06/08", 41f, 31f, 5, "موجة رطوبة استوائية خانقة", "☀️")
            ),
            monthlyForecast = listOf(
                ForecastMonth("الأسبوع 1", 36.8f, "ارتفاع طفيف", 89, 0, "+0.9°C"),
                ForecastMonth("الأسبوع 2", 37.5f, "ارتفاع ملموس", 86, 0, "+1.4°C"),
                ForecastMonth("الأسبوع 3", 38.0f, "مستقر رطب", 87, 1, "+1.1°C"),
                ForecastMonth("الأسبوع 4", 39.2f, "ارتفاع حراري كبير", 89, 0, "+1.9°C")
            )
        ),
        LocationData(
            id = "KWT",
            nameAr = "الكويت",
            nameEn = "Kuwait City",
            countryAr = "الكويت",
            countryEn = "Kuwait",
            latitude = 29.3759,
            longitude = 47.9774,
            elevationMeters = 8,
            temperature = 41.5f,
            minTemp = 29.0f,
            maxTemp = 48.0f,
            humidityPercent = 15,
            pressureHpa = 1009,
            windSpeedKmh = 28,
            windDirectionDeg = 320, // NW
            windDirectionAr = "شمالية غربية شديدة الحرارة مغبرة (بارح)",
            cloudsCoverage = 0,
            rainRateMm = 0.0f,
            climateChangeIndex = ClimateAnomalyIndex(
                tempAnomaly = +1.8f, // الكويت تسجل درجات حرارة قياسية عالمياً تجاوزت +53 مئوية
                rainAnomalyPercent = -22,
                desertificationRisk = "شديد الخطورة (عواصف رملية وجفاف ناري)",
                vulnerabilityScore = 88
            ),
            dailyForecast = listOf(
                DailyWeather("06:00 ص", 31f, "☀️", "صافٍ جاف ولافح", 0, 15),
                DailyWeather("12:00 م", 46f, "💨", "مترب جداً حارق لاهب نهاراً", 0, 32),
                DailyWeather("06:00 م", 43f, "💨", "مترب حار هواء البارح", 0, 26),
                DailyWeather("12:00 ص", 35f, "🌙", "جاف ودافئ للغاية ليلاً", 0, 14)
            ),
            weeklyForecast = listOf(
                ForecastDay("الأحد", "Sunday", "06/02", 46f, 30f, 0, "موجة بارح الشمال الشديد الجاف", "💨"),
                ForecastDay("الاثنين", "Monday", "06/03", 47f, 31f, 0, "عواصف ترابية شديدة نهاراً", "💨"),
                ForecastDay("الثلاثاء", "Tuesday", "06/04", 48f, 32f, 0, "لاهب حارق قياسي ومستمر", "☀️"),
                ForecastDay("الأربعاء", "Wednesday", "06/05", 45f, 29f, 0, "أهدأ نسبياً مع غبار عالق", "⛅"),
                ForecastDay("الخميس", "Thursday", "06/06", 46f, 31f, 0, "مشمس لاهب وجاف جداً", "☀️"),
                ForecastDay("الجمعة", "Friday", "06/07", 47f, 32f, 0, "حرارة قياسية جافة", "☀️"),
                ForecastDay("السبت", "Saturday", "06/08", 49f, 33f, 0, "أجواء جهنمية شديدة الجفاف والرياح", "☀️")
            ),
            monthlyForecast = listOf(
                ForecastMonth("الأسبوع 1", 45.9f, "ارتفاع حاد", 15, 0, "+2.4°C"),
                ForecastMonth("الأسبوع 2", 46.8f, "تطرف قياسي", 13, 0, "+2.8°C"),
                ForecastMonth("الأسبوع 3", 47.1f, "ارتفاع قياسي ومجهد", 14, 0, "+2.5°C"),
                ForecastMonth("الأسبوع 4", 48.0f, "مستقر متطرف حار", 10, 0, "+2.9°C")
            )
        )
    )

    // Calculate distance on earth to find nearest station (Haversine formula)
    fun findNearestLocation(lat: Double, lon: Double): LocationData {
        var minDistance = Double.MAX_VALUE
        var nearest = locations[0]
        for (loc in locations) {
            val dist = haversineDistance(lat, lon, loc.latitude, loc.longitude)
            if (dist < minDistance) {
                minDistance = dist
                nearest = loc
            }
        }
        return nearest
    }

    private fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
