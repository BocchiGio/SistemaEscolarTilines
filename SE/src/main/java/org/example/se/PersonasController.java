package org.example.se;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.SimpleTimeZone;

public class PersonasController {

    @FXML
    private MenuButton btnAcciones;

    @FXML
    private TableColumn<Persona, String> colApellido;

    @FXML
    private TableColumn<Persona, String> colDetalles;

    @FXML
    private TableColumn<Persona, Integer> colId;

    @FXML
    private TableColumn<Persona, LocalDate> colNacimiento;

    @FXML
    private TableColumn<Persona, String> colNombre;

    @FXML
    private TableColumn<Persona, String> colRol;

    @FXML
    private TableColumn<Persona, String> colSexo;

    @FXML
    private DatePicker fchNacimiento;

    @FXML
    private MenuItem menuEditar;

    @FXML
    private MenuItem menuEliminar;

    @FXML
    private MenuItem menuInsertar;

    @FXML
    private ComboBox<Rol> rol;

    @FXML
    private ComboBox<String> sexo;

    @FXML
    private TableView<Persona> tblPersonas;

    @FXML
    private TextField txtApellido;

    @FXML
    private TextField txtNombre;

    @FXML
    void onEditar(ActionEvent event) {

    }

    @FXML
    void onEliminar(ActionEvent event) {

    }

    @FXML
    void onInsertar(ActionEvent event) {
        if (conn == null) {
            mostrarAlerta("Conexión", "Sin conexión a la BD");
            return;
        }

        String nombre = trim(txtNombre.getText());
        String apellido = trim(txtApellido.getText());
        LocalDate nacimiento= fchNacimiento.getValue();
        String sexoUi = sexo.getValue();
        Rol rolSel = rol.getValue();

        if (nombre.isEmpty() || apellido.isEmpty() || nacimiento == null || sexoUi == null || rolSel == null) {
            mostrarAlerta("Datos imcompletos", "Todos los campos son obligatorios");
            return;
        }

        if (!confirm ("¿Insertar nuevo registro?"))
            return;

        String sexoDb = uiToDb(sexoUi);
        String sql = "INSERT INTO personas_escuela(nombre, apellido, sexo, fh_nac, id_rol) VALUES (?, ?, ?, ?, ?)";

        try(PreparedStatement st = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            st.setString(1,nombre);
            st.setString(2, apellido);
            st.setString(3, sexoDb);

            if(nacimiento != null) st.setDate(4, Date.valueOf(nacimiento));
            else st.setNull(4, Types.DATE);
            st.setInt(5, rolSel.getId());

            int n = st .executeUpdate();
            if (n == 1) {
                int newId = -1;
                try (ResultSet k = st.getGeneratedKeys()) { if (k.next()) newId = k.getInt(1); }
                cargarPersonas();
                marcarDetallePorId(newId, "Persona insertada");
                limpiar();
                info("Éxito", "Usuario insertado (ID " + newId + ").");
            } else {
                mostrarAlerta("Aviso", "No se insertó el registro.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Insert", e.getMessage());
        }
    }

    private Connection conn;
    private Session sshSession;

    public PersonasController() {
        tunelSSH();
    }

    private void tunelSSH() {
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

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

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

    public static class Persona {
        private final int id;
        private final String nombre;
        private final String apellido;
        private final String sexo;
        private final LocalDate fecha;
        private final String rol;
        private String detalles = "";

        public Persona(int id, String nombre, String apellido, String sexo, LocalDate fecha, String rol) {
            this.id = id;
            this.nombre = nombre;
            this.apellido = apellido;
            this.sexo = sexo;
            this.fecha = fecha;
            this.rol = rol;
        }

        public int getId() {
            return id;
        }

        public String getNombre() {
            return nombre;
        }

        public String getApellido() {
            return apellido;
        }

        public String getSexo() {
            return sexo;
        }

        public LocalDate getFecha() {
            return fecha;
        }

        public String getRol() {
            return rol;
        }

        public String getDetalles() {
            return detalles;
        }

        public void setDetalles(String detalles) {
            this.detalles = detalles;
        }

    }

    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApellido.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        colSexo.setCellValueFactory(new PropertyValueFactory<>("sexo"));
        colNacimiento.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("rol"));
        colDetalles.setCellValueFactory(new PropertyValueFactory<>("detalles"));
        sexo.getItems().addAll("Hombre", "Mujer", "Otro");

        cargarRoles();
        cargarPersonas();
    }

    private void cargarRoles() {
        rol.getItems().clear();
        if (conn == null) {
            mostrarAlerta("Conexión", "Sin conexión a la base de datos.");
            return;
        }

        String sql = "SELECT id_rol, descripcion FROM roles ORDER BY descripcion";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                rol.getItems().add(new Rol(rs.getInt("id_rol"), rs.getString("descripcion")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Roles", e.getMessage());
        }
    }

    private void cargarPersonas() {
        if (conn == null) {
            mostrarAlerta("Conexión", "Sin conexión a la base de datos.");
            return;
        }

        ObservableList<Persona> data = javafx.collections.FXCollections.observableArrayList();
        String sql = """
        SELECT p.id_persona, p.nombre, p.apellido, p.sexo, p.fh_nac, r.descripcion AS rol
        FROM personas_escuela p
        JOIN roles r ON r.id_rol = p.id_rol
        ORDER BY p.id_persona DESC
        """;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String sexoUi = dbToUi(rs.getString("sexo"));
                LocalDate nac = rs.getDate("fh_nac") == null ? null : rs.getDate("fh_nac").toLocalDate();
                data.add(new Persona(
                        rs.getInt("id_persona"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        sexoUi,
                        nac,
                        rs.getString("rol")));
            }
            tblPersonas.setItems(data);
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Personas", e.getMessage());
        }
    }

    // Convierte el valor que viene de BD a lo que muestras en UI
    private static String dbToUi(String db) {
        if (db == null) return "";
        switch (db.toLowerCase()) {
            case "h": return "Hombre";
            case "m":
            case "f": return "Mujer";
            case "o": return "Otro";
            default:  return db;
        }
    }

    // Convierte de lo que eliges en la UI a lo que guardas en BD
    private static String uiToDb(String ui) {
        if (ui == null) return null;
        switch (ui) {
            case "Hombre": return "h";
            case "Mujer":  return "m";
            case "Otro":   return "o";
            default:       return ui;
        }
    }

    private static String trim(String s){
        return s == null ? "": s.trim();
    }

    private boolean confirm(String m){
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, m, ButtonType.YES, ButtonType.NO);
        return a.showAndWait().filter(b -> b==ButtonType.YES).isPresent();
    }

    private void info(String titulo, String msg){
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(titulo);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void limpiar() {
        txtNombre.clear();
        txtApellido.clear();
        fchNacimiento.setValue(null);
        txtNombre.requestFocus();
    }

    public void cerrarConexion() {
        try { if (conn != null && !conn.isClosed()) conn.close();
        } catch (SQLException ignored) {}
        if (sshSession != null && sshSession.isConnected()) sshSession.disconnect();
        sshSession = null;
    }

    /** Busca la fila por id en la tabla, escribe el texto en la columna Detalles,
     *  selecciona y hace scroll a esa fila. */
    private void marcarDetallePorId(int id, String texto) {
        if (id <= 0) return; // por si no vino el id generado
        for (int i = 0; i < tblPersonas.getItems().size(); i++) {
            Persona p = tblPersonas.getItems().get(i);
            if (p.getId() == id) {
                p.setDetalles(texto);
                tblPersonas.getItems().set(i, p);
                tblPersonas.refresh();
                tblPersonas.getSelectionModel().select(i);
                tblPersonas.scrollTo(i);
                break;
            }
        }
    }

}


