package com.example.notes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.notes.ui.screens.LoginScreen
import com.example.notes.ui.screens.NoteDetailScreen
import com.example.notes.ui.screens.RegisterScreen
import com.example.notes.ui.screens.PairingScreen
import com.example.notes.ui.screens.NotesScreen
import com.example.notes.viewmodel.AuthViewModel

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Pairing : Screen("pairing")
    data object Notes : Screen("notes")
    data object NoteDetail : Screen("noteDetail/{docId}") {
        fun createRoute(docId: String) = "noteDetail/$docId"
    }
}

@Composable
fun NotesApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()

    val startDestination = when {
        authState.isLoggedIn && authState.isPaired -> Screen.Notes.route
        authState.isLoggedIn && !authState.isPaired -> Screen.Pairing.route
        else -> Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = {
                    val dest = if (authState.isPaired) Screen.Notes.route else Screen.Pairing.route
                    navController.navigate(dest) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.Pairing.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Pairing.route) {
            PairingScreen(
                viewModel = authViewModel,
                onPairingComplete = {
                    navController.navigate(Screen.Notes.route) {
                        popUpTo(Screen.Pairing.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Notes.route) {
            NotesScreen(
                authViewModel = authViewModel,
                onLogout = {
                    authViewModel.signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onOpenDocument = { docId ->
                    navController.navigate(Screen.NoteDetail.createRoute(docId))
                }
            )
        }
        composable(
            route = Screen.NoteDetail.route,
            arguments = listOf(navArgument("docId") { type = NavType.StringType })
        ) { backStackEntry ->
            val docId = backStackEntry.arguments?.getString("docId") ?: return@composable
            NoteDetailScreen(
                partnershipId = authState.partnership?.id ?: "",
                documentId = docId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
