import express from "express";
import cors from "cors";
import {Router} from "express";
import { Login } from "./config/Account";



const port = 3000;
const server = express();
const routes = Router();



server.use(cors());
routes.get('/',Login.InfoAdd)







server.use(routes);
server.listen(port,()=>{
    console.log(`Server rodando na porta:${port}`);
})