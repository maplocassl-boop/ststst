package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.GppMaybe
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.ActionControlItem
import com.example.model.SstAuditReport
import com.example.ui.components.HierarchyControlBadge
import com.example.ui.components.RiskLevelBadge
import com.example.ui.components.RiskMatrixVisualCard
import com.example.ui.components.accentColor
import com.example.ui.theme.IndustrialCyan500
import com.example.ui.theme.RiskLow
import com.example.ui.theme.SafetyAmber500
import com.example.ui.theme.Slate950
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AuditReportDetailScreen(
    report: SstAuditReport,
    isAskingFollowUp: Boolean,
    statusBannerMessage: String?,
    onDismissBanner: () -> Unit,
    onToggleActionItem: (String) -> Unit,
    onAskFollowUp: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    var showThoughtProcess by rememberSaveable { mutableStateOf(false) }
    var followUpInput by rememberSaveable { mutableStateOf("") }
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy · HH:mm", Locale.getDefault()) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 720.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Action Bar
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("back_to_dashboard_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver"
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "DICTAMEN TÉCNICO DE AUDITORÍA SST",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = report.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Button(
                            onClick = {
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, report.title)
                                    putExtra(Intent.EXTRA_TEXT, report.toShareableFormattedText())
                                }
                                context.startActivity(
                                    Intent.createChooser(sendIntent, "Compartir Acta SST")
                                )
                            },
                            modifier = Modifier.testTag("share_report_button"),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = stringResource(R.string.share_audit_report),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Exportar",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }

            // Optional Status Banner
            if (statusBannerMessage != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clickable { onDismissBanner() },
                        colors = CardDefaults.cardColors(
                            containerColor = SafetyAmber500.copy(alpha = 0.14f)
                        ),
                        border = BorderStroke(1.dp, SafetyAmber500.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = statusBannerMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            // Audited Photo Header + Risk Summary Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.5.dp, report.riskLevel.accentColor().copy(alpha = 0.65f))
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(195.dp)
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
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Slate950.copy(alpha = 0.25f),
                                                Slate950.copy(alpha = 0.88f)
                                            )
                                        )
                                    )
                            )
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(14.dp)
                            ) {
                                RiskLevelBadge(riskLevel = report.riskLevel)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${report.sectorTag} · ${dateFormatter.format(Date(report.timestamp))}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }

                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "FACTORES DE PELIGRO DETECTADOS EN EL ENTORNO",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                report.hazardCategories.forEach { category ->
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                    ) {
                                        Text(
                                            text = "Riesgo $category",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Collapsible High Thinking Process Summary
            if (!report.thoughtSummary.isNullOrBlank()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clickable { showThoughtProcess = !showThoughtProcess },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        border = BorderStroke(1.dp, IndustrialCyan500.copy(alpha = 0.45f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Psychology,
                                    contentDescription = null,
                                    tint = IndustrialCyan500,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "CADENA DE RAZONAMIENTO PROFUNDO (THINKING LEVEL: HIGH)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = IndustrialCyan500,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = if (showThoughtProcess) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                    contentDescription = "Expandir razonamiento",
                                    tint = IndustrialCyan500
                                )
                            }
                            AnimatedVisibility(visible = showThoughtProcess) {
                                Column {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = report.thoughtSummary,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 1: DIAGNÓSTICO DEL ENTORNO
            item {
                AuditReportSectionCard(
                    sectionNumber = "01",
                    title = stringResource(R.string.section_1_title),
                    subtitle = "Actividad, puesto de trabajo y sector industrial",
                    icon = Icons.Filled.Factory,
                    accentColor = IndustrialCyan500,
                    bodyText = report.section1Diagnostico,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .testTag("section_1_card")
                )
            }

            // SECTION 2: IDENTIFICACIÓN DE PELIGROS Y ACTOS INSEGUROS
            item {
                AuditReportSectionCard(
                    sectionNumber = "02",
                    title = stringResource(R.string.section_2_title),
                    subtitle = "Riesgos físicos, químicos, ergonómicos, locativos, mecánicos, eléctricos y EPP",
                    icon = Icons.Filled.GppMaybe,
                    accentColor = report.riskLevel.accentColor(),
                    bodyText = report.section2Peligros,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .testTag("section_2_card")
                )
            }

            // SECTION 3: EVALUACIÓN DEL NIVEL DE RIESGO + MATRIZ VISUAL
            item {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .testTag("section_3_card"),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AuditReportSectionCard(
                        sectionNumber = "03",
                        title = stringResource(R.string.section_3_title),
                        subtitle = "Estimación de severidad y probabilidad de ocurrencia",
                        icon = Icons.Filled.Speed,
                        accentColor = report.riskLevel.accentColor(),
                        bodyText = report.section3Evaluacion
                    )
                    RiskMatrixVisualCard(
                        riskLevel = report.riskLevel,
                        severityLabel = report.severityLabel,
                        probabilityLabel = report.probabilityLabel
                    )
                }
            }

            // SECTION 4: MEDIDAS PREVENTIVAS Y CORRECTIVAS + CHECKLIST JERARQUÍA DE CONTROLES
            item {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .testTag("section_4_card"),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AuditReportSectionCard(
                        sectionNumber = "04",
                        title = stringResource(R.string.section_4_title),
                        subtitle = "Plan de acción aplicando la Jerarquía de Controles ISO 45001",
                        icon = Icons.Filled.Build,
                        accentColor = SafetyAmber500,
                        bodyText = report.section4Medidas
                    )

                    if (report.actionItems.isNotEmpty()) {
                        HierarchyOfControlsChecklistCard(
                            actionItems = report.actionItems,
                            completedCount = report.completedActionsCount,
                            totalCount = report.totalActionsCount,
                            onToggleItem = onToggleActionItem
                        )
                    }
                }
            }

            // SECTION 5: RECOMENDACIONES TÉCNICAS Y TIPS DE CULTURA PREVENTIVA
            item {
                AuditReportSectionCard(
                    sectionNumber = "05",
                    title = stringResource(R.string.section_5_title),
                    subtitle = "Normativas ISO 45001 / OSHA y dinámicas de concientización",
                    icon = Icons.Filled.Lightbulb,
                    accentColor = RiskLow,
                    bodyText = report.section5Recomendaciones,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .testTag("section_5_card")
                )
            }

            // SPECIALIST FOLLOW-UP CONSULTATION SECTION
            item {
                FollowUpSpecialistCard(
                    report = report,
                    followUpInput = followUpInput,
                    isAskingFollowUp = isAskingFollowUp,
                    onInputChange = { followUpInput = it },
                    onSubmitQuestion = { q ->
                        onAskFollowUp(q)
                        followUpInput = ""
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun AuditReportSectionCard(
    sectionNumber: String,
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    bodyText: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "SECCIÓN $sectionNumber",
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = bodyText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun HierarchyOfControlsChecklistCard(
    actionItems: List<ActionControlItem>,
    completedCount: Int,
    totalCount: Int,
    onToggleItem: (String) -> Unit
) {
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = BorderStroke(1.dp, SafetyAmber500.copy(alpha = 0.55f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.AssignmentTurnedIn,
                        contentDescription = null,
                        tint = SafetyAmber500,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "VERIFICACIÓN EN CAMPO · JERARQUÍA ISO 45001",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = SafetyAmber500
                    )
                }
                Text(
                    text = "$completedCount/$totalCount listos",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (completedCount == totalCount) RiskLow else MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (completedCount == totalCount) RiskLow else SafetyAmber500
            )
            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                actionItems.forEach { item ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onToggleItem(item.id) }
                            .testTag("control_item_${item.hierarchyLevel.name}"),
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (item.isCompleted) {
                                RiskLow.copy(alpha = 0.6f)
                            } else {
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = if (item.isCompleted) {
                                    Icons.Filled.CheckBox
                                } else {
                                    Icons.Filled.CheckBoxOutlineBlank
                                },
                                contentDescription = if (item.isCompleted) "Completado" else "Pendiente",
                                tint = if (item.isCompleted) RiskLow else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(22.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                HierarchyControlBadge(level = item.hierarchyLevel)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = item.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                    color = if (item.isCompleted) {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FollowUpSpecialistCard(
    report: SstAuditReport,
    followUpInput: String,
    isAskingFollowUp: Boolean,
    onInputChange: (String) -> Unit,
    onSubmitQuestion: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val quickPrompts = listOf(
        "Redacta una charla de 5 minutos para esta área",
        "¿Qué especificación técnica ANSI/EN requiere el EPP?",
        "¿Cómo aplicar bloqueo LOTO o Permiso de Trabajo aquí?"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, IndustrialCyan500.copy(alpha = 0.55f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(IndustrialCyan500.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Psychology,
                        contentDescription = null,
                        tint = IndustrialCyan500,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "CONSULTA TÉCNICA DE SEGUIMIENTO SST",
                        style = MaterialTheme.typography.labelMedium,
                        color = IndustrialCyan500,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Profundiza en normativas, EPP o planes de emergencia con High Thinking",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickPrompts.forEach { prompt ->
                    AssistChip(
                        onClick = { onSubmitQuestion(prompt) },
                        enabled = !isAskingFollowUp,
                        label = {
                            Text(
                                text = prompt,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    )
                }
            }

            if (report.followUpHistory.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    report.followUpHistory.forEach { exchange ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Supervisor: ${exchange.question}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = SafetyAmber500
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = exchange.answer,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = followUpInput,
                    onValueChange = onInputChange,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("followup_input"),
                    placeholder = { Text("Pregunta sobre ISO 45001, OSHA, controles...") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { onSubmitQuestion(followUpInput) },
                    enabled = followUpInput.isNotBlank() && !isAskingFollowUp,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .testTag("send_followup_button")
                ) {
                    if (isAskingFollowUp) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Enviar consulta",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}
