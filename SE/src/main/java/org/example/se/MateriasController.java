package org.example.se;

import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

public class MateriasController {

    // Controles de la interfaz (FXML)
    @FXML private TextField txtBuscar;
    @FXML private ComboBox<Integer> cbSemestre;
    @FXML private Spinner<Integer> spMinCreditos;
    @FXML private Button btnNuevo, btnEditar, btnEliminar, btnRefrescar;

    // Tabla de Materias
    @FXML private TableView<Materia> tblMaterias;
    @FXML private TableColumn<Materia, Integer> colId, colSemestre, colCreditos;
    @FXML private TableColumn<Materia, String> colDescripcion, colCreated, colUpdated;

    // Controles para el detalle de la materia
    @FXML private TextField txtDescripcion;
    @FXML private Spinner<Integer> spSemestre, spCreditos;
    @FXML private Label lblCreated, lblUpdated;
    @FXML private Button btnGuardar, btnCancelar;

    // Información de estado
    @FXML private Label lblStatus;
    @FXML private Pagination pagination;

    // Repositorio en memoria (simula la base de datos)
    private final InMemoryMateriaRepo repo = new InMemoryMateriaRepo();
    private FilteredList<Materia> filtered;

    @FXML
    private void initialize() {
        // Configuración de los spinners (semestre y créditos)
        setupSpinner(spSemestre, 1, 12, 1);
        setupSpinner(spCreditos, 0, 20, 4);
        setupSpinner(spMinCreditos, 0, 20, 0);

        // Configuración del ComboBox para los semestres
        cbSemestre.getItems().add(null);  // Agrega la opción "Todos"
        for (int i = 1; i <= 12; i++) {
            cbSemestre.getItems().add(i);
        }
        cbSemestre.setConverter(new StringConverter<Integer>() {
            @Override
            public String toString(Integer v) {
                return v == null ? "Todos" : String.valueOf(v);
            }

            @Override
            public Integer fromString(String s) {
                return null;
            }
        });
        cbSemestre.getSelectionModel().selectFirst();

        // Configuración de la tabla de materias
        colId.setCellValueFactory(c -> c.getValue().idMateriaProperty().asObject());
        colDescripcion.setCellValueFactory(c -> c.getValue().descripcionProperty());
        colSemestre.setCellValueFactory(c -> c.getValue().semestreProperty().asObject());
        colCreditos.setCellValueFactory(c -> c.getValue().creditosProperty().asObject());
        colCreated.setCellValueFactory(c -> c.getValue().createdAtProperty());
        colUpdated.setCellValueFactory(c -> c.getValue().updatedAtProperty());

        // Filtrado de la lista de materias
        filtered = new FilteredList<>(repo.list(), m -> true);
        tblMaterias.setItems(filtered);

        tblMaterias.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> {
            boolean has = b != null;
            btnEditar.setDisable(!has);
            btnEliminar.setDisable(!has);
            if (has) bindForm(b);
        });

        lblStatus.setText("Listo");
    }

    // Configuración de los spinners
    private void setupSpinner(Spinner<Integer> sp, int min, int max, int val) {
        sp.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, val, 1));
        sp.setEditable(true);
    }

    // Vincula los datos de la materia seleccionada al formulario de detalle
    private void bindForm(Materia m) {
        txtDescripcion.setText(m.getDescripcion());
        spSemestre.getValueFactory().setValue(m.getSemestre());
        spCreditos.getValueFactory().setValue(m.getCreditos());
        lblCreated.setText(m.getCreatedAt());
        lblUpdated.setText(m.getUpdatedAt());
    }

    // Limpia el formulario de detalle
    private void clearForm() {
        txtDescripcion.clear();
        spSemestre.getValueFactory().setValue(1);
        spCreditos.getValueFactory().setValue(0);
        lblCreated.setText("");
        lblUpdated.setText("");
    }

    // Acciones de los botones en la parte superior
    @FXML
    private void onNuevo() {
        clearForm();
    }

    @FXML
    private void onEditar() {
        // Implementar la lógica para editar una materia (si es necesario)
    }

    @FXML
    private void onEliminar() {
        Materia selected = tblMaterias.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        repo.delete(selected);
        lblStatus.setText("Eliminado id " + selected.getIdMateria());
    }

    @FXML
    private void onRefrescar() {
        tblMaterias.refresh();
        lblStatus.setText("Refrescado");
    }

    // Guardar/Cancelar (en el detalle)
    @FXML
    private void onGuardar() {
        String descripcion = txtDescripcion.getText() == null ? "" : txtDescripcion.getText().trim();
        Integer semestre = spSemestre.getValue();
        Integer creditos = spCreditos.getValue();
        if (descripcion.isEmpty()) {
            lblStatus.setText("Descripción requerida");
            return;
        }
        Materia m = repo.add(descripcion, semestre, creditos);
        tblMaterias.getSelectionModel().select(m);
        lblStatus.setText("Guardado id " + m.getIdMateria());
    }

    @FXML
    private void onCancelar() {
        clearForm();
        lblStatus.setText("Cancelado");
    }

    // Filtros
    @FXML
    private void onBuscarChanged() {
        applyFilters();
    }

    @FXML
    private void onFiltroSemestre() {
        applyFilters();
    }

    // Aplicación de los filtros (por búsqueda y semestre)
    private void applyFilters() {
        String query = txtBuscar.getText() == null ? "" : txtBuscar.getText().toLowerCase().trim();
        Integer semestre = cbSemestre.getValue();
        Integer minCreditos = spMinCreditos.getValue();

        filtered.setPredicate(m -> {
            boolean matchesQuery = query.isEmpty() || m.getDescripcion().toLowerCase().contains(query);
            boolean matchesSemestre = semestre == null || m.getSemestre() == semestre;
            boolean matchesCreditos = minCreditos == null || m.getCreditos() >= minCreditos;
            return matchesQuery && matchesSemestre && matchesCreditos;
        });
    }
}
