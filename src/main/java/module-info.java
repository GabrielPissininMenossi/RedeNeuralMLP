module fipp.muscleandiq.redeneuralmlp {
    requires javafx.controls;
    requires javafx.fxml;


    opens fipp.muscleandiq.redeneuralmlp to javafx.fxml;
    opens fipp.muscleandiq.redeneuralmlp.entities to javafx.base;
    exports fipp.muscleandiq.redeneuralmlp;
}