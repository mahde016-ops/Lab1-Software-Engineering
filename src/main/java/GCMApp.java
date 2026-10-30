
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GCMApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        UserRepository userRepository = new UserRepository();

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/login.fxml")
        );

        Scene scene = new Scene(loader.load());

        LoginController controller = loader.getController();
        controller.setUserRepository(userRepository);

        stage.setTitle("GCM - Login");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
