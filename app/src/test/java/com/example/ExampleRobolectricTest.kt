package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.remote.GeminiSstSpecialistRepository
import com.example.data.remote.ThinkingConfig
import com.example.data.remote.ThinkingLevel
import com.example.model.RiskLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches Auditoría SST`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Auditoría SST", appName)
    }

    @Test
    fun `parse 5 mandatory SST sections and high thinking config`() {
        val config = ThinkingConfig(thinkingLevel = ThinkingLevel.HIGH)
        assertEquals(ThinkingLevel.HIGH, config.thinkingLevel)

        val repo = GeminiSstSpecialistRepository()
        val sampleResponse = """
            ### 1. DIAGNÓSTICO DEL ENTORNO
            Taller de soldadura industrial con estructuras de acero.
            ### 2. IDENTIFICACIÓN DE PELIGROS Y ACTOS INSEGUROS
            Riesgo eléctrico por cables en piso y riesgo físico por radiación UV. Falta de EPP respiratorio.
            ### 3. EVALUACIÓN DEL NIVEL DE RIESGO
            Severidad alta y probabilidad alta. [NIVEL_RIESGO: CRÍTICO]
            ### 4. MEDIDAS PREVENTIVAS Y CORRECTIVAS
            - [ELIMINACIÓN]: Retirar solventes inflamables del radio de chispas.
            - [SUSTITUCIÓN]: Usar electrodos de baja emisión de humos.
            - [CONTROLES DE INGENIERÍA]: Instalar campana de extracción localizada.
            - [CONTROLES ADMINISTRATIVOS]: Emitir permiso de trabajo en caliente.
            - [EPP]: Utilizar careta ANSI Z87.1 y respirador P100.
            ### 5. RECOMENDACIONES TÉCNICAS Y TIPS DE CULTURA PREVENTIVA
            Aplicar ISO 45001:2018 y charla de 5 minutos pre-operacional.
        """.trimIndent()

        val report = repo.parseSstResponseToReport(
            rawText = sampleResponse,
            thoughtSummary = "Cadena de pensamiento técnico SST",
            sectorHint = "Soldadura y Metalmecánica",
            imageSourceLabel = "Prueba Unitaria",
            sampleDrawableRes = null,
            customImageUri = null
        )

        assertEquals(RiskLevel.CRITICO, report.riskLevel)
        assertTrue(report.section1Diagnostico.contains("Taller de soldadura"))
        assertTrue(report.section2Peligros.contains("Riesgo eléctrico"))
        assertEquals(5, report.actionItems.size)
        assertTrue(report.section5Recomendaciones.contains("ISO 45001:2018"))
    }
}
