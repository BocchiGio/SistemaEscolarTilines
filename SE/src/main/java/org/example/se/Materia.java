package org.example.se;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;

public class Materia {

    private final IntegerProperty idMateria;
    private final StringProperty descripcion;
    private final IntegerProperty semestre;
    private final IntegerProperty creditos;
    private final StringProperty detalles;

    public Materia(int idMateria, String descripcion, int semestre, int creditos) {
        this.idMateria = new SimpleIntegerProperty(idMateria);
        this.descripcion = new SimpleStringProperty(descripcion);
        this.semestre = new SimpleIntegerProperty(semestre);
        this.creditos = new SimpleIntegerProperty(creditos);
        this.detalles = new SimpleStringProperty("");
    }

    public int getIdMateria() { return idMateria.get(); }
    public String getDescripcion() { return descripcion.get(); }
    public int getSemestre() { return semestre.get(); }
    public int getCreditos() { return creditos.get(); }
    public String getDetalles() { return detalles.get(); }

    public void setDetalles(String value) { this.detalles.set(value); }

    public IntegerProperty idMateriaProperty() { return idMateria; }
    public StringProperty descripcionProperty() { return descripcion; }
    public IntegerProperty semestreProperty() { return semestre; }
    public IntegerProperty creditosProperty() { return creditos; }
    public StringProperty detallesProperty() { return detalles; }
}