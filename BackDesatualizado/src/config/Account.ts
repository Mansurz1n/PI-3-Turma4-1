import {Request, response, Response} from "express";
import * as functions from 'firebase-functions' 
import * as admin from 'firebase-admin';
import * as QRCode from 'qrcode';
import { error } from "console";
import { v4 as uuidv4} from 'uuid';
const { initializeApp } = require('firebase-admin/app');
const { getFirestore, collection, where, query, getDocs } = require('firebase-admin/firestore');


const firebaseConfig = {
    apiKey: "AIzaSyCA4lfnTxq1c-wvhJIvgBjqyWWSNfzHF14",
    authDomain: "super-id-fb179.firebaseapp.com",
    projectId: "super-id-fb179",
    storageBucket: "super-id-fb179.firebasestorage.app",
    messagingSenderId: "774292125293",
    appId: "1:774292125293:web:9a873653b7af03814e3164",
    measurementId: "G-NZ1Y2T4LYK"
};

    

    
    const app = initializeApp(firebaseConfig);
    const db = getFirestore(app);
    

    
    
    /*export async function InfoAdd(email:string) {
        const colecao =  collection(db, 'usuarios');
        const q = query(colecao, where("email","==",email));
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
    };*/

    export async function Login(res:Response,req:Request){
        var email = req.get('email')
        var token = req.get('token')
        if(email && token){
            var result =email
            if(!result){
                res.send('Conta não criada')
            }else{
                res.send(result)
            }
    }
    }
    export async function performAuth(req:Request, res:Response) {
        const requestData = req.headers['apikey'] 
        const apiKey = 'API' 
        console.dir(requestData)
        console.dir(admin.initializeApp)
        //const validAPIKey =  functions.config().api.key//firebase functions:config:set api.key= "A chave q a gnt for fazer"
        if(requestData !== apiKey){
            
            res.status(404).send('APIKey invalida')
            return
        }
        try{
            console.log('1. Gerando token...');
            const logintokenVdd = uuidv4();

            console.log('2. Gerando QRCode...');
            const qrcode = await QRCode.toDataURL(logintokenVdd);

            console.log('3. Gerando timestamp...');
            const dataAtual = admin.firestore.Timestamp.now().toDate.toString;

            console.log('4. Gravando no Firestore...');
            await admin.firestore().collection('logins').doc(logintokenVdd).set({
                    APIKey: requestData,
                    loginToken:logintokenVdd,
                    dataEHorario:dataAtual,
                    tentativas:3
                });
            console.dir('Data base OK')
            
            

            const responseData = {
                qrcode:qrcode,
                loginToken:logintokenVdd
            }

            res.status(200).json(responseData)
            return


        }catch(err){
            console.dir(error);
            response.status(500).json({error : 'Erro '})
        }

    }
      


        export async function getLoginStatus(res:Response, req:Request) {
            const { loginToken } = req.body


            if (!loginToken){
                res.status(400).json({error : 'Token faltando'})
                return
            }
            try{
                const tokenDoc = await admin.firestore().collection('loginTokens').doc(loginToken).get();


                if(!tokenDoc.exists){
                    res.status(404).json({error:'Token Não encontrado'})

                }

                const data = tokenDoc.data()!

                const now  = admin.firestore.Timestamp.now();
                
                
                const secs = now.seconds - data.dataEHorario.seconds 


                if(secs > 60 || data.tentativas<=0){
                    await  tokenDoc.ref.delete()
                    res.status(410).json({error: 'Token expirado.'})
                    performAuth
                }
                


                await tokenDoc.ref.update({tentativas:admin.firestore.FieldValue.increment(-1)})


                if (data.userId){
                    const user = await admin.auth().getUser(data.userId)
                    res.status(200).json({
                        user: {
                            uid:user.uid,
                            email:user.email
                        }
                    })
                    return
                }
                res.status(200).json({
                    tentativas:data.tentativas - 1
                })



            }catch(err){
                console.dir('Erro' + err)
                res.status(500).json({error : 'Erro '})
            }




            
        }


    

    