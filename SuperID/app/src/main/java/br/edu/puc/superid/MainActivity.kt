package br.edu.puc.superid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.edu.puc.superid.ui.IntroScreen
import br.edu.puc.superid.ui.theme.SuperIDTheme
//import br.edu.puc.superid.ui.RegisterScreen TODO: tela de registro

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        setContent {
            SuperIDTheme {
                val navController = rememberNavController()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "intro",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("intro") {
                            IntroScreen(
                                onContinue = {
                                    navController.navigate("register")
                                }
                            )
                        }
                        composable("register") {
                           // RegisterScreen()
                        }
                    }
                }
            }
        }
    }
}


