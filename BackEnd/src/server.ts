import express from "express";
import cors from "cors";
import {Router} from "express";
import { InfoAdd, performAuth} from "./config/Account";
import { initializeApp } from 'firebase/app';
import { getFirestore, collection, getDocs } from 'firebase/firestore/lite';
import Firebase from "firebase";










const port = 3000;
const server = express();
const routes = Router();








server.use(cors());
routes.post('/performAuth',performAuth);







server.use(routes);
server.listen(port,()=>{
    console.log(`Server rodando na porta:${port}`);
})