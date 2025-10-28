package org.example.se;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;

public class InscripcionController {

    @FXML
    private TableView<Inscripcion> tblInscripciones;
    @FXML
    private TableColumn<Inscripcion, Integer> colID;
    @FXML
    private TableColumn<Inscripcion, String> colAlumno;
    @FXML
    private TableColumn<Inscripcion, String> colSemestre;
    @FXML
    private TableColumn<Inscripcion, LocalDate> colFechaInsc;

    @FXML
    private ComboBox<String> comboSemestre;
    @FXML
    private TextField txtAlumnoBusqueda; // Ahora: nombre del alumno
    @FXML
    private DatePicker dpFechaInscripcion;

    @FXML
    private MenuButton menuAcciones;
    @FXML
    private MenuItem menuAgregar;
    @FXML
    private MenuItem menuEliminar;

    private ObservableList<Inscripcion> todosLosDatos;
    private FilteredList<Inscripcion> datosFiltrados;


    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void limpiarCampos() {
        this.txtAlumnoBusqueda.clear();
        this.comboSemestre.getSelectionModel().clearSelection();
        this.dpFechaInscripcion.setValue(LocalDate.now());
    }


    @FXML
    public void initialize() {
        // Enlazar columnas
        this.colID.setCellValueFactory(new PropertyValueFactory<Inscripcion, Integer>("idInscripcion"));
        this.colAlumno.setCellValueFactory(new PropertyValueFactory<Inscripcion, String>("nombreAlumno"));
        this.colSemestre.setCellValueFactory(new PropertyValueFactory<Inscripcion, String>("descripcionSemestre"));
        this.colFechaInsc.setCellValueFactory(new PropertyValueFactory<Inscripcion, LocalDate>("fechaInscripcion"));

        // Valor por defecto de fecha
        this.dpFechaInscripcion.setValue(LocalDate.now());

        // Aclarar en UI que se escribe el NOMBRE del alumno
        this.txtAlumnoBusqueda.setPromptText("Nombre del alumno");

        Connection conn = ConexionBD.getConnection();
        if (conn != null) {
            this.cargarDatos();
            this.cargarComboSemestres();
            // Filtro en vivo por nombre
            this.txtAlumnoBusqueda.textProperty().addListener((obs, oldV, newV) -> this.filtrarTabla(newV));
        } else {
            this.mostrarAlerta("Conexión", "No hay conexión con la BD.");
        }
    }


    public void cargarDatos() {
        Connection conn = ConexionBD.getConnection();
        if (conn == null) {
            this.mostrarAlerta("Error", "No se pudieron cargar datos, no hay conexión con la BD.");
            return;
        }

        this.todosLosDatos = FXCollections.observableArrayList();

        String query =
                "SELECT i.id_inscripcion, " +
                        "       p.nombre AS nombre_alumno, " +
                        "       m.descripcion AS descripcion_semestre, " +
                        "       i.created_at AS fecha_insc, " +
                        "       p.id_persona AS id_estudiante, " +
                        "       m.id_materia AS id_materia " +
                        "FROM inscripciones i " +
                        "JOIN personas_escuela p ON i.id_estudiante = p.id_persona " +
                        "JOIN materias m ON i.id_materia = m.id_materia " +
                        "ORDER BY i.created_at DESC, p.nombre";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int idInscripcion = rs.getInt("id_inscripcion");
                String nombreAlumno = rs.getString("nombre_alumno");
                String descripcionSemestre = rs.getString("descripcion_semestre");
                LocalDate fechaInscripcion = rs.getTimestamp("fecha_insc") != null
                        ? rs.getTimestamp("fecha_insc").toLocalDateTime().toLocalDate()
                        : null;
                String idEstudiante = String.valueOf(rs.getInt("id_estudiante"));
                String idMateria = String.valueOf(rs.getInt("id_materia"));

                this.todosLosDatos.add(new Inscripcion(
                        idInscripcion,
                        nombreAlumno,
                        descripcionSemestre,
                        fechaInscripcion,
                        idEstudiante,
                        idMateria
                ));
            }

        } catch (SQLException e) {
            this.mostrarAlerta("Error", "No se pudieron cargar los datos de inscripciones: " + e.getMessage());
        }

        this.datosFiltrados = new FilteredList<>(this.todosLosDatos);
        this.tblInscripciones.setItems(this.datosFiltrados);
        this.datosFiltrados.setPredicate((insc) -> true);
    }

    private void filtrarTabla(String filtro) {
        String filtroLower = trim(filtro).toLowerCase();
        this.datosFiltrados.setPredicate((inscripcion) -> {
            if (filtroLower.isEmpty()) return true;
            return inscripcion.getNombreAlumno() != null
                    && inscripcion.getNombreAlumno().toLowerCase().contains(filtroLower);
        });
    }

    private void cargarComboSemestres() {
        Connection conn = ConexionBD.getConnection();
        if (conn == null) {
            this.mostrarAlerta("Error", "No hay conexión con la BD.");
            return;
        }

        ObservableList<String> semestresList = FXCollections.observableArrayList();
        semestresList.add("");

        String query = "SELECT descripcion FROM materias WHERE descripcion IS NOT NULL ORDER BY descripcion";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                semestresList.add(rs.getString("descripcion"));
            }
        } catch (SQLException e) {
            this.mostrarAlerta("Error", "No se pudieron cargar los semestres: " + e.getMessage());
        }

        this.comboSemestre.setItems(semestresList);
    }


    /** Obtiene id_materia a partir de la descripción visible en el combo. */
    private String obtenerIdMateriaPorDescripcion(String descripcion) {
        Connection conn = ConexionBD.getConnection();
        if (conn == null) return null;

        String id = null;
        String query = "SELECT id_materia FROM materias WHERE descripcion = ? LIMIT 1";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, descripcion);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    id = rs.getString("id_materia");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }


    private Integer getOrCreateEstudianteIdPorNombre(String nombreAlumno) {
        Connection conn = ConexionBD.getConnection();
        if (conn == null) return null;

        // 1) Buscar por nombre exacto
        String qSelect = "SELECT id_persona FROM personas_escuela WHERE nombre = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(qSelect)) {
            ps.setString(1, nombreAlumno);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_persona");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }


        String qInsert = "INSERT INTO personas_escuela (nombre) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(qInsert, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombreAlumno);
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    @FXML
    private void registrarInscripcion() {
        Connection conn = ConexionBD.getConnection();
        if (conn == null) {
            this.mostrarAlerta("Conexión", "Sin conexión a la BD");
            return;
        }


        String nombreAlumno = trim(this.txtAlumnoBusqueda.getText());
        String descripcionSemestre = this.comboSemestre.getSelectionModel().getSelectedItem();
        LocalDate fecha = this.dpFechaInscripcion.getValue();

        if (nombreAlumno.isEmpty() || descripcionSemestre == null || descripcionSemestre.isEmpty() || fecha == null) {
            this.mostrarAlerta("Datos incompletos", "Todos los campos son obligatorios: Alumno, Materia/Grupo y Fecha.");
            return;
        }


        String idMateriaStr = this.obtenerIdMateriaPorDescripcion(descripcionSemestre);
        if (idMateriaStr == null) {
            this.mostrarAlerta("Error", "La Materia/Grupo '" + descripcionSemestre + "' no existe en la base de datos.");
            return;
        }
        int idMateria = Integer.parseInt(idMateriaStr);


        Integer idEstudiante = this.getOrCreateEstudianteIdPorNombre(nombreAlumno);
        if (idEstudiante == null) {
            this.mostrarAlerta("Error", "No se pudo obtener/crear el alumno '" + nombreAlumno + "'.");
            return;
        }


        String query = "INSERT INTO inscripciones (id_estudiante, id_materia, created_at) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, idEstudiante);
            stmt.setInt(2, idMateria);
            stmt.setDate(3, Date.valueOf(fecha));
            if (stmt.executeUpdate() > 0) {
                this.mostrarAlerta("Éxito", "✅ Inscripción registrada para '" + nombreAlumno + "' en '" + descripcionSemestre + "'.");
                this.cargarDatos();
                this.limpiarCampos();
            } else {
                this.mostrarAlerta("Aviso", "No se insertó el registro de inscripción.");
            }
        } catch (SQLException e) {
            this.mostrarAlerta("Error SQL", "No se pudo registrar la inscripción: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void darDeBaja() {
        Inscripcion seleccion = this.tblInscripciones.getSelectionModel().getSelectedItem();


        if (seleccion != null) {
            String idInscripcion = String.valueOf(seleccion.getIdInscripcion());
            String query = "DELETE FROM inscripciones WHERE id_inscripcion = ?";

            try (PreparedStatement stmt = ConexionBD.getConnection().prepareStatement(query)) {
                stmt.setInt(1, Integer.parseInt(idInscripcion));
                if (stmt.executeUpdate() > 0) {
                    this.mostrarAlerta("Éxito", "❌ Inscripción (ID " + idInscripcion + ") dada de baja correctamente.");
                    this.cargarDatos();
                } else {
                    this.mostrarAlerta("Aviso", "No se encontró la inscripción para dar de baja.");
                }
            } catch (SQLException e) {
                this.mostrarAlerta("Error SQL", "No se pudo dar de baja la inscripción: " + e.getMessage());
            }
            return;
        }


        String nombreAlumno = trim(this.txtAlumnoBusqueda.getText());
        String descripcionSemestre = this.comboSemestre.getSelectionModel().getSelectedItem();

        if (nombreAlumno.isEmpty() || descripcionSemestre == null || descripcionSemestre.isEmpty()) {
            this.mostrarAlerta("Advertencia", "Seleccione una fila o indique Alumno y Materia para dar de baja (se eliminará solo la coincidencia más reciente).");
            return;
        }

        Integer idEstudiante = this.getOrCreateEstudianteIdPorNombre(nombreAlumno);
        if (idEstudiante == null) {
            this.mostrarAlerta("Error", "No se encontró/creó el alumno '" + nombreAlumno + "'.");
            return;
        }

        String idMateriaStr = this.obtenerIdMateriaPorDescripcion(descripcionSemestre);
        if (idMateriaStr == null) {
            this.mostrarAlerta("Error", "La Materia/Grupo '" + descripcionSemestre + "' no existe en la base de datos.");
            return;
        }
        int idMateria = Integer.parseInt(idMateriaStr);


        String qDeleteOne =
                "DELETE FROM inscripciones " +
                        "WHERE id_inscripcion IN ( " +
                        "    SELECT id_inscripcion FROM ( " +
                        "        SELECT i.id_inscripcion " +
                        "        FROM inscripciones i " +
                        "        WHERE i.id_estudiante = ? AND i.id_materia = ? " +
                        "        ORDER BY i.created_at DESC " +
                        "        LIMIT 1 " +
                        "    ) t " +
                        ")";

        try (PreparedStatement ps = ConexionBD.getConnection().prepareStatement(qDeleteOne)) {
            ps.setInt(1, idEstudiante);
            ps.setInt(2, idMateria);
            int affected = ps.executeUpdate();
            if (affected > 0) {
                this.mostrarAlerta("Éxito", "❌ Se dio de baja la inscripción más reciente de '" + nombreAlumno + "' en '" + descripcionSemestre + "'.");
                this.cargarDatos();
            } else {
                this.mostrarAlerta("Aviso", "No se encontró una inscripción para '" + nombreAlumno + "' en '" + descripcionSemestre + "'.");
            }
        } catch (SQLException e) {
            this.mostrarAlerta("Error SQL", "No se pudo dar de baja la inscripción: " + e.getMessage());
        }
    }
}
