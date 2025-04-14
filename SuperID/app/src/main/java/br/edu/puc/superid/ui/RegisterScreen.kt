package br.edu.puc.superid.ui

import android.telephony.TelephonyManager
import android.widget.Toast
import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.security.MessageDigest

@Composable
fun RegisterScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 48.dp, start = 24.dp, end = 24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Cadastro no SuperID!",
            modifier = Modifier.padding(10.dp),
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 26.sp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nome:") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email:") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = senha,
            onValueChange = { senha = it },
            label = { Text("Senha:") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(email, senha)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = FirebaseAuth.getInstance().currentUser
                            user?.sendEmailVerification()?.addOnCompleteListener @androidx.annotation.RequiresPermission(
                                "android.permission.READ_PRIVILEGED_PHONE_STATE"
                            ) { verificationTask ->
                                if (verificationTask.isSuccessful) {
                                    val uid = user.uid
                                    val db = Firebase.firestore

                                    // Obter IMEI (com tratamento)
                                    /*val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                                    val imei = try {
                                        telephonyManager.imei ?: "IMEI_NAO_DISPONIVEL"
                                    } catch (e: SecurityException) {
                                        "PERMISSAO_NAO_CONCEDIDA"
                                    }
                                    */

                                    val hashedPassword = hashPassword(senha)

                                    val usuario = mapOf(
                                        "uid" to uid,
                                        "nome" to name,
                                        "email" to email,
                                        "senha" to hashedPassword,
                                        //"imei" to imei
                                    )

                                    db.collection("usuarios").document(uid).set(usuario)
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "Verifique seu e-mail!", Toast.LENGTH_LONG).show()
                                            navController.navigate("second/${name}/${email}/${senha}")
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(context, "Erro ao salvar dados: ${it.message}", Toast.LENGTH_LONG).show()
                                        }
                                } else {
                                    Toast.makeText(context, "Erro ao enviar e-mail de verificação", Toast.LENGTH_LONG).show()
                                }
                            }
                        } else {
                            Toast.makeText(context, "Erro ao criar usuário: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cadastrar")
        }
    }
}

fun hashPassword(senha: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(senha.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}


