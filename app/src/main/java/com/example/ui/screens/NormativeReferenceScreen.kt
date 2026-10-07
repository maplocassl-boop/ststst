package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.ControlHierarchyLevel
import com.example.model.RiskLevel
import com.example.ui.components.HierarchyControlBadge
import com.example.ui.components.RiskLevelBadge
import com.example.ui.components.accentColor
import com.example.ui.theme.IndustrialCyan500
import com.example.ui.theme.SafetyAmber500

@Composable
fun NormativeReferenceScreen(
    modifier: Modifier = Modifier
) {
    var selectedControlLevel by rememberSaveable {
        mutableStateOf<ControlHierarchyLevel?>(ControlHierarchyLevel.ELIMINACION)
    }
    var selectedSeverityIndex by rememberSaveable { mutableIntStateOf(2) } // 0: Baja, 1: Media, 2: Alta
    var selectedProbabilityIndex by rememberSaveable { mutableIntStateOf(2) } // 0: Baja, 1: Media, 2: Alta

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 720.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "MANUAL TÉCNICO DEL AUDITOR SENIOR",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Jerarquía ISO 45001, Matriz y Normativa OSHA",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 1. Interactive Hierarchy of Controls Pyramid (ISO 45001 Cl. 8.1.2)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Layers,
                                contentDescription = null,
                                tint = SafetyAmber500,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "JERARQUÍA DE CONTROLES OPERACIONALES",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = SafetyAmber500,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Toca cada peldaño de ISO 45001 Cláusula 8.1.2 para ver criterios de auditoría",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ControlHierarchyLevel.entries.forEach { level ->
                                val isExpanded = selectedControlLevel == level
                                val color = level.accentColor()
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            selectedControlLevel = if (isExpanded) null else level
                                        }
                                        .testTag("hierarchy_level_${level.name}"),
                                    color = if (isExpanded) color.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                    border = BorderStroke(
                                        width = if (isExpanded) 1.5.dp else 1.dp,
                                        color = if (isExpanded) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                HierarchyControlBadge(level = level)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = level.effectiveness,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Icon(
                                                imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                                contentDescription = null,
                                                tint = color
                                            )
                                        }

                                        AnimatedVisibility(visible = isExpanded) {
                                            Column {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = level.description,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Referencia: ${level.isoReference}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = color,
                                                    fontWeight = FontWeight.Bold
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

            // 2. Interactive Risk Level Calculator (Severidad x Probabilidad)
            item {
                val calculatedRisk = when {
                    selectedSeverityIndex == 2 && selectedProbabilityIndex == 2 -> RiskLevel.CRITICO
                    (selectedSeverityIndex == 2 && selectedProbabilityIndex == 1) ||
                        (selectedSeverityIndex == 1 && selectedProbabilityIndex == 2) -> RiskLevel.ALTO
                    (selectedSeverityIndex == 2 && selectedProbabilityIndex == 0) ||
                        (selectedSeverityIndex == 1 && selectedProbabilityIndex == 1) ||
                        (selectedSeverityIndex == 0 && selectedProbabilityIndex == 2) -> RiskLevel.MEDIO
                    else -> RiskLevel.BAJO
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, IndustrialCyan500.copy(alpha = 0.55f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Calculate,
                                contentDescription = null,
                                tint = IndustrialCyan500,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "CALCULADORA RÁPIDA DE NIVEL DE RIESGO",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = IndustrialCyan500,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Simulador en campo de Severidad × Probabilidad (Metodología INSST / GTC 45)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "1. Severidad del Daño Potencial:",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Baja (Leve)", "Media (Dañino)", "Alta (Grave/Fatal)").forEachIndexed { idx, label ->
                                FilterChip(
                                    selected = selectedSeverityIndex == idx,
                                    onClick = { selectedSeverityIndex = idx },
                                    label = { Text(label, style = MaterialTheme.typography.bodySmall) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "2. Probabilidad de Ocurrencia:",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Baja (Remota)", "Media (Posible)", "Alta (Inminente)").forEachIndexed { idx, label ->
                                FilterChip(
                                    selected = selectedProbabilityIndex == idx,
                                    onClick = { selectedProbabilityIndex = idx },
                                    label = { Text(label, style = MaterialTheme.typography.bodySmall) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Dictamen de Matriz:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            RiskLevelBadge(riskLevel = calculatedRisk)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = calculatedRisk.actionTimeframe,
                            style = MaterialTheme.typography.bodyMedium,
                            color = calculatedRisk.accentColor(),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // 3. Key Normative Standards Reference Cards
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Gavel,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "MARCO NORMATIVO DE REFERENCIA EN AUDITORÍAS",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        NormativeStandardItem(
                            code = "ISO 45001:2018 · Cláusulas 6.1.2 y 8.1.2",
                            title = "Sistemas de Gestión de la SST",
                            summary = "Exige la identificación proactiva de peligros continuos, evaluación de riesgos laborales y aplicación estricta de la Jerarquía de Controles para eliminar peligros."
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        NormativeStandardItem(
                            code = "OSHA 29 CFR 1910 (Industria) / 1926 (Construcción)",
                            title = "Estándares de Seguridad Operativa y EPP",
                            summary = "Regula superficies de tránsito (Subparte D), bloqueo de energías peligrosas LOTO (1910.147), trabajo en caliente (1910.252), seguridad eléctrica (Subparte S) y selección de EPP (1910.132)."
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        NormativeStandardItem(
                            code = "ISO 11228 / Ecuación NIOSH / EN 1005",
                            title = "Ergonomía e Higiene Postural Industrial",
                            summary = "Límites biomecánicos para levantamiento manual de cargas (máx. 23 kg en condiciones ideales), posturas forzadas y movimientos repetitivos de extremidades superiores."
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NormativeStandardItem(
    code: String,
    title: String,
    summary: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = code,
                style = MaterialTheme.typography.labelMedium,
                color = SafetyAmber500,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
