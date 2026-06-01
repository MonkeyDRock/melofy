package com.example.melofy.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Auth : Screen("auth")
    object Main : Screen("main") // Dashboard containing bottom tabs
    
    // Bottom tabs destinations (nested inside main or referenced directly)
    object Home : Screen("home")
    object Search : Screen("search")
    object Library : Screen("library")
    object Profile : Screen("profile")

    // Details routes
    object Player : Screen("player")
    object PlaylistDetails : Screen("playlist_details/{playlistId}") {
        fun createRoute(playlistId: String) = "playlist_details/$playlistId"
    }
    object Settings : Screen("settings")
    object AiDj : Screen("ai_dj")
    object Subscription : Screen("subscription")
    object Preferences : Screen("preferences")
    object Podcast : Screen("podcast")
    object PodcastPlayer : Screen("podcast_player")
}
