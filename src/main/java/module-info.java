module com.mycompany.serverxo {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.mycompany.serverxo to javafx.fxml;
    exports com.mycompany.serverxo;
}
