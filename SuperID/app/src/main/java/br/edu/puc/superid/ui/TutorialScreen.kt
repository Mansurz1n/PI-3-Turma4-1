// File: TutorialScreen.kt
package br.edu.puc.superid.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorialScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tutorial do SuperID") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text("Bem-vindo(a) ao 'tutorial' \nMAIS (in)ÚTIL do INCRIVEL, ESTUPENDO, SOFISTICADO E MAGNANIMO...  \nSuperID!", fontSize = 20.sp)
            Spacer(Modifier.height(16.dp))

            Text("1️⃣ **Abra sua mente (não literalmente por favor)**\n" +
                    "Esqueça tudo que você já ouviu sobre gerenciadores de senhas. Aqui é frustração.")
            Spacer(Modifier.height(12.dp))

            Text("2️⃣ **Leia os termos**\n" +
                    "Sim, você vai clicar naquela caixa chatinha, e sim, você vai ler tudinho sem reclamar, entendeu? É só uma formalidade… ou quase.")
            Spacer(Modifier.height(12.dp))

            Text("3️⃣ **Crie sua conta**\n" +
                    "Insira nome, e-mail e senha mestre. Se errar, pelo amor de Deus, reveja o passo 1 e tente de novo, de novo, e de novo...")
            Spacer(Modifier.height(12.dp))

            Text("4️⃣ **Cadastre senhas**\n" +
                    "A gente guarda senha para sites, aplicativos, cofres, sonhos, desejos, romances… se você lembrar, a gente guarda.")
            Spacer(Modifier.height(12.dp))

            Text("5️⃣ **Use o login sem senha**\n" +
                    "Fique na 'moda': escaneie, confirme e… voilà, você está logado no único site que a gente possui licenssa.")
            Spacer(Modifier.height(12.dp))

            Text("6️⃣ **Esqueceu a senha bebê?**\n" +
                    "Clique em “Esqueci minha senha” caso tenha esquecido sua senha super complexa de apenas 8 caracteres. Insira seu email (de preferencia o  que tem sua conta cadastrada né), e prontinho a gente consegue salvar sua conta")
            Spacer(Modifier.height(12.dp))

            Text("7️⃣ **Fique tranquilo(a)(e)**\n" +
                    "Tudo aqui é educativo e muito mal feito. Se por acaso vazar sua senha, a culpa é do seu doppelgänger digital e não nossa. ")
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Entendi mestre, leve-me ao app")
            }
        }
    }
}
