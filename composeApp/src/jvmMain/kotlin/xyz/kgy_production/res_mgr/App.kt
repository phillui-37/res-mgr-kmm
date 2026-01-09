package xyz.kgy_production.res_mgr

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel
import xyz.kgy_production.res_mgr.di.clientModule
import xyz.kgy_production.res_mgr.model.ItemDto
import xyz.kgy_production.res_mgr.viewmodel.ResourceViewModel
import xyz.kgy_production.res_mgr.viewmodel.SettingsViewModel
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import java.nio.file.Files

@Composable
@Preview
fun App() {
    KoinApplication(application = {
        modules(clientModule)
    }) {
        MaterialTheme {
            var currentScreen by remember { mutableStateOf("main") }

            when(currentScreen) {
                "main" -> MainScreen(onNavigateSettings = { currentScreen = "settings" })
                "settings" -> SettingsScreen(onNavigateBack = { currentScreen = "main" })
            }
        }
    }
}

@Composable
fun MainScreen(onNavigateSettings: () -> Unit) {
    val viewModel = koinViewModel<ResourceViewModel>()
    val state by viewModel.uiState.collectAsState()

    // Derived tabs: "All" + categories
    // Map index to null (All) or CategoryDto
    val tabs = remember(state.categories) {
        listOf(null) + state.categories
    }

    val selectedIndex = tabs.indexOf(state.selectedCategory).let { if (it == -1) 0 else it }

    Scaffold(
        topBar = {
            Column {
                // Search & Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Search...") },
                        leadingIcon = { Icon(Icons.Filled.Search, "Search") }
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        openFileForImport { bytes -> viewModel.importBatch(bytes) }
                    }) {
                        Icon(Icons.Filled.Add, "Import")
                        Spacer(Modifier.width(4.dp))
                        Text("Import Batch")
                    }
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = onNavigateSettings) {
                         Icon(Icons.Filled.Settings, "Settings")
                    }
                }

                // Tabs
                ScrollableTabRow(selectedTabIndex = selectedIndex) {
                    tabs.forEachIndexed { index, category ->
                        Tab(
                            selected = selectedIndex == index,
                            onClick = { viewModel.selectCategory(category) },
                            text = { Text(category?.name ?: "All") }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.error != null) {
                Text(
                    text = "Error: ${state.error}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                ItemList(
                    items = state.items, // TODO: Filter by tab client-side or update query?
                    onDelete = { viewModel.deleteItem(it) }
                )
            }
        }
    }
}

@Composable
fun ItemList(items: List<ItemDto>, onDelete: (String) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { item ->
            ItemRow(item, onDelete)
        }
    }
}

@Composable
fun ItemRow(item: ItemDto, onDelete: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.name, style = MaterialTheme.typography.titleMedium)

                // Tags
                if (item.tags.isNotEmpty()) {
                    Text(
                        text = "Tags: ${item.tags.joinToString { it.name }}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Props
                if (item.props.isNotEmpty()) {
                    val propsStr = item.props.entries.joinToString { "${it.key}: ${it.value}" }
                    Text(
                        text = propsStr,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(Modifier.height(4.dp))
                Text(text = "ID: ${item.id}", style = MaterialTheme.typography.labelSmall)
            }
            IconButton(onClick = { onDelete(item.id) }) {
                Icon(Icons.Filled.Delete, "Delete")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateBack: () -> Unit) {
    val viewModel = koinViewModel<SettingsViewModel>()
    val state by viewModel.uiState.collectAsState()

    // Auto-navigate back if saved? Or show snackbar?
    // For now simple UI.

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                     Button(onClick = onNavigateBack) { Text("Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Client Configuration", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = state.serverUrl,
                onValueChange = { viewModel.updateServerUrl(it) },
                label = { Text("Server URL") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.clientName,
                onValueChange = { viewModel.updateClientName(it) },
                label = { Text("Client Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { viewModel.saveSettings() },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Save Settings")
            }

            if (state.isSaved) {
                Text("Settings Saved!", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

fun openFileForImport(onFileSelected: (ByteArray) -> Unit) {
    val fileChooser = JFileChooser()
    fileChooser.fileFilter = FileNameExtensionFilter("GZIP CSV", "gz")
    val result = fileChooser.showOpenDialog(null)
    if (result == JFileChooser.APPROVE_OPTION) {
        val file = fileChooser.selectedFile
        try {
            val bytes = Files.readAllBytes(file.toPath())
            onFileSelected(bytes)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}