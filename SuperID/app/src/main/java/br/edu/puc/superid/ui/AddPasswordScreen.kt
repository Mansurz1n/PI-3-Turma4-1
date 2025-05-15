package br.edu.puc.superid.ui

import android.widget.Toast
import android.util.Base64
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.security.SecureRandom

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPasswordScreen(navController: NavController) {
    val context = LocalContext.current
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

    // Estado dos campos
    var category by remember { mutableStateOf("Sites Web") }
    val categories = listOf("Sites Web", "Aplicativos", "Teclados de Acesso Físico")
    var expanded by remember { mutableStateOf(false) }

    var serviceName by remember { mutableStateOf("") }
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Nova Senha",
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 26.sp),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Categoria
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = category,
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
                categories.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            category = option
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Nome do serviço
        OutlinedTextField(
            value = serviceName,
            onValueChange = { serviceName = it },
            label = { Text("Serviço") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Login (opcional)
        OutlinedTextField(
            value = login,
            onValueChange = { login = it },
            label = { Text("Login (opcional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Senha (texto puro)
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Senha") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Botão Salvar
        Button(
            onClick = {
                if (serviceName.isBlank() || password.isBlank()) {
                    Toast.makeText(context, "Serviço e senha são obrigatórios", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                // Gera accessToken de 256 chars em Base64
                val tokenBytes = ByteArray(192)
                SecureRandom().nextBytes(tokenBytes)
                val accessToken = Base64.encodeToString(tokenBytes, Base64.NO_WRAP)

                // Monta o objeto com senha em texto puro
                val entry = mapOf(
                    "categoria" to category,
                    "servico" to serviceName,
                    "login" to login,
                    "senha" to password,        // **sem criptografia por enquanto**
                    "accessToken" to accessToken
                )

                // Salva no Firestore
                val db = Firebase.firestore
                db.collection("usuarios")
                    .document(uid)
                    .collection("senhas")
                    .add(entry)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Senha cadastrada!", Toast.LENGTH_SHORT).show()
                        navController.popBackStack() // volta pro Home
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
}
