package Controller;
import model.ChatMessage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Server {
	private static int uniqueId;
	private ArrayList<ClientThread> clientsConnected; //ArrayList for list of the Client
	private ServerController serverController;
	private SimpleDateFormat sdf;
	private int port;
	private boolean keepGoing;

	public Server(int port) {
		this(port, null);
	}

	public Server(int port, ServerController serverController) {
		this.serverController = serverController;
		this.port = port;
		sdf = new SimpleDateFormat("HH:mm:ss");
		// ArrayList for the Client list
		clientsConnected = new ArrayList<ClientThread>();
	}
	public void start() {
		keepGoing = true;

		try 
		{

			ServerSocket serverSocket = new ServerSocket(port);


			while(keepGoing) 
			{

				display("Server waiting for Clients on port " + port + ".");
				Socket socket = serverSocket.accept();  // accept connection

				if(!keepGoing)
					break;
				ClientThread t = new ClientThread(socket);
				clientsConnected.add(t);
				t.start();
			}
			try {
				serverSocket.close();
				for(int i = 0; i < clientsConnected.size(); ++i) {
					ClientThread tc = clientsConnected.get(i);
					try {
						tc.sInput.close();
						tc.sOutput.close();
						tc.socket.close();
					}
					catch(IOException ioE) {

					}
				}
			}
			catch(Exception e) {
				display("Exception closing the server and clients: " + e);
			}
		}

		catch (IOException e) {
			String msg = sdf.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
			display(msg);
		}
	}		

	public void stop() {
		keepGoing = false;

		// Socket socket = serverSocket.accept();
		try {
			new Socket("localhost", port);
		}
		catch(Exception e) {

		}
	}

	private void display(String msg) {
		String time = sdf.format(new Date()) + " " + msg;
		serverController.appendEvent(time + "\n");
	}

	private synchronized void broadcast(String message) {

		String time = sdf.format(new Date());
		String messageLf;
		if (message.contains("WHOISIN") || message.contains("REMOVE")){
			messageLf = message;
		} else {
			messageLf = time + " " + message + "\n";
			serverController.appendRoom(messageLf);
		}

		for(int i = clientsConnected.size(); --i >= 0;) {
			ClientThread ct = clientsConnected.get(i);

			if(!ct.writeMsg(messageLf)) {
				clientsConnected.remove(i);
				serverController.remove(ct.username);
				display("Disconnected Client " + ct.username + " removed from list.");
			}
		}
	}
	synchronized void remove(int id) {

		for(int i = 0; i < clientsConnected.size(); ++i) {
			ClientThread ct = clientsConnected.get(i);

			if(ct.id == id) {
				serverController.remove(ct.username);
				ct.writeMsg(ct.username + ":REMOVE");
				clientsConnected.remove(i);
				return;
			}
		}
	}


	class ClientThread extends Thread {

		Socket socket;
		ObjectInputStream sInput;
		ObjectOutputStream sOutput;
		int id;
		String username;
		ChatMessage cm;
		String date;


		ClientThread(Socket socket) {

			id = ++uniqueId;
			this.socket = socket;

			System.out.println("Thread trying to create Object Input/Output Streams");
			try
			{

				sOutput = new ObjectOutputStream(socket.getOutputStream());
				sInput  = new ObjectInputStream(socket.getInputStream());
				// read the username
				username = (String) sInput.readObject();
				serverController.addUser(username);
				broadcast(username + ":WHOISIN");
				writeMsg(username + ":WHOISIN");
				for(ClientThread client : clientsConnected) {
					writeMsg(client.username + ":WHOISIN");
				}
				display(username + " just connected.");
			}
			catch (IOException e) {
				display("Exception creating new Input/output Streams: " + e);
				return;
			}

			catch (ClassNotFoundException e) {
			}
			date = new Date().toString() + "\n";
		}


		public void run() {

			boolean keepGoing = true;
			while(keepGoing) {

				try {
					cm = (ChatMessage) sInput.readObject();
				}
				catch (IOException e) {
					display(username + " Exception reading Streams: " + e);
					break;				
				}
				catch(ClassNotFoundException e2) {
					break;
				}
				String message = cm.getMessage();
				switch(cm.getType()) {

				case ChatMessage.MESSAGE:
					broadcast(username + ": " + message);
					break;
				case ChatMessage.LOGOUT:
					display(username + " disconnected with a LOGOUT message.");
					broadcast(username + ":REMOVE");
					keepGoing = false;
					break;
				}
			}
			remove(id);
			close();
		}


		private void close() {

			try {
				if(sOutput != null) sOutput.close();
			}
			catch(Exception e) {}
			try {
				if(sInput != null) sInput.close();
			}
			catch(Exception e) {};
			try {
				if(socket != null) socket.close();
			}
			catch (Exception e) {}
		}
		private boolean writeMsg(String msg) {

			if(!socket.isConnected()) {
				close();
				return false;
			}

			try {
				sOutput.writeObject(msg);
			}

			catch(IOException e) {
				display("Error sending message to " + username);
				display(e.toString());
			}
			return true;
		}
	}
}



