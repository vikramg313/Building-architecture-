package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.ui.screens.CategoriesOverviewScreen
import com.example.ui.screens.CategoryDirectoryScreen
import com.example.ui.screens.LoginSettingsScreen
import com.example.ui.screens.RfqManagerScreen
import com.example.ui.screens.SavedSuppliersScreen
import com.example.ui.screens.SupplierDetailScreen
import com.example.ui.theme.TradeSourceTheme
import com.example.viewmodel.AppScreen
import com.example.viewmodel.WholesaleViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: WholesaleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
            val isDark = when (themeMode) {
                com.example.model.AppThemeMode.SYSTEM -> isSystemDark
                com.example.model.AppThemeMode.LIGHT -> false
                com.example.model.AppThemeMode.DARK -> true
                com.example.model.AppThemeMode.NAVY -> true
            }
            TradeSourceTheme(darkTheme = isDark) {
                MainAppContainer(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContainer(viewModel: WholesaleViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val selectedSupplier by viewModel.selectedSupplier.collectAsState()
    val rfqs by viewModel.allRfqs.collectAsState()
    val savedSuppliers by viewModel.bookmarkedSuppliers.collectAsState()
    val userAccount by viewModel.userAccount.collectAsState()

    BackHandler(enabled = currentScreen !is AppScreen.Directory) {
        viewModel.handleBackPress()
    }

    val showBottomBar = currentScreen !is AppScreen.SupplierDetail

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    modifier = Modifier.testTag("main_bottom_nav"),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = NavigationBarDefaults.Elevation
                ) {
                    NavigationBarItem(
                        selected = currentScreen is AppScreen.Directory,
                        onClick = { viewModel.navigateTo(AppScreen.Directory) },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen is AppScreen.Directory) Icons.Filled.Storefront else Icons.Outlined.Storefront,
                                contentDescription = "Directory"
                            )
                        },
                        label = { Text("Directory") },
                        modifier = Modifier.testTag("nav_directory")
                    )

                    NavigationBarItem(
                        selected = currentScreen is AppScreen.CategoriesOverview,
                        onClick = { viewModel.navigateTo(AppScreen.CategoriesOverview) },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen is AppScreen.CategoriesOverview) Icons.Filled.Category else Icons.Outlined.Category,
                                contentDescription = "Categories"
                            )
                        },
                        label = { Text("Categories") },
                        modifier = Modifier.testTag("nav_categories")
                    )

                    NavigationBarItem(
                        selected = currentScreen is AppScreen.RfqDashboard,
                        onClick = { viewModel.navigateTo(AppScreen.RfqDashboard) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    val pendingCount = rfqs.count { it.status == "Pending Quote" || it.status == "Quoted" }
                                    if (pendingCount > 0) {
                                        Badge { Text(pendingCount.toString()) }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (currentScreen is AppScreen.RfqDashboard) Icons.AutoMirrored.Filled.ReceiptLong else Icons.AutoMirrored.Outlined.ReceiptLong,
                                    contentDescription = "My RFQs"
                                )
                            }
                        },
                        label = { Text("My RFQs") },
                        modifier = Modifier.testTag("nav_rfqs")
                    )

                    NavigationBarItem(
                        selected = currentScreen is AppScreen.SavedSuppliers,
                        onClick = { viewModel.navigateTo(AppScreen.SavedSuppliers) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (savedSuppliers.isNotEmpty()) {
                                        Badge { Text(savedSuppliers.size.toString()) }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (currentScreen is AppScreen.SavedSuppliers) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                    contentDescription = "Saved"
                                )
                            }
                        },
                        label = { Text("Saved") },
                        modifier = Modifier.testTag("nav_saved")
                    )

                    NavigationBarItem(
                        selected = currentScreen is AppScreen.AccountSettings,
                        onClick = { viewModel.navigateTo(AppScreen.AccountSettings) },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen is AppScreen.AccountSettings) Icons.Filled.AccountCircle else Icons.Outlined.AccountCircle,
                                contentDescription = "Account"
                            )
                        },
                        label = { Text(if (userAccount.isLoggedIn) "Account" else "Sign In") },
                        modifier = Modifier.testTag("nav_account")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val screen = currentScreen) {
                is AppScreen.Directory -> {
                    CategoryDirectoryScreen(viewModel = viewModel)
                }
                is AppScreen.CategoriesOverview -> {
                    CategoriesOverviewScreen(viewModel = viewModel)
                }
                is AppScreen.SupplierDetail -> {
                    val supplier = selectedSupplier ?: viewModel.allSuppliers.collectAsState().value.find { it.id == screen.supplierId }
                    if (supplier != null) {
                        SupplierDetailScreen(
                            supplier = supplier,
                            viewModel = viewModel,
                            onBack = { viewModel.handleBackPress() }
                        )
                    } else {
                        CategoryDirectoryScreen(viewModel = viewModel)
                    }
                }
                is AppScreen.RfqDashboard -> {
                    RfqManagerScreen(viewModel = viewModel)
                }
                is AppScreen.SavedSuppliers -> {
                    SavedSuppliersScreen(viewModel = viewModel)
                }
                is AppScreen.AccountSettings -> {
                    LoginSettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
