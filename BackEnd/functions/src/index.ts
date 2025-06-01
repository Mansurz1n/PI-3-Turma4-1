import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import * as QRCode from "qrcode";
import {onCall, HttpsError} from "firebase-functions/v2/https";
import {v4 as uuidv4} from "uuid";


admin.initializeApp();


export const performAuth = onCall(async (request) => {
  try {
    // 1. Validação de dados
    if (!request.data.APIkey) {
      throw new functions.https.HttpsError(
        "failed-precondition",
        "Parâmetro APIkey obrigatório"
      );
    }

    if (request.data.APIkey !== "Teste do SuperID") {
      throw new functions.https.HttpsError(
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
      UserID: "",
    });

    // 5. Retorno formatado
    return {
      qrcode: qrcode,
      token: loginToken,
    };
  } catch (error) {
    // 6. Tratamento de erros
    console.error("Erro na função performAuth:", error);
    throw new functions.https.HttpsError(
      "internal",
      `Falha ao processar requisição ${error}`,
    );
  }
});


export const getLoginStatus = onCall(async (request) =>{
  if (!request.data.loginToken) {
    throw new HttpsError(
      "failed-precondition",
      "Parâmetro loginToken Obrigatorio"
    );
  }
  const loginToken = request.data.loginToken;
  const tokenDoc =
  await admin.firestore().collection("logins").doc(loginToken).get();

  if (!tokenDoc.exists) {
    throw new HttpsError(
      "permission-denied",
      "Login Token invalida"
    );
  }
  const data = tokenDoc.data();

  if (!data) {
    throw new HttpsError(
      "failed-precondition",
      "Login token com falta de data"
    );
  }

  const now = admin.firestore.Timestamp.now();

  const secs = now.seconds - data.DataEhorario.seconds;
  if (secs>60 || data.tentativas<=0) {
    await tokenDoc.ref.delete();
    throw new HttpsError(
      "aborted",
      "Acabou o tempo ou o numero de tentativas"
    );
  }

  if (data.UserId) {
    await tokenDoc.ref.update({
      status: "Completed",
    });
    const user =
    await admin.auth().getUser(data.UserId);
    return {
      uid: user.uid,
      email: user.email,
    };
  }
  return "";
});


