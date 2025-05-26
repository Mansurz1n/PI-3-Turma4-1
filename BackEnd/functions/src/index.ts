import * as admin from "firebase-admin";
import * as QRCode from "qrcode";
import {onCall, HttpsError} from "firebase-functions/v2/https";
import {v4 as uuidv4} from "uuid";


admin.initializeApp();

export const performAuth = onCall(async (request) => {
  try {
    // 1. Validação de dados
    if (!request.data.APIkey) {
      throw new HttpsError(
        "failed-precondition",
        "Parâmetro APIkey obrigatório"
      );
    }

    if (request.data.APIkey !== "API") {
      throw new HttpsError(
        "permission-denied",
        "API key inválida"
      );
    }
    // 2. Geração de dados
    const loginToken = uuidv4();
    const dataAtual = admin.firestore.Timestamp.now();

    // 3. Geração do QR Code (assíncrona)
    const qrcode = await QRCode.toDataURL(loginToken, {type: "image/png"});

    // 4. Salvamento no Firestore
    await admin.firestore().collection("logins").doc(loginToken).set({
      API: request.data.APIkey,
      DataEhorario: dataAtual,
      logintoken: loginToken,
      tentativas: 3,
      status: "pending",
    });

    // 5. Retorno formatado
    return {
      qrcode: qrcode,
      token: loginToken,
    };
  } catch (error) {
    // 6. Tratamento de erros
    console.error("Erro na função performAuth:", error);
    throw new HttpsError(
      "internal",
      ` Falha ao processar requisição ${error}`,
    );
  }
});


