package br.edu.puc.superid.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import br.edu.puc.superid.R
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var showResetDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo SuperID",
            modifier = Modifier
                .size(150.dp)
                .padding(bottom = 24.dp)
        )

        Text(
            "Login no SuperID!",
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 26.sp),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                containerColor = MaterialTheme.colorScheme.surface,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = senha,
            onValueChange = { senha = it },
            label = { Text("Senha") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                containerColor = MaterialTheme.colorScheme.surface,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "Esqueci minha senha",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier
                .clickable { showResetDialog = true }
                .padding(4.dp)
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (email.isNotBlank() && senha.isNotBlank()) {
                    auth.signInWithEmailAndPassword(email, senha)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Login bem-sucedido!", Toast.LENGTH_SHORT).show()
                                navController.navigate("home")
                            } else {
                                Toast.makeText(
                                    context,
                                    "Erro no login: ${task.exception?.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                } else {
                    Toast.makeText(context, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Continuar")
        }

        Spacer(Modifier.height(16.dp))

        TextButton(
            onClick = { navController.navigate("register") },
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                "Não possuo conta",
                fontSize = 14.sp,
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
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("E-mail") },
                        singleLine = true,
                        textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            containerColor = MaterialTheme.colorScheme.surface,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (resetEmail.isNotBlank()) {
                        auth.sendPasswordResetEmail(resetEmail)
                            .addOnCompleteListener { resetTask ->
                                if (resetTask.isSuccessful) {
                                    Toast.makeText(context, "Link enviado para $resetEmail", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "Erro: ${resetTask.exception?.message}", Toast.LENGTH_LONG).show()
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
