package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesomeMosaic
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Compare
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.navigation.Screen
import com.example.ui.screens.BeforeAfterScreen
import com.example.ui.screens.ContactSheetScreen
import com.example.ui.screens.InspirationScreen
import com.example.ui.screens.MixDetailScreen
import com.example.ui.screens.MixScreen
import com.example.ui.screens.PhotoDetailScreen
import com.example.ui.screens.PhotosScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SeriesDetailScreen
import com.example.ui.screens.SeriesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.ArchiveAccentNeutral
import com.example.ui.theme.ArchiveBackground
import com.example.ui.theme.ArchiveSurface
import com.example.ui.theme.ArchiveSurfaceBorder
import com.example.ui.theme.ArchiveTextMuted
import com.example.ui.theme.ArchiveTextPrimary
import com.example.ui.theme.ArchiveTextSecondary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ArchiveViewModel
import com.example.ui.viewmodel.ContactSheetViewModel
import com.example.ui.viewmodel.InspirationViewModel
import com.example.ui.viewmodel.MixViewModel
import com.example.ui.viewmodel.PhotoDetailViewModel
import com.example.ui.viewmodel.SearchViewModel
import com.example.ui.viewmodel.SeriesViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as VisualArchiveApplication
        val sharedIntent = intent

        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()

                // Check for incoming share intent (e.g. sharing from browser to Inspiration)
                LaunchedEffect(sharedIntent) {
                    if (sharedIntent?.action == Intent.ACTION_SEND) {
                        val text = sharedIntent.getStringExtra(Intent.EXTRA_TEXT)
                        val streamUri = sharedIntent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                        if (streamUri != null || !text.isNullOrBlank()) {
                            navController.navigate(Screen.Inspiration.route)
                        }
                    }
                }

                VisualArchiveApp(
                    navController = navController,
                    app = app
                )
            }
        }
    }
}

@Composable
fun VisualArchiveApp(
    navController: NavHostController,
    app: VisualArchiveApplication
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isTopLevelRoute = currentRoute in listOf(
        Screen.Photos.route,
        Screen.Series.route,
        Screen.Inspiration.route,
        Screen.Mix.route
    )

    // ViewModels scoped properly
    val archiveViewModel: ArchiveViewModel = viewModel(
        factory = ArchiveViewModel.Factory(app.archiveRepository)
    )
    val seriesViewModel: SeriesViewModel = viewModel(
        factory = SeriesViewModel.Factory(app.archiveRepository)
    )
    val inspirationViewModel: InspirationViewModel = viewModel(
        factory = InspirationViewModel.Factory(app.inspirationRepository)
    )
    val mixViewModel: MixViewModel = viewModel(
        factory = MixViewModel.Factory(app.mixRepository, app.archiveRepository, app.inspirationRepository)
    )
    val searchViewModel: SearchViewModel = viewModel(
        factory = SearchViewModel.Factory(app.archiveRepository, app.inspirationRepository, app.mixRepository)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ArchiveBackground,
        bottomBar = {
            if (isTopLevelRoute) {
                SubtleBottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Photos.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // 1. PHOTOS (Main / Archive)
            composable(Screen.Photos.route) {
                PhotosScreen(
                    viewModel = archiveViewModel,
                    onPhotoClick = { photoId ->
                        navController.navigate(Screen.PhotoDetail.createRoute(photoId))
                    },
                    onOpenSearch = {
                        navController.navigate(Screen.Search.route)
                    },
                    onOpenSettings = {
                        navController.navigate(Screen.Settings.route)
                    },
                    onNavigateToSeries = { seriesId ->
                        navController.navigate(Screen.SeriesDetail.createRoute(seriesId))
                    },
                    onNavigateToContactSheet = { sheetId ->
                        navController.navigate(Screen.ContactSheet.createRoute(sheetId))
                    },
                    onNavigateToMixCreate = { idA, _ ->
                        navController.navigate(Screen.Mix.route)
                    }
                )
            }

            // 2. SERIES
            composable(Screen.Series.route) {
                SeriesScreen(
                    viewModel = seriesViewModel,
                    onSeriesClick = { seriesId ->
                        navController.navigate(Screen.SeriesDetail.createRoute(seriesId))
                    },
                    onContactSheetClick = { sheetId ->
                        navController.navigate(Screen.ContactSheet.createRoute(sheetId))
                    }
                )
            }

            // 3. INSPIRATION
            composable(Screen.Inspiration.route) {
                InspirationScreen(
                    viewModel = inspirationViewModel
                )
            }

            // 4. MIX
            composable(Screen.Mix.route) {
                MixScreen(
                    viewModel = mixViewModel,
                    onMixClick = { mixId ->
                        navController.navigate(Screen.MixDetail.createRoute(mixId))
                    }
                )
            }

            // PHOTO DETAIL
            composable(
                route = Screen.PhotoDetail.route,
                arguments = listOf(navArgument("photoId") { type = NavType.LongType })
            ) { backStackEntry ->
                val photoId = backStackEntry.arguments?.getLong("photoId") ?: 0L
                val detailViewModel: PhotoDetailViewModel = viewModel(
                    key = "photo_$photoId",
                    factory = PhotoDetailViewModel.Factory(
                        photoId = photoId,
                        repository = app.archiveRepository,
                        audioRecorderManager = app.audioRecorderManager
                    )
                )
                PhotoDetailScreen(
                    viewModel = detailViewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToBeforeAfter = { pId ->
                        navController.navigate(Screen.BeforeAfter.createRoute(pId))
                    },
                    onNavigateToMix = { _ ->
                        navController.navigate(Screen.Mix.route)
                    }
                )
            }

            // BEFORE / AFTER
            composable(
                route = Screen.BeforeAfter.route,
                arguments = listOf(navArgument("photoId") { type = NavType.LongType })
            ) { backStackEntry ->
                val photoId = backStackEntry.arguments?.getLong("photoId") ?: 0L
                val detailViewModel: PhotoDetailViewModel = viewModel(
                    key = "photo_$photoId",
                    factory = PhotoDetailViewModel.Factory(
                        photoId = photoId,
                        repository = app.archiveRepository,
                        audioRecorderManager = app.audioRecorderManager
                    )
                )
                BeforeAfterScreen(
                    viewModel = detailViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // CONTACT SHEET
            composable(
                route = Screen.ContactSheet.route,
                arguments = listOf(navArgument("sheetId") { type = NavType.LongType })
            ) { backStackEntry ->
                val sheetId = backStackEntry.arguments?.getLong("sheetId") ?: 0L
                val contactSheetViewModel: ContactSheetViewModel = viewModel(
                    key = "sheet_$sheetId",
                    factory = ContactSheetViewModel.Factory(
                        sheetId = sheetId,
                        repository = app.archiveRepository
                    )
                )
                ContactSheetScreen(
                    viewModel = contactSheetViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // SERIES DETAIL
            composable(
                route = Screen.SeriesDetail.route,
                arguments = listOf(navArgument("seriesId") { type = NavType.LongType })
            ) { backStackEntry ->
                val seriesId = backStackEntry.arguments?.getLong("seriesId") ?: 0L
                SeriesDetailScreen(
                    seriesId = seriesId,
                    viewModel = seriesViewModel,
                    onBack = { navController.popBackStack() },
                    onPhotoClick = { pId ->
                        navController.navigate(Screen.PhotoDetail.createRoute(pId))
                    }
                )
            }

            // MIX DETAIL
            composable(
                route = Screen.MixDetail.route,
                arguments = listOf(navArgument("mixId") { type = NavType.LongType })
            ) { backStackEntry ->
                val mixId = backStackEntry.arguments?.getLong("mixId") ?: 0L
                MixDetailScreen(
                    mixId = mixId,
                    viewModel = mixViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // GLOBAL SEARCH
            composable(Screen.Search.route) {
                SearchScreen(
                    viewModel = searchViewModel,
                    onBack = { navController.popBackStack() },
                    onPhotoClick = { photoId ->
                        navController.navigate(Screen.PhotoDetail.createRoute(photoId))
                    },
                    onSeriesClick = { seriesId ->
                        navController.navigate(Screen.SeriesDetail.createRoute(seriesId))
                    },
                    onMixClick = { mixId ->
                        navController.navigate(Screen.MixDetail.createRoute(mixId))
                    }
                )
            }

            // SETTINGS
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

/**
 * Clean, subtle, dark bottom navigation bar.
 * Restrained typography, minimal indicator, no social media bubbles.
 */
@Composable
fun SubtleBottomNavigationBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .height(58.dp)
            .border(1.dp, ArchiveSurfaceBorder),
        containerColor = ArchiveSurface,
        contentColor = ArchiveTextPrimary,
        tonalElevation = 0.dp
    ) {
        val navItems = listOf(
            Triple(Screen.Photos.route, "PHOTOS", Icons.Default.GridView),
            Triple(Screen.Series.route, "SERIES", Icons.Default.Collections),
            Triple(Screen.Inspiration.route, "INSPIRATION", Icons.Default.Lightbulb),
            Triple(Screen.Mix.route, "MIX", Icons.Default.Compare)
        )

        navItems.forEach { (route, label, icon) ->
            val isSelected = currentRoute == route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(route) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(18.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        )
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ArchiveTextPrimary,
                    selectedTextColor = ArchiveTextPrimary,
                    unselectedIconColor = ArchiveTextMuted,
                    unselectedTextColor = ArchiveTextMuted,
                    indicatorColor = Color.Transparent
                ),
                modifier = Modifier.testTag("nav_item_${label.lowercase()}")
            )
        }
    }
}
