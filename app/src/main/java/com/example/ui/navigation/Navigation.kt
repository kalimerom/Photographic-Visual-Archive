package com.example.ui.navigation

sealed class Screen(val route: String) {
    // Primary 4 tabs
    object Photos : Screen("photos")
    object Series : Screen("series")
    object Inspiration : Screen("inspiration")
    object Mix : Screen("mix")

    // Sub-screens
    object PhotoDetail : Screen("photo_detail/{photoId}") {
        fun createRoute(photoId: Long) = "photo_detail/$photoId"
    }
    object BeforeAfter : Screen("before_after/{photoId}") {
        fun createRoute(photoId: Long) = "before_after/$photoId"
    }
    object ContactSheet : Screen("contact_sheet/{sheetId}") {
        fun createRoute(sheetId: Long) = "contact_sheet/$sheetId"
    }
    object SeriesDetail : Screen("series_detail/{seriesId}") {
        fun createRoute(seriesId: Long) = "series_detail/$seriesId"
    }
    object MixDetail : Screen("mix_detail/{mixId}") {
        fun createRoute(mixId: Long) = "mix_detail/$mixId"
    }
    object Search : Screen("search")
    object Settings : Screen("settings")
}
