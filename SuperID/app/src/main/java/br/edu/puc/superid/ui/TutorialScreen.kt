// File: app/src/main/java/br/edu/puc/superid/ui/TutorialScreen.kt
package br.edu.puc.superid.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import br.edu.puc.superid.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorialScreen(navController: NavController) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                // Removemos navigationIcon e texto; centralizamos apenas a logo
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Logo SuperID",
                            modifier = Modifier.size(40.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Bem-vindo ao SuperID",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Divider()

            tutorialStep(
                title = "1️⃣ O que é o SuperID",
                description = "O SuperID é um gerenciador de autenticações que unifica o armazenamento seguro de senhas pessoais " +
                        "e oferece login sem senha junto a sites parceiros. Nesta aplicação, você criará uma conta, manterá " +
                        "suas credenciais criptografadas e poderá autorizar acessos externos por QR Code."
            )

            tutorialStep(
                title = "2️⃣ Cadastro de Conta Inicial",
                description = "Ao abrir o aplicativo pela primeira vez, será exibida uma breve explicação sobre o SuperID. " +
                        "Confirme a aceitação dos Termos de Uso para prosseguir com o cadastro. Forneça Nome, E-mail " +
                        "e uma Senha Mestre. Após preencher, um e-mail de validação será enviado pelo Firebase Authentication. " +
                        "Valide o seu e-mail para que todos os recursos de login sem senha funcionem corretamente."
            )

            tutorialStep(
                title = "3️⃣ Armazenamento no Firestore",
                description = "Após criar a conta, o aplicativo salva no Firebase Firestore um documento com o seu UID " +
                        "e o identificador do dispositivo (IMEI). Isso permite associar seu perfil ao aparelho utilizado."
            )

            tutorialStep(
                title = "4️⃣ Gerenciamento de Senhas Pessoais",
                description = "No menu principal, acesse 'Gerenciar Senhas'. Cada senha é organizada em categorias " +
                        "(por exemplo: Sites Web, Aplicativos, Teclados Físicos). Para cadastrar uma nova senha, informe " +
                        "o nome do serviço, a credencial e, opcionalmente, uma descrição. O SuperID gera um token " +
                        "Base64 de 256 caracteres e criptografa o dado antes de armazenar no Firestore. Use as opções " +
                        "disponíveis para editar ou remover entradas existentes."
            )

            tutorialStep(
                title = "5️⃣ Login Sem Senha com Parceiros",
                description = "Para acessar um site parceiro sem digitar sua Senha Mestre, selecione 'Login Sem Senha'. " +
                        "Digite sua Senha Mestre para desbloquear a câmera e escanear o QR Code gerado pelo parceiro. " +
                        "O aplicativo atualizará no Firestore o documento de login com seu UID e horário de acesso, " +
                        "autorizando o site a completar o processo de autenticação."
            )

            tutorialStep(
                title = "6️⃣ Recuperar Senha Mestre",
                description = "Se você esquecer sua Senha Mestre, utilize a opção 'Esqueci Minha Senha'. " +
                        "Informe o e-mail cadastrado e, desde que já tenha validado seu e-mail, um link de " +
                        "redefinição será enviado. Siga o passo a passo no e-mail para criar uma nova senha."
            )

            tutorialStep(
                title = "7️⃣ Boas Práticas de Segurança",
                description = "Mantenha sempre sua conta e dispositivo protegidos: use uma Senha Mestre forte (mínimo 6 caracteres), " +
                        "não compartilhe seu aparelho nem o token Base64 gerado para cada senha. Caso identifique acesso não autorizado, " +
                        "altere sua Senha Mestre imediatamente e revogue sessões em dispositivos extras, se necessário."
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Entendi, leve-me ao app")
            }
        }
    }
}

@Composable
private fun ColumnScope.tutorialStep(title: String, description: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground
    )
    Text(
        description,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = 16.sp
    )
    Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f))
}
