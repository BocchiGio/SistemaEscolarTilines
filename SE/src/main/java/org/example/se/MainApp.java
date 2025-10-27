package org.example.se;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        ConexionBD.conectar();

        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("Menu.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        stage.setTitle("Sistema Escolar ITS");
        stage.setScene(scene);

        // Cerrar toda la aplicación cuando se cierre la ventana principal
        stage.setOnCloseRequest(event -> {
            System.out.println("Desconectando");
            ConexionBD.desconectar();
            Platform.exit();
        });

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}