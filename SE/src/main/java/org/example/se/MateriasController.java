package org.example.se;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.*;
import java.util.Optional;

public class MateriasController {

    @FXML
    private TextField txtMateria;

    @FXML
    private Spinner<Integer> creditos;

    @FXML
    private ComboBox<Integer> semestre;

    @FXML
    private MenuButton btnAcciones;

    @FXML
    private MenuItem menuEditar;

    @FXML
    private MenuItem menuEliminar;

    @FXML
    private MenuItem menuInsertar;

    @FXML
    private TableView<Materia> tblMaterias;

    @FXML
    private TableColumn<Materia, Integer> colId;

    @FXML
    private TableColumn<Materia, String> colMateria;

    @FXML
    private TableColumn<Materia, Integer> colSemestre;

    @FXML
    private TableColumn<Materia, Integer> colCreditos;

    @FXML
    private TableColumn<Materia, String> colDetalles;

    private ObservableList<Materia> listaMaterias = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idMateria"));
        colMateria.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colSemestre.setCellValueFactory(new PropertyValueFactory<>("semestre"));
        colCreditos.setCellValueFactory(new PropertyValueFactory<>("creditos"));
        colDetalles.setCellValueFactory(new PropertyValueFactory<>("detalles"));

        tblMaterias.setItems(listaMaterias);
        SpinnerValueFactory<Integer> creditosFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory
                (0, 10, 0);
        this.creditos.setValueFactory(creditosFactory);
        this.creditos.setEditable(true); // Permite escribir el número

        this.semestre.setPromptText("Semestre");
        this.semestre.getItems().addAll(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);

        cargarDatosDeLaBD();
    }

    @FXML
    void onInsertar(ActionEvent event) {
        Connection conn = ConexionBD.getConnection();
        if (conn == null) {
            mostrarAlerta("Error de Conexión", "No se pudo conectar a la base de datos.");
            return;
        }

        String descripcion = trim(txtMateria.getText());
        Integer sem = semestre.getValue();

        // Validar que los datos no estén vacíos
        String mensajeError = "";

        if (descripcion.isEmpty()) {
            mensajeError += "Debe introducir una 'Materia'.\n";
            txtMateria.requestFocus();
        }
        if (sem == null) {
            mensajeError += "Debe seleccionar un 'Semestre'.\n";
        }

        // Si hay cualquier error
        if (!mensajeError.isEmpty()) {
            mostrarAlerta("Datos incompletos", mensajeError);
            return;
        }

        Integer cred = creditos.getValue();

        if (!confirm("¿Está seguro de que desea CREAR la nueva materia: '" + descripcion + "'?")) {
            return;
        }

        String sql = "INSERT INTO materias (descripcion, semestre, creditos) VALUES (?, ?, ?)";

        try (PreparedStatement st = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            st.setString(1, descripcion);
            st.setInt(2, sem);
            st.setInt(3, cred);

            int filasAfectadas = st.executeUpdate();

            if (filasAfectadas > 0) {
                int newId = -1;
                try (ResultSet k = st.getGeneratedKeys()) {
                    if (k.next()) newId = k.getInt(1);
                }

                info("Éxito", "Materia '" + descripcion + "' creada correctamente.");

                // Actualizar la tabla y limpia los campos
                cargarDatosDeLaBD();
                marcarDetallePorId(newId, "Materia Insertada");
                limpiar();
            } else {
                mostrarAlerta("Error", "No se pudo insertar la materia.");
            }

        } catch (SQLException e) {
            mostrarAlerta("Error de SQL (Insert)", "No se pudo guardar la materia: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void onEditar(ActionEvent event) {
        // Falta implementar
    }

    @FXML
    void onEliminar(ActionEvent event) {
        // Falta implementar
    }

    private void cargarDatosDeLaBD() {
        Connection conn = ConexionBD.getConnection();
        if (conn == null) return;

        listaMaterias.clear();

        String sql = "SELECT id_materia, descripcion, semestre, creditos FROM materias ORDER BY id_materia DESC";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                listaMaterias.add(new Materia(
                        rs.getInt("id_materia"),
                        rs.getString("descripcion"),
                        rs.getInt("semestre"),
                        rs.getInt("creditos")
                ));
            }
        } catch (SQLException e) {
            mostrarAlerta("Error de SQL", "Error al cargar las materias: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void limpiar() {
        txtMateria.clear();
        semestre.getSelectionModel().selectFirst();
        creditos.getValueFactory().setValue(0);
        txtMateria.requestFocus();
    }

    private void marcarDetallePorId(int id, String texto) {
        if (id <= 0) return; // No se pudo obtener el ID

        for (Materia m : listaMaterias) {
            if (m.getIdMateria() == id) {
                m.setDetalles(texto);
                tblMaterias.refresh();

                // Selecciona y enfoca la nueva fila
                tblMaterias.getSelectionModel().select(m);
                tblMaterias.scrollTo(m);
                break;
            }
        }
    }

    private static String trim(String s) {
        return (s == null) ? "" : s.trim();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void info(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(titulo);
        a.setContentText(msg);
        a.showAndWait();
    }

    private boolean confirm(String m) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, m, ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = a.showAndWait();
        return result.isPresent() && result.get() == ButtonType.YES;
    }
}