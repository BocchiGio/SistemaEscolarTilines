package org.example.se;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.DatePicker;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.cell.PropertyValueFactory;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.sql.*;
import java.time.LocalDate;

public class AsistenciaController {

    @FXML
    private TableView<Asistencia> tblAsistencia;

    @FXML
    private TableColumn<Asistencia, String> colPersona;
    @FXML
    private TableColumn<Asistencia, String> colMateria;
    @FXML
    private TableColumn<Asistencia, LocalDate> colFecha;

    @FXML
    private ComboBox<String> comboPersona;
    @FXML
    private ComboBox<String> comboMateria;
    @FXML
    private DatePicker dpFecha;

    @FXML
    private MenuButton menuAcciones;
    @FXML
    private MenuItem menuAgregar;
    @FXML
    private MenuItem menuEliminar;

    private Connection conn;
    private Session sshSession;
    private ObservableList<Asistencia> todosLosDatos;
    private FilteredList<Asistencia> datosFiltrados;

    public AsistenciaController() {
        establecerConexionSSH();
    }

    private void establecerConexionSSH() {
        try {
            String hostname = "fi.jcaguilar.dev";
            String sshUser = "patito";
            String sshPass = "cuack";
            String dbUser = "becario";
            String dbPass = "FdI-its-5a";

            JSch jsch = new JSch();
            sshSession = jsch.getSession(sshUser, hostname);
            sshSession.setPassword(sshPass);
            sshSession.setConfig("StrictHostKeyChecking", "no");

            sshSession.connect();

            int port = sshSession.setPortForwardingL(0, "localhost", 3306);
            String conString = "jdbc:mariadb://localhost:" + port + "/its5a";

            conn = DriverManager.getConnection(conString, dbUser, dbPass);

            if (conn != null && !conn.isClosed()) {
                System.out.println("Conexión exitosa a la base de datos");
            }

        } catch (JSchException | SQLException e) {
            mostrarAlerta("Error de Conexión", "No se pudo conectar: " + e.getMessage());
        }
    }

    @FXML
    public void initialize() {
        colPersona.setCellValueFactory(new PropertyValueFactory<>("persona"));
        colMateria.setCellValueFactory(new PropertyValueFactory<>("materia"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));

        // Fecha por defecto: hoy
        dpFecha.setValue(LocalDate.now());

        if (conn != null) {
            cargarDatos();
            cargarComboMaterias();

            // Cuando se selecciona una materia, cargar las personas y filtrar tabla
            comboMateria.setOnAction(event -> {
                cargarPersonasPorMateria();
                filtrarTablaPorMateria();
            });
        }
    }

    public void cargarDatos() {
        todosLosDatos = FXCollections.observableArrayList();

        String query = "SELECT " +
                "COALESCE(p.nombre, 'NULL') AS persona, " +
                "COALESCE(m.descripcion, 'NULL') AS materia, " +
                "a.fecha AS fecha, " +
                "p.id_persona as id_persona, " +
                "m.id_materia as id_materia " +
                "FROM asistencias a " +
                "JOIN inscripciones i ON a.id_inscripcion = i.id_inscripcion " +
                "JOIN personas_escuela p ON i.id_estudiante = p.id_persona " +
                "JOIN materias m ON i.id_materia = m.id_materia " +
                "ORDER BY a.fecha DESC, p.nombre";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String persona = rs.getString("persona");
                String materia = rs.getString("materia");
                LocalDate fecha = rs.getDate("fecha") != null ? rs.getDate("fecha").toLocalDate() : null;
                String idPersona = rs.getString("id_persona");
                String idMateria = rs.getString("id_materia");

                todosLosDatos.add(new Asistencia(persona, materia, fecha, idPersona, idMateria));
            }

        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar los datos: " + e.getMessage());
        }

        // Crear lista filtrada
        datosFiltrados = new FilteredList<>(todosLosDatos);
        tblAsistencia.setItems(datosFiltrados);

        // Mostrar todos los datos al inicio
        datosFiltrados.setPredicate(asistencia -> true);
    }

    private void filtrarTablaPorMateria() {
        String materiaSeleccionada = comboMateria.getSelectionModel().getSelectedItem();

        if (materiaSeleccionada == null || materiaSeleccionada.isEmpty()) {
            // Mostrar todos los datos si no hay materia seleccionada
            datosFiltrados.setPredicate(asistencia -> true);
        } else {
            // Filtrar por la materia seleccionada
            datosFiltrados.setPredicate(asistencia ->
                    asistencia.getMateria().equalsIgnoreCase(materiaSeleccionada)
            );
        }
    }

    private void cargarComboMaterias() {
        ObservableList<String> materiasList = FXCollections.observableArrayList();

        // Agregar opción vacía para mostrar todos
        materiasList.add("");

        String query = "SELECT descripcion FROM materias WHERE descripcion IS NOT NULL ORDER BY descripcion";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                materiasList.add(rs.getString("descripcion"));
            }

        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar las materias: " + e.getMessage());
        }

        comboMateria.setItems(materiasList);
    }

    private void cargarPersonasPorMateria() {
        String materiaSeleccionada = comboMateria.getSelectionModel().getSelectedItem();

        if (materiaSeleccionada == null || materiaSeleccionada.isEmpty()) {
            comboPersona.getItems().clear();
            return;
        }

        ObservableList<String> personasList = FXCollections.observableArrayList();

        // Cargar solo las personas inscritas en la materia seleccionada
        String query = "SELECT DISTINCT p.nombre, p.id_persona " +
                "FROM personas_escuela p " +
                "JOIN inscripciones i ON p.id_persona = i.id_estudiante " +
                "JOIN materias m ON i.id_materia = m.id_materia " +
                "WHERE m.descripcion = ? " +
                "ORDER BY p.nombre";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, materiaSeleccionada);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String nombre = rs.getString("nombre");
                String idPersona = rs.getString("id_persona");
                System.out.println("Cargando persona: " + nombre + " (ID: " + idPersona + ") para materia: " + materiaSeleccionada);
                personasList.add(nombre);
            }

            System.out.println("Total de personas cargadas: " + personasList.size() + " para materia: " + materiaSeleccionada);

        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar las personas para esta materia: " + e.getMessage());
            e.printStackTrace();
        }

        comboPersona.setItems(personasList);
        comboPersona.getSelectionModel().clearSelection(); // Limpiar selección anterior
    }

    @FXML
    private void agregarAsistencia() {
        String nombrePersona = comboPersona.getSelectionModel().getSelectedItem();
        String nombreMateria = comboMateria.getSelectionModel().getSelectedItem();
        LocalDate fecha = dpFecha.getValue();

        if (nombrePersona == null || nombreMateria == null || nombreMateria.isEmpty() || fecha == null) {
            mostrarAlerta("Advertencia", "Seleccione todos los campos:\n• Materia\n• Alumno\n• Fecha");
            return;
        }

        // Obtener ID de la persona seleccionada
        String idPersona = obtenerIdPersonaPorNombre(nombrePersona);
        if (idPersona == null) {
            mostrarAlerta("Error", "No se encontró la persona seleccionada: " + nombrePersona);
            return;
        }

        // Obtener ID de la materia seleccionada
        String idMateria = obtenerIdMateriaPorDescripcion(nombreMateria);
        if (idMateria == null) {
            mostrarAlerta("Error", "No se encontró la materia seleccionada: " + nombreMateria);
            return;
        }

        System.out.println("Buscando inscripción para Persona ID: " + idPersona + ", Materia ID: " + idMateria);

        // Obtener id_inscripcion
        String idInscripcion = obtenerIdInscripcion(idPersona, idMateria);
        if (idInscripcion == null) {
            mostrarAlerta("Error", "No existe inscripción para " + nombrePersona + " en " + nombreMateria +
                    ". Verifique que el alumno esté correctamente inscrito en esta materia.");
            return;
        }

        System.out.println("Inscripción encontrada: " + idInscripcion);

        // Verificar si ya existe el registro de asistencia
        if (existeRegistro(idInscripcion, fecha)) {
            mostrarAlerta("Advertencia", "Ya existe asistencia para " + nombrePersona + " en " + nombreMateria + " para la fecha " + fecha);
            return;
        }

        String query = "INSERT INTO asistencias (id_inscripcion, fecha) VALUES (?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, Integer.parseInt(idInscripcion));
            stmt.setDate(2, Date.valueOf(fecha));
            stmt.executeUpdate();

            mostrarAlerta("Éxito", "✅ Asistencia registrada correctamente para:\n" +
                    "Alumno: " + nombrePersona + "\n" +
                    "Materia: " + nombreMateria + "\n" +
                    "Fecha: " + fecha);

            // Recargar datos y mostrar todos (sin filtro)
            cargarDatos();
            comboMateria.getSelectionModel().clearSelection();
            limpiarCampos();

        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudo registrar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void eliminarAsistencia() {
        String nombrePersona = comboPersona.getSelectionModel().getSelectedItem();
        String nombreMateria = comboMateria.getSelectionModel().getSelectedItem();
        LocalDate fecha = dpFecha.getValue();

        if (nombrePersona == null || nombreMateria == null || nombreMateria.isEmpty() || fecha == null) {
            mostrarAlerta("Advertencia", "Seleccione todos los campos:\n• Materia\n• Alumno\n• Fecha");
            return;
        }

        // Obtener ID de la persona
        String idPersona = obtenerIdPersonaPorNombre(nombrePersona);
        if (idPersona == null) {
            mostrarAlerta("Error", "No se encontró la persona seleccionada.");
            return;
        }

        // Obtener ID de la materia
        String idMateria = obtenerIdMateriaPorDescripcion(nombreMateria);
        if (idMateria == null) {
            mostrarAlerta("Error", "No se encontró la materia seleccionada.");
            return;
        }

        // Obtener id_inscripcion
        String idInscripcion = obtenerIdInscripcion(idPersona, idMateria);
        if (idInscripcion == null) {
            mostrarAlerta("Error", "No hay inscripción para " + nombrePersona + " en " + nombreMateria);
            return;
        }

        // Verificar si existe el registro antes de eliminar
        if (!existeRegistro(idInscripcion, fecha)) {
            mostrarAlerta("Advertencia", "No existe asistencia para " + nombrePersona + " en " + nombreMateria + " para la fecha " + fecha);
            return;
        }

        String query = "DELETE FROM asistencias WHERE id_inscripcion = ? AND fecha = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, Integer.parseInt(idInscripcion));
            stmt.setDate(2, Date.valueOf(fecha));

            if (stmt.executeUpdate() > 0) {
                mostrarAlerta("Éxito", "❌ Asistencia eliminada correctamente para:\n" +
                        "Alumno: " + nombrePersona + "\n" +
                        "Materia: " + nombreMateria + "\n" +
                        "Fecha: " + fecha);

                // Recargar datos y mostrar todos (sin filtro)
                cargarDatos();
                comboMateria.getSelectionModel().clearSelection();
                limpiarCampos();
            } else {
                mostrarAlerta("Error", "No se pudo eliminar la asistencia.");
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudo eliminar: " + e.getMessage());
        }
    }

    // Los métodos auxiliares (obtenerIdPersonaPorNombre, obtenerIdMateriaPorDescripcion, etc.)
    // se mantienen igual que en la versión anterior...

    private String obtenerIdPersonaPorNombre(String nombre) {
        String query = "SELECT id_persona FROM personas_escuela WHERE nombre = ? LIMIT 1";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, nombre);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String id = rs.getString("id_persona");
                System.out.println("Persona encontrada: " + nombre + " -> ID: " + id);
                return id;
            } else {
                System.out.println("Persona NO encontrada: " + nombre);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String obtenerIdMateriaPorDescripcion(String descripcion) {
        String query = "SELECT id_materia FROM materias WHERE descripcion = ? LIMIT 1";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, descripcion);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String id = rs.getString("id_materia");
                System.out.println("Materia encontrada: " + descripcion + " -> ID: " + id);
                return id;
            } else {
                System.out.println("Materia NO encontrada: " + descripcion);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String obtenerIdInscripcion(String idPersona, String idMateria) {
        String query = "SELECT id_inscripcion FROM inscripciones WHERE id_estudiante = ? AND id_materia = ? LIMIT 1";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, Integer.parseInt(idPersona));
            stmt.setInt(2, Integer.parseInt(idMateria));

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String id = rs.getString("id_inscripcion");
                System.out.println("Inscripción encontrada: Persona " + idPersona + " -> Materia " + idMateria + " -> Inscripción: " + id);
                return id;
            } else {
                System.out.println("Inscripción NO encontrada: Persona " + idPersona + " -> Materia " + idMateria);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean existeRegistro(String idInscripcion, LocalDate fecha) {
        String query = "SELECT COUNT(*) as count FROM asistencias WHERE id_inscripcion = ? AND fecha = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, Integer.parseInt(idInscripcion));
            stmt.setDate(2, Date.valueOf(fecha));

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                boolean existe = rs.getInt("count") > 0;
                System.out.println("Asistencia existe: " + existe + " para inscripción " + idInscripcion + " y fecha " + fecha);
                return existe;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void limpiarCampos() {
        comboPersona.getSelectionModel().clearSelection();
        dpFecha.setValue(LocalDate.now()); // Reset a fecha actual
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public void cerrarConexion() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("Conexión a BD cerrada.");
            }
            if (sshSession != null && sshSession.isConnected()) {
                sshSession.disconnect();
                System.out.println("Sesión SSH cerrada.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}