package com.diracks.app.app.app_state


import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope

@Composable
fun rememberBBScoutAppState(
    //windowSizeClass: WindowSizeClass,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    navController: NavHostController = rememberNavController(),
    dashboardNavController: NavHostController = rememberNavController(),
    userAccountAppState: UserAccountAppState = UserAccountAppState()
): BBScoutAppState {
    return remember(
        navController,
        coroutineScope,
       // windowSizeClass,
        dashboardNavController,
    ) {
        BBScoutAppState(
            navController = navController,
            coroutineScope = coroutineScope,
           // windowSizeClass = windowSizeClass,
            dashboardNavController = dashboardNavController,
            userAccountAppState = userAccountAppState
        )
    }
}
@Stable
class BBScoutAppState(
   // val windowSizeClass: WindowSizeClass,
    val navController: NavHostController,
    var dashboardNavController: NavHostController,
    val coroutineScope: CoroutineScope,
    var userAccountAppState: UserAccountAppState
) {

    val currentDestination: NavDestination?
        @Composable get() = navController
            .currentBackStackEntryAsState().value?.destination


    val dashboardCurrentDestination: NavDestination?
        @Composable get() = dashboardNavController
            .currentBackStackEntryAsState().value?.destination

    fun updateAppMainAccountState(state: UserState?) {
        userAccountAppState = userAccountAppState.copy(
            mainAccount = state
        )
    }

}