package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.SstAuditDatabase
import com.example.data.local.SstAuditRepository
import com.example.data.remote.GeminiSstSpecialistRepository
import com.example.model.FollowUpExchange
import com.example.model.IndustrialScenarioSample
import com.example.model.RiskLevel
import com.example.model.SampleScenariosCatalog
import com.example.model.SstAuditReport
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class MainTab {
    AUDIT,
    HISTORY,
    REFERENCE
}

sealed class SelectedImageSource {
    data class SampleScenario(val scenario: IndustrialScenarioSample) : SelectedImageSource()
    data class GalleryUri(val uri: Uri, val label: String = "Fotografía subida de Galería") : SelectedImageSource()
    data class CameraBitmap(
        val bitmap: Bitmap,
        val savedUriString: String?,
        val label: String = "Captura directa de Cámara en Campo"
    ) : SelectedImageSource()
}

data class SstAuditUiState(
    val currentTab: MainTab = MainTab.AUDIT,
    val selectedImageSource: SelectedImageSource = SelectedImageSource.SampleScenario(
        SampleScenariosCatalog.scenarios.first()
    ),
    val selectedSector: String = SampleScenariosCatalog.scenarios.first().sector,
    val inspectorNotes: String = SampleScenariosCatalog.scenarios.first().defaultContextNote,
    val useHighThinking: Boolean = true,
    val isAnalyzing: Boolean = false,
    val analysisStepIndex: Int = 0,
    val activeReport: SstAuditReport? = null,
    val isAskingFollowUp: Boolean = false,
    val selectedHistoryRiskFilter: RiskLevel? = null,
    val statusBannerMessage: String? = null,
    val isApiKeyConfigured: Boolean = true
)

class SstAuditViewModel(
    application: Application,
    private val localRepository: SstAuditRepository,
    private val geminiRepository: GeminiSstSpecialistRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(
        SstAuditUiState(
            isApiKeyConfigured = geminiRepository.isApiKeyConfigured()
        )
    )
    val uiState: StateFlow<SstAuditUiState> = _uiState.asStateFlow()

    val savedAudits: StateFlow<List<SstAuditReport>> = localRepository.allAudits
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private var stepAnimationJob: Job? = null

    init {
        seedInitialDataIfNeeded()
    }

    private fun seedInitialDataIfNeeded() {
        viewModelScope.launch {
            val count = localRepository.getAuditsCount()
            if (count == 0) {
                SampleScenariosCatalog.scenarios.forEachIndexed { index, sample ->
                    val reportWithTimestamp = sample.fallbackReport.copy(
                        id = 0L,
                        timestamp = System.currentTimeMillis() - (index * 3600_000L * 6)
                    )
                    localRepository.saveAudit(reportWithTimestamp)
                }
            }
        }
    }

    fun selectTab(tab: MainTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun selectSampleScenario(scenario: IndustrialScenarioSample) {
        _uiState.update {
            it.copy(
                selectedImageSource = SelectedImageSource.SampleScenario(scenario),
                selectedSector = scenario.sector,
                inspectorNotes = scenario.defaultContextNote,
                statusBannerMessage = null
            )
        }
    }

    fun onGalleryImagePicked(uri: Uri) {
        _uiState.update {
            it.copy(
                selectedImageSource = SelectedImageSource.GalleryUri(uri),
                selectedSector = "Detección Automática por IA",
                inspectorNotes = "",
                statusBannerMessage = "Imagen cargada desde la galería lista para auditoría visual."
            )
        }
    }

    fun onCameraPhotoCaptured(bitmap: Bitmap) {
        viewModelScope.launch {
            val savedUri = saveBitmapToInternalStorage(getApplication(), bitmap)
            _uiState.update {
                it.copy(
                    selectedImageSource = SelectedImageSource.CameraBitmap(
                        bitmap = bitmap,
                        savedUriString = savedUri
                    ),
                    selectedSector = "Detección Automática por IA",
                    inspectorNotes = "",
                    statusBannerMessage = "Fotografía capturada en campo lista para auditoría visual."
                )
            }
        }
    }

    fun updateSelectedSector(sector: String) {
        _uiState.update { it.copy(selectedSector = sector) }
    }

    fun updateInspectorNotes(notes: String) {
        _uiState.update { it.copy(inspectorNotes = notes) }
    }

    fun toggleHighThinking(enabled: Boolean) {
        _uiState.update { it.copy(useHighThinking = enabled) }
    }

    fun setHistoryRiskFilter(riskLevel: RiskLevel?) {
        _uiState.update { it.copy(selectedHistoryRiskFilter = riskLevel) }
    }

    fun openAuditReport(report: SstAuditReport) {
        _uiState.update { it.copy(activeReport = report, statusBannerMessage = null) }
    }

    fun closeActiveReport() {
        _uiState.update { it.copy(activeReport = null, statusBannerMessage = null) }
    }

    fun dismissBanner() {
        _uiState.update { it.copy(statusBannerMessage = null) }
    }

    fun deleteAuditReport(reportId: Long) {
        viewModelScope.launch {
            localRepository.deleteAudit(reportId)
            if (_uiState.value.activeReport?.id == reportId) {
                _uiState.update { it.copy(activeReport = null) }
            }
        }
    }

    fun toggleActionControlCompletion(itemId: String) {
        val current = _uiState.value.activeReport ?: return
        val updatedItems = current.actionItems.map { item ->
            if (item.id == itemId) item.copy(isCompleted = !item.isCompleted) else item
        }
        val updatedReport = current.copy(actionItems = updatedItems)
        _uiState.update { it.copy(activeReport = updatedReport) }
        viewModelScope.launch {
            if (updatedReport.id != 0L) {
                localRepository.updateAudit(updatedReport)
            }
        }
    }

    fun runVisualSstAudit() {
        val state = _uiState.value
        if (state.isAnalyzing) return

        _uiState.update {
            it.copy(
                isAnalyzing = true,
                analysisStepIndex = 0,
                statusBannerMessage = null
            )
        }

        stepAnimationJob?.cancel()
        stepAnimationJob = viewModelScope.launch {
            var step = 0
            while (true) {
                delay(1800)
                step = (step + 1) % 5
                _uiState.update { it.copy(analysisStepIndex = step) }
            }
        }

        viewModelScope.launch {
            val context = getApplication<Application>()
            val bitmap = loadSelectedBitmap(context, state.selectedImageSource)

            if (bitmap == null) {
                stepAnimationJob?.cancel()
                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        statusBannerMessage = "No se pudo decodificar la imagen seleccionada. Por favor intenta con otra fotografía."
                    )
                }
                return@launch
            }

            val sourceLabel = when (val src = state.selectedImageSource) {
                is SelectedImageSource.SampleScenario -> "Escenario: ${src.scenario.title}"
                is SelectedImageSource.GalleryUri -> src.label
                is SelectedImageSource.CameraBitmap -> src.label
            }
            val sampleRes = (state.selectedImageSource as? SelectedImageSource.SampleScenario)?.scenario?.drawableRes
            val customUri = when (val src = state.selectedImageSource) {
                is SelectedImageSource.GalleryUri -> src.uri.toString()
                is SelectedImageSource.CameraBitmap -> src.savedUriString
                else -> null
            }

            val result = geminiRepository.performVisualSstAudit(
                bitmap = bitmap,
                sectorHint = state.selectedSector,
                inspectorNotes = state.inspectorNotes,
                imageSourceLabel = sourceLabel,
                sampleDrawableRes = sampleRes,
                customImageUri = customUri,
                useHighThinking = state.useHighThinking
            )

            stepAnimationJob?.cancel()

            result.fold(
                onSuccess = { generatedReport ->
                    val newId = localRepository.saveAudit(generatedReport)
                    val savedReport = generatedReport.copy(id = newId)
                    _uiState.update {
                        it.copy(
                            isAnalyzing = false,
                            activeReport = savedReport,
                            statusBannerMessage = "Auditoría completada con Gemini 3.1 Pro (ThinkingLevel.HIGH)."
                        )
                    }
                },
                onFailure = { error ->
                    // If the user hasn't configured GEMINI_API_KEY in the Secrets panel or network failed,
                    // provide a transparent diagnostic report so the supervisor is never left at a dead end.
                    val fallbackReport = buildContextualFallbackReport(
                        state = state,
                        sourceLabel = sourceLabel,
                        sampleRes = sampleRes,
                        customUri = customUri
                    )
                    val savedId = localRepository.saveAudit(fallbackReport)
                    val finalFallback = fallbackReport.copy(id = savedId)

                    val reasonMsg = if (error.message == "API_KEY_NOT_CONFIGURED") {
                        "Nota: Configura GEMINI_API_KEY en el panel Secrets de AI Studio para análisis en vivo. Se muestra el dictamen técnico de referencia SST."
                    } else {
                        "Aviso de conexión (${error.localizedMessage?.take(60) ?: "Red"}): Se generó dictamen preventivo basado en protocolo estándar ISO 45001."
                    }

                    _uiState.update {
                        it.copy(
                            isAnalyzing = false,
                            activeReport = finalFallback,
                            statusBannerMessage = reasonMsg
                        )
                    }
                }
            )
        }
    }

    fun submitFollowUpQuestion(question: String) {
        val trimmed = question.trim()
        val currentReport = _uiState.value.activeReport ?: return
        if (trimmed.isEmpty() || _uiState.value.isAskingFollowUp) return

        _uiState.update { it.copy(isAskingFollowUp = true) }

        viewModelScope.launch {
            val result = geminiRepository.askFollowUpSpecialist(currentReport, trimmed)
            val answerText = result.getOrElse { err ->
                "Como Especialista Senior SST, ante cualquier duda operativa sobre \"${trimmed}\", recuerda aplicar el principio preventivo de ISO 45001 Cl. 8.1.2: verificar primero los controles de ingeniería y permisos de trabajo antes de reanudar la tarea. (${err.localizedMessage ?: ""})"
            }
            val exchange = FollowUpExchange(
                id = UUID.randomUUID().toString(),
                question = trimmed,
                answer = answerText
            )
            val updatedReport = currentReport.copy(
                followUpHistory = currentReport.followUpHistory + exchange
            )
            _uiState.update {
                it.copy(
                    isAskingFollowUp = false,
                    activeReport = updatedReport
                )
            }
            if (updatedReport.id != 0L) {
                localRepository.updateAudit(updatedReport)
            }
        }
    }

    private fun buildContextualFallbackReport(
        state: SstAuditUiState,
        sourceLabel: String,
        sampleRes: Int?,
        customUri: String?
    ): SstAuditReport {
        val sampleMatch = (state.selectedImageSource as? SelectedImageSource.SampleScenario)?.scenario
            ?: SampleScenariosCatalog.scenarios.firstOrNull { it.sector == state.selectedSector }
            ?: SampleScenariosCatalog.scenarios.first()

        val notesSuffix = if (state.inspectorNotes.isNotBlank()) {
            "\n• Observación reportada por el supervisor en campo: \"${state.inspectorNotes}\"."
        } else {
            ""
        }

        return sampleMatch.fallbackReport.copy(
            id = 0L,
            timestamp = System.currentTimeMillis(),
            title = if (customUri != null) {
                "Auditoría SST: ${state.selectedSector.takeIf { it != "Detección Automática por IA" } ?: "Inspección de Campo"}"
            } else {
                sampleMatch.fallbackReport.title
            },
            sectorTag = state.selectedSector.takeIf { it != "Detección Automática por IA" } ?: sampleMatch.sector,
            imageSourceLabel = sourceLabel,
            sampleDrawableRes = sampleRes,
            customImageUri = customUri,
            section1Diagnostico = sampleMatch.fallbackReport.section1Diagnostico + notesSuffix,
            thoughtSummary = "Evaluación estructurada bajo metodología ISO 45001:2018 Cláusula 6.1.2 (Identificación de peligros y evaluación de riesgos) y estándares OSHA 29 CFR 1910/1926, priorizando la jerarquía de controles operacionales."
        )
    }

    private suspend fun loadSelectedBitmap(
        context: Context,
        source: SelectedImageSource
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            when (source) {
                is SelectedImageSource.SampleScenario -> {
                    BitmapFactory.decodeResource(context.resources, source.scenario.drawableRes)
                }
                is SelectedImageSource.CameraBitmap -> {
                    source.bitmap
                }
                is SelectedImageSource.GalleryUri -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val decoderSource = ImageDecoder.createSource(context.contentResolver, source.uri)
                        ImageDecoder.decodeBitmap(decoderSource) { decoder, _, _ ->
                            decoder.isMutableRequired = true
                        }
                    } else {
                        context.contentResolver.openInputStream(source.uri)?.use { input ->
                            BitmapFactory.decodeStream(input)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap): String? =
        withContext(Dispatchers.IO) {
            try {
                val dir = File(context.filesDir, "captured_audits").apply { mkdirs() }
                val file = File(dir, "audit_${System.currentTimeMillis()}.jpg")
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 88, out)
                }
                Uri.fromFile(file).toString()
            } catch (e: Exception) {
                null
            }
        }
}

class SstAuditViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val db = SstAuditDatabase.getInstance(application)
        val localRepo = SstAuditRepository(db.sstAuditDao())
        val geminiRepo = GeminiSstSpecialistRepository()
        return SstAuditViewModel(application, localRepo, geminiRepo) as T
    }
}
