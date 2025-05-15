package br.edu.puc.superid.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

data class SenhaItem(
    val id: String,
    val titulo: String,
    val login: String,
    val categoria: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = Firebase.firestore
    var senhas by remember { mutableStateOf<List<SenhaItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Carregar as senhas do usuário
    LaunchedEffect(auth.currentUser?.uid) {
        isLoading = true
        val userId = auth.currentUser?.uid

        if (userId != null) {
            try {
                val querySnapshot = db.collection("usuarios")
                    .document(userId)
                    .collection("senhas")
                    .get()
                    .await()

                senhas = querySnapshot.documents.map { doc ->
                    SenhaItem(
                        id = doc.id,
                        titulo = doc.getString("titulo") ?: "",
                        login = doc.getString("login") ?: "",
                        categoria = doc.getString("categoria") ?: ""
                    )
                }
                isLoading = false
            } catch (e: Exception) {
                // Tratar erro
                isLoading = false
            }
        } else {
            // Usuário não está autenticado
            navController.navigate("login") {
                popUpTo("home") { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Minhas Senhas") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.LightGray)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = { /* TODO: abrir configurações */ }) {
                    Text("Configurações")
                }
                Button(onClick = { /* TODO: abrir scanner QR Code */ }) {
                    Text("QR Code")
                }
                Button(onClick = { navController.navigate("password_add") }) {
                    Text("Adicionar")
                }
            }
        },
        containerColor = Color.Black
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            } else if (senhas.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Você ainda não tem senhas salvas.",
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { navController.navigate("password_add") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Adicionar Senha")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    items(senhas) { senha ->
                        PasswordCard(senha) {
                            // Navegar para detalhes da senha (implementação futura)
                            //navController.navigate("password_detail/${senha.id}")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordCard(senha: SenhaItem, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.DarkGray
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = senha.titulo,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = senha.categoria,
                    color = Color.LightGray,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = senha.login,
                color = Color.LightGray,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}