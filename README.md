# PI-3-Turma4-1

# SuperID - Gerenciador de Autenticações Seguro :lock:

## :pushpin: Visão Geral
O SuperID é um ecossistema completo de gerenciamento de autenticações que oferece:
- Armazenamento seguro de credenciais
- Login sem senha via QR Code
- Criptografia avançada de dados
- Integração com sites parceiros

**Componentes principais:**
- :mobile_phone: Aplicativo Mobile (Android/Kotlin)
- :globe_with_meridians: Backend (Firebase Functions/Firestore)
- :link: API para integração com sites parceiros

## :sparkles: Funcionalidades

### :closed_lock_with_key: RF1 - Cadastro Inicial
- Tour explicativo sobre o SuperID
- Aceite de termos de uso
- Cadastro com Nome, Email e Senha Mestre
- Validação de email via Firebase Auth
- Armazenamento de UID e IMEI no Firestore

### :key2: RF2 - Gerenciamento de Senhas
- Cadastro/edição/exclusão de senhas
- Organização por categorias (Sites Web, Aplicativos, etc.)
- Geração de accessToken (256 chars Base64)
- Criptografia avançada das senhas
- Categorias padrão e personalizadas

### :calling: RF3 - Login Sem Senha
- Integração com sites parceiros
- Geração de QR Code para autenticação
- Fluxo completo de confirmação via app
- Funções `performAuth` e `getLoginStatus`
- Validação em tempo real

### :arrows_counterclockwise: RF4 - Recuperação de Senha
- Redefinição via email
- Requer validação prévia de email
- Fluxo seguro usando Firebase Auth
- Notificações de status de validação

## :tools: Tecnologias Utilizadas

### Mobile (Android)
- Kotlin
- CameraX (para leitura de QR Code)
- Firebase Authentication
- Firebase Firestore

### Backend
- Firebase Functions
- Firebase Firestore
- Firebase Storage
- API RESTful

### Segurança
- Criptografia AES-256 (ou algoritmo escolhido)
- Tokens JWT
- Validação de API Keys
- Base64 para codificação

## :busts_in_silhouette: Equipe
- **@Mansurz1n** 
- **@FillipeFar1a** 
- **@FlavioDario0** 
- **@JCChiozzini** 

## :rocket: Como Executar
Abrir o projeto Android no Android Studio
Inicie o Android Studio.

Na tela de boas-vindas, clique em “Open an existing project” (ou “Open”).

Navegue até a pasta raiz do repositório que você acabou de clonar.

Selecione a subpasta do módulo Android.

Exemplo: seu-repositorio/android-app/ ou simplesmente seu-repositorio/.

Clique em “OK” ou “Open” para carregar o projeto.

Aguarde o Gradle sincronizar automaticamente:

O Android Studio irá baixar todas as dependências necessárias e configurar o ambiente.

Se aparecer alguma notificação pedindo para atualizar o Gradle ou o plugin, clique em “Update” para ficar na versão recomendada.
Executar no Emulador ou Dispositivo
Conecte um dispositivo Android físico (ativando a depuração USB) ou
crie/emule um Android Virtual Device (AVD) no Android Studio:

Vá em “AVD Manager” (ícone de celular no canto superior direito)

Clique em “Create Virtual Device”, escolha um modelo e API desejada, e finalize.

Com o projeto aberto, selecione o Módulo “app” no menu de execução (geralmente aparece já selecionado).

Escolha o dispositivo/alvo (seu AVD ou dispositivo físico) e clique em Run ▶ (ícone de play).

Aguarde a instalação/aplicação ser iniciada no dispositivo:

Se tudo estiver configurado corretamente, o app será instalado e aberto automaticamente.
Abrir o site (arquivo HTML),
Após clonar, localize a pasta onde está o arquivo HTML do site (geralmente ./site ou ./web).

Dentro dessa pasta, você deverá encontrar um arquivo chamado index.html (ou algo semelhante).

Para visualizar no navegador localize index.html no explorador de arquivos do seu sistema operacional.

Dê um duplo-clique nele. Isso abrirá a página no navegador padrão.
### Pré-requisitos
- Android Studio (para desenvolvimento mobile)
- Node.js (para Firebase Functions)
- Conta Firebase com projetos configurados

### Configuração
1. Clone o repositório:
   ```bash
   git clone https://github.com/Mansurz1n/PI-3-Semestre.git
