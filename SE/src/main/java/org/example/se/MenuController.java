package org.example.se;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import java.io.IOException;

public class MenuController {

    @FXML
    private void abrirInscripciones() {
        abrirVentana("Inscripciones.fxml", "Módulo de Inscripciones");
    }

    @FXML
    private void abrirAsistencias() {
        abrirVentana("Asistencias.fxml", "Sistema de Asistencias");
    }

    @FXML
    private void abrirPersonas() {
        abrirVentana("Personas.fxml", "Módulo de Personas");
    }

    @FXML
    private void abrirMaterias() {
        abrirVentana("Materias.fxml", "Módulo de Materias");
    }

    private void abrirVentana(String fxmlFile, String titulo) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = fxmlLoader.load();

            Stage stage = new Stage();
            stage.setTitle(titulo);
            stage.setScene(new Scene(root, 1100, 600));
            stage.show();

        } catch (IOException e) {
            mostrarAlerta("Error", "No se pudo abrir el módulo: " + e.getMessage());
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}