package br.edu.puc.superid.ui

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
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
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit
) {
    // Obtém UID; se nulo, não renderiza HomeScreen
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

    // SnackbarHostState e CoroutineScope
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Estados principais da UI
    var showSettings by remember { mutableStateOf(false) }
    var nomeUsuario by remember { mutableStateOf("") }
    var categories by remember { mutableStateOf(listOf<CategoryData>()) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var passwordList by remember { mutableStateOf(listOf<SenhaEntry>()) }

    // Estados para o diálogo de edição
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedPassword by remember { mutableStateOf<SenhaEntry?>(null) }
    var editServico by remember { mutableStateOf("") }
    var editSenha by remember { mutableStateOf("") }
    var editCategoria by remember { mutableStateOf("") }
    var expandedCategorias by remember { mutableStateOf(false) }

    // Estados para o gerenciador de categorias
    var showCategoryManager by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<CategoryData?>(null) }
    var showDeleteCategoryConfirm by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }

    // Estado para confirmação de exclusão
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showDeleteCategoryError by remember { mutableStateOf(false) }

    // Inicializa o gerenciador de criptografia
    val cryptoManager = remember { CryptoManager() }

    // Carrega nome do usuário do Firestore
    LaunchedEffect(uid) {
        Firebase.firestore
            .collection("usuarios")
            .document(uid)
            .addSnapshotListener { doc, _ ->
                nomeUsuario = doc?.getString("nome") ?: ""
            }
    }

    // Carrega categorias e garante categorias padrão
    LaunchedEffect(uid) {
        ensureDefaultCategories(uid)

        Firebase.firestore
            .collection("usuarios")
            .document(uid)
            .collection("categorias")
            .addSnapshotListener { snap, err ->
                if (err != null || snap == null) return@addSnapshotListener
                val cats = snap.documents.map { doc ->
                    CategoryData(
                        id = doc.id,
                        nome = doc.getString("nome") ?: "",
                        isDefault = doc.getBoolean("isDefault") ?: false,
                        canDelete = doc.getBoolean("canDelete") ?: true
                    )
                }
                categories = cats
                if (selectedCategory == null && cats.isNotEmpty()) {
                    selectedCategory = cats.find { it.nome == "Sites Web" }?.nome
                        ?: cats.firstOrNull()?.nome
                }
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
                        val encryptedPassword = doc.getString("senha") ?: ""
                        val decryptedPassword = try {
                            cryptoManager.decrypt(encryptedPassword)
                        } catch (e: Exception) {
                            Log.e("HomeScreen", "Erro ao descriptografar: ${e.message}", e)
                            encryptedPassword
                        }
                        SenhaEntry(
                            id = doc.id,
                            servico = doc.getString("servico") ?: "",
                            senha = decryptedPassword,
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
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("SuperID") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Configurações",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
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
                // ===== Barra de ações: Nova Senha / Categorias =====
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { navController.navigate("addPassword") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Nova Senha")
                    }

                    Button(
                        onClick = { showCategoryManager = true },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Default.Category, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Categorias")
                    }
                }

                // ===== Seletor de categorias =====
                CategorySelector(
                    categories = categories.map { it.nome },
                    selectedCategory = selectedCategory,
                    onSelect = { category -> selectedCategory = category }
                )

                Spacer(Modifier.height(12.dp))

                // ===== Lista de senhas =====
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

                // ===== Botão QR Code =====
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = {
                            val currentUser = FirebaseAuth.getInstance().currentUser
                            if (currentUser != null) {
                                // Força a recarga do estado do usuário antes de checar isEmailVerified
                                currentUser.reload()
                                    .addOnSuccessListener {
                                        // Depois de recarregar, verifica isEmailVerified atualizado
                                        if (currentUser.isEmailVerified) {
                                            navController.navigate("scanQRCode")
                                        } else {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Verifique seu email")
                                            }
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("HomeScreen", "Erro ao reload do usuário: ${e.message}", e)
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Erro ao verificar e-mail")
                                        }
                                    }
                            } else {
                                // Se usuário não está logado, volta para login
                                navController.navigate("login") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = "Scan QR Code",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            // ===== Diálogo Gerenciar Categorias =====
            if (showCategoryManager) {
                AlertDialog(
                    onDismissRequest = { showCategoryManager = false },
                    title = { Text("Gerenciar Categorias") },
                    text = {
                        Column {
                            // Entrada para nova categoria
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = newCategoryName,
                                    onValueChange = { newCategoryName = it },
                                    label = { Text("Nova Categoria") },
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(Modifier.width(8.dp))
                                IconButton(onClick = {
                                    if (newCategoryName.isNotBlank()) {
                                        val newCategory = hashMapOf(
                                            "nome" to newCategoryName,
                                            "isDefault" to false,
                                            "canDelete" to true
                                        )
                                        Firebase.firestore
                                            .collection("usuarios")
                                            .document(uid)
                                            .collection("categorias")
                                            .add(newCategory)
                                            .addOnSuccessListener {
                                                newCategoryName = ""
                                            }
                                    }
                                }) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Adicionar categoria",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(Modifier.height(16.dp))
                            Text("Categorias existentes:", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))

                            LazyColumn(
                                modifier = Modifier.heightIn(max = 250.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(categories) { category ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = category.nome,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (category.canDelete) {
                                            IconButton(onClick = {
                                                categoryToDelete = category
                                                showDeleteCategoryConfirm = true
                                            }) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "Excluir categoria",
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    }
                                    Divider()
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showCategoryManager = false }) {
                            Text("Fechar")
                        }
                    }
                )
            }

            // ===== Diálogo Confirmar Exclusão de Categoria =====
            if (showDeleteCategoryConfirm && categoryToDelete != null) {
                AlertDialog(
                    onDismissRequest = { showDeleteCategoryConfirm = false },
                    title = { Text("Excluir Categoria") },
                    text = {
                        Column {
                            Text("Tem certeza que deseja excluir a categoria '${categoryToDelete?.nome}'?")
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Aviso: Todas as senhas nesta categoria também serão excluídas!",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                categoryToDelete?.let { category ->
                                    Firebase.firestore
                                        .collection("usuarios")
                                        .document(uid)
                                        .collection("senhas")
                                        .whereEqualTo("categoria", category.nome)
                                        .get()
                                        .addOnSuccessListener { querySnapshot ->
                                            for (doc in querySnapshot.documents) {
                                                doc.reference.delete()
                                            }
                                            Firebase.firestore
                                                .collection("usuarios")
                                                .document(uid)
                                                .collection("categorias")
                                                .document(category.id)
                                                .delete()
                                                .addOnSuccessListener {
                                                    if (selectedCategory == category.nome) {
                                                        selectedCategory = categories
                                                            .filter { it.nome != category.nome }
                                                            .firstOrNull()?.nome
                                                    }
                                                }
                                        }
                                }
                                showDeleteCategoryConfirm = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Excluir")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteCategoryConfirm = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            // ===== Diálogo Editar Senha =====
            if (showEditDialog && selectedPassword != null) {
                AlertDialog(
                    onDismissRequest = { showEditDialog = false },
                    title = { Text("Editar Senha") },
                    text = {
                        Column {
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
                            ExposedDropdownMenuBox(
                                expanded = expandedCategorias,
                                onExpandedChange = { expandedCategorias = it }
                            ) {
                                OutlinedTextField(
                                    value = editCategoria,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Categoria") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(
                                            expanded = expandedCategorias
                                        )
                                    },
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
                                    categories.forEach { category ->
                                        DropdownMenuItem(
                                            text = { Text(category.nome) },
                                            onClick = {
                                                editCategoria = category.nome
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
                            Button(
                                onClick = {
                                    selectedPassword?.id?.let { id ->
                                        if (
                                            editServico.isNotBlank() &&
                                            editSenha.isNotBlank() &&
                                            editCategoria.isNotBlank()
                                        ) {
                                            val encryptedPassword = cryptoManager.encrypt(editSenha)
                                            val senhaData = hashMapOf(
                                                "servico" to editServico,
                                                "senha" to encryptedPassword,
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

            // ===== Diálogo Confirmar Exclusão de Senha =====
            if (showDeleteConfirmDialog && selectedPassword != null) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmDialog = false },
                    title = { Text("Excluir senha") },
                    text = {
                        Text("Tem certeza que deseja excluir a senha de ${selectedPassword?.servico}?")
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                selectedPassword?.id?.let { id ->
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

            // ===== Diálogo Erro ao Excluir Categoria Protegida =====
            if (showDeleteCategoryError) {
                AlertDialog(
                    onDismissRequest = { showDeleteCategoryError = false },
                    title = { Text("Não é possível excluir") },
                    text = { Text("A categoria Sites Web é obrigatória e não pode ser excluída.") },
                    confirmButton = {
                        Button(onClick = { showDeleteCategoryError = false }) {
                            Text("OK")
                        }
                    }
                )
            }

            // ===== Painel de Configurações =====
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
                        Text(
                            "Configurações",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(12.dp))
                        Text("Nome: $nomeUsuario", color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            "Email: ${FirebaseAuth.getInstance().currentUser?.email}",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(16.dp))
                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                        Spacer(Modifier.height(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Tema escuro",
                                Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onSurface
                            )
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

// Classe que gerencia a criptografia das senhas
class CryptoManager {
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    private val encryptCipher = Cipher.getInstance(TRANSFORMATION)
    private val decryptCipher = Cipher.getInstance(TRANSFORMATION)

    companion object {
        private const val KEYSTORE_ALIAS = "SuperIDPasswordKey"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val IV_LENGTH = 12 // GCM recommended IV length
    }

    init {
        initOrCreateKey()
        initCiphers()
    }

    private fun initOrCreateKey() {
        if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
            )
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEYSTORE_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            ).apply {
                setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                setKeySize(256)
                setUserAuthenticationRequired(false)
            }.build()
            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }
    }

    private fun initCiphers() {
        val key = keyStore.getKey(KEYSTORE_ALIAS, null) as SecretKey
        encryptCipher.init(Cipher.ENCRYPT_MODE, key)
    }

    fun encrypt(plainText: String): String {
        val encryptedBytes = encryptCipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        val iv = encryptCipher.iv
        initCiphers() // Reinicializa para próxima operação
        val combined = ByteArray(iv.size + encryptedBytes.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)
        return Base64.encodeToString(combined, Base64.DEFAULT)
    }

    fun decrypt(encryptedData: String): String {
        return try {
            val decodedData = Base64.decode(encryptedData, Base64.DEFAULT)
            val iv = decodedData.copyOfRange(0, IV_LENGTH)
            val encryptedBytes = decodedData.copyOfRange(IV_LENGTH, decodedData.size)
            val key = keyStore.getKey(KEYSTORE_ALIAS, null) as SecretKey
            decryptCipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
            val decryptedBytes = decryptCipher.doFinal(encryptedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e("CryptoManager", "Erro na descriptografia: ${e.message}", e)
            encryptedData
        }
    }
}

// Função para garantir que as categorias padrão existam para o usuário
private fun ensureDefaultCategories(uid: String) {
    val db = Firebase.firestore
    val categoriesRef = db.collection("usuarios").document(uid).collection("categorias")

    val defaultCategories = listOf(
        mapOf("nome" to "Sites Web", "isDefault" to true, "canDelete" to false),
        mapOf("nome" to "Aplicativos", "isDefault" to true, "canDelete" to true),
        mapOf("nome" to "Teclados de Acesso Físico", "isDefault" to true, "canDelete" to true)
    )

    categoriesRef.get().addOnSuccessListener { snapshot ->
        val existingCategories = snapshot.documents.mapNotNull { it.getString("nome") }
        defaultCategories.forEach { categoryData ->
            val categoryName = categoryData["nome"] as String
            if (!existingCategories.contains(categoryName)) {
                categoriesRef.add(categoryData).addOnFailureListener { e ->
                    Log.e("HomeScreen", "Erro ao criar categoria padrão: $categoryName", e)
                }
            } else {
                if (categoryName == "Sites Web") {
                    categoriesRef
                        .whereEqualTo("nome", "Sites Web")
                        .get()
                        .addOnSuccessListener { result ->
                            for (doc in result) {
                                doc.reference.update("isDefault", true, "canDelete", false)
                            }
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

// Modelo de dados para SenhaEntry
data class SenhaEntry(
    val id: String,
    val servico: String,
    val senha: String,
    val categoria: String = ""
)

// Modelo de dados para CategoryData
data class CategoryData(
    val id: String = "",
    val nome: String = "",
    val isDefault: Boolean = false,
    val canDelete: Boolean = true
)
