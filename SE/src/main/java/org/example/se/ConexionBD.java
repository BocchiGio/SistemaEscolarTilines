package org.example.se;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {

    private static Session sshSession;
    private static Connection dbConnection;

    public static void conectar() {

        try{
            String hostname = "fi.jcaguilar.dev";
            String sshUser = "patito";
            String sshPass = "cuack";

            String dbUser = "becario";
            String dbPass = "FdI-its-5a";

            JSch jsch = new JSch();

            sshSession = jsch.getSession(sshUser, hostname);
            sshSession.setPassword(sshPass);
            sshSession.setConfig("StrictHostKeyChecking", "no");

            System.out.println("Conectando a la BD...");
            sshSession.connect();
            System.out.println("Conexión creada");

            int port = sshSession.setPortForwardingL(0, "localhost",3306);

            String conString = "jdbc:mariadb://localhost:" + port + "/its5a";
            System.out.println(conString);

            dbConnection = DriverManager.getConnection(conString, dbUser, dbPass);

        } catch (JSchException e) {
            System.err.println("Error al conectar al Túnel: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Error al conectar a la BD: " + e.getMessage());
        }
    }

    public static void desconectar() {
        try{
            if (dbConnection != null && !dbConnection.isClosed()) {
                dbConnection.close();
                System.err.println("Conexión BD cerrada");
            }
        } catch (SQLException e) {
            System.err.println("Error al salir de la DB: " + e.getMessage());
        }

        if (sshSession != null && sshSession.isConnected()) {
            sshSession.disconnect();
            System.out.println("Túnel desconectado");
        }
    }

    public static Connection getConnection() {
        return dbConnection;
    }
}
