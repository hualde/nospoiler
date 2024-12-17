package com.example.nospoilerapk.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object SearchResults : Screen("search_results/{query}") {
        fun createRoute(query: String) = "search_results/$query"
    }
    object RangeSelector : Screen("range_selector/{mediaId}") {
        fun createRoute(mediaId: String) = "range_selector/$mediaId"
    }
    object Summary : Screen("summary/{mediaId}/{rangeStart}/{rangeEnd}") {
        fun createRoute(mediaId: String, rangeStart: Int, rangeEnd: Int) = 
            "summary/$mediaId/$rangeStart/$rangeEnd"
    }
    object Settings : Screen("settings")
} 