package com.example.nospoilerapk.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object SearchResults : Screen("search_results/{query}") {
        const val QUERY = "query"
        fun createRoute(query: String) = "search_results/$query"
    }
    object RangeSelector : Screen("range_selector/{mediaId}") {
        const val MEDIA_ID = "mediaId"
        fun createRoute(mediaId: String) = "range_selector/$mediaId"
    }
    object Summary : Screen("summary/{mediaId}/{rangeStart}/{rangeEnd}") {
        const val MEDIA_ID = "mediaId"
        const val RANGE_START = "rangeStart"
        const val RANGE_END = "rangeEnd"
        fun createRoute(mediaId: String, rangeStart: Int, rangeEnd: Int) = 
            "summary/$mediaId/$rangeStart/$rangeEnd"
    }
    object Settings : Screen("settings")
    object Timeline : Screen("timeline/{mediaId}/{rangeStart}/{rangeEnd}") {
        const val MEDIA_ID = "mediaId"
        const val RANGE_START = "rangeStart"
        const val RANGE_END = "rangeEnd"
        fun createRoute(mediaId: String, rangeStart: Int, rangeEnd: Int) = 
            "timeline/$mediaId/$rangeStart/$rangeEnd"
    }
    object About : Screen("about")
} 