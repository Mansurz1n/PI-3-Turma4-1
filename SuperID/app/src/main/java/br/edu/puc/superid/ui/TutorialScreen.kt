// File: app/src/main/java/br/edu/puc/superid/ui/TutorialScreen.kt
package br.edu.puc.superid.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import br.edu.puc.superid.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorialScreen(navController: NavController) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Tutorial do SuperID",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.weight(2f))
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Logo SuperID",
                            modifier = Modifier
                                .size(40.dp)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Voltar",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Bem-vindo(a) ao tutorial do SuperID!",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Divider()

            tutorialStep(
                "1️⃣ Abra sua mente (não literalmente)",
                "Esqueça tudo que você já ouviu sobre gerenciadores de senhas. Aqui é frustração."
            )
            tutorialStep("2️⃣ Leia os termos", "Você vai clicar naquela caixa chatinha… fique tranquilo, quase ninguém lê mesmo.")
            tutorialStep("3️⃣ Crie sua conta", "Insira nome, e-mail e senha mestre. Se errar, reveja o passo 1 e tente de novo.")
            tutorialStep("4️⃣ Cadastre senhas", "Sites, apps, cofres, sonhos… se lembrar, a gente guarda.")
            tutorialStep("5️⃣ Login sem senha", "Escaneie, confirme e… voilà, você está logado.")
            tutorialStep("6️⃣ Esqueceu a senha?", "Clique em “Esqueci minha senha” e receba o link de redefinição.")
            tutorialStep("7️⃣ Fique tranquilo(a)", "Se vazar sua senha, a culpa é do seu doppelgänger digital.")

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Entendi, leve-me ao app")
            }
        }
    }
}

@Composable
private fun ColumnScope.tutorialStep(title: String, description: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground
    )
    Text(
        description,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = 16.sp
    )
    Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f))
}
