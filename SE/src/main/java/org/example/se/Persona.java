package org.example.se;

import javafx.beans.property.*;

import java.time.LocalDate;

public class Persona {

    public static class Rol {
        private final int id;
        private final String descripcion;

        public Rol(int id, String descripcion) {
            this.id = id;
            this.descripcion = descripcion;
        }

        public int getId() {
            return id;
        }

        public String getDescripcion() {
            return descripcion;
        }

        @Override
        public String toString() {
            return descripcion;
        }
    }

    private final IntegerProperty id;
    private final StringProperty nombre;
    private final StringProperty apellido;
    private final StringProperty sexo;
    private final ObjectProperty<LocalDate> fecha;
    private final StringProperty rol;
    private StringProperty detalles;

    public Persona(int id, String nombre, String apellido, String sexo, LocalDate fecha, String rol) {
        this.id = new SimpleIntegerProperty(id);
        this.nombre = new SimpleStringProperty(nombre);
        this.apellido = new SimpleStringProperty(apellido);
        this.sexo = new SimpleStringProperty(sexo);
        this.fecha = new SimpleObjectProperty<>(fecha);
        this.rol = new SimpleStringProperty(rol);
        this.detalles = new SimpleStringProperty("");
    }

    public int getId() {
        return id.get();
    }

    public String getNombre() {
        return nombre.get();
    }

    public String getApellido() {
        return apellido.get();
    }

    public String getSexo() {
        return sexo.get();
    }

    public LocalDate getFecha() {
        return fecha.get();
    }

    public String getRol() {
        return rol.get();
    }

    public String getDetalles() {
        return detalles.get();
    }

    public void setDetalles(String detalles) {
        this.detalles.set(detalles);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public StringProperty nombreProperty() {
        return nombre;
    }

    public StringProperty apellidoProperty() {
        return apellido;
    }

    public StringProperty sexoProperty() {
        return sexo;
    }

    public ObjectProperty<LocalDate> fechaProperty() {
        return fecha;
    }

    public StringProperty rolProperty() {
        return rol;
    }

    public StringProperty detallesProperty() {
        return detalles;
    }
}

