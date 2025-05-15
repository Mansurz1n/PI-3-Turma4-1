    package br.edu.puc.superid

    import android.content.pm.PackageManager
    import android.os.Bundle
    import android.widget.Toast
    import androidx.activity.ComponentActivity
    import androidx.activity.compose.setContent
    import androidx.activity.enableEdgeToEdge
    import androidx.activity.result.ActivityResultLauncher
    import androidx.activity.result.contract.ActivityResultContracts
    import androidx.core.content.ContextCompat
    import androidx.compose.foundation.layout.*
    import androidx.compose.material3.*
    import androidx.compose.runtime.*
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.platform.LocalContext
    import androidx.compose.ui.unit.dp
    import androidx.navigation.NavType
    import androidx.navigation.compose.NavHost
    import androidx.navigation.compose.composable
    import androidx.navigation.compose.rememberNavController
    import androidx.navigation.navArgument
    import br.edu.puc.superid.ui.AddPasswordScreen
    import br.edu.puc.superid.ui.HomeScreen
    import br.edu.puc.superid.ui.IntroScreen
    import br.edu.puc.superid.ui.LoginScreen
    import br.edu.puc.superid.ui.RegisterScreen
    import br.edu.puc.superid.ui.hashPassword
    import br.edu.puc.superid.ui.theme.SuperIDTheme
    import com.google.firebase.auth.FirebaseAuth
    import com.google.firebase.firestore.ktx.firestore
    import com.google.firebase.ktx.Firebase

    class MainActivity : ComponentActivity() {

        private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)


            /*requestPermissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                val msg = if (isGranted) "Permissão concedida!" else "Permissão negada."
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }

            pedirPermissaoTelefone()
             */
            enableEdgeToEdge()
            setContent {
                SuperIDTheme {
                    val navController = rememberNavController()

                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = "intro",
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable("intro") {
                                IntroScreen(navController) // Agora a IntroScreen usa navController diretamente
                            }
                            composable("register") {
                                RegisterScreen(navController)
                            }
                            composable("login") {
                                LoginScreen(navController)
                            }
                            composable("home") {
                                HomeScreen(navController)
                            }
                            composable("addPassword") {
                                AddPasswordScreen(navController)
                            }

                        }
                    }
                }
            }
        }

        /*private fun pedirPermissaoTelefone() {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_PHONE_STATE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(android.Manifest.permission.READ_PHONE_STATE)
            }
        }

        private fun limparBancoFirestore() {
            val db = Firebase.firestore
            db.collection("users")
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        db.collection("users").document(document.id).delete()
                    }
                }
        }*/
    }


    @Composable
    fun CadastroSucessoScreen(nome: String, email: String, senha: String) {
        val context = LocalContext.current
        val db = Firebase.firestore
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        var senhaCriptografada by remember { mutableStateOf("") }
        //var imei by remember { mutableStateOf("") }

        LaunchedEffect(uid) {
            if (uid != null) {
                db.collection("usuarios").document(uid).get()
                    .addOnSuccessListener { document ->
                        senhaCriptografada = document.getString("senha") ?: "Senha não encontrada"
                        //imei = document.getString("imei") ?: "IMEI não encontrado"
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Erro ao carregar dados", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("✅ Cadastro concluído!", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                Text("UID: $uid")
                Text("Nome: $nome")
                Text("Email: $email")
                //Text("IMEI: $imei")
                Spacer(modifier = Modifier.height(16.dp))
                Text("Senha (original): $senha")
                Text("Senha (criptografada):")
                Text(senhaCriptografada, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
