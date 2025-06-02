package br.edu.puc.superid.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPasswordScreen(navController: NavController) {
    val context = LocalContext.current
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val cryptoManager = remember { CryptoManager() }

    // — Estados de categorias —
    var categories by remember { mutableStateOf(listOf<String>()) }
    var selectedCategory by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    // Diálogo para criar nova categoria
    var showAddCatDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }

    // Busca categorias em tempo real
    LaunchedEffect(uid) {
        Firebase.firestore
            .collection("usuarios")
            .document(uid)
            .collection("categorias")
            .addSnapshotListener { snap, err ->
                if (err != null || snap == null) return@addSnapshotListener
                val cats = snap.documents.mapNotNull { it.getString("nome") }
                categories = cats
                if (selectedCategory.isEmpty() && cats.isNotEmpty()) {
                    selectedCategory = cats.first()
                }
            }
    }

    // — Estados do formulário —
    var serviceName by remember { mutableStateOf("") }
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Nova Senha",
                style = MaterialTheme.typography.headlineSmall
            )

            IconButton(
                onClick = { showAddCatDialog = true },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Category,
                    contentDescription = "Gerenciar Categorias"
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // Dropdown de Categoria
        ExposedDropdownMenuBox(
            expanded = dropdownExpanded,
            onExpandedChange = { dropdownExpanded = !dropdownExpanded }
        ) {
            OutlinedTextField(
                value = selectedCategory,
                onValueChange = {},
                readOnly = true,
                label = { Text("Categoria") },
                trailingIcon = { TrailingIcon(expanded = dropdownExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false }
            ) {
                categories.forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(cat) },
                        onClick = {
                            selectedCategory = cat
                            dropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Nome do serviço
        OutlinedTextField(
            value = serviceName,
            onValueChange = { serviceName = it },
            label = { Text("Serviço") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // Login (opcional)
        OutlinedTextField(
            value = login,
            onValueChange = { login = it },
            label = { Text("Login (opcional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // Senha (texto puro)
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Senha") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        // Botão Salvar
        Button(
            onClick = {
                if (selectedCategory.isBlank()) {
                    Toast.makeText(context, "Selecione ou crie uma categoria", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (serviceName.isBlank() || password.isBlank()) {
                    Toast.makeText(context, "Serviço e senha são obrigatórios", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                // Criptografa a senha antes de salvar
                val encryptedPassword = cryptoManager.encrypt(password)

                // Prepara objeto para salvar
                val entry = mapOf(
                    "categoria" to selectedCategory,
                    "servico"   to serviceName,
                    "login"     to login,
                    "senha"     to encryptedPassword
                )

                // Salva no Firestore
                Firebase.firestore
                    .collection("usuarios")
                    .document(uid)
                    .collection("senhas")
                    .add(entry)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Senha cadastrada!", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Salvar")
        }
    }

    // — Diálogo para criar categoria —
    if (showAddCatDialog) {
        AlertDialog(
            onDismissRequest = { showAddCatDialog = false },
            title = { Text("Nova Categoria") },
            text = {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text("Nome da categoria") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newCategoryName.isNotBlank()) {
                        Firebase.firestore
                            .collection("usuarios")
                            .document(uid)
                            .collection("categorias")
                            .add(mapOf(
                                "nome" to newCategoryName,
                                "isDefault" to false,
                                "canDelete" to true
                            ))
                            .addOnSuccessListener {
                                Toast.makeText(context, "Categoria criada!", Toast.LENGTH_SHORT).show()
                                newCategoryName = ""
                                showAddCatDialog = false
                                selectedCategory = newCategoryName
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Erro: ${it.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                }) {
                    Text("Adicionar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCatDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}