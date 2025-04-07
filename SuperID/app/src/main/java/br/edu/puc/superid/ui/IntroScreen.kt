    package br.edu.puc.superid.ui

    import android.content.Intent
    import android.net.Uri
    import androidx.compose.foundation.layout.Arrangement
    import androidx.compose.foundation.layout.Column
    import androidx.compose.foundation.layout.Row
    import androidx.compose.foundation.layout.Spacer
    import androidx.compose.foundation.layout.fillMaxSize
    import androidx.compose.foundation.layout.fillMaxWidth
    import androidx.compose.foundation.layout.height
    import androidx.compose.foundation.layout.padding
    import androidx.compose.foundation.layout.width
    import androidx.compose.foundation.text.ClickableText
    import androidx.compose.material3.Button
    import androidx.compose.material3.Checkbox
    import androidx.compose.material3.MaterialTheme
    import androidx.compose.material3.Text
    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.getValue
    import androidx.compose.runtime.mutableStateOf
    import androidx.compose.runtime.remember
    import androidx.compose.runtime.setValue
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.platform.LocalContext
    import androidx.compose.ui.text.SpanStyle
    import androidx.compose.ui.text.buildAnnotatedString
    import androidx.compose.ui.text.style.TextAlign
    import androidx.compose.ui.text.withStyle
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.unit.sp

    @Composable
    fun IntroScreen(onContinue: () -> Unit) {
        var acceptTerms by remember { mutableStateOf(false) }
        val context = LocalContext.current



        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp, start = 24.dp, end = 24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ){

            Text(
                text = "Bem-vindo ao SuperID!",
                modifier = Modifier.padding(10.dp),
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 26.sp)
            )
            Text(
                text = "O SuperID é um aplicativo que ajuda você a armazenar suas senhas de forma segura. Com ele, você pode cadastrar suas credenciais, acessar sites parceiros sem precisar digitar senha e gerenciar tudo em um único lugar. Marcando a caixa de texto você diz que leu e concordou com os termos de uso do app",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(250.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Checkbox(
                    checked = acceptTerms,
                    onCheckedChange = { acceptTerms = it }
                )
                val termosTexto = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color.White)) {
                        append("Li e aceito os ")
                    }
                    pushStringAnnotation(tag = "TERMS", annotation = "https://www.youtube.com/watch?v=dQw4w9WgXcQ")
                    withStyle(style = SpanStyle(color = Color(0xFF42A5F5))) {
                        append("termos de uso")
                    }
                    pop()
                }
                ClickableText(
                    text = termosTexto,
                    onClick = { offset ->
                        termosTexto.getStringAnnotations(tag = "TERMS", start = offset, end = offset)
                            .firstOrNull()?.let { annotation ->
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                                context.startActivity(intent)
                            }
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    if (acceptTerms) {
                        onContinue()
                    }
                },
                enabled = acceptTerms
            ) {
                Text("Continuar")
            }
        }
    }