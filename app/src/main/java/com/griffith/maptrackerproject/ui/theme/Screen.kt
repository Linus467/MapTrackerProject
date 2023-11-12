package com.griffith.maptrackerproject.ui.theme

sealed class Screen(val route: String) {
    object MapView : Screen("mapView")
    object History : Screen("history")
    object DayStatistics : Screen("day_statistics/{date}/{mapVisible}") {
        fun createRoute(date: String, mapVisible: Boolean): String {
            return "day_statistics/$date/$mapVisible"
        }
    }
}