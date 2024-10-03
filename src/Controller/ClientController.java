package Controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import model.ChatMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientController {

    @FXML
    private Button btnLogin;
    @FXML
    private Button btnLogout;
    @FXML
    private TextArea txtAreaServerMsgs;
    @FXML
    private TextField txtHostIP;
    @FXML
    private TextField txtHostPort;
    @FXML
    private TextField txtUsername;
    @FXML
    private ListView<String> listUser;
    @FXML
    private TextArea txtUserMsg;

    private ObservableList<String> users;


    private boolean connected;
    private String server, username;
    private int port;


    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;
    private Socket socket;

    public void login() {
        port = Integer.valueOf(txtHostPort.getText());
        server = txtHostIP.getText();
        System.out.println(server);
        username = txtUsername.getText();

        if(!start())
            return;
        connected = true;
        btnLogin.setDisable(true);
        btnLogout.setDisable(false);
        txtUsername.setEditable(false);
        txtHostIP.setEditable(false);
    }


    public void logout() {
        if (connected) {
            ChatMessage msg = new ChatMessage(ChatMessage.LOGOUT, "");
            try {
                sOutput.writeObject(msg);
                txtUserMsg.setText("");
                btnLogout.setDisable(false);
                btnLogin.setDisable(true);
                txtUsername.setEditable(true);
                txtHostIP.setEditable(true);
            }
            catch(IOException e) {
                display("Exception writing to server: " + e);
            }
        }
    }


    public void sendMessage() {
        if (connected) {
            ChatMessage msg = new ChatMessage(ChatMessage.MESSAGE, txtUserMsg.getText());
            try {
                sOutput.writeObject(msg);
                txtUserMsg.setText("");
            }
            catch(IOException e) {
                display("Exception writing to server: " + e);
            }
        }
    }


    public void handleEnterPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            sendMessage();
            event.consume();
        }
    }


    public boolean start() {

        try {
            socket = new Socket(server, port);
        }

        catch(Exception ec) {
            display("Error connectiong to server:" + ec);
            return false;
        }

        String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
        display(msg);


        try
        {
            sInput  = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        }
        catch (IOException eIO) {
            display("Exception creating new Input/output Streams: " + eIO);
            return false;
        }


        new ListenFromServer().start();

        try
        {
            sOutput.writeObject(username);
        }
        catch (IOException eIO) {
            display("Exception doing login : " + eIO);
            disconnect();
            return false;
        }

        return true;
    }
    private void display(String msg) {
        txtAreaServerMsgs.appendText(msg + "\n");
    }


    private void disconnect() {
        try {
            if(sInput != null) sInput.close();
        }
        catch(Exception e) {}
        try {
            if(sOutput != null) sOutput.close();
        }
        catch(Exception e) {}
        try{
            if(socket != null) socket.close();
        }
        catch(Exception e) {}


        connectionFailed();

    }

    public void connectionFailed() {
        btnLogin.setDisable(false);
        btnLogout.setDisable(true);
        txtHostIP.setEditable(true);
        connected = false;
    }


    class ListenFromServer extends Thread {

        public void run() {
            users =	FXCollections.observableArrayList();
            listUser.setItems(users);
            while(true) {
                try {
                    String msg = (String) sInput.readObject();
                    String[] split = msg.split(":");
                    if (split[1].equals("WHOISIN")) {
                        Platform.runLater(() -> {
                            users.add(split[0]);
                        });;
                    } else if (split[1].equals("REMOVE")) {
                        Platform.runLater(() -> {
                            users.remove(split[0]);
                        });
                    } else{
                        txtAreaServerMsgs.appendText(msg);
                    }
                }
                catch(IOException e) {
                    display("Server has close the connection");
                    connectionFailed();
                    Platform.runLater(() -> {
                        listUser.setItems(null);
                    });
                    break;
                }

                catch(ClassNotFoundException e2) {

                }
            }
        }
    }
}
