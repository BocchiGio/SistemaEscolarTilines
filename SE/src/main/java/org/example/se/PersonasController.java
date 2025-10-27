package org.example.se;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.*;
import java.time.LocalDate;

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
    private ComboBox<Persona.Rol> rol;

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
        //Falta implementar
    }

    @FXML
    void onEliminar(ActionEvent event) {
        //Falta implementar
    }

    @FXML
    void onInsertar(ActionEvent event) {

        Connection conn = ConexionBD.getConnection();

        if (conn == null) {
            mostrarAlerta("Conexión", "Sin conexión a la BD");
            return;
        }

        String nombre = trim(txtNombre.getText());
        String apellido = trim(txtApellido.getText());
        LocalDate nacimiento = fchNacimiento.getValue();
        String sexoUi = sexo.getValue();

        Persona.Rol rolSel = rol.getValue();

        if (nombre.isEmpty() || apellido.isEmpty() || nacimiento == null || sexoUi == null || rolSel == null) {
            mostrarAlerta("Datos imcompletos", "Todos los campos son obligatorios");
            return;
        }

        if (!confirm("¿Insertar nuevo registro?"))
            return;

        String sexoDb = uiToDb(sexoUi);
        String sql = "INSERT INTO personas_escuela(nombre, apellido, sexo, fh_nac, id_rol) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement st = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            st.setString(1, nombre);
            st.setString(2, apellido);
            st.setString(3, sexoDb);

            if (nacimiento != null) st.setDate(4, Date.valueOf(nacimiento));
            else st.setNull(4, Types.DATE);
            st.setInt(5, rolSel.getId());

            int n = st.executeUpdate();
            if (n == 1) {
                int newId = -1;
                try (ResultSet k = st.getGeneratedKeys()) {
                    if (k.next()) newId = k.getInt(1);
                }
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

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
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

        Connection conn = ConexionBD.getConnection();

        rol.getItems().clear();
        if (conn == null) {
            mostrarAlerta("Conexión", "Sin conexión a la base de datos.");
            return;
        }

        String sql = "SELECT id_rol, descripcion FROM roles ORDER BY descripcion";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                rol.getItems().add(new Persona.Rol(rs.getInt("id_rol"), rs.getString("descripcion")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Roles", e.getMessage());
        }
    }

    private void cargarPersonas() {

        Connection conn = ConexionBD.getConnection();

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

    // Convertir el valor que viene de la BD a lo que se muestra en UI
    private static String dbToUi(String db) {
        if (db == null) return "";
        switch (db.toLowerCase()) {
            case "h":
                return "Hombre";
            case "m":
            case "f":
                return "Mujer";
            case "o":
                return "Otro";
            default:
                return db;
        }
    }

    // Convertir lo que se selecciona en la UI a lo que se guarda en la BD
    private static String uiToDb(String ui) {
        if (ui == null) return null;
        switch (ui) {
            case "Hombre":
                return "h";
            case "Mujer":
                return "m";
            case "Otro":
                return "o";
            default:
                return ui;
        }
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    //Confirmación Si o No para implementar la acción
    private boolean confirm(String m) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, m, ButtonType.YES, ButtonType.NO);
        return a.showAndWait().filter(b -> b == ButtonType.YES).isPresent();
    }

    //Crear unaa ventana para mostrar lo que se hizo
    private void info(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(titulo);
        a.setContentText(msg);
        a.showAndWait();
    }

    //Limmpiar los campos
    private void limpiar() {
        txtNombre.clear();
        txtApellido.clear();
        fchNacimiento.setValue(null);
        txtNombre.requestFocus();
    }

    // Muestra lo que se le hizo a la persona
    private void marcarDetallePorId(int id, String texto) {
        if (id <= 0) return; // por si no vino el id generado
        for (int i = 0; i < tblPersonas.getItems().size(); i++) {
            Persona p = tblPersonas.getItems().get(i);
            if (p.getId() == id) {

                p.setDetalles(texto);

                tblPersonas.refresh();
                tblPersonas.getSelectionModel().select(i);
                tblPersonas.scrollTo(i);
                break;
            }
        }
    }

}

