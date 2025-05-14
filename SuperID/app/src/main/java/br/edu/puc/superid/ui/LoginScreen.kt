package br.edu.puc.superid.ui

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var showResetDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Login no SuperID",
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 26.sp),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = senha,
            onValueChange = { senha = it },
            label = { Text("Senha") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Esqueci minha senha",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier
                .clickable { showResetDialog = true }
                .padding(4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (email.isNotBlank() && senha.isNotBlank()) {
                    auth.signInWithEmailAndPassword(email, senha)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                // Verifica se o e-mail foi verificado
                                if (user != null && user.isEmailVerified) {
                                    Toast.makeText(context, "Login bem-sucedido!", Toast.LENGTH_SHORT).show()
                                    navController.navigate("home")
                                } else {
                                    // Se o e-mail não foi verificado
                                    Toast.makeText(context, "Por favor, verifique seu e-mail antes de fazer login.", Toast.LENGTH_LONG).show()
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    "Erro no login: ${task.exception?.message?.split(":")?.last()?.trim() ?: "Erro desconhecido"}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                } else {
                    Toast.makeText(context, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Entrar")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { navController.navigate("register") }
        ) {
            Text(
                text = "Não possuo conta",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline
            )
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Recuperar Senha") },
            text = {
                Column {
                    Text("Informe seu e-mail para receber o link de redefinição:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("E-mail") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (resetEmail.isNotBlank()) {
                        // Verificação se o e-mail está associado a um usuário
                        auth.fetchSignInMethodsForEmail(resetEmail).addOnCompleteListener { emailTask ->
                            if (emailTask.isSuccessful && emailTask.result?.signInMethods?.isNotEmpty() == true) {
                                auth.sendPasswordResetEmail(resetEmail)
                                    .addOnCompleteListener { resetTask ->
                                        if (resetTask.isSuccessful) {
                                            Toast.makeText(
                                                context,
                                                "Link enviado para $resetEmail",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Erro: ${resetTask.exception?.message?.split(":")?.last()?.trim() ?: "Erro desconhecido"}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                            } else {
                                Toast.makeText(context, "Este e-mail não está associado a uma conta.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "Digite um e-mail válido", Toast.LENGTH_SHORT).show()
                    }
                    showResetDialog = false
                }) {
                    Text("Enviar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}