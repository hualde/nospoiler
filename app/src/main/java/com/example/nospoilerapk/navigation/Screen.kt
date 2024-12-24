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
    object Summary : Screen("summary/{mediaId}/{rangeStart}/{rangeEnd}/{season}/{isFromBeginning}") {
        const val MEDIA_ID = "mediaId"
        const val RANGE_START = "rangeStart"
        const val RANGE_END = "rangeEnd"
        const val SEASON = "season"
        const val IS_FROM_BEGINNING = "isFromBeginning"
        
        fun createRoute(
            mediaId: String, 
            rangeStart: Int, 
            rangeEnd: Int, 
            season: Int,
            isFromBeginning: Boolean = false
        ) = "summary/$mediaId/$rangeStart/$rangeEnd/$season/$isFromBeginning"
    }
    object Settings : Screen("settings")
    object Timeline : Screen("timeline/{mediaId}/{rangeStart}/{rangeEnd}/{season}/{isFromBeginning}") {
        const val MEDIA_ID = "mediaId"
        const val RANGE_START = "rangeStart"
        const val RANGE_END = "rangeEnd"
        const val SEASON = "season"
        const val IS_FROM_BEGINNING = "isFromBeginning"

        fun createRoute(
            mediaId: String,
            rangeStart: Int,
            rangeEnd: Int,
            season: Int,
            isFromBeginning: Boolean = false
        ): String {
            return "timeline/$mediaId/$rangeStart/$rangeEnd/$season/$isFromBeginning"
        }
    }
    object About : Screen("about")
    object Help : Screen("help")
    object Terms : Screen("terms")
} 