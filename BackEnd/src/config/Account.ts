import {Request, response, Response} from "express";
import { initializeApp } from 'firebase/app';
import { getFirestore, collection, getDocs, query, where, doc } from 'firebase/firestore/lite';
import * as QRCode from 'qrcode';
import * as adm from 'firebase-admin';
import * as functions from 'firebase-functions' 
import { error } from "console";
import { v4 as uuidv4, v4 } from 'uuid';


const firebaseConfig = {
    apiKey: "AIzaSyCA4lfnTxq1c-wvhJIvgBjqyWWSNfzHF14",
    authDomain: "super-id-fb179.firebaseapp.com",
    projectId: "super-id-fb179",
    storageBucket: "super-id-fb179.firebasestorage.app",
    messagingSenderId: "774292125293",
    appId: "1:774292125293:web:9a873653b7af03814e3164",
    measurementId: "G-NZ1Y2T4LYK"
};

    

    adm.initializeApp(); 
    const app  = initializeApp(firebaseConfig);
    const db = getFirestore(app);

    
    
    export async function InfoAdd(email:string) {
        const colecao =  collection(db, 'usuarios');
        const q = query(colecao, where("email","==","gb.mansur@gmail.com"));
        //TODO: Trocar o meu email pela variavel
        const aaaa = await getDocs(colecao);//todos
        const aaaa2 = await getDocs(q)//especififo
        const result = aaaa.docs.map(doc => doc.data());//Lista todos
        const result2 = aaaa2.docs.map(doc => doc.data());//especififo
       if(result2.length == 0){
        console.log("Conta não criada")
       }
       else{
        console.log("Conta Criada:", result2)
       }
    };

    export async function Login(res:Response,req:Request){
        var email = req.get('email')
        var token = req.get('token')
        if(email && token){
            var result = InfoAdd(email)
            if(!result){
                res.send('Conta não criada')
            }else{
                res.send(result)
            }
    }
    }
    export async function performAuth(res:Response, req:Request) {
        const requestData = req.body
        const apiKey = 'API' 
        const validAPIKey =  functions.config().api.key//firebase functions:config:set api.key= "A chave q a gnt for fazer"
        if(requestData !== apiKey){
            res.status(401).send('APIKey invalida')
            return
        }
        try{
            const loginToken = "TokenAleatorio" //pode ser um uuidv4()
            const logintokenVdd = uuidv4()
            const dataAtual =  adm.firestore.Timestamp.now();
            await adm.firestore().collection('logins').doc(logintokenVdd).set({
                    APIKey: requestData.APIKey,
                    dataEHorario:dataAtual,
                    loginToken:logintokenVdd
                });

            const qrcode = await QRCode.toDataURL(logintokenVdd, {type:'image/png'})
            

            const responseData = {
                qrcode:qrcode,
                loginToken:logintokenVdd
            }

            res.status(200).json(responseData)



        }catch(err){
            console.log(error);
            response.status(500).send('Erro')
        }

    }




    

    