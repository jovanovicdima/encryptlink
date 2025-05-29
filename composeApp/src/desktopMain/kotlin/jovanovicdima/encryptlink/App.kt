package jovanovicdima.encryptlink

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import jovanovicdima.encryptlink.components.RadioTabs
import jovanovicdima.encryptlink.navigation.Navigation
import jovanovicdima.encryptlink.screens.client.ClientScreen
import jovanovicdima.encryptlink.screens.main.MainScreen
import jovanovicdima.encryptlink.screens.server.ServerScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
//    val navController: NavHostController = rememberNavController()
//    var selectedTab: String by remember { mutableStateOf("Server") }

    MaterialTheme {
        MainScreen(modifier = Modifier.fillMaxSize().padding(8.dp))


//        Scaffold(
//            modifier = Modifier.systemBarsPadding().padding(4.dp),
//            containerColor = MaterialTheme.colorScheme.background,
//            topBar = {
//                RadioTabs(
//                    modifier = Modifier.fillMaxWidth(),
//                    tabs = listOf("Server", "Client"),
//                    onTabSelected = {
//                        selectedTab = it
//                        if (selectedTab == "Server") {
//                            navController.navigate(Navigation.Server) {
//                                popUpTo(Navigation) {
//                                    inclusive = true
//                                }
//                                launchSingleTop = true
//                            }
//                        } else {
//                            navController.navigate(Navigation.Client) {
//                                popUpTo(Navigation) {
//                                    inclusive = true
//                                }
//                                launchSingleTop = true
//                            }
//                        }
//                    },
//                    selectedTab = selectedTab,
//                    selectedTabTextColor = MaterialTheme.colorScheme.background,
//                )
//            },
//            content = { contentPadding ->
//                NavHost(
//                    navController = navController,
//                    route = Navigation::class,
//                    startDestination = Navigation.Server,
//                    enterTransition = { fadeIn(animationSpec = tween(300)) },
//                    exitTransition = { fadeOut(animationSpec = tween(1)) },
//                    popEnterTransition = { fadeIn(animationSpec = tween(300)) },
//                    popExitTransition = { fadeOut(animationSpec = tween(1)) }) {
//                    composable<Navigation.Server> {
//                        ServerScreen(modifier = Modifier.fillMaxSize().padding(contentPadding))
//                    }
//                    composable<Navigation.Client> {
//                        ClientScreen(modifier = Modifier.fillMaxSize().padding(contentPadding))
//                    }
//                }
//            })
    }
}