package com.example.ui.screens

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.RiskLevel
import com.example.model.SstAuditReport
import com.example.ui.components.RiskLevelBadge
import com.example.ui.components.accentColor
import com.example.ui.theme.RiskCritical
import com.example.ui.theme.RiskLow
import com.example.ui.theme.SafetyAmber500
import com.example.ui.theme.Slate950
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AuditHistoryScreen(
    audits: List<SstAuditReport>,
    selectedRiskFilter: RiskLevel?,
    onSelectRiskFilter: (RiskLevel?) -> Unit,
    onOpenAudit: (SstAuditReport) -> Unit,
    onDeleteAudit: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredAudits = remember(audits, selectedRiskFilter) {
        if (selectedRiskFilter == null) audits else audits.filter { it.riskLevel == selectedRiskFilter }
    }

    val criticalOrHighCount = remember(audits) {
        audits.count { it.riskLevel == RiskLevel.CRITICO || it.riskLevel == RiskLevel.ALTO }
    }
    val totalControls = remember(audits) { audits.sumOf { it.totalActionsCount } }
    val completedControls = remember(audits) { audits.sumOf { it.completedActionsCount } }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 720.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "BITÁCORA DE AUDITORÍAS SST",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Historial de Inspecciones y Controles",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // KPI Summary Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiStatCard(
                        title = "AUDITORÍAS",
                        value = audits.size.toString(),
                        subtitle = "Registradas",
                        accentColor = SafetyAmber500,
                        modifier = Modifier.weight(1f)
                    )
                    KpiStatCard(
                        title = "RIESGO ALTO/CRÍT.",
                        value = criticalOrHighCount.toString(),
                        subtitle = "Prioridad I-II",
                        accentColor = RiskCritical,
                        modifier = Modifier.weight(1f)
                    )
                    KpiStatCard(
                        title = "CONTROLES",
                        value = "$completedControls/$totalControls",
                        subtitle = "Verificados",
                        accentColor = RiskLow,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Filter Chips by Risk Level
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedRiskFilter == null,
                        onClick = { onSelectRiskFilter(null) },
                        label = { Text("Todos (${audits.size})") },
                        modifier = Modifier.testTag("filter_chip_all")
                    )
                    RiskLevel.entries.forEach { level ->
                        val count = audits.count { it.riskLevel == level }
                        FilterChip(
                            selected = selectedRiskFilter == level,
                            onClick = { onSelectRiskFilter(level) },
                            label = { Text("${level.name} ($count)") },
                            modifier = Modifier.testTag("filter_chip_${level.name}")
                        )
                    }
                }
            }

            if (filteredAudits.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FolderOpen,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No hay auditorías registradas en esta categoría",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Sube o captura una fotografía desde la pestaña 'Auditar' para generar un nuevo dictamen SST.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredAudits, key = { it.id }) { report ->
                    AuditHistoryItemCard(
                        report = report,
                        onClick = { onOpenAudit(report) },
                        onDelete = { onDeleteAudit(report.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun KpiStatCard(
    title: String,
    value: String,
    subtitle: String,
    accentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = accentColor,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AuditHistoryItemCard(
    report: SstAuditReport,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy · HH:mm", Locale.getDefault()) }
    val progress = if (report.totalActionsCount > 0) {
        report.completedActionsCount.toFloat() / report.totalActionsCount.toFloat()
    } else {
        0f
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("history_audit_card_${report.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, report.riskLevel.accentColor().copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Slate950)
                ) {
                    when {
                        report.sampleDrawableRes != null -> {
                            Image(
                                painter = painterResource(id = report.sampleDrawableRes),
                                contentDescription = report.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        report.customImageUri != null -> {
                            AsyncImage(
                                model = Uri.parse(report.customImageUri),
                                contentDescription = report.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        else -> {
                            Image(
                                painter = painterResource(id = R.drawable.img_sst_hero_banner),
                                contentDescription = report.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RiskLevelBadge(riskLevel = report.riskLevel)
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteOutline,
                                contentDescription = "Eliminar auditoría",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = report.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${report.sectorTag} · ${dateFormatter.format(Date(report.timestamp))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = report.section1Diagnostico.replace("\n", " "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Assignment,
                        contentDescription = null,
                        tint = SafetyAmber500,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Controles: ${report.completedActionsCount}/${report.totalActionsCount}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .width(110.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (progress >= 1f) RiskLow else SafetyAmber500
                )
            }
        }
    }
}
