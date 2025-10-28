package org.example.se;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;

public class Materia {

    private final IntegerProperty idMateria;
    private final StringProperty descripcion;
    private final IntegerProperty semestre;
    private final IntegerProperty creditos;
    private final StringProperty createdAt;
    private final StringProperty updatedAt;

    public Materia(int idMateria, String descripcion, int semestre, int creditos, String createdAt, String updatedAt) {
        this.idMateria = new SimpleIntegerProperty(idMateria);
        this.descripcion = new SimpleStringProperty(descripcion);
        this.semestre = new SimpleIntegerProperty(semestre);
        this.creditos = new SimpleIntegerProperty(creditos);
        this.createdAt = new SimpleStringProperty(createdAt);
        this.updatedAt = new SimpleStringProperty(updatedAt);
    }

    public int getIdMateria() {
        return idMateria.get();
    }

    public String getDescripcion() {
        return descripcion.get();
    }

    public int getSemestre() {
        return semestre.get();
    }

    public int getCreditos() {
        return creditos.get();
    }

    public String getCreatedAt() {
        return createdAt.get();
    }

    public String getUpdatedAt() {
        return updatedAt.get();
    }

    public IntegerProperty idMateriaProperty() {
        return idMateria;
    }

    public StringProperty descripcionProperty() {
        return descripcion;
    }

    public IntegerProperty semestreProperty() {
        return semestre;
    }

    public IntegerProperty creditosProperty() {
        return creditos;
    }

    public StringProperty createdAtProperty() {
        return createdAt;
    }

    public StringProperty updatedAtProperty() {
        return updatedAt;
    }
}
