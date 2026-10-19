package com.gym.manager

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gym.manager.ui.components.NoInsets
import com.gym.manager.ui.navigation.GymBottomBar
import com.gym.manager.ui.navigation.GymNavHost
import com.gym.manager.ui.navigation.topDestinations
import com.gym.manager.ui.theme.GymTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )
        setContent { GymTheme { GymRoot() } }
    }
}

@Composable
private fun GymRoot() {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val showBar = topDestinations.any { it.route == route }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = NoInsets,
        bottomBar = { if (showBar) GymBottomBar(route, nav) }
    ) { inner ->
        GymNavHost(
            nav = nav,
            modifier = Modifier
                .padding(inner)
                .then(if (showBar) Modifier else Modifier.navigationBarsPadding())
        )
    }
}
