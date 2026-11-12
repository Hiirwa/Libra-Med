package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.*
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.LibraryMedicineTheme

sealed class AppScreen {
    object Login : AppScreen()
    object Home : AppScreen()
    data class ProjectDetail(val projectUuid: String) : AppScreen()
    data class RecordEditor(val projectUuid: String, val recordUuid: String?) : AppScreen()
    object SyncCenter : AppScreen()
    object Settings : AppScreen()
    object Help : AppScreen()
}

data class NavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val screen: AppScreen
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as LibraryMedicineApp
        val repository = app.repository

        setContent {
            var isDarkTheme by remember { mutableStateOf(false) }
            var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Home) }
            val outbox by repository.outbox.collectAsState()

            val navItems = listOf(
                NavItem("Projects", Icons.Filled.Folder, Icons.Outlined.Folder, AppScreen.Home),
                NavItem("Sync", Icons.Filled.Sync, Icons.Outlined.Sync, AppScreen.SyncCenter),
                NavItem("Settings", Icons.Filled.Settings, Icons.Outlined.Settings, AppScreen.Settings),
                NavItem("Help", Icons.Filled.Help, Icons.Outlined.HelpOutline, AppScreen.Help)
            )

            val showBottomNav = currentScreen is AppScreen.Home ||
                    currentScreen is AppScreen.SyncCenter ||
                    currentScreen is AppScreen.Settings ||
                    currentScreen is AppScreen.Help

            LibraryMedicineTheme(darkTheme = isDarkTheme) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomNav) {
                            NavigationBar {
                                navItems.forEach { item ->
                                    val isSelected = when (currentScreen) {
                                        is AppScreen.Home -> item.screen is AppScreen.Home
                                        is AppScreen.SyncCenter -> item.screen is AppScreen.SyncCenter
                                        is AppScreen.Settings -> item.screen is AppScreen.Settings
                                        is AppScreen.Help -> item.screen is AppScreen.Help
                                        else -> false
                                    }

                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = { currentScreen = item.screen },
                                        label = { Text(item.title) },
                                        icon = {
                                            if (item.title == "Sync" && outbox.isNotEmpty()) {
                                                BadgedBox(badge = { Badge { Text("${outbox.size}") } }) {
                                                    Icon(
                                                        if (isSelected) item.selectedIcon else item.unselectedIcon,
                                                        contentDescription = item.title
                                                    )
                                                }
                                            } else {
                                                Icon(
                                                    if (isSelected) item.selectedIcon else item.unselectedIcon,
                                                    contentDescription = item.title
                                                )
                                            }
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            indicatorColor = BrandBlue.copy(alpha = 0.15f)
                                        )
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    when (val screen = currentScreen) {
                        is AppScreen.Login -> {
                            LoginScreen(
                                repository = repository,
                                onLoginSuccess = { currentScreen = AppScreen.Home },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        is AppScreen.Home -> {
                            HomeScreen(
                                repository = repository,
                                onSelectProject = { uuid ->
                                    currentScreen = AppScreen.ProjectDetail(uuid)
                                },
                                onNewRecord = { projectUuid ->
                                    currentScreen = AppScreen.RecordEditor(projectUuid, null)
                                },
                                onOpenSync = { currentScreen = AppScreen.SyncCenter },
                                onOpenSettings = { currentScreen = AppScreen.Settings },
                                onOpenHelp = { currentScreen = AppScreen.Help },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        is AppScreen.ProjectDetail -> {
                            ProjectDetailScreen(
                                projectUuid = screen.projectUuid,
                                repository = repository,
                                onBack = { currentScreen = AppScreen.Home },
                                onOpenRecord = { recUuid ->
                                    currentScreen = AppScreen.RecordEditor(screen.projectUuid, recUuid)
                                },
                                onNewRecord = {
                                    currentScreen = AppScreen.RecordEditor(screen.projectUuid, null)
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        is AppScreen.RecordEditor -> {
                            RecordEditorScreen(
                                recordUuid = screen.recordUuid,
                                projectUuid = screen.projectUuid,
                                repository = repository,
                                onBack = {
                                    currentScreen = AppScreen.ProjectDetail(screen.projectUuid)
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        is AppScreen.SyncCenter -> {
                            SyncCenterScreen(
                                repository = repository,
                                onBack = { currentScreen = AppScreen.Home },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        is AppScreen.Settings -> {
                            SettingsScreen(
                                repository = repository,
                                isDarkTheme = isDarkTheme,
                                onToggleDarkTheme = { isDarkTheme = it },
                                onBack = { currentScreen = AppScreen.Home },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        is AppScreen.Help -> {
                            HelpScreen(
                                onBack = { currentScreen = AppScreen.Home },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }
}
