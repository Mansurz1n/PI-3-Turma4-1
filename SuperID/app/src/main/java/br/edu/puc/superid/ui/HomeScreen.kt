package br.edu.puc.superid.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// Modelo de dados

data class SenhaEntry(
    val id: String,
    val servico: String = "",
    val senha: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

    var showSettings by remember { mutableStateOf(false) }
    var nomeUsuario by remember { mutableStateOf("") }
    var categories by remember { mutableStateOf(listOf<String>()) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var passwordList by remember { mutableStateOf(listOf<SenhaEntry>()) }

    // Carrega nome do usuário
    LaunchedEffect(uid) {
        Firebase.firestore
            .collection("usuarios")
            .document(uid)
            .addSnapshotListener { doc, _ ->
                nomeUsuario = doc?.getString("nome") ?: ""
            }
    }

    // Carrega categorias
    LaunchedEffect(uid) {
        Firebase.firestore
            .collection("usuarios")
            .document(uid)
            .collection("categorias")
            .addSnapshotListener { snap, err ->
                if (err != null || snap == null) return@addSnapshotListener
                val cats = snap.documents.mapNotNull { it.getString("nome") }
                categories = cats
                if (selectedCategory == null && cats.isNotEmpty()) selectedCategory = cats.first()
            }
    }

    // Carrega senhas filtradas
    LaunchedEffect(uid, selectedCategory) {
        if (selectedCategory != null) {
            Firebase.firestore
                .collection("usuarios")
                .document(uid)
                .collection("senhas")
                .whereEqualTo("categoria", selectedCategory)
                .addSnapshotListener { snap, err ->
                    if (err != null || snap == null) return@addSnapshotListener
                    passwordList = snap.documents.map { doc ->
                        SenhaEntry(
                            id = doc.id,
                            servico = doc.getString("servico") ?: "",
                            senha = doc.getString("senha") ?: ""
                        )
                    }
                }
        } else {
            passwordList = emptyList()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("SuperID") },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Configurações",
                            tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            Column {
                // Botões no topo
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = { navController.navigate("addPassword") },
                        modifier = Modifier.padding(horizontal = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) { Text("Nova") }
                    Button(
                        onClick = { /* editar */ },
                        modifier = Modifier.padding(horizontal = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) { Text("Editar") }
                }

                // Seletor de categoria
                CategorySelector(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onSelect = { selectedCategory = it }
                )

                Spacer(Modifier.height(12.dp))

                // Lista de senhas
                LazyColumn(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(passwordList, key = { it.id }) { entry -> PasswordCard(entry) }
                }

                // Ícone QR Code
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = { navController.navigate("scanQRCode") }) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan QR Code",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onBackground)
                    }
                }
            }

            // Configurações
            if (showSettings) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                        .clickable { showSettings = false }
                )
                Card(
                    Modifier
                        .fillMaxWidth(0.9f)
                        .align(Alignment.TopCenter)
                        .padding(top = 80.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Configurações", style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(12.dp))
                        Text("Nome: $nomeUsuario", color = MaterialTheme.colorScheme.onSurface)
                        Text("Email: ${FirebaseAuth.getInstance().currentUser?.email}",
                            color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(16.dp))
                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                        Spacer(Modifier.height(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Tema escuro", Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onSurface)
                            Switch(
                                checked = isDarkTheme,
                                onCheckedChange = onToggleTheme,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = {
                                FirebaseAuth.getInstance().signOut()
                                navController.popBackStack()
                                navController.navigate("login")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        ) { Text("Sair") }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategorySelector(
    categories: List<String>,
    selectedCategory: String?,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedCategory.orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text("Categoria") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { cat ->
                DropdownMenuItem(
                    text = { Text(cat) },
                    onClick = {
                        onSelect(cat)
                        expanded = false
                    }
                )
            }
        }
    }
    Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f))
}

@Composable
private fun PasswordCard(entry: SenhaEntry) {
    var visible by remember { mutableStateOf(false) }
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text("Site: ${entry.servico}", fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (visible) entry.senha else "••••••••••",
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = { visible = !visible }) {
                    Icon(
                        imageVector = if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (visible) "Ocultar" else "Mostrar",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}