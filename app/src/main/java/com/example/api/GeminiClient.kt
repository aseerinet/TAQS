package com.example.api

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    @Json(name = "contents") val contents: List<Content>,
    @Json(name = "generationConfig") val generationConfig: GenerationConfig? = null,
    @Json(name = "systemInstruction") val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "parts") val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    @Json(name = "temperature") val temperature: Float? = null,
    @Json(name = "topP") val topP: Float? = null,
    @Json(name = "topK") val topK: Int? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    @Json(name = "candidates") val candidates: List<Candidate>? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    @Json(name = "content") val content: Content? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiRetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    private val moshi = com.squareup.moshi.Moshi.Builder()
        .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
        .build()

    val service: GeminiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}

object ClimateAiExpert {

    suspend fun generateClimateReport(
        cityName: String,
        countryName: String,
        elevation: Int,
        temperature: Float,
        humidity: Int,
        windAr: String,
        tempAnomaly: Float,
        vulnerability: Int,
        selectedPeriod: String // "اليومي" or "الأسبوعي" or "الشهري"
    ): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return """
                ⚠️ مفتاح الخبير الذكي (Gemini API Key) غير مهيأ بالكامل في لوحة أسرار المنصة. 
                يرجى إضافة مفتاح GEMINI_API_KEY صحيح وصالح من لوحة Secrets في AI Studio لتفعيل التقارير المباشرة والمحاكاة الذكية الشاملة.
                
                📋 التحليل الاسترشادي التلقائي لموقع $cityName ($elevation م):
                • جغرافية المنطقة: تقع على ارتفاع $elevation متراً عن سطح البحر، ما يمنحها خصائص حرارية متباينة.
                • حالة الطقس والمناخ الحالي: درجة الحرارة $temperature مئوية ورطوبة بنسبة $humidity% بتأثير رياح $windAr.
                • مؤشر التغير الشامل: انحراف حراري بمقدار $tempAnomaly°C وحساسية مناخية $vulnerability/100.
                • التغيرات المتوقعة: تزايد حدة موجات الجفاف أو تطرف هطول الأمطار الفجائي حسب النماذج الإقليمية والمحلية مثل الهيئة العامة للأرصاد وحماية البيئة.
            """.trimIndent()
        }

        // Construct a highly detailed climate analysis prompt
        val prompt = """
            أنت خبير مناخي وأرصاد جوية متخصص بالدول العربية والشرق الأوسط والخليج العربي والمملكة العربية السعودية.
            قم بإعداد تقرير مناخي احترافي مقتضب وعالي الدقة ومكتوب بلغة عربية رسمية فصحى جذابة وبأسلوب منسق جداً لمدينة/منطقة: $cityName في دولة $countryName.
            
            البيانات المناخية المسجلة حالياً التي يجب أن تدعم تقريرك بها:
            1. الارتفاع عن سطح البحر: $elevation متر.
            2. درجة الحرارة الحالية: $temperature درجة مئوية.
            3. نسبة الرطوبة: $humidity%.
            4. نوع الرياح واتجاهها مسجلاً محلياً: $windAr.
            5. الانحراف الحراري (مؤشر التغير عن المتوسط 30 سنة): $tempAnomaly درجة مئوية.
            6. حساسية التعرض للتغيرات المناخية: $vulnerability/100.
            7. نوع الفترة المطلوبة للتحليل: $selectedPeriod.

            هيكل التقرير المطلوب (وزعها في نقاط واضحة ومقتضبة باستخدام رموز تعبيرية مناخية ومصطلحات تخصصية مثل "الهطول الحِمْلي، ضغط الهواء المرتفع، نسيم الجبل، منخفض جوي"):
            
            📍 1. تحليل الأثر الجغرافي والارتفاع: 
            (اشرح علمياً كيف يؤثر الارتفاع الحالي وهو $elevation متر على تشكيل مناخها المحلي المحلي السنوي، وطبقة الغلاف المناخي الملامسة وضغط الهواء)
            
            🚨 2. مؤشر التغير المناخي والإنذار المبكر:
            (اشرح الانحراف $tempAnomaly م ومدى ملاءمته للتغيرات الحالية المرصودة إقليمياً ودولياً من قبل هيئات مثل الهيئة السعودية للأرصاد NCM، الهيئة الحكومية IPCC أو NOAA وكيفية تكييف الموارد المائية ومخاطر الجفاف أو السيول)
            
            💡 3. توصيات الخبير الاستراتيجية للفترة ($selectedPeriod):
            (توصيات حكيمة تخصصية جداً للزراعة، ترشيد المياه، حماية البنية التحتية، أو إرشادات السفر والنشاطات الخارجية الملائمة لنوع هذه الفترة الحالية)
            
            اجعل نبرة التقرير علمية، تشخيصية، رصينة، تبعث على الاستعداد والوعي البيئي دون تضخيم وهلع، بطول معقول ومسافات وفواصل مريحة للمشاهدة.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = prompt)))
            ),
            generationConfig = GenerationConfig(
                temperature = 0.3f
            ),
            systemInstruction = Content(
                parts = listOf(Part(text = "طبيعتك هي خبير مناخي عربي معتمد لتقديم أطالس وبيانات مناخية محترفة، تستعين بالدلائل العلمية الرصينة."))
            )
        )

        return try {
            val response = GeminiRetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "⚠️ عذراً، لم نتمكن من الحصول على استجابة مناسبة من خبير المناخ الذكي حالياً."
        } catch (e: Exception) {
            "❌ فشل الاتصال بخبير المناخ (Gemini API) بسبب: ${e.localizedMessage}. تأكد من تفعيل الاتصال بالإنترنت وتهيئة مفتاح API بشكل سليم."
        }
    }
}
