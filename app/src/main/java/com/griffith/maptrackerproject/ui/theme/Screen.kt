package com.griffith.maptrackerproject.ui.theme

sealed class Screen(val route: String) {
    object MapView : Screen("mapView")
    object History : Screen("history")
}