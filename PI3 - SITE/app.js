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

//codigos do modal 
const button = document.querySelector("#authBtn")
const modal = document.querySelector("dialog")
const buttonClose = document.querySelector("#close-btn")

button.onclick = function() {
  modal.showModal()
  console.log(modal)
}

buttonClose.onclick = function() {
  stopStatusPolling()
    modal.close()
}

// Function to stop the polling
function stopStatusPolling() {
  if (interval) {
    clearInterval(interval);
    interval = null; // Reset the interval variable
    console.log("Polling stopped.");
  }
}

// Função para gerar QR Code
async function generateNewQRCode() {
  const fn = httpsCallable(functions, "performAuth");
  try {
    elemtId("AAA").style.display = "none"
    document.getElementById("hor").style.display = "none";
    document.getElementById("uid").style.display = "none";
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
let interval; 
function startStatusPolling(a) {
  const b = httpsCallable(functions, "getLoginStatus");
  
  interval = setInterval(async () => {
    try {
      const result = await b({ loginToken: a });
      
      if (result.data.status === "Completed") {
        clearInterval(interval);
        document.getElementById("qrcode").style.display = "none";
        elemtId("AAA").style.display = "block"
        document.getElementById("hor").style.display = "block";
        document.getElementById("uid").style.display = "block";
        elemtId("hor").textContent = "Data: " + new Date().toLocaleDateString()
        elemtId("AAA").textContent = "Hora: " + new Date().toLocaleTimeString();
        elemtId("uid").textContent = "User  ID: " + result.data.UserID; 
      
      }
      console.log(result);
    } catch (error) {
      clearInterval(interval);
      console.error("Erro na chamada StartStatus/getLoginStatus:", error);
      generateNewQRCode();
    }
  }, 5000); // 5 segundos
}

// Event Listener para o botão
//    elemtId("authBtn")
//    .addEventListener("click", performAuth);

    elemtId("authBtn").addEventListener("click", () =>
      {
        generateNewQRCode()
      });

document.getElementById("generate-btn")?.addEventListener("click", generateNewQRCode);

