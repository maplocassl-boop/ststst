package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.model.ControlHierarchyLevel
import com.example.model.RiskLevel
import com.example.ui.theme.IndustrialCyan500
import com.example.ui.theme.RiskCritical
import com.example.ui.theme.RiskHigh
import com.example.ui.theme.RiskLow
import com.example.ui.theme.RiskMedium
import com.example.ui.theme.SafetyAmber500

fun RiskLevel.accentColor(): Color = when (this) {
    RiskLevel.CRITICO -> RiskCritical
    RiskLevel.ALTO -> RiskHigh
    RiskLevel.MEDIO -> RiskMedium
    RiskLevel.BAJO -> RiskLow
}

fun ControlHierarchyLevel.accentColor(): Color = when (this) {
    ControlHierarchyLevel.ELIMINACION -> Color(0xFF10B981)
    ControlHierarchyLevel.SUSTITUCION -> Color(0xFF0EA5E9)
    ControlHierarchyLevel.INGENIERIA -> Color(0xFF6366F1)
    ControlHierarchyLevel.ADMINISTRATIVO -> Color(0xFFF59E0B)
    ControlHierarchyLevel.EPP -> Color(0xFFEC4899)
}

@Composable
fun RiskLevelBadge(
    riskLevel: RiskLevel,
    modifier: Modifier = Modifier
) {
    val color = riskLevel.accentColor()
    val icon = when (riskLevel) {
        RiskLevel.CRITICO -> Icons.Filled.Error
        RiskLevel.ALTO -> Icons.Filled.ReportProblem
        RiskLevel.MEDIO -> Icons.Filled.HealthAndSafety
        RiskLevel.BAJO -> Icons.Filled.CheckCircle
    }

    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.16f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.65f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = riskLevel.label,
                tint = color,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = riskLevel.badgeText,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun HierarchyControlBadge(
    level: ControlHierarchyLevel,
    modifier: Modifier = Modifier
) {
    val color = level.accentColor()
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.55f))
    ) {
        Text(
            text = "${level.orderNumber}. ${level.shortTag}",
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun RiskMatrixVisualCard(
    riskLevel: RiskLevel,
    severityLabel: String,
    probabilityLabel: String,
    modifier: Modifier = Modifier
) {
    val activeRow = when {
        severityLabel.uppercase().contains("ALTA") || severityLabel.uppercase().contains("EXTREM") -> 0
        severityLabel.uppercase().contains("MEDIA") -> 1
        else -> 2
    }
    val activeCol = when {
        probabilityLabel.uppercase().contains("ALTA") && !probabilityLabel.uppercase().contains("MEDIA") -> 2
        probabilityLabel.uppercase().contains("MEDIA") -> 1
        else -> 0
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MATRIZ DE RIESGO SST (SEVERIDAD × PROBABILIDAD)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Filled.Shield,
                    contentDescription = null,
                    tint = riskLevel.accentColor(),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "SEVERIDAD ESTIMADA",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = severityLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "PROBABILIDAD",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = probabilityLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3x3 Visual Grid (Rows: Severidad Alta/Media/Baja, Cols: Probabilidad Baja/Media/Alta)
            val rows = listOf("Sev. Alta", "Sev. Media", "Sev. Baja")
            val cols = listOf("Prob. Baja", "Prob. Media", "Prob. Alta")
            val matrixLabels = listOf(
                listOf("MEDIO" to RiskMedium, "ALTO" to RiskHigh, "CRÍTICO" to RiskCritical),
                listOf("BAJO" to RiskLow, "MEDIO" to RiskMedium, "ALTO" to RiskHigh),
                listOf("BAJO" to RiskLow, "BAJO" to RiskLow, "MEDIO" to RiskMedium)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(68.dp))
                cols.forEach { colTitle ->
                    Text(
                        text = colTitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            rows.forEachIndexed { rIdx, rowTitle ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = rowTitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(68.dp)
                    )
                    matrixLabels[rIdx].forEachIndexed { cIdx, (cellName, cellColor) ->
                        val isSelected = (rIdx == activeRow && cIdx == activeCol)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 3.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isSelected) cellColor else cellColor.copy(alpha = 0.16f)
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color.White else cellColor.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isSelected) "● $cellName" else cellName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else cellColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Acción Requerida: ${riskLevel.actionTimeframe}",
                style = MaterialTheme.typography.bodySmall,
                color = riskLevel.accentColor(),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun HighThinkingAnalysisProgressCard(
    stepIndex: Int,
    useHighThinking: Boolean,
    modifier: Modifier = Modifier
) {
    val steps = listOf(
        "1/5 · Diagnosticando entorno laboral, maquinaria y procesos visibles...",
        "2/5 · Escaneando peligros físicos, químicos, eléctricos, locativos y EPP...",
        "3/5 · Calculando matriz de Severidad × Probabilidad (ISO 45001 / GTC 45)...",
        "4/5 · Estructurando Jerarquía de Controles (Eliminación a EPP)...",
        "5/5 · Redactando normativas OSHA/ISO y tips de cultura preventiva..."
    )

    val transition = rememberInfiniteTransition(label = "thinking_pulse")
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(1.5.dp, SafetyAmber500.copy(alpha = 0.7f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SafetyAmber500.copy(alpha = 0.2f))
                        .alpha(pulseAlpha),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Psychology,
                        contentDescription = null,
                        tint = SafetyAmber500,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (useHighThinking) {
                            "AUDITORÍA PROFUNDA EN CURSO · THINKING LEVEL: HIGH"
                        } else {
                            "ANALIZANDO FOTOGRAFÍA LABORAL..."
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = SafetyAmber500,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Modelo: gemini-3.1-pro-preview · Especialista Senior SST",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = SafetyAmber500,
                trackColor = IndustrialCyan500.copy(alpha = 0.2f)
            )
            Spacer(modifier = Modifier.height(10.dp))

            AnimatedVisibility(visible = true) {
                Text(
                    text = steps.getOrElse(stepIndex) { steps.first() },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
