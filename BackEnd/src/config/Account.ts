import {Request, Response} from "express";
import { initializeApp } from 'firebase/app';
import { getFirestore, collection, getDocs, query, where, doc } from 'firebase/firestore/lite';



const firebaseConfig = {
    apiKey: "AIzaSyCA4lfnTxq1c-wvhJIvgBjqyWWSNfzHF14",
    authDomain: "super-id-fb179.firebaseapp.com",
    projectId: "super-id-fb179",
    storageBucket: "super-id-fb179.firebasestorage.app",
    messagingSenderId: "774292125293",
    appId: "1:774292125293:web:9a873653b7af03814e3164",
    measurementId: "G-NZ1Y2T4LYK"
};

    


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
    return 
    }

    

    