import { initializeApp } from "https://www.gstatic.com/firebasejs/10.7.1/firebase-app.js";;
import { 
  getFunctions, 
  httpsCallable 
} from "https://www.gstatic.com/firebasejs/10.7.1/firebase-functions.js";

const firebaseConfig = {
  apiKey: "AIzaSyAU9K5TQGHKfkGLBHmivR7KeA6qXKMBoF0",
  authDomain: "qrcodeaula-ead85.firebaseapp.com",
  projectId: "qrcodeaula-ead85",
  storageBucket: "qrcodeaula-ead85.firebasestorage.app",
  messagingSenderId: "314095348103",
  appId: "1:314095348103:web:ce4fa3be4ce8f5dcde4b22",
  measurementId: "G-MDV728DNV3"
};



function elemtId(s){ 
  return document.getElementById(s)
}
const app = initializeApp(firebaseConfig);
const functions = getFunctions(app);



async function performAuth() {
  const fn = httpsCallable(functions, "performAuth");
    try {

        const apikey = 'API'
        const result = await fn({ APIkey: apikey });

        const { qrcode, token } = result.data;
        elemtId("qrImg").src = qrcode;
        if(elemtId("qrcode").style.display !== "block"){
          elemtId("qrcode").style.display = "block"
        }
        console.log("Token gerado:", token);
      } catch (err) {
        console.error("Erro na chamada performAuth:", err);
        alert("Falha ao gerar QR: " + err.message);
      }
}

    // 5) Vincula ao clique do botão







// Função para gerar QR Code
async function generateNewQRCode() {
  const fn = httpsCallable(functions, "performAuth");
  try {
    const apikey = 'API'
    const result = await fn({ APIkey: apikey });

    const { qrcode, token } = result.data;
    elemtId("qrImg").src = qrcode;
    if(elemtId("qrcode").style.display !== "block"){
      elemtId("qrcode").style.display = "block"
    }
    console.log("Token gerado:", token);

    // Iniciar verificação de status
    startStatusPolling(token);

  } catch (err) {
        console.error("Erro na chamada performAuth:", err);
        alert("Falha ao gerar QR: " + err.message);
    }
}

// Função de polling de status
function startStatusPolling(a) {
  const b = httpsCallable(functions,"getLoginStatus");
  const interval = setInterval(async () => {
    try {
      const result = await b({ loginToken:a });
      
      if (result.data.status === "completed") {
        clearInterval(interval);
        elemtId("qrcode").style.display = "none"
        alert(`Usuário autenticado: ${result.data.user?.email}`);
      }
      console.log(result)
    } catch (error) {
      clearInterval(interval);
      console.error("Erro na chamada StartStatus/getLoginStatus:", error);
      generateNewQRCode();
    }
  }, 5000);//5 segundos
}

// Event Listener para o botão
//    elemtId("authBtn")
//    .addEventListener("click", performAuth);

    elemtId("authBtn").addEventListener("click", () =>
      {
        generateNewQRCode()
      });

document.getElementById("generate-btn")?.addEventListener("click", generateNewQRCode);