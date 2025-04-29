import express from "express";
import cors from "cors";
import {Router} from "express";
import { InfoAdd} from "./config/Account";
import { initializeApp } from 'firebase/app';
import { getFirestore, collection, getDocs } from 'firebase/firestore/lite';





const port = 3000;
const server = express();
const routes = Router();



server.use(cors());
routes.get('/',InfoAdd);







server.use(routes);
server.listen(port,()=>{
    console.log(`Server rodando na porta:${port}`);
})