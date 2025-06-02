// RegisterScreen.kt

package br.edu.puc.superid.ui

import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.security.MessageDigest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController) {
    // Estados de campos e validações
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var aceitarTermos by remember { mutableStateOf(false) }

    var nomeError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var senhaError by remember { mutableStateOf<String?>(null) }
    var authError by remember { mutableStateOf<String?>(null) }

    // Estados para "Termos de Uso"
    var mostrarDialogo by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = Firebase.firestore

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo no topo
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo SuperID",
            modifier = Modifier
                .size(150.dp)
                .padding(bottom = 24.dp)
        )

        // Título
        Text(
            "Cadastro no SuperID!",
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 26.sp),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Campo Nome com asterisco e validação
        OutlinedTextField(
            value = nome,
            onValueChange = {
                nome = it
                nomeError = null
                authError = null
            },
            label = {
                val annotated = buildAnnotatedString {
                    append("Nome")
                    withStyle(
                        style = androidx.compose.ui.text.SpanStyle(
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(" *")
                    }
                }
                Text(text = annotated)
            },
            singleLine = true,
            isError = nomeError != null,
            textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = if (nomeError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = if (nomeError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                containerColor = MaterialTheme.colorScheme.surface,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = if (nomeError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
        if (nomeError != null) {
            Text(
                text = nomeError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp)
            )
        }

        // Campo Email com asterisco e validação
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
                    withStyle(
                        style = androidx.compose.ui.text.SpanStyle(
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
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

        // Campo Senha com asterisco e validação
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
                    withStyle(
                        style = androidx.compose.ui.text.SpanStyle(
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
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

        // Checkbox de Termos de Uso
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            Checkbox(
                checked = aceitarTermos,
                onCheckedChange = {
                    aceitarTermos = it
                    authError = null
                },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(Modifier.width(8.dp))
            Text("Li e aceito os ")
            Text(
                "Termos de Uso",
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable { mostrarDialogo = true }
            )
        }

        // Botão "Cadastrar" com erros centralizados
        Button(
            onClick = {
                // Limpa erros anteriores
                nomeError = null
                emailError = null
                senhaError = null
                authError = null

                // Validação de campos
                var valid = true
                if (nome.isBlank()) {
                    nomeError = "Campo obrigatório"
                    valid = false
                }
                if (email.isBlank()) {
                    emailError = "Campo obrigatório"
                    valid = false
                }
                if (senha.isBlank()) {
                    senhaError = "Campo obrigatório"
                    valid = false
                }
                if (!aceitarTermos) {
                    authError = "Você deve aceitar os Termos de Uso"
                    valid = false
                }

                if (valid) {
                    auth.createUserWithEmailAndPassword(email.trim(), senha)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser!!
                                user.sendEmailVerification()
                                val androidId = Settings.Secure.getString(
                                    context.contentResolver,
                                    Settings.Secure.ANDROID_ID
                                )
                                val hashed = hashPassword(senha)
                                db.collection("usuarios").document(user.uid).set(
                                    mapOf(
                                        "uid" to user.uid,
                                        "nome" to nome,
                                        "email" to email,
                                        "senha" to hashed,
                                        "androidId" to androidId
                                    )
                                ).addOnSuccessListener {
                                    navController.navigate("login")
                                }.addOnFailureListener { e ->
                                    authError = e.message ?: "Erro ao salvar usuário"
                                }
                            } else {
                                // Traduzir o erro para português
                                val exception = task.exception
                                val errorCode = (exception as? FirebaseAuthException)?.errorCode

                                authError = when (errorCode) {
                                    "ERROR_INVALID_EMAIL" -> "E-mail mal formatado"
                                    "ERROR_EMAIL_ALREADY_IN_USE" -> "E-mail já cadastrado"
                                    "ERROR_WEAK_PASSWORD" -> "Senha muito fraca (mín. 6 caracteres)"
                                    "ERROR_OPERATION_NOT_ALLOWED" -> "Cadastro não permitido"
                                    else -> exception?.localizedMessage
                                        ?: "Erro desconhecido no cadastro"
                                }
                            }
                        }
                }
            },
            enabled = aceitarTermos,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Cadastrar")
        }

        // Texto de erro geral (autenticação ou termos), centralizado
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

        Spacer(Modifier.height(12.dp))

        // Texto "Já possuo conta"
        TextButton(
            onClick = { navController.navigate("login") },
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                "Já possuo conta",
                textDecoration = TextDecoration.Underline
            )
        }
    }

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text("Termos de Uso") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(end = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. Aceitação
                    Text(
                        "1. Aceitação",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "Ao usar o SuperID, você concorda com estes Termos de Uso. Se não concordar, não deve utilizar o aplicativo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 16.sp
                    )

                    // 2. Cadastro de Conta
                    Text(
                        "2. Cadastro de Conta",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "Para criar sua conta, informe Nome, E-mail válido e Senha Mestre (mín. 6 caracteres). É necessário confirmar seu e-mail para concluir o cadastro.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 16.sp
                    )

                    // 3. Dados e Segurança
                    Text(
                        "3. Dados e Segurança",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "Suas senhas são criptografadas antes de serem enviadas ao nosso banco de dados. Não armazenamos sua Senha Mestre em texto simples. Proteja seu dispositivo e senha.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 16.sp
                    )

                    // 4. Privacidade
                    Text(
                        "4. Privacidade",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "Coletamos apenas Nome, E-mail, UID e identificador do dispositivo. Suas senhas ficam armazenadas apenas em formato criptografado. Dados vinculados à sua conta serão excluídos permanentemente se você solicitar remoção.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 16.sp
                    )

                    // 5. Responsabilidade
                    Text(
                        "5. Responsabilidade",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "O SuperID é fornecido “no estado em que se encontra”, sem garantias. Não nos responsabilizamos por perdas diretas ou indiretas decorrentes do uso do aplicativo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 16.sp
                    )

                    // 6. Contato
                    Text(
                        "6. Contato",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "Em caso de dúvidas, envie e-mail para: suporte@superid.com.br (e-mail 100% fictício, não tente contactar)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 16.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarDialogo = false }) {
                    Text("Fechar")
                }
            }
        )
    }
}

    // Função de hashing no mesmo arquivo
private fun hashPassword(senha: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(senha.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}
