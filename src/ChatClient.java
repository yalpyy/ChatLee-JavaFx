import Controller.ClientController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class ChatClient extends Application {

    private Stage primaryStage;
    private VBox chatLayout;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Data Communication Chat App");

        initChatLayout();
    }

    private void initChatLayout() {
        try {

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(ChatClient.class.getResource("ClientGUI.fxml"));
            loader.setController(new ClientController());
            chatLayout = (VBox) loader.load();


            Scene scene = new Scene(chatLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
