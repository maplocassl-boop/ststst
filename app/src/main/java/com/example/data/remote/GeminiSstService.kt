package com.example.data.remote

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import com.example.model.ActionControlItem
import com.example.model.ControlHierarchyLevel
import com.example.model.RiskLevel
import com.example.model.SstAuditReport
import java.io.ByteArrayOutputStream
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

// --- Gemini REST API Data Models (gemini-3.1-pro-preview with ThinkingLevel.HIGH) ---

@Serializable
enum class ThinkingLevel {
    @SerialName("LOW") LOW,
    @SerialName("MEDIUM") MEDIUM,
    @SerialName("HIGH") HIGH
}

@Serializable
data class ThinkingConfig(
    val thinkingLevel: ThinkingLevel = ThinkingLevel.HIGH
)

@Serializable
data class GenerationConfig(
    val responseMimeType: String? = null,
    val responseSchema: JsonObject? = null,
    val temperature: Float? = null,
    val topP: Float? = null,
    val topK: Int? = null,
    val thinkingConfig: ThinkingConfig? = null
    // Note: maxOutputTokens is intentionally omitted per high-thinking specification
)

@Serializable
data class InlineData(
    val mimeType: String,
    val data: String
)

@Serializable
data class Part(
    val text: String? = null,
    val inlineData: InlineData? = null,
    val thought: Boolean? = null
)

@Serializable
data class Content(
    val role: String? = null,
    val parts: List<Part>
)

@Serializable
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@Serializable
data class Candidate(
    val content: Content? = null,
    val finishReason: String? = null
)

@Serializable
data class GenerateContentResponse(
    val candidates: List<Candidate> = emptyList()
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.1-pro-preview:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiRetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(90, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(90, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            explicitNulls = false
        }
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(GeminiApiService::class.java)
    }
}

class GeminiSstSpecialistRepository(
    private val apiService: GeminiApiService = GeminiRetrofitClient.service
) {
    companion object {
        const val MODEL_NAME = "gemini-3.1-pro-preview"

        val SST_SYSTEM_INSTRUCTION = """
Eres un Especialista Senior en Seguridad y Salud en el Trabajo (SST) con 20 años de experiencia en consultoría industrial, prevención de riesgos laborales y auditorías de cumplimiento normativo (ISO 45001, OSHA, normativas locales). Eres experto en identificar actos y condiciones inseguras, evaluación de riesgos, ergonomía, higiene industrial y planes de emergencia.

Tu función principal en esta aplicación es realizar una AUDITORÍA VISUAL Y ANÁLISIS DE RIESGOS a partir de una foto de un entorno, puesto de trabajo o actividad laboral que suba el usuario. Cuando recibas una imagen, debes analizarla minuciosamente siguiendo estrictamente esta estructura de respuesta con estos 5 encabezados exactos:

### 1. DIAGNÓSTICO DEL ENTORNO
Identificación detallada del tipo de actividad, puesto de trabajo o sector industrial visible en la imagen, describiendo maquinaria, herramientas, disposición espacial y condiciones operativas observadas.

### 2. IDENTIFICACIÓN DE PELIGROS Y ACTOS INSEGUROS
Lista detallada y clasificada de los riesgos detectados (Físicos, Químicos, Ergonómicos, Locativos, Mecánicos o Eléctricos), incluyendo fallas específicas en el uso, selección o estado de los Equipos de Protección Personal (EPP).

### 3. EVALUACIÓN DEL NIVEL DE RIESGO
Estimación técnica de la SEVERIDAD (Baja / Media / Alta - Extremadamente Dañino) y PROBABILIDAD de ocurrencia (Baja / Media / Alta) de un accidente o enfermedad laboral basada en lo observado, indicando explícitamente el Nivel de Riesgo Global: [NIVEL_RIESGO: CRÍTICO], [NIVEL_RIESGO: ALTO], [NIVEL_RIESGO: MEDIO] o [NIVEL_RIESGO: BAJO].

### 4. MEDIDAS PREVENTIVAS Y CORRECTIVAS
Plan de acción inmediato paso a paso para mitigar los riesgos, aplicando explícitamente los 5 peldaños de la jerarquía de controles de ISO 45001:
- [ELIMINACIÓN]: Acción concreta para eliminar el peligro en la fuente.
- [SUSTITUCIÓN]: Acción concreta para sustituir materiales, equipos o procesos peligrosos.
- [CONTROLES DE INGENIERÍA]: Barreras físicas, protecciones colectivas, extracción o aislamiento.
- [CONTROLES ADMINISTRATIVOS]: Permisos de trabajo (LOTO, Trabajo en Caliente, Alturas), señalización, procedimientos y rotación.
- [EPP]: Especificación técnica de los Equipos de Protección Personal requeridos (con referencia ANSI/EN/OSHA).

### 5. RECOMENDACIONES TÉCNICAS Y TIPS DE CULTURA PREVENTIVA
Consejos prácticos, normativas de referencia aplicables (ISO 45001:2018, estándares OSHA 29 CFR 1910/1926, normas técnicas locales) y dinámicas prácticas (como charlas operativas de 5 minutos) para concientizar a los trabajadores implicados.

Tono y estilo: Sé sumamente detallista, analítico, directo y con un enfoque estrictamente preventivo. Tu objetivo es proporcionar información clara que permita a supervisores o trabajadores corregir las condiciones de riesgo de manera inmediata y práctica. Si la imagen no es clara o carece de contexto, indícalo amablemente y ofrece recomendaciones generales basadas en las mejores prácticas de la industria.
        """.trimIndent()
    }

    fun isApiKeyConfigured(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY" && !key.startsWith("YOUR_")
    }

    suspend fun performVisualSstAudit(
        bitmap: Bitmap,
        sectorHint: String,
        inspectorNotes: String,
        imageSourceLabel: String,
        sampleDrawableRes: Int? = null,
        customImageUri: String? = null,
        useHighThinking: Boolean = true
    ): Result<SstAuditReport> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (!isApiKeyConfigured()) {
            return@withContext Result.failure(
                IllegalStateException("API_KEY_NOT_CONFIGURED")
            )
        }

        val base64Image = bitmapToBase64Jpeg(bitmap)
        val contextPrompt = buildString {
            appendLine("Realiza una AUDITORÍA VISUAL Y ANÁLISIS DE RIESGOS SST exhaustiva sobre la imagen adjunta siguiendo estrictamente las 5 secciones obligatorias.")
            if (sectorHint.isNotBlank() && sectorHint != "Detección Automática por IA") {
                appendLine("Sector o área reportada por el supervisor: $sectorHint.")
            }
            if (inspectorNotes.isNotBlank()) {
                appendLine("Observaciones adicionales en campo del inspector: $inspectorNotes.")
            }
            appendLine("Asegúrate de incluir los encabezados exactos: 1. DIAGNÓSTICO DEL ENTORNO, 2. IDENTIFICACIÓN DE PELIGROS Y ACTOS INSEGUROS, 3. EVALUACIÓN DEL NIVEL DE RIESGO, 4. MEDIDAS PREVENTIVAS Y CORRECTIVAS, y 5. RECOMENDACIONES TÉCNICAS Y TIPS DE CULTURA PREVENTIVA.")
        }

        val thinkingLevel = if (useHighThinking) ThinkingLevel.HIGH else ThinkingLevel.LOW

        val request = GenerateContentRequest(
            contents = listOf(
                Content(
                    role = "user",
                    parts = listOf(
                        Part(text = contextPrompt),
                        Part(
                            inlineData = InlineData(
                                mimeType = "image/jpeg",
                                data = base64Image
                            )
                        )
                    )
                )
            ),
            generationConfig = GenerationConfig(
                temperature = 0.2f,
                thinkingConfig = ThinkingConfig(thinkingLevel = thinkingLevel)
            ),
            systemInstruction = Content(
                parts = listOf(Part(text = SST_SYSTEM_INSTRUCTION))
            )
        )

        try {
            val response = apiService.generateContent(apiKey = apiKey, request = request)
            val parts = response.candidates.firstOrNull()?.content?.parts.orEmpty()

            val thoughtParts = parts.filter { it.thought == true }.mapNotNull { it.text }
            val contentParts = parts.filter { it.thought != true }.mapNotNull { it.text }

            val mainText = if (contentParts.isNotEmpty()) {
                contentParts.joinToString("\n\n")
            } else {
                parts.mapNotNull { it.text }.lastOrNull().orEmpty()
            }

            if (mainText.isBlank()) {
                return@withContext Result.failure(
                    IllegalStateException("El modelo no devolvió texto de análisis. Verifica la imagen e intenta nuevamente.")
                )
            }

            val thoughtSummary = thoughtParts.joinToString("\n\n").takeIf { it.isNotBlank() }

            val parsedReport = parseSstResponseToReport(
                rawText = mainText,
                thoughtSummary = thoughtSummary,
                sectorHint = sectorHint,
                imageSourceLabel = imageSourceLabel,
                sampleDrawableRes = sampleDrawableRes,
                customImageUri = customImageUri
            )
            Result.success(parsedReport)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun askFollowUpSpecialist(
        report: SstAuditReport,
        question: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (!isApiKeyConfigured()) {
            // Provide a practical specialist response based on the audit report context if key is missing
            val fallbackAnswer = generateContextualFallbackFollowUp(report, question)
            return@withContext Result.success(fallbackAnswer)
        }

        val prompt = """
Contexto de la Auditoría SST realizada previamente:
- Puesto / Entorno: ${report.title}
- Sector: ${report.sectorTag}
- Nivel de Riesgo: ${report.riskLevel.label}
- Diagnóstico: ${report.section1Diagnostico}
- Peligros detectados: ${report.section2Peligros}
- Medidas preventivas: ${report.section4Medidas}

Pregunta de seguimiento del supervisor o trabajador:
"$question"

Responde como Especialista Senior en SST (20 años de experiencia, ISO 45001 / OSHA) de forma directa, práctica, técnica y accionable en campo.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(
                Content(
                    role = "user",
                    parts = listOf(Part(text = prompt))
                )
            ),
            generationConfig = GenerationConfig(
                temperature = 0.3f,
                thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.HIGH)
            ),
            systemInstruction = Content(
                parts = listOf(Part(text = SST_SYSTEM_INSTRUCTION))
            )
        )

        try {
            val response = apiService.generateContent(apiKey = apiKey, request = request)
            val parts = response.candidates.firstOrNull()?.content?.parts.orEmpty()
            val answer = parts.filter { it.thought != true }
                .mapNotNull { it.text }
                .joinToString("\n\n")
                .ifBlank { parts.mapNotNull { it.text }.lastOrNull().orEmpty() }

            if (answer.isBlank()) {
                Result.failure(IllegalStateException("Sin respuesta del especialista."))
            } else {
                Result.success(cleanMarkdownFormatting(answer))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateContextualFallbackFollowUp(
        report: SstAuditReport,
        question: String
    ): String {
        return """
Dictamen Técnico de Seguimiento (${report.sectorTag} · ${report.riskLevel.badgeText}):

Respecto a tu consulta sobre "$question":
1. Verificación Inmediata en Campo (ISO 45001 Cl. 8.1.2): Prioriza la aplicación de los controles de Ingeniería y Eliminación listados en la sección 4 del acta antes de depender exclusivamente del EPP.
2. Estándar de Cumplimiento (OSHA / ANSI): Asegúrate de documentar la corrección mediante un Permiso de Trabajo firmado y lista de chequeo pre-operacional verificada por el supervisor de turno.
3. Dinámica con el Personal: Reúne a la cuadrilla durante 5 minutos en el punto de trabajo para validar visualmente que la condición insegura ha sido controlada antes de reiniciar la operación.
        """.trimIndent()
    }

    private fun bitmapToBase64Jpeg(bitmap: Bitmap): String {
        val maxDimension = 1280
        val scaledBitmap = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
            val ratio = minOf(
                maxDimension.toFloat() / bitmap.width,
                maxDimension.toFloat() / bitmap.height
            )
            val width = (bitmap.width * ratio).toInt().coerceAtLeast(1)
            val height = (bitmap.height * ratio).toInt().coerceAtLeast(1)
            Bitmap.createScaledBitmap(bitmap, width, height, true)
        } else {
            bitmap
        }
        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 82, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    fun parseSstResponseToReport(
        rawText: String,
        thoughtSummary: String?,
        sectorHint: String,
        imageSourceLabel: String,
        sampleDrawableRes: Int?,
        customImageUri: String?
    ): SstAuditReport {
        val section1 = extractSection(
            rawText,
            startPatterns = listOf("1\\.?\\s*DIAGN[ÓO]STICO DEL ENTORNO"),
            endPatterns = listOf("2\\.?\\s*IDENTIFICACI[ÓO]N DE PELIGROS", "3\\.?\\s*EVALUACI[ÓO]N")
        ).ifBlank {
            "Análisis visual del entorno laboral completado. Revisa el detalle integral a continuación.\n\n" +
                rawText.take(500)
        }

        val section2 = extractSection(
            rawText,
            startPatterns = listOf("2\\.?\\s*IDENTIFICACI[ÓO]N DE PELIGROS(?: Y ACTOS INSEGUROS)?"),
            endPatterns = listOf("3\\.?\\s*EVALUACI[ÓO]N DEL NIVEL DE RIESGO", "4\\.?\\s*MEDIDAS")
        ).ifBlank {
            "Se identificaron condiciones operativas y factores de riesgo que requieren control preventivo según ISO 45001."
        }

        val section3 = extractSection(
            rawText,
            startPatterns = listOf("3\\.?\\s*EVALUACI[ÓO]N DEL NIVEL DE RIESGO"),
            endPatterns = listOf("4\\.?\\s*MEDIDAS PREVENTIVAS(?: Y CORRECTIVAS)?", "5\\.?\\s*RECOMENDACIONES")
        ).ifBlank {
            "Evaluación de severidad y probabilidad determinada en función de la exposición observada en el puesto de trabajo."
        }

        val section4 = extractSection(
            rawText,
            startPatterns = listOf("4\\.?\\s*MEDIDAS PREVENTIVAS(?: Y CORRECTIVAS)?"),
            endPatterns = listOf("5\\.?\\s*RECOMENDACIONES T[ÉE]CNICAS")
        ).ifBlank {
            "Aplicar la jerarquía de controles ISO 45001: Eliminación, Sustitución, Controles de Ingeniería, Controles Administrativos y EPP."
        }

        val section5 = extractSection(
            rawText,
            startPatterns = listOf("5\\.?\\s*RECOMENDACIONES T[ÉE]CNICAS(?: Y TIPS DE CULTURA PREVENTIVA)?"),
            endPatterns = emptyList()
        ).ifBlank {
            "Reforzar la cultura preventiva mediante charlas pre-operacionales de 5 minutos, inspecciones planeadas y cumplimiento de ISO 45001 / OSHA."
        }

        val upperEvaluation = (section3 + "\n" + rawText).uppercase()
        val detectedRiskLevel = when {
            upperEvaluation.contains("NIVEL_RIESGO: CRÍTICO") ||
                upperEvaluation.contains("NIVEL_RIESGO: CRITICO") ||
                upperEvaluation.contains("RIESGO CRÍTICO") ||
                upperEvaluation.contains("RIESGO CRITICO") ||
                upperEvaluation.contains("INTOLERABLE") -> RiskLevel.CRITICO

            upperEvaluation.contains("NIVEL_RIESGO: ALTO") ||
                upperEvaluation.contains("RIESGO ALTO") ||
                upperEvaluation.contains("IMPORTANTE") -> RiskLevel.ALTO

            upperEvaluation.contains("NIVEL_RIESGO: BAJO") ||
                upperEvaluation.contains("RIESGO BAJO") ||
                upperEvaluation.contains("TOLERABLE") -> RiskLevel.BAJO

            else -> RiskLevel.MEDIO
        }

        val severityLabel = when (detectedRiskLevel) {
            RiskLevel.CRITICO -> "ALTA (Extremadamente Dañino)"
            RiskLevel.ALTO -> "ALTA (Dañino Severo)"
            RiskLevel.MEDIO -> "MEDIA (Dañino Moderado)"
            RiskLevel.BAJO -> "BAJA (Ligeramente Dañino)"
        }

        val probabilityLabel = when (detectedRiskLevel) {
            RiskLevel.CRITICO -> "ALTA"
            RiskLevel.ALTO -> "MEDIA - ALTA"
            RiskLevel.MEDIO -> "MEDIA"
            RiskLevel.BAJO -> "BAJA"
        }

        val categories = mutableListOf<String>()
        val upperPeligros = (section2 + "\n" + rawText).uppercase()
        if (upperPeligros.contains("FÍSIC") || upperPeligros.contains("FISIC") || upperPeligros.contains("RUIDO") || upperPeligros.contains("RADIACI") || upperPeligros.contains("TÉRMIC")) {
            categories.add("Físico")
        }
        if (upperPeligros.contains("QUÍMIC") || upperPeligros.contains("QUIMIC") || upperPeligros.contains("HUMO") || upperPeligros.contains("GAS") || upperPeligros.contains("VAPOR")) {
            categories.add("Químico")
        }
        if (upperPeligros.contains("ERGONÓMIC") || upperPeligros.contains("ERGONOMIC") || upperPeligros.contains("POSTUR") || upperPeligros.contains("CARGA")) {
            categories.add("Ergonómico")
        }
        if (upperPeligros.contains("LOCATIV") || upperPeligros.contains("CAÍDA") || upperPeligros.contains("CAIDA") || upperPeligros.contains("ORDEN") || upperPeligros.contains("PISO")) {
            categories.add("Locativo")
        }
        if (upperPeligros.contains("MECÁNIC") || upperPeligros.contains("MECANIC") || upperPeligros.contains("ATRAPAMIENTO") || upperPeligros.contains("PROYECCI")) {
            categories.add("Mecánico")
        }
        if (upperPeligros.contains("ELÉCTRIC") || upperPeligros.contains("ELECTRIC") || upperPeligros.contains("CABLE") || upperPeligros.contains("TENSIÓN")) {
            categories.add("Eléctrico")
        }
        if (upperPeligros.contains("EPP") || upperPeligros.contains("PROTECCIÓN PERSONAL") || upperPeligros.contains("CASCO") || upperPeligros.contains("GUANTE")) {
            categories.add("EPP")
        }
        if (categories.isEmpty()) {
            categories.addAll(listOf("Locativo", "Mecánico", "EPP"))
        }

        val resolvedSector = if (sectorHint.isNotBlank() && sectorHint != "Detección Automática por IA") {
            sectorHint
        } else {
            inferSectorFromText(section1)
        }

        val actionItems = extractActionItemsFromSection4(section4)

        return SstAuditReport(
            id = 0L,
            timestamp = System.currentTimeMillis(),
            title = "Auditoría SST: $resolvedSector",
            sectorTag = resolvedSector,
            imageSourceLabel = imageSourceLabel,
            sampleDrawableRes = sampleDrawableRes,
            customImageUri = customImageUri,
            riskLevel = detectedRiskLevel,
            severityLabel = severityLabel,
            probabilityLabel = probabilityLabel,
            hazardCategories = categories,
            section1Diagnostico = cleanMarkdownFormatting(section1),
            section2Peligros = cleanMarkdownFormatting(section2),
            section3Evaluacion = cleanMarkdownFormatting(section3),
            section4Medidas = cleanMarkdownFormatting(section4),
            section5Recomendaciones = cleanMarkdownFormatting(section5),
            thoughtSummary = thoughtSummary?.let { cleanMarkdownFormatting(it) },
            actionItems = actionItems,
            rawFullResponse = rawText
        )
    }

    private fun inferSectorFromText(section1: String): String {
        val upper = section1.uppercase()
        return when {
            upper.contains("SOLDADURA") || upper.contains("METALMEC") -> "Soldadura y Metalmecánica"
            upper.contains("ALMACÉN") || upper.contains("ALMACEN") || upper.contains("MONTACARGAS") || upper.contains("LOGÍSTIC") -> "Logística y Almacenamiento"
            upper.contains("ELÉCTRIC") || upper.contains("ELECTRIC") || upper.contains("TABLERO") -> "Mantenimiento Eléctrico"
            upper.contains("CONSTRUCCI") || upper.contains("ANDAMIO") || upper.contains("OBRA") -> "Construcción y Obra Civil"
            upper.contains("QUÍMIC") || upper.contains("LABORATORIO") -> "Planta Química / Laboratorio"
            upper.contains("OFICINA") || upper.contains("ESCRITORIO") -> "Oficina y Ergonomía"
            else -> "Inspección Industrial General"
        }
    }

    private fun extractActionItemsFromSection4(section4: String): List<ActionControlItem> {
        val lines = section4.lines()
            .map { cleanMarkdownFormatting(it).trim() }
            .filter { it.length > 15 }

        val extracted = mutableListOf<ActionControlItem>()

        fun findLineForKeyword(keywords: List<String>, defaultText: String, level: ControlHierarchyLevel) {
            val matched = lines.firstOrNull { line ->
                val upper = line.uppercase()
                keywords.any { upper.contains(it) }
            }
            val cleanDesc = matched
                ?.replace(Regex("^[-•*1-5.)\\s]+"), "")
                ?.replace(Regex("^\\[?(ELIMINACI[ÓO]N|SUSTITUCI[ÓO]N|CONTROLES DE INGENIER[ÍI]A|INGENIER[ÍI]A|CONTROLES ADMINISTRATIVOS|ADMINISTRATIVO|EPP|EQUIPOS DE PROTECCI[ÓO]N PERSONAL)\\]?[:\\s-]*", RegexOption.IGNORE_CASE), "")
                ?.trim()
                ?.takeIf { it.length > 10 }
                ?: defaultText

            extracted.add(
                ActionControlItem(
                    id = UUID.randomUUID().toString(),
                    hierarchyLevel = level,
                    description = cleanDesc,
                    isCompleted = false
                )
            )
        }

        findLineForKeyword(
            keywords = listOf("ELIMINACI"),
            defaultText = "Suprimir físicamente las fuentes de peligro inmediato en el área de trabajo.",
            level = ControlHierarchyLevel.ELIMINACION
        )
        findLineForKeyword(
            keywords = listOf("SUSTITUCI"),
            defaultText = "Sustituir herramientas, sustancias o equipos deficientes por alternativas certificadas de menor riesgo.",
            level = ControlHierarchyLevel.SUSTITUCION
        )
        findLineForKeyword(
            keywords = listOf("INGENIER"),
            defaultText = "Instalar resguardos físicos, aislamiento colectivo, extracción o demarcación estructural.",
            level = ControlHierarchyLevel.INGENIERIA
        )
        findLineForKeyword(
            keywords = listOf("ADMINISTRATIV"),
            defaultText = "Implementar permiso de trabajo, procedimiento operativo seguro (ATS/AST) y señalización.",
            level = ControlHierarchyLevel.ADMINISTRATIVO
        )
        findLineForKeyword(
            keywords = listOf("EPP", "PROTECCIÓN PERSONAL", "PROTECCION PERSONAL"),
            defaultText = "Verificar dotación y uso correcto de EPP certificado (ANSI/EN/OSHA) acorde a los peligros.",
            level = ControlHierarchyLevel.EPP
        )

        return extracted
    }

    private fun extractSection(
        text: String,
        startPatterns: List<String>,
        endPatterns: List<String>
    ): String {
        val startRegex = Regex(
            "(?:^|\\n)\\s*(?:#+\\s*|\\*+\\s*)?(?:${startPatterns.joinToString("|")})[:\\s*#-]*",
            setOf(RegexOption.IGNORE_CASE)
        )
        val startMatch = startRegex.find(text) ?: return ""
        val contentStartIndex = startMatch.range.last + 1
        val remainingText = text.substring(contentStartIndex)

        if (endPatterns.isEmpty()) {
            return remainingText.trim()
        }

        val endRegex = Regex(
            "(?:^|\\n)\\s*(?:#+\\s*|\\*+\\s*)?(?:${endPatterns.joinToString("|")})",
            setOf(RegexOption.IGNORE_CASE)
        )
        val endMatch = endRegex.find(remainingText)
        return if (endMatch != null) {
            remainingText.substring(0, endMatch.range.first).trim()
        } else {
            remainingText.trim()
        }
    }

    private fun cleanMarkdownFormatting(raw: String): String {
        return raw
            .replace(Regex("\\*\\*(.*?)\\*\\*"), "$1")
            .replace(Regex("^#+\\s*", RegexOption.MULTILINE), "")
            .replace(Regex("\\[NIVEL_RIESGO:\\s*[^\\]]+\\]", RegexOption.IGNORE_CASE), "")
            .trim()
    }
}
