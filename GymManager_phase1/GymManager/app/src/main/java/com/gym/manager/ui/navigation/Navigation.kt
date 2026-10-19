package com.gym.manager.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gym.manager.ui.screens.DashboardScreen
import com.gym.manager.ui.screens.MemberDetailScreen
import com.gym.manager.ui.screens.MemberFormScreen
import com.gym.manager.ui.screens.MembersScreen

object Routes {
    const val DASHBOARD = "dashboard"
    const val MEMBERS = "members"
    const val MEMBER = "member/{id}"
    const val MEMBER_FORM = "member_form?id={id}"

    fun member(id: String) = "member/$id"
    fun addMember() = "member_form"
    fun editMember(id: String) = "member_form?id=$id"
}

/** Items of the bottom bar. To add a new main section later, add one line here + one composable below. */
data class TopDestination(val route: String, val label: String, val icon: ImageVector)

val topDestinations = listOf(
    TopDestination(Routes.DASHBOARD, "الرئيسية", Icons.Filled.Dashboard),
    TopDestination(Routes.MEMBERS, "الأعضاء", Icons.Filled.Groups)
)

@Composable
fun GymBottomBar(currentRoute: String?, nav: NavHostController) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        topDestinations.forEach { d ->
            NavigationBarItem(
                selected = currentRoute == d.route,
                onClick = {
                    nav.navigate(d.route) {
                        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(d.icon, contentDescription = d.label) },
                label = { Text(d.label) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
fun GymNavHost(nav: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = nav, startDestination = Routes.DASHBOARD, modifier = modifier) {
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onAddMember = { nav.navigate(Routes.addMember()) },
                onOpenMembers = {
                    nav.navigate(Routes.MEMBERS) {
                        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onOpenMember = { nav.navigate(Routes.member(it)) }
            )
        }
        composable(Routes.MEMBERS) {
            MembersScreen(
                onAdd = { nav.navigate(Routes.addMember()) },
                onOpen = { nav.navigate(Routes.member(it)) }
            )
        }
        composable(Routes.MEMBER, arguments = listOf(navArgument("id") { type = NavType.StringType })) { entry ->
            val id = entry.arguments?.getString("id").orEmpty()
            MemberDetailScreen(
                memberId = id,
                onBack = { nav.popBackStack() },
                onEdit = { nav.navigate(Routes.editMember(it)) }
            )
        }
        composable(
            Routes.MEMBER_FORM,
            arguments = listOf(navArgument("id") { type = NavType.StringType; nullable = true; defaultValue = null })
        ) { entry ->
            MemberFormScreen(
                editId = entry.arguments?.getString("id"),
                onBack = { nav.popBackStack() }
            )
        }
    }
}
