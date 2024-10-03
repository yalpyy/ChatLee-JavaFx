package Controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ServerController {
    @FXML
    private TextArea txtAreaChatMsg;
    @FXML
    private TextArea txtAreaEventLog;
    @FXML
    private TextField txtHostPort;
    @FXML
    private ListView<String> listUsersConnected;
    @FXML
    private Button btnStartServer;
    @FXML
    private Button btnStopServer;

    public Server server;

    private ObservableList<String> users;

    public void startServer() {
        // create a new Server
        server = new Server(Integer.valueOf(txtHostPort.getText()), this);
        users = FXCollections.observableArrayList();
        listUsersConnected.setItems(users);
        new ServerRunning().start();
        btnStartServer.setDisable(true);
        btnStopServer.setDisable(false);
    }

    public void stopServer() {
        if(server != null) {
            server.stop();
            btnStopServer.setDisable(true);
            btnStartServer.setDisable(false);
            listUsersConnected.setItems(null);
            server = null;
            return;
        }
    }


    class ServerRunning extends Thread {
        public void run() {
            server.start();

            appendEvent("Server Stopped\n");
            server = null;
            users = null;
        }
    }

    public void addUser(String user) {
        Platform.runLater(() -> {
            users.add(user);
        });
    }
    public void appendEvent(String string) {
        txtAreaEventLog.appendText(string);
    }

    public void appendRoom(String messageLf) {
        txtAreaChatMsg.appendText(messageLf);
    }

    public void remove(String username) {
        Platform.runLater(() -> {
            users.remove(username);
        });
    }
}

