package br.edu.puc.superid.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

private fun hashSenha(senha: String, categoria: String): Pair<String, String> {
    // Escolhe o algoritmo com base na categoria
    val algoritmo = when (categoria) {
        "Rede Social" -> "SHA-256"
        "Streaming" -> "SHA-512"
        "Banco" -> "SHA-384"
        "E-mail" -> "SHA-1"
        "Trabalho" -> "SHA-224"
        else -> "MD5"
    }

    // Gera o hash usando o algoritmo escolhido
    val messageDigest = MessageDigest.getInstance(algoritmo)
    val hash = messageDigest.digest(senha.toByteArray())
    val hashBase64 = Base64.getEncoder().encodeToString(hash)

    // Gera o accessToken de 256 caracteres em Base64
    val random = SecureRandom()
    val tokenBytes = ByteArray(192) // 192 bytes em Base64 gera aprox. 256 caracteres
    random.nextBytes(tokenBytes)
    val accessToken = Base64.getEncoder().encodeToString(tokenBytes)

    // Retorna o hash e o accessToken
    return Pair("$algoritmo:$hashBase64", accessToken)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordAddScreen(navController: NavController) {
    var titulo by remember { mutableStateOf("") }
    var login by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var senhaVisivel by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Para o dropdown de categorias
    var categorias = listOf("Rede Social", "Streaming", "Banco", "E-mail", "Trabalho", "Outro")
    var categoriaSelecionada by remember { mutableStateOf(categorias[0]) }
    var expandidoDropdown by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = Firebase.firestore

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Adicionar Senha") },
                navigationIcon = {
                    TextButton(onClick = { navController.navigateUp() }) {
                        Text("Cancelar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Campo Título
            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título", fontSize = 18.sp) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color.DarkGray,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo Login
            OutlinedTextField(
                value = login,
                onValueChange = { login = it },
                label = { Text("Login", fontSize = 18.sp) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color.DarkGray,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo Senha
            OutlinedTextField(
                value = senha,
                onValueChange = { senha = it },
                label = { Text("Senha", fontSize = 18.sp) },
                visualTransformation = if (senhaVisivel) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    TextButton(onClick = { senhaVisivel = !senhaVisivel }) {
                        Text(if (senhaVisivel) "Ocultar" else "Mostrar")
                    }
                },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color.DarkGray,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Dropdown para Categoria
            ExposedDropdownMenuBox(
                expanded = expandidoDropdown,
                onExpandedChange = { expandidoDropdown = !expandidoDropdown },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    readOnly = true,
                    value = categoriaSelecionada,
                    onValueChange = {},
                    label = { Text("Categoria") },
                    trailingIcon = {
                        Icon(
                            Icons.Filled.ArrowDropDown,
                            contentDescription = "Selecionar categoria",
                            tint = Color.White
                        )
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = Color.DarkGray,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.Gray
                    )
                )

                ExposedDropdownMenu(
                    expanded = expandidoDropdown,
                    onDismissRequest = { expandidoDropdown = false },
                    modifier = Modifier.background(Color.DarkGray)
                ) {
                    categorias.forEach { categoria ->
                        DropdownMenuItem(
                            text = { Text(categoria, color = Color.White) },
                            onClick = {
                                categoriaSelecionada = categoria
                                expandidoDropdown = false
                            },
                            modifier = Modifier.background(Color.DarkGray)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botão Salvar
            Button(
                onClick = {
                    if (titulo.isNotBlank() && login.isNotBlank() && senha.isNotBlank()) {
                        isLoading = true
                        val currentUser = auth.currentUser

                        if (currentUser != null) {
                            val userId = currentUser.uid
                            val (senhaCriptografada, accessToken) = hashSenha(senha, categoriaSelecionada)

                            val senhaData = hashMapOf(
                                "titulo" to titulo,
                                "login" to login,
                                "senha" to senhaCriptografada,
                                "categoria" to categoriaSelecionada,
                                "dataCriacao" to System.currentTimeMillis(),
                                "accessToken" to accessToken
                            )

                            db.collection("usuarios")
                                .document(userId)
                                .collection("senhas")
                                .add(senhaData)
                                .addOnSuccessListener {
                                    isLoading = false
                                    Toast.makeText(context, "Senha adicionada com sucesso!", Toast.LENGTH_SHORT).show()
                                    navController.navigateUp()
                                }
                                .addOnFailureListener { e ->
                                    isLoading = false
                                    Toast.makeText(context, "Erro ao salvar senha: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                        } else {
                            isLoading = false
                            Toast.makeText(context, "Usuário não autenticado", Toast.LENGTH_SHORT).show()
                            navController.navigate("login")
                        }
                    } else {
                        Toast.makeText(context, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && titulo.isNotBlank() && login.isNotBlank() && senha.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text("Salvar Senha")
                }
            }
        }
    }
}