package org.example;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.sql.*;

public class Main {

    public static void main(String[] args) throws JSchException, SQLException {
        String hostname = "fi.jcaguilar.dev";
        String sshUser = "patito";
        String sshPass = "cuack";

        String dbUser = "becario";
        String dbPass = "FdI-its-5a";

        //Túnel
        JSch jsch = new JSch();

        //ssh patito@fi.jcaguilar.dev (lo mismo que terminal)
        Session sesion = jsch.getSession(sshUser, hostname);

        //la contraseña
        sesion.setPassword(sshPass);

        //Deshabilitar los mensajes de error
        sesion.setConfig("StrictHostKeyChecking", "no");

        //Obtenemos un puerto redireccionado
        sesion.connect();
        int port = sesion.setPortForwardingL(0,"localhost",3306);

        String conString = "jdbc:mariadb://localhost:" + port + "/its5a";
        System.out.println(conString);


    }
}


