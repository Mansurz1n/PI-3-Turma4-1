package br.edu.puc.superid.ui

import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import br.edu.puc.superid.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.security.MessageDigest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController) {
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var aceitarTermos by remember { mutableStateOf(false) }
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

        Text(
            "Cadastro no SuperID!",
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 26.sp),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text("Nome") },
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

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
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            Checkbox(
                checked = aceitarTermos,
                onCheckedChange = { aceitarTermos = it },
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

        Button(
            onClick = {
                if (nome.isBlank() || email.isBlank() || senha.isBlank()) {
                    Toast.makeText(context, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                auth.createUserWithEmailAndPassword(email, senha)
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
                                Toast.makeText(context, "Cadastro realizado!", Toast.LENGTH_LONG).show()
                                navController.navigate("login")
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "Erro: ${task.exception?.message}",
                                Toast.LENGTH_LONG
                            ).show()
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

        Spacer(Modifier.height(12.dp))

        TextButton(
            onClick = { navController.navigate("login") },
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Já possuo conta", textDecoration = TextDecoration.Underline)
        }
    }

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text("Termos de Uso") },
            text = {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        """
                        1️⃣ Aceitação
                        Ao utilizar este aplicativo, você automaticamente concorda com absolutamente tudo que está escrito aqui, mesmo sem ter lido nada (porque ninguém lê, né?). Qualquer choro posterior será ignorado com sucesso.

                        2️⃣ Coleta de Dados (ou quase isso)
                        A gente coleta tudo. Seu nome, seu e-mail, sua senha, seu Android ID (porque a gente precisa parecer sério, nem sei se isso existe), seu histórico de navegação, seus sonhos, suas frustrações e até aquela busca estranha que você fez às 3h da manhã. E sim, a gente julga todas elas.

                        3️⃣ Uso das Informações
                        Usaremos seus dados para coisas super importantes, como guardar suas senhas… e, sei lá, talvez vender para a Associação dos Illuminati. Mas fique tranquilo, só em casos extremos (ou quando estiver precisando pagar o boleto do mês).

                        4️⃣ Segurança (ou perto disso porque é mínima)
                        Prometemos guardar suas senhas com a maior segurança possível (leia-se: senha123 guardada em um post-it digital). Se der ruim, a culpa é sua por confiar KKKKKKKKKKKKKKK.

                        5️⃣ Responsabilidades
                        Você é 100% responsável por tudo que fizer. Se esquecer a senha ou bloquear sua conta, não adianta mandar mensagem chorando. Ninguém vai responder. Mentira, NÓS respondemos… depois de 30 dias úteis

                        6️⃣ Alterações nos Termos
                        A gente pode mudar tudo isso aqui sem avisar. Pode ser amanhã, pode ser agora mesmo enquanto você lê. É a vida. Não gostou? sente, chore e faça o L

                        7️⃣ Considerações Finais
                        Ao clicar em “Aceito”, você basicamente nos dá a sua alma e do seu ente querido de maior preferencia, um rim, metade do fígado e 4 pontos extras nossa minha ficha do D&D 5e, obrigado mestre.
                        """.trimIndent()
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

// função de hashing no mesmo arquivo
private fun hashPassword(senha: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(senha.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}