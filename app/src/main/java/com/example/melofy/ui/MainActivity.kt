package com.example.melofy.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.melofy.ui.components.FloatingMiniPlayer
import com.example.melofy.ui.navigation.Screen
import com.example.melofy.ui.screens.AuthScreen
import com.example.melofy.ui.screens.HomeScreen
import com.example.melofy.ui.screens.LibraryScreen
import com.example.melofy.ui.screens.OnboardingScreen
import com.example.melofy.ui.screens.PlaylistDetailsScreen
import com.example.melofy.ui.screens.PlayerScreen
import com.example.melofy.ui.screens.ProfileScreen
import com.example.melofy.ui.screens.SearchScreen
import com.example.melofy.ui.screens.SettingsScreen
import com.example.melofy.ui.screens.SplashScreen
import com.example.melofy.ui.screens.AiDjScreen
import com.example.melofy.ui.screens.SubscriptionScreen
import com.example.melofy.ui.screens.PreferencesScreen
import com.example.melofy.ui.viewmodel.PodcastViewModel
import com.example.melofy.ui.screens.PodcastScreen
import com.example.melofy.ui.screens.PodcastPlayerScreen
import com.example.melofy.ui.theme.Accent
import com.example.melofy.ui.theme.Background
import com.example.melofy.ui.theme.MelofyTheme
import com.example.melofy.ui.theme.Primary
import com.example.melofy.ui.theme.Surface
import com.example.melofy.ui.viewmodel.AuthViewModel
import com.example.melofy.ui.viewmodel.HomeViewModel
import com.example.melofy.ui.viewmodel.LibraryViewModel
import com.example.melofy.ui.viewmodel.PlayerViewModel
import com.example.melofy.ui.viewmodel.SearchViewModel
import com.example.melofy.ui.viewmodel.AiDjViewModel
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        setContent {
            MelofyTheme {
                MainAppScreen()
            }
        }
    }
}

@Composable
fun MainAppScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Scoped Shared ViewModels
    val authViewModel: AuthViewModel = hiltViewModel()
    val homeViewModel: HomeViewModel = hiltViewModel()
    val searchViewModel: SearchViewModel = hiltViewModel()
    val playerViewModel: PlayerViewModel = hiltViewModel()
    val libraryViewModel: LibraryViewModel = hiltViewModel()
    val aiDjViewModel: AiDjViewModel = hiltViewModel()
    val podcastViewModel: PodcastViewModel = hiltViewModel()

    val playbackState by playerViewModel.playbackState.collectAsState()
    val userState by authViewModel.userState.collectAsState()

    // Determine whether to show bottom navigation bar (only for Home, Search, Podcast, Library, Profile)
    val bottomTabs = listOf(Screen.Home, Screen.Search, Screen.Podcast, Screen.Library, Screen.Profile)
    val showBottomBar = bottomTabs.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = Surface.copy(0.9f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .background(Color.Transparent),
                    tonalElevation = 8.dp
                ) {
                    val tabs = listOf(
                        Triple(Screen.Home, Icons.Default.Home, "Home"),
                        Triple(Screen.Search, Icons.Default.Search, "Search"),
                        Triple(Screen.Podcast, Icons.Default.Mic, "Podcast"),
                        Triple(Screen.Library, Icons.Default.LibraryMusic, "Library"),
                        Triple(Screen.Profile, Icons.Default.Person, "Profile")
                    )

                    tabs.forEach { (screen, icon, label) ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(imageVector = icon, contentDescription = label) },
                            label = { Text(text = label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Accent,
                                selectedTextColor = Accent,
                                unselectedIconColor = Color.White.copy(0.6f),
                                unselectedTextColor = Color.White.copy(0.6f),
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (showBottomBar) paddingValues else androidx.compose.foundation.layout.PaddingValues(0.dp))
        ) {
            // Main Navigation Host
            NavHost(
                navController = navController,
                startDestination = Screen.Splash.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Screen.Splash.route) {
                    val isReady = userState !is AuthViewModel.UserState.Idle && userState !is AuthViewModel.UserState.Loading
                    SplashScreen(
                        isReady = isReady,
                        onNavigateNext = {
                            val nextRoute = if (userState is AuthViewModel.UserState.Authenticated) {
                                val user = (userState as AuthViewModel.UserState.Authenticated).user
                                if (user.country.isBlank() || user.favoriteArtists.isBlank() || user.favoriteGenres.isBlank()) {
                                    Screen.Preferences.route
                                } else {
                                    Screen.Home.route
                                }
                            } else {
                                Screen.Onboarding.route
                            }
                            navController.navigate(nextRoute) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Onboarding.route) {
                    OnboardingScreen(
                        onNavigateNext = {
                            navController.navigate(Screen.Auth.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Auth.route) {
                    AuthScreen(
                        viewModel = authViewModel,
                        onAuthSuccess = {
                            val user = (authViewModel.userState.value as? AuthViewModel.UserState.Authenticated)?.user
                            val nextRoute = if (user != null && (user.country.isBlank() || user.favoriteArtists.isBlank() || user.favoriteGenres.isBlank())) {
                                Screen.Preferences.route
                            } else {
                                Screen.Home.route
                            }
                            navController.navigate(nextRoute) {
                                popUpTo(Screen.Auth.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Home.route) {
                    HomeScreen(
                        viewModel = homeViewModel,
                        onSearchBannerClick = { navController.navigate(Screen.Search.route) },
                        onMoodClick = { mood ->
                            // Trigger query in search and navigate there
                            searchViewModel.executeSearch(mood)
                            navController.navigate(Screen.Search.route)
                        },
                        onSongClick = { song, queue ->
                            homeViewModel.playSong(song, queue)
                        },
                        onAiDjClick = {
                            if (navController.currentDestination?.route != Screen.AiDj.route) {
                                navController.navigate(Screen.AiDj.route) {
                                    launchSingleTop = true
                                }
                            }
                        },
                        onPodcastClick = {
                            navController.navigate(Screen.Podcast.route)
                        }
                    )
                }

                composable(Screen.Search.route) {
                    SearchScreen(
                        viewModel = searchViewModel,
                        libraryViewModel = libraryViewModel,
                        onSongClick = { song, queue ->
                            searchViewModel.playSong(song, queue)
                        }
                    )
                }

                composable(Screen.Library.route) {
                    LibraryScreen(
                        viewModel = libraryViewModel,
                        onPlaylistClick = { playlistId ->
                            navController.navigate(Screen.PlaylistDetails.createRoute(playlistId))
                        },
                        onSongClick = { song, queue ->
                            libraryViewModel.playSong(song, queue)
                        }
                    )
                }

                composable(Screen.Profile.route) {
                    ProfileScreen(
                        authViewModel = authViewModel,
                        libraryViewModel = libraryViewModel,
                        onSettingsClick = { navController.navigate(Screen.Settings.route) },
                        onUpgradeClick = { navController.navigate(Screen.Subscription.route) },
                        onVoiceSearchClick = { query ->
                            searchViewModel.executeSearch(query)
                            navController.navigate(Screen.Search.route)
                        }
                    )
                }

                composable(Screen.Player.route) {
                    PlayerScreen(
                        viewModel = playerViewModel,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(Screen.PlaylistDetails.route) { backStackEntry ->
                    val playlistId = backStackEntry.arguments?.getString("playlistId") ?: ""
                    PlaylistDetailsScreen(
                        playlistId = playlistId,
                        viewModel = libraryViewModel,
                        onBackClick = { navController.popBackStack() },
                        onSongClick = { song, queue ->
                            libraryViewModel.playSong(song, queue)
                        }
                    )
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(
                        authViewModel = authViewModel,
                        onBackClick = { navController.popBackStack() },
                        onLogoutSuccess = {
                            navController.navigate(Screen.Auth.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.AiDj.route) {
                    AiDjScreen(
                        viewModel = aiDjViewModel,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(Screen.Subscription.route) {
                    SubscriptionScreen(
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(Screen.Preferences.route) {
                    PreferencesScreen(
                        authViewModel = authViewModel,
                        onPreferencesSaved = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Preferences.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Podcast.route) {
                    PodcastScreen(
                        viewModel = podcastViewModel,
                        onEpisodeClick = { episode ->
                            // Pause the standard music player if playing to prevent overlap
                            if (playerViewModel.playbackState.value.isPlaying) {
                                playerViewModel.togglePlayPause()
                            }
                            podcastViewModel.playEpisode(episode)
                            navController.navigate(Screen.PodcastPlayer.route)
                        }
                    )
                }

                composable(Screen.PodcastPlayer.route) {
                    PodcastPlayerScreen(
                        viewModel = podcastViewModel,
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }

            // Floating Mini Player overlay (only when song is active, and NOT currently on Player screen details or Podcast screens)
            val showMiniPlayer = playbackState.currentSong != null && 
                    currentRoute != Screen.Player.route && 
                    currentRoute != Screen.Splash.route && 
                    currentRoute != Screen.Onboarding.route && 
                    currentRoute != Screen.Auth.route &&
                    currentRoute != Screen.Podcast.route &&
                    currentRoute != Screen.PodcastPlayer.route
            
            AnimatedVisibility(
                visible = showMiniPlayer,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = if (showBottomBar) 8.dp else 16.dp)
            ) {
                FloatingMiniPlayer(
                    playbackState = playbackState,
                    onPlayPauseClick = { playerViewModel.togglePlayPause() },
                    onNextClick = { playerViewModel.skipToNext() },
                    onClick = { navController.navigate(Screen.Player.route) }
                )
            }
        }
    }
}
