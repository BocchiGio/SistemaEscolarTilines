package org.example.se;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.ObjectProperty;

import java.time.LocalDate;

public class Asistencia {
    private StringProperty persona;
    private StringProperty materia;
    private ObjectProperty<LocalDate> fecha;
    private StringProperty idPersona;
    private StringProperty idMateria;

    public Asistencia(String persona, String materia, LocalDate fecha, String idPersona, String idMateria) {
        this.persona = new SimpleStringProperty(persona);
        this.materia = new SimpleStringProperty(materia);
        this.fecha = new SimpleObjectProperty<>(fecha);
        this.idPersona = new SimpleStringProperty(idPersona);
        this.idMateria = new SimpleStringProperty(idMateria);
    }

    public Asistencia(String persona, String materia, LocalDate fecha) {
        this(persona, materia, fecha, "", "");
    }

    public String getPersona() {
        return persona.get();
    }

    public void setPersona(String persona) {
        this.persona.set(persona);
    }

    public StringProperty personaProperty() {
        return persona;
    }

    public String getMateria() {
        return materia.get();
    }

    public void setMateria(String materia) {
        this.materia.set(materia);
    }

    public StringProperty materiaProperty() {
        return materia;
    }

    public LocalDate getFecha() {
        return fecha.get();
    }

    public void setFecha(LocalDate fecha) {
        this.fecha.set(fecha);
    }

    public ObjectProperty<LocalDate> fechaProperty() {
        return fecha;
    }

    public String getIdPersona() {
        return idPersona.get();
    }

    public void setIdPersona(String idPersona) {
        this.idPersona.set(idPersona);
    }

    public StringProperty idPersonaProperty() {
        return idPersona;
    }

    public String getIdMateria() {
        return idMateria.get();
    }

    public void setIdMateria(String idMateria) {
        this.idMateria.set(idMateria);
    }

    public StringProperty idMateriaProperty() {
        return idMateria;
    }
}