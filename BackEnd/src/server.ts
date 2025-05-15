import express from "express";
import cors from "cors";
import {Router} from "express";
import { InfoAdd, performAuth} from "./config/Account";
import { initializeApp } from 'firebase/app';
import { getFirestore, collection, getDocs } from 'firebase/firestore/lite';




var firebaseui = require('firebaseui')
var firebase =  require('firebase')


var ui = new firebaseui.auth.AuthUI(firebase.auth())
const port = 3000;
const server = express();
const routes = Router();






ui.start('')

server.use(cors());
routes.get('/',performAuth);







server.use(routes);
server.listen(port,()=>{
    console.log(`Server rodando na porta:${port}`);
})