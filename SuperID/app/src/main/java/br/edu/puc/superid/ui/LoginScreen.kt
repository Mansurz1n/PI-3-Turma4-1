package br.edu.puc.superid.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import br.edu.puc.superid.R
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    // Estados de login
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var senhaError by remember { mutableStateOf<String?>(null) }
    var authError by remember { mutableStateOf<String?>(null) }

    // Estados para "Esqueci minha senha"
    var showResetDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    var resetError by remember { mutableStateOf<String?>(null) }
    var resetSuccess by remember { mutableStateOf<String?>(null) }

    val auth = FirebaseAuth.getInstance()

    Column(
        modifier = Modifier
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

        // Título
        Text(
            "Login no SuperID!",
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 26.sp),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Campo de Email com asterisco e validações
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = null
                authError = null
            },
            label = {
                val annotated = buildAnnotatedString {
                    append("Email")
                    withStyle(style = androidx.compose.ui.text.SpanStyle(
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )) {
                        append(" *")
                    }
                }
                Text(text = annotated)
            },
            singleLine = true,
            isError = emailError != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = if (emailError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = if (emailError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                containerColor = MaterialTheme.colorScheme.surface,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = if (emailError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        )
        if (emailError != null) {
            Text(
                text = emailError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        // Campo de Senha com asterisco e validações
        OutlinedTextField(
            value = senha,
            onValueChange = {
                senha = it
                senhaError = null
                authError = null
            },
            label = {
                val annotated = buildAnnotatedString {
                    append("Senha")
                    withStyle(style = androidx.compose.ui.text.SpanStyle(
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )) {
                        append(" *")
                    }
                }
                Text(text = annotated)
            },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            isError = senhaError != null,
            textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = if (senhaError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = if (senhaError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                containerColor = MaterialTheme.colorScheme.surface,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = if (senhaError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        )
        if (senhaError != null) {
            Text(
                text = senhaError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(Modifier.height(8.dp))

        // Texto "Esqueci minha senha"
        Text(
            "Esqueci minha senha",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier
                .clickable {
                    showResetDialog = true
                    resetEmail = ""
                    resetError = null
                    resetSuccess = null
                }
                .padding(4.dp)
        )

        Spacer(Modifier.height(24.dp))

        // Botão "Continuar" para login (com erro centralizado)
        Button(
            onClick = {
                // Limpa erros anteriores
                emailError = null
                senhaError = null
                authError = null

                // Validação de campos
                var valid = true
                if (email.isBlank()) {
                    emailError = "Campo obrigatório"
                    valid = false
                }
                if (senha.isBlank()) {
                    senhaError = "Campo obrigatório"
                    valid = false
                }

                if (valid) {
                    auth.signInWithEmailAndPassword(email.trim(), senha)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                navController.navigate("home")
                            } else {
                                authError = "Email ou senha incorretos"
                            }
                        }
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

        // Texto de erro de autenticação, centralizado horizontalmente
        if (authError != null) {
            Text(
                text = authError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        // Texto "Não possuo conta"
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

    // Dialog "Recuperar Senha"
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = {
                showResetDialog = false
                resetError = null
                resetSuccess = null
            },
            title = { Text("Recuperar Senha") },
            text = {
                Column {
                    Text("Informe seu e-mail para receber o link de redefinição:")
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = {
                            resetEmail = it
                            resetError = null
                            resetSuccess = null
                        },
                        label = { Text("E-mail") },
                        singleLine = true,
                        isError = resetError != null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = if (resetError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = if (resetError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                            containerColor = MaterialTheme.colorScheme.surface,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = if (resetError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (resetError != null) {
                        Text(
                            text = resetError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, top = 4.dp)
                        )
                    }

                    if (resetSuccess != null) {
                        Text(
                            text = resetSuccess!!,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (resetEmail.isBlank()) {
                        resetError = "Digite um e-mail válido"
                        resetSuccess = null
                        return@TextButton
                    }
                    FirebaseAuth.getInstance()
                        .sendPasswordResetEmail(resetEmail.trim())
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                resetSuccess = "Link enviado para $resetEmail"
                                resetError = null
                            } else {
                                resetError = task.exception?.message ?: "Erro ao enviar link"
                                resetSuccess = null
                            }
                        }
                }) {
                    Text("Enviar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showResetDialog = false
                    resetError = null
                    resetSuccess = null
                }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
