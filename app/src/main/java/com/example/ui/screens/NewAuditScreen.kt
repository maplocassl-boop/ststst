package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.R
import com.example.model.IndustrialScenarioSample
import com.example.model.SampleScenariosCatalog
import com.example.ui.components.HighThinkingAnalysisProgressCard
import com.example.ui.components.RiskLevelBadge
import com.example.ui.theme.SafetyAmber500
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.viewmodel.SelectedImageSource
import com.example.viewmodel.SstAuditUiState

@Composable
fun NewAuditScreen(
    uiState: SstAuditUiState,
    totalAuditsCount: Int,
    onSelectScenario: (IndustrialScenarioSample) -> Unit,
    onGalleryImagePicked: (android.net.Uri) -> Unit,
    onCameraPhotoCaptured: (android.graphics.Bitmap) -> Unit,
    onSelectSector: (String) -> Unit,
    onUpdateNotes: (String) -> Unit,
    onToggleHighThinking: (Boolean) -> Unit,
    onRunAudit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Zero-permission Android Photo Picker (Google Play Policy compliant)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onGalleryImagePicked(uri)
        }
    }

    // Camera capture preview launcher
    val cameraPreviewLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            onCameraPhotoCaptured(bitmap)
        }
    }

    // Runtime permission request for CAMERA
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraPreviewLauncher.launch(null)
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 720.dp),
            contentPadding = PaddingValues(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Industrial Hero Banner with Specialist Credentials
            item {
                SstSpecialistHeroBanner(totalAuditsCount = totalAuditsCount)
            }

            // 2. High Thinking Mode Banner (gemini-3.1-pro-preview + ThinkingLevel.HIGH)
            item {
                HighThinkingModeConfigCard(
                    enabled = uiState.useHighThinking,
                    onToggle = onToggleHighThinking,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // 3. Active Image Preview + Upload / Camera Actions
            item {
                WorkplaceImageSelectorCard(
                    selectedImageSource = uiState.selectedImageSource,
                    onPickGallery = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onCaptureCamera = {
                        val hasCameraPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                        if (hasCameraPermission) {
                            cameraPreviewLauncher.launch(null)
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // 4. Quick Industrial Scenario Presets (1-Tap Inspection Scenarios)
            item {
                SampleScenariosSection(
                    selectedSource = uiState.selectedImageSource,
                    onSelectScenario = onSelectScenario
                )
            }

            // 5. Sector & Inspector Context Notes
            item {
                AuditContextInputCard(
                    selectedSector = uiState.selectedSector,
                    inspectorNotes = uiState.inspectorNotes,
                    onSelectSector = onSelectSector,
                    onUpdateNotes = onUpdateNotes,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // 6. Progress Overlay or Primary Action CTA
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    if (uiState.isAnalyzing) {
                        HighThinkingAnalysisProgressCard(
                            stepIndex = uiState.analysisStepIndex,
                            useHighThinking = uiState.useHighThinking
                        )
                    } else {
                        Button(
                            onClick = onRunAudit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp)
                                .testTag("run_audit_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Analytics,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = stringResource(R.string.btn_run_audit),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = stringResource(R.string.security_prototype_notice),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SstSpecialistHeroBanner(
    totalAuditsCount: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(215.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_sst_hero_banner),
            contentDescription = "Especialista Senior en Seguridad y Salud en el Trabajo",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Slate950.copy(alpha = 0.45f),
                            Slate950.copy(alpha = 0.85f),
                            Slate950.copy(alpha = 0.96f)
                        )
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = SafetyAmber500,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.VerifiedUser,
                            contentDescription = null,
                            tint = Slate950,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "AUDITOR SENIOR SST · 20 AÑOS EXP.",
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate950,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Surface(
                    color = Slate900.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
                ) {
                    Text(
                        text = "ISO 45001 · OSHA",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Auditoría Visual y Análisis de Riesgos Laborales",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Diagnóstico de entorno, peligros, matriz de riesgo, jerarquía de controles y cultura preventiva ($totalAuditsCount actas registradas).",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.85f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun HighThinkingModeConfigCard(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (enabled) SafetyAmber500.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Psychology,
                contentDescription = null,
                tint = if (enabled) SafetyAmber500 else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.label_thinking_mode),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = SafetyAmber500.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "HIGH",
                            style = MaterialTheme.typography.labelSmall,
                            color = SafetyAmber500,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.desc_thinking_mode),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                modifier = Modifier.testTag("high_thinking_switch"),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Slate950,
                    checkedTrackColor = SafetyAmber500
                )
            )
        }
    }
}

@Composable
private fun WorkplaceImageSelectorCard(
    selectedImageSource: SelectedImageSource,
    onPickGallery: () -> Unit,
    onCaptureCamera: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.HealthAndSafety,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "EVIDENCIA VISUAL DEL PUESTO / ENTORNO",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Image preview box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Slate950)
            ) {
                when (selectedImageSource) {
                    is SelectedImageSource.SampleScenario -> {
                        Image(
                            painter = painterResource(id = selectedImageSource.scenario.drawableRes),
                            contentDescription = selectedImageSource.scenario.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    is SelectedImageSource.GalleryUri -> {
                        AsyncImage(
                            model = selectedImageSource.uri,
                            contentDescription = selectedImageSource.label,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    is SelectedImageSource.CameraBitmap -> {
                        Image(
                            bitmap = selectedImageSource.bitmap.asImageBitmap(),
                            contentDescription = selectedImageSource.label,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Bottom caption overlay inside image preview
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Slate950.copy(alpha = 0.88f))
                            )
                        )
                        .padding(12.dp)
                ) {
                    val captionText = when (selectedImageSource) {
                        is SelectedImageSource.SampleScenario ->
                            "Escenario seleccionado: ${selectedImageSource.scenario.title}"
                        is SelectedImageSource.GalleryUri ->
                            selectedImageSource.label
                        is SelectedImageSource.CameraBitmap ->
                            selectedImageSource.label
                    }
                    Text(
                        text = captionText,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onPickGallery,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("pick_photo_button"),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.btn_pick_photo),
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                OutlinedButton(
                    onClick = onCaptureCamera,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("take_photo_button"),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AddAPhoto,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.btn_take_photo),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
private fun SampleScenariosSection(
    selectedSource: SelectedImageSource,
    onSelectScenario: (IndustrialScenarioSample) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Factory,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "ESCENARIOS INDUSTRIALES DE INSPECCIÓN RÁPIDA",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(SampleScenariosCatalog.scenarios, key = { it.id }) { scenario ->
                val isSelected = (selectedSource as? SelectedImageSource.SampleScenario)?.scenario?.id == scenario.id
                Card(
                    modifier = Modifier
                        .width(245.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSelectScenario(scenario) }
                        .testTag("scenario_card_${scenario.id}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(105.dp)
                        ) {
                            Image(
                                painter = painterResource(id = scenario.drawableRes),
                                contentDescription = scenario.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            if (isSelected) {
                                Surface(
                                    color = SafetyAmber500,
                                    shape = RoundedCornerShape(bottomStart = 8.dp),
                                    modifier = Modifier.align(Alignment.TopEnd)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = "Seleccionado",
                                            tint = Slate950,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "ACTIVO",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Slate950,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = scenario.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = scenario.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            RiskLevelBadge(riskLevel = scenario.expectedRiskPreview)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AuditContextInputCard(
    selectedSector: String,
    inspectorNotes: String,
    onSelectSector: (String) -> Unit,
    onUpdateNotes: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "CONTEXTO OPERATIVO Y SECTOR INDUSTRIAL",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SampleScenariosCatalog.industrialSectors.forEach { sector ->
                    val isSelected = sector == selectedSector
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectSector(sector) },
                        label = {
                            Text(
                                text = sector,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = inspectorNotes,
                onValueChange = onUpdateNotes,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("inspector_notes_input"),
                label = { Text("Observaciones adicionales del supervisor (opcional)") },
                placeholder = {
                    Text("Ej. Ruido >85 dB, tarea no rutinaria, ausencia de extracción localizada...")
                },
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(10.dp)
            )
        }
    }
}
