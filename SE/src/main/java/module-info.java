module org.example.se {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.jcraft.jsch;
    requires javafx.base;


    opens org.example.se to javafx.fxml;
    exports org.example.se;
}