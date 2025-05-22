package br.edu.puc.superid.ui

import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.security.MessageDigest

@Composable
fun RegisterScreen(navController: NavController) {
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var aceitarTermos by remember { mutableStateOf(false) }
    var mostrarDialogo by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Cadastro no SuperID",
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 26.sp),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text("Nome") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

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

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = aceitarTermos,
                onCheckedChange = { aceitarTermos = it }
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Li e aceito os ")
                Text(
                    text = "Termos de Uso",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        mostrarDialogo = true
                    },
                    style = LocalTextStyle.current.copy(
                        textDecoration = TextDecoration.Underline
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (nome.isBlank() || email.isBlank() || senha.isBlank()) {
                    Toast.makeText(context, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(email, senha)
                    .addOnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            Toast.makeText(context,
                                "Erro ao criar usuário: ${task.exception?.message}",
                                Toast.LENGTH_LONG
                            ).show()
                            return@addOnCompleteListener
                        }

                        val user = FirebaseAuth.getInstance().currentUser!!
                        user.sendEmailVerification().addOnCompleteListener { verifTask ->
                            if (!verifTask.isSuccessful) {
                                Toast.makeText(context,
                                    "Erro ao enviar e-mail de verificação",
                                    Toast.LENGTH_LONG
                                ).show()
                                return@addOnCompleteListener
                            }

                            // Aqui pegamos o Android ID
                            val androidId = Settings.Secure.getString(
                                context.contentResolver,
                                Settings.Secure.ANDROID_ID
                            )

                            val uid = user.uid
                            val db = Firebase.firestore
                            val hashedPassword = hashPassword(senha)

                            val usuario = mapOf(
                                "uid" to uid,
                                "nome" to nome,
                                "email" to email,
                                "senha" to hashedPassword,
                                "androidId" to androidId
                            )

                            db.collection("usuarios").document(uid).set(usuario)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        context,
                                        "Cadastro realizado! Verifique seu e-mail.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    navController.navigate("login")
                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        context,
                                        "Erro ao salvar no Firestore: ${it.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        }
                    }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = aceitarTermos
        ) {
            Text("Cadastrar")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { navController.navigate("login") }) {
            Text(
                text = "Já possuo conta",
                color = MaterialTheme.colorScheme.primary,
                style = LocalTextStyle.current.copy(
                    textDecoration = TextDecoration.Underline
                )
            )
        }
    }

    // Diálogo de Termos de Uso com Scroll
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

fun hashPassword(senha: String): String {
    val bytes = MessageDigest.getInstance("SHA-256")
        .digest(senha.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}