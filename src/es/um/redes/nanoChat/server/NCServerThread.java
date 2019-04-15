package es.um.redes.nanoChat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import es.um.redes.nanoChat.messageFV.NCControlMessage;
import es.um.redes.nanoChat.messageFV.NCInfoMessage;
import es.um.redes.nanoChat.messageFV.NCMessage;
import es.um.redes.nanoChat.messageFV.NCMessageOp;
import es.um.redes.nanoChat.messageFV.NCNameMessage;
import es.um.redes.nanoChat.server.roomManager.NCRoomDescription;
import es.um.redes.nanoChat.server.roomManager.NCRoomManager;

/**
 * A new thread runs for each connected client
 */
public class NCServerThread extends Thread {
	
	private Socket socket = null;
	//Manager global compartido entre los Threads
	private NCServerManager serverManager = null;
	//Input and Output Streams
	private DataInputStream dis;
	private DataOutputStream dos;
	//Usuario actual al que atiende este Thread
	String user;
	//RoomManager actual (dependerá de la sala a la que entre el usuario)
	NCRoomManager roomManager;
	//Sala actual
	String currentRoom;

	// Inicialización de la sala
	public NCServerThread(NCServerManager manager, Socket socket) throws IOException {
		super("NCServerThread");
		this.socket = socket;
		this.serverManager = manager;
	}

	//Main loop
	public void run() {
		try {
			//Se obtienen los streams a partir del Socket
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
			//En primer lugar hay que recibir y verificar el nick
			receiveAndVerifyNickname();
			//Mientras que la conexión esté activa entonces...
			while (true) {
				// Obtenemos el mensaje que llega y analizamos su código de operación
				NCMessage message = NCMessage.readMessageFromSocket(dis);
				switch (message.getOp()) {
				case ROOMS_LIST: // 1) si se nos pide la lista de salas se envía llamando a sendRoomList();
					sendRoomList();
					break;
				
				// Request to create a room
				case CREATE:
					String roomName = ((NCNameMessage) message).getName();
					roomManager = serverManager.createRoom(user, roomName, socket);
					// If allowed, notify the client moved to the room and answered OK
					if (roomManager != null) {
						dos.writeUTF(new NCControlMessage(NCMessageOp.OK).toEncodedString());
						currentRoom = roomName;
						processRoomMessages();
					} else dos.writeUTF(new NCControlMessage(NCMessageOp.DENIED).toEncodedString());
					break;
				
				// Request to enter a room
				case ENTER:
					String room = ((NCNameMessage) message).getName();
					roomManager = serverManager.enterRoom(user, room, socket);
					// If allowed, notify the client and start processing messages with processRoomMessages()
					if (roomManager != null) {
						dos.writeUTF(new NCControlMessage(NCMessageOp.OK).toEncodedString());
						currentRoom = room;
						processRoomMessages();
					} else dos.writeUTF(new NCControlMessage(NCMessageOp.DENIED).toEncodedString());
					break;
				
				default: // Si el mensaje no se encuentra entre los que se pueden enviar:
					NCControlMessage answer = new NCControlMessage(NCMessageOp.INVALID_CODE);
					dos.writeUTF(answer.toEncodedString());
				}
			}
		} catch (Exception e) {
			//If an error occurs with the communications the user is removed from all the managers and the connection is closed
			System.out.println("* User "+ user + " disconnected.");
			serverManager.leaveRoom(user, currentRoom);
			serverManager.removeUser(user);
		}
		finally {
			if (!socket.isClosed())
				try {
					socket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
		}
	}

	// Obtenemos el nick y solicitamos al ServerManager que verifique si está duplicado
	private void receiveAndVerifyNickname() throws IOException {
		// La lógica de nuestro programa nos obliga a que haya un nick registrado antes de proseguir
		// Extraer el nick del mensaje
		NCNameMessage message = (NCNameMessage) NCMessage.readMessageFromSocket(dis); //TODO comprobar que es de este tipo
		// Entramos en un bucle hasta comprobar que alguno de los nicks proporcionados no está duplicado
		// Validar el nick utilizando el ServerManager - addUser()
		while (!serverManager.addUser(message.getName())) {
			dos.writeUTF(new NCControlMessage(NCMessageOp.REPEATED).toEncodedString());
			message = (NCNameMessage) NCMessage.readMessageFromSocket(dis); //TODO comprobar que es de este tipo
		};
		// Contestar al cliente con el resultado (éxito o duplicado)
		user = message.getName();
		dos.writeUTF(new NCControlMessage(NCMessageOp.OK).toEncodedString());
	}

	//Mandamos al cliente la lista de salas existentes
	private void sendRoomList() throws IOException {
		// La lista de salas debe obtenerse a partir del ServerManager y después enviarse mediante su mensaje correspondiente
		ArrayList<NCRoomDescription> rooms = serverManager.getRoomList();
		NCInfoMessage answer = new NCInfoMessage(NCMessageOp.ROOMS_INFO, rooms);
		dos.writeUTF(answer.toEncodedString());
	}

	private void processRoomMessages()  {
		//TODO Comprobamos los mensajes que llegan hasta que el usuario decida salir de la sala
		boolean exit = false;
		while (!exit) {
			//TODO Se recibe el mensaje enviado por el usuario
			//TODO Se analiza el código de operación del mensaje y se trata en consecuencia
		}
	}
}
