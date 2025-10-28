package org.example.se;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.ObjectProperty;

import java.time.LocalDate;

public class Inscripcion {

    private final IntegerProperty idInscripcion;
    private final StringProperty nombreAlumno;
    private final StringProperty descripcionSemestre;
    private final ObjectProperty<LocalDate> fechaInscripcion;
    private final StringProperty idEstudiante;
    private final StringProperty idMateria;

    public Inscripcion(int idInscripcion, String nombreAlumno, String descripcionSemestre, LocalDate fechaInscripcion, String idEstudiante, String idMateria) {
        this.idInscripcion = new SimpleIntegerProperty(idInscripcion);
        this.nombreAlumno = new SimpleStringProperty(nombreAlumno);
        this.descripcionSemestre = new SimpleStringProperty(descripcionSemestre);
        this.fechaInscripcion = new SimpleObjectProperty<>(fechaInscripcion);
        this.idEstudiante = new SimpleStringProperty(idEstudiante);
        this.idMateria = new SimpleStringProperty(idMateria);
    }

    public int getIdInscripcion() {
        return idInscripcion.get();
    }

    public String getNombreAlumno() {
        return nombreAlumno.get();
    }

    public String getDescripcionSemestre() {
        return descripcionSemestre.get();
    }

    public LocalDate getFechaInscripcion() {
        return fechaInscripcion.get();
    }

    public IntegerProperty idInscripcionProperty() {
        return idInscripcion;
    }

    public StringProperty nombreAlumnoProperty() {
        return nombreAlumno;
    }

    public StringProperty descripcionSemestreProperty() {
        return descripcionSemestre;
    }

    public ObjectProperty<LocalDate> fechaInscripcionProperty() {
        return fechaInscripcion;
    }

    public String getIdEstudiante() {
        return idEstudiante.get();
    }

    public String getIdMateria() {
        return idMateria.get();
    }
}