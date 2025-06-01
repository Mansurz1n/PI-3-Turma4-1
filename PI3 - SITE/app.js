import { initializeApp } from "https://www.gstatic.com/firebasejs/10.7.1/firebase-app.js";;
import { 
  getFunctions, 
  httpsCallable 
} from "https://www.gstatic.com/firebasejs/10.7.1/firebase-functions.js";

const firebaseConfig = {
  apiKey: "AIzaSyCA4lfnTxq1c-wvhJIvgBjqyWWSNfzHF14",
  authDomain: "super-id-fb179.firebaseapp.com",
  projectId: "super-id-fb179",
  storageBucket: "super-id-fb179.firebasestorage.app",
  messagingSenderId: "774292125293",
  appId: "1:774292125293:web:9a873653b7af03814e3164",
  measurementId: "G-NZ1Y2T4LYK"
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
    const apikey = 'Teste do SuperID'
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

