package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.outlined.FactCheck
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AuditHistoryScreen
import com.example.ui.screens.AuditReportDetailScreen
import com.example.ui.screens.NewAuditScreen
import com.example.ui.screens.NormativeReferenceScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MainTab
import com.example.viewmodel.SstAuditViewModel
import com.example.viewmodel.SstAuditViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true) {
                val viewModel: SstAuditViewModel = viewModel(
                    factory = SstAuditViewModelFactory(application)
                )
                SstAuditorApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun SstAuditorApp(
    viewModel: SstAuditViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val savedAudits by viewModel.savedAudits.collectAsStateWithLifecycle()

    // Handle back press when on non-default tab and not viewing a detail report
    BackHandler(enabled = uiState.activeReport == null && uiState.currentTab != MainTab.AUDIT) {
        viewModel.selectTab(MainTab.AUDIT)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isExpandedScreen = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                if (!isExpandedScreen && uiState.activeReport == null) {
                    SstBottomNavigationBar(
                        currentTab = uiState.currentTab,
                        savedAuditsCount = savedAudits.size,
                        onSelectTab = viewModel::selectTab
                    )
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isExpandedScreen && uiState.activeReport == null) {
                    SstNavigationRail(
                        currentTab = uiState.currentTab,
                        savedAuditsCount = savedAudits.size,
                        onSelectTab = viewModel::selectTab
                    )
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    val activeReport = uiState.activeReport
                    if (activeReport != null) {
                        AuditReportDetailScreen(
                            report = activeReport,
                            isAskingFollowUp = uiState.isAskingFollowUp,
                            statusBannerMessage = uiState.statusBannerMessage,
                            onDismissBanner = viewModel::dismissBanner,
                            onToggleActionItem = viewModel::toggleActionControlCompletion,
                            onAskFollowUp = viewModel::submitFollowUpQuestion,
                            onBack = viewModel::closeActiveReport
                        )
                    } else {
                        when (uiState.currentTab) {
                            MainTab.AUDIT -> {
                                NewAuditScreen(
                                    uiState = uiState,
                                    totalAuditsCount = savedAudits.size,
                                    onSelectScenario = viewModel::selectSampleScenario,
                                    onGalleryImagePicked = viewModel::onGalleryImagePicked,
                                    onCameraPhotoCaptured = viewModel::onCameraPhotoCaptured,
                                    onSelectSector = viewModel::updateSelectedSector,
                                    onUpdateNotes = viewModel::updateInspectorNotes,
                                    onToggleHighThinking = viewModel::toggleHighThinking,
                                    onRunAudit = viewModel::runVisualSstAudit
                                )
                            }
                            MainTab.HISTORY -> {
                                AuditHistoryScreen(
                                    audits = savedAudits,
                                    selectedRiskFilter = uiState.selectedHistoryRiskFilter,
                                    onSelectRiskFilter = viewModel::setHistoryRiskFilter,
                                    onOpenAudit = viewModel::openAuditReport,
                                    onDeleteAudit = viewModel::deleteAuditReport
                                )
                            }
                            MainTab.REFERENCE -> {
                                NormativeReferenceScreen()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SstBottomNavigationBar(
    currentTab: MainTab,
    savedAuditsCount: Int,
    onSelectTab: (MainTab) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        NavigationBarItem(
            selected = currentTab == MainTab.AUDIT,
            onClick = { onSelectTab(MainTab.AUDIT) },
            icon = {
                Icon(
                    imageVector = if (currentTab == MainTab.AUDIT) Icons.AutoMirrored.Filled.FactCheck else Icons.AutoMirrored.Outlined.FactCheck,
                    contentDescription = stringResource(R.string.nav_audit)
                )
            },
            label = { Text(stringResource(R.string.nav_audit)) },
            modifier = Modifier.testTag("nav_tab_audit")
        )

        NavigationBarItem(
            selected = currentTab == MainTab.HISTORY,
            onClick = { onSelectTab(MainTab.HISTORY) },
            icon = {
                BadgedBox(
                    badge = {
                        if (savedAuditsCount > 0) {
                            Badge { Text(savedAuditsCount.toString()) }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (currentTab == MainTab.HISTORY) Icons.Filled.History else Icons.Outlined.History,
                        contentDescription = stringResource(R.string.nav_history)
                    )
                }
            },
            label = { Text(stringResource(R.string.nav_history)) },
            modifier = Modifier.testTag("nav_tab_history")
        )

        NavigationBarItem(
            selected = currentTab == MainTab.REFERENCE,
            onClick = { onSelectTab(MainTab.REFERENCE) },
            icon = {
                Icon(
                    imageVector = if (currentTab == MainTab.REFERENCE) Icons.Filled.Gavel else Icons.Outlined.Gavel,
                    contentDescription = stringResource(R.string.nav_reference)
                )
            },
            label = { Text(stringResource(R.string.nav_reference)) },
            modifier = Modifier.testTag("nav_tab_reference")
        )
    }
}

@Composable
private fun SstNavigationRail(
    currentTab: MainTab,
    savedAuditsCount: Int,
    onSelectTab: (MainTab) -> Unit
) {
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        NavigationRailItem(
            selected = currentTab == MainTab.AUDIT,
            onClick = { onSelectTab(MainTab.AUDIT) },
            icon = {
                Icon(
                    imageVector = if (currentTab == MainTab.AUDIT) Icons.AutoMirrored.Filled.FactCheck else Icons.AutoMirrored.Outlined.FactCheck,
                    contentDescription = stringResource(R.string.nav_audit)
                )
            },
            label = { Text(stringResource(R.string.nav_audit)) },
            modifier = Modifier.testTag("rail_tab_audit")
        )

        NavigationRailItem(
            selected = currentTab == MainTab.HISTORY,
            onClick = { onSelectTab(MainTab.HISTORY) },
            icon = {
                BadgedBox(
                    badge = {
                        if (savedAuditsCount > 0) {
                            Badge { Text(savedAuditsCount.toString()) }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (currentTab == MainTab.HISTORY) Icons.Filled.History else Icons.Outlined.History,
                        contentDescription = stringResource(R.string.nav_history)
                    )
                }
            },
            label = { Text(stringResource(R.string.nav_history)) },
            modifier = Modifier.testTag("rail_tab_history")
        )

        NavigationRailItem(
            selected = currentTab == MainTab.REFERENCE,
            onClick = { onSelectTab(MainTab.REFERENCE) },
            icon = {
                Icon(
                    imageVector = if (currentTab == MainTab.REFERENCE) Icons.Filled.Gavel else Icons.Outlined.Gavel,
                    contentDescription = stringResource(R.string.nav_reference)
                )
            },
            label = { Text(stringResource(R.string.nav_reference)) },
            modifier = Modifier.testTag("rail_tab_reference")
        )
    }
}
