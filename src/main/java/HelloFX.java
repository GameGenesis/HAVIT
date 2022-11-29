import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class HelloFX extends Application {

    @Override
    public void start(Stage stage) {
        //Get some information to display in our label
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");

        //Create a layout pane to act at the main container for all UI elements
        VBox rootPane = new VBox();
        //Align anything added to this layout pane so it looks nicer
        rootPane.setAlignment(Pos.CENTER);

        //Create a label to put some text on the UI
        Label l = new Label("Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".");
        //Add the label to the layout pane
        rootPane.getChildren().add(l);

        //Create the scene and display it.
        Scene scene = new Scene(rootPane, 640, 480);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}