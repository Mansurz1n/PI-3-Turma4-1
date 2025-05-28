package br.edu.puc.superid.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

    // Estados principais da UI
    var showSettings by remember { mutableStateOf(false) }
    var nomeUsuario by remember { mutableStateOf("") }
    var categories by remember { mutableStateOf(listOf<String>()) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var passwordList by remember { mutableStateOf(listOf<SenhaEntry>()) }

    // Estados para o diálogo de edição
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedPassword by remember { mutableStateOf<SenhaEntry?>(null) }
    var editServico by remember { mutableStateOf("") }
    var editSenha by remember { mutableStateOf("") }
    var editCategoria by remember { mutableStateOf("") }
    var expandedCategorias by remember { mutableStateOf(false) }

    // Estado para confirmação de exclusão
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Carrega nome do usuário do Firestore
    LaunchedEffect(uid) {
        Firebase.firestore
            .collection("usuarios")
            .document(uid)
            .addSnapshotListener { doc, _ ->
                nomeUsuario = doc?.getString("nome") ?: ""
            }
    }

    // Carrega as categorias disponíveis para o usuário atual
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

    // Carrega senhas filtradas pela categoria selecionada
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
                            senha = doc.getString("senha") ?: "",
                            categoria = doc.getString("categoria") ?: ""
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
                colors = TopAppBarDefaults.topAppBarColors(
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
                // Barra de ações com botão Nova
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
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Nova Senha")
                    }
                }

                // Seletor de categoria
                CategorySelector(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onSelect = { category -> selectedCategory = category }
                )

                Spacer(Modifier.height(12.dp))

                // Lista de senhas da categoria selecionada
                LazyColumn(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(passwordList, key = { it.id }) { entry ->
                        PasswordCard(
                            entry = entry,
                            onEdit = {
                                selectedPassword = entry
                                editServico = entry.servico
                                editSenha = entry.senha
                                editCategoria = entry.categoria
                                showEditDialog = true
                            },
                            onDelete = {
                                selectedPassword = entry
                                showDeleteConfirmDialog = true
                            }
                        )
                    }
                }

                // Botão QR Code para scanear
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

            // Diálogo de edição de senha
            if (showEditDialog && selectedPassword != null) {
                AlertDialog(
                    onDismissRequest = { showEditDialog = false },
                    title = { Text("Editar Senha") },
                    text = {
                        Column {
                            // Campo de serviço
                            OutlinedTextField(
                                value = editServico,
                                onValueChange = { editServico = it },
                                label = { Text("Serviço") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    cursorColor = MaterialTheme.colorScheme.primary
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Campo de senha
                            OutlinedTextField(
                                value = editSenha,
                                onValueChange = { editSenha = it },
                                label = { Text("Senha") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    cursorColor = MaterialTheme.colorScheme.primary
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Menu dropdown para seleção de categoria
                            ExposedDropdownMenuBox(
                                expanded = expandedCategorias,
                                onExpandedChange = { expandedCategorias = it }
                            ) {
                                OutlinedTextField(
                                    value = editCategoria,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Categoria") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategorias) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        focusedLabelColor = MaterialTheme.colorScheme.primary
                                    )
                                )

                                ExposedDropdownMenu(
                                    expanded = expandedCategorias,
                                    onDismissRequest = { expandedCategorias = false }
                                ) {
                                    categories.forEach { categoria ->
                                        DropdownMenuItem(
                                            text = { Text(categoria) },
                                            onClick = {
                                                editCategoria = categoria
                                                expandedCategorias = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(onClick = { showEditDialog = false }) {
                                Text("Cancelar")
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Botão para salvar as alterações
                            Button(
                                onClick = {
                                    selectedPassword?.id?.let { id ->
                                        // Valida e atualiza os dados no Firestore
                                        if (editServico.isNotBlank() && editSenha.isNotBlank() && editCategoria.isNotBlank()) {
                                            val senhaData = hashMapOf(
                                                "servico" to editServico,
                                                "senha" to editSenha,
                                                "categoria" to editCategoria
                                            )

                                            Firebase.firestore
                                                .collection("usuarios")
                                                .document(uid)
                                                .collection("senhas")
                                                .document(id)
                                                .update(senhaData as Map<String, Any>)
                                                .addOnSuccessListener {
                                                    showEditDialog = false
                                                }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text("Salvar")
                            }
                        }
                    }
                )
            }

            // Diálogo de confirmação de exclusão
            if (showDeleteConfirmDialog && selectedPassword != null) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmDialog = false },
                    title = { Text("Excluir senha") },
                    text = { Text("Tem certeza que deseja excluir a senha de ${selectedPassword?.servico}?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                selectedPassword?.id?.let { id ->
                                    // Remove a senha do Firestore
                                    Firebase.firestore
                                        .collection("usuarios")
                                        .document(uid)
                                        .collection("senhas")
                                        .document(id)
                                        .delete()
                                        .addOnSuccessListener {
                                            showDeleteConfirmDialog = false
                                        }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        ) {
                            Text("Excluir")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = { showDeleteConfirmDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            // Painel de configurações
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

// Componente para exibir um cartão de senha na lista
@Composable
private fun PasswordCard(
    entry: SenhaEntry,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
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
                // Mostra senha ou oculta com asteriscos
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

            // Linha de ações para cada cartão
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Excluir",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

// Componente para seleção de categorias
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategorySelector(
    categories: List<String>,
    selectedCategory: String?,
    onSelect: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            val isSelected = category == selectedCategory
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(category) },
                label = { Text(category) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

// Modelo de dados para os itens de senha
data class SenhaEntry(
    val id: String,
    val servico: String,
    val senha: String,
    val categoria: String = ""
)