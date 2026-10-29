package com.haps.android.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haps.android.model.PredictionResponse
import com.haps.android.ui.components.SkeletonList
import com.haps.android.ui.components.StatusPill
import com.haps.android.ui.viewmodel.HistoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    historyViewModel: HistoryViewModel,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToPredict: () -> Unit
) {
    val historyList by historyViewModel.filteredList.collectAsState()
    val filterRisk by historyViewModel.filterRisk.collectAsState()
    val searchQuery by historyViewModel.searchQuery.collectAsState()
    val isLoading by historyViewModel.isLoading.collectAsState()
    val isOnline by historyViewModel.isOnline.collectAsState()

    var showClearConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clinical History") },
                actions = {
                    IconButton(onClick = { historyViewModel.loadHistory() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    if (historyList.isNotEmpty()) {
                        IconButton(onClick = { showClearConfirm = true }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear All")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToPredict,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Assessment")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { historyViewModel.setSearchQuery(it) },
                placeholder = { Text("Search by ID, Risk level...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { historyViewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp)
            )

            // Risk filter tabs
            val tabs = listOf("ALL", "High Risk", "Moderate Risk", "Low Risk")
            ScrollableTabRow(
                selectedTabIndex = tabs.indexOf(filterRisk).coerceAtLeast(0),
                edgePadding = 0.dp,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                tabs.forEach { tab ->
                    Tab(
                        selected = filterRisk == tab,
                        onClick = { historyViewModel.setRiskFilter(tab) },
                        text = { Text(tab, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            // Offline Indicator Banner if disconnected
            if (!isOnline) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudOff, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Displaying cached records from local Room database", fontSize = 11.sp)
                    }
                }
            }

            // Main List content
            if (isLoading && historyList.isEmpty()) {
                SkeletonList(count = 5)
            } else if (historyList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No assessments found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Execute your first cardiac prediction to populate clinical history.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(historyList, key = { it.id ?: it.hashCode() }) { item ->
                        HistoryCardItem(
                            item = item,
                            onClick = { item.id?.let { onNavigateToDetail(it) } },
                            onDelete = { item.id?.let { historyViewModel.deleteItem(it) } }
                        )
                    }
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear All Prediction History?") },
            text = { Text("This will remove all stored assessments from local offline cache.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        historyViewModel.clearAllHistory()
                        showClearConfirm = false
                    }
                ) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun HistoryCardItem(
    item: PredictionResponse,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Assessment #${item.id ?: "0"}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusPill(riskLevel = item.riskLevel)
                }

                Spacer(modifier = Modifier.height(4.dp))

                val formattedDate = item.createdAt?.take(16)?.replace("T", " ") ?: "Recorded Assessment"
                Text(
                    text = "Risk Probability: ${(item.probability * 100).toInt()}% • $formattedDate",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}
