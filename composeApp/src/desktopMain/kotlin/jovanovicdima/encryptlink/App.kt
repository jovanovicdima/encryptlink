package jovanovicdima.encryptlink

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import jovanovicdima.encryptlink.components.RadioTabs
import jovanovicdima.encryptlink.navigation.Navigation
import jovanovicdima.encryptlink.screens.ServerScreen
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.toString

@Composable
@Preview
fun App() {
    val navController: NavHostController = rememberNavController()
    var selectedTab: String by remember { mutableStateOf("Server") }

    MaterialTheme {
        Scaffold(
            modifier = Modifier.systemBarsPadding().padding(4.dp),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                RadioTabs(
                    modifier = Modifier.fillMaxWidth(),
                    tabs = listOf("Server", "Client"),
                    onTabSelected = {
                        selectedTab = it
                        if (selectedTab == "Server") {
                            navController.navigate(Navigation.Server) {
                                popUpTo(Navigation) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        } else {
                            navController.navigate(Navigation.Client) {
                                popUpTo(Navigation) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    },
                    selectedTab = selectedTab,
                    selectedTabTextColor = MaterialTheme.colorScheme.background,
                )
            },
            content = { contentPadding ->
                NavHost(
                    navController = navController,
                    route = Navigation::class,
                    startDestination = Navigation.Server,
                    enterTransition = { fadeIn(animationSpec = tween(300)) },
                    exitTransition = { fadeOut(animationSpec = tween(1)) },
                    popEnterTransition = { fadeIn(animationSpec = tween(300)) },
                    popExitTransition = { fadeOut(animationSpec = tween(1)) }) {
                    composable<Navigation.Server> {
                        ServerScreen(modifier = Modifier.fillMaxSize())
                    }
                    composable<Navigation.Client> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text("client")
                        }
                    }
                }
            })
    }
}