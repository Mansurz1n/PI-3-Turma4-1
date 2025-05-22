package br.edu.puc.superid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.edu.puc.superid.ui.AddPasswordScreen
import br.edu.puc.superid.ui.HomeScreen
import br.edu.puc.superid.ui.IntroScreen
import br.edu.puc.superid.ui.LoginScreen
import br.edu.puc.superid.ui.RegisterScreen
import br.edu.puc.superid.ui.TutorialScreen
import br.edu.puc.superid.ui.theme.SuperIDTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            // Estado de tema no nível mais alto
            var isDarkTheme by rememberSaveable { mutableStateOf(false) }

            SuperIDTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()

                // Define rota inicial conforme autenticação
                val startDestination =
                    if (FirebaseAuth.getInstance().currentUser != null) "home"
                    else "intro"

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = startDestination,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("intro")    { IntroScreen(navController) }
                        composable("register") { RegisterScreen(navController) }
                        composable("login")    { LoginScreen(navController) }
                        composable("home")     {
                            HomeScreen(
                                navController   = navController,
                                isDarkTheme     = isDarkTheme,
                                onToggleTheme   = { dark -> isDarkTheme = dark }
                            )
                        }
                        composable("addPassword") { AddPasswordScreen(navController) }
                        composable("tutorial")    { TutorialScreen(navController) }
                    }
                }
            }
        }
    }
}