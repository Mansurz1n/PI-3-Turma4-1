import express from "express";
import cors from "cors";
import {Router} from "express";
import {performAuth} from "./config/Account";



const port = 3000;
const server = express();
const routes = Router();

//firebase deploy --only functions





server.use(cors());
routes.post('/performAuth',performAuth);







server.use(routes);
server.listen(port,()=>{
    console.log(`Server rodando na porta:${port}`);
})