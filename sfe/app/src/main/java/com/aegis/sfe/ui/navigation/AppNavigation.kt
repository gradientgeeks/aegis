package com.aegis.sfe.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aegis.sfe.ui.screen.auth.DeviceRebindingScreen
import com.aegis.sfe.ui.screen.auth.LoginScreen
import com.aegis.sfe.ui.screen.dashboard.DashboardScreen
import com.aegis.sfe.ui.screen.provisioning.DeviceProvisioningScreen
import com.aegis.sfe.ui.screen.transfer.TransferScreen
import com.aegis.sfe.ui.screen.history.TransactionHistoryScreen

sealed class Screen(val route: String) {
    object DeviceProvisioning : Screen("device_provisioning")
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object Transfer : Screen("transfer")
    object TransactionHistory : Screen("transaction_history")
    object DeviceRebinding : Screen("device_rebinding/{username}") {
        fun createRoute(username: String) = "device_rebinding/$username"
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.DeviceProvisioning.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.DeviceProvisioning.route) {
            DeviceProvisioningScreen(
                onProvisioningComplete = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.DeviceProvisioning.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRebinding = { username ->
                    navController.navigate(Screen.DeviceRebinding.createRoute(username))
                }
            )
        }
        
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToTransfer = {
                    navController.navigate(Screen.Transfer.route)
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.TransactionHistory.route)
                }
            )
        }
        
        composable(Screen.Transfer.route) {
            TransferScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onTransferComplete = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.TransactionHistory.route) {
            TransactionHistoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = Screen.DeviceRebinding.route,
            arguments = listOf(navArgument("username") { type = NavType.StringType })
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            DeviceRebindingScreen(
                username = username,
                onSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onCancel = {
                    navController.popBackStack()
                }
            )
        }
    }
}