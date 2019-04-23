package es.um.redes.nanoChat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import es.um.redes.nanoChat.messageFV.*;
import es.um.redes.nanoChat.messageFV.messages.NCControlMessage;
import es.um.redes.nanoChat.messageFV.messages.NCCreateMessage;
import es.um.redes.nanoChat.messageFV.messages.NCEnterMessage;
import es.um.redes.nanoChat.messageFV.messages.NCMessage;
import es.um.redes.nanoChat.messageFV.messages.NCRegisterMessage;
import es.um.redes.nanoChat.messageFV.messages.NCRenameMessage;
import es.um.redes.nanoChat.messageFV.messages.NCRoomInfoMessage;
import es.um.redes.nanoChat.messageFV.messages.NCRoomListMessage;
import es.um.redes.nanoChat.messageFV.messages.NCSendMessage;
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
	//TODO probablemente es más adecuado sustituir los getType() -> cast por instanceOf, pero habrá que ver cómo manejamos los mensajes de control (clases separadas?) 

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
				switch (message.getType()) {
				case ROOMS_LIST: // 1) si se nos pide la lista de salas se envía llamando a sendRoomList();
					sendRoomList();
					break;
				
				// Request to create a room
				case CREATE:
					String roomName = ((NCCreateMessage) message).getRoomName();
					roomManager = serverManager.createRoom(user, roomName, socket);
					// If allowed, notify the client moved to the room and answered OK
					if (roomManager != null) {
						dos.writeUTF(new NCControlMessage(NCMessageType.OK).encode());
						currentRoom = roomName;
						processRoomMessages();
					} else dos.writeUTF(new NCControlMessage(NCMessageType.DENIED).encode());
					break;
				
				// Request to enter a room
				case ENTER:
					String room = ((NCEnterMessage) message).getRoomName();
					roomManager = serverManager.enterRoom(user, room, socket);
					// If allowed, notify the client and start processing messages with processRoomMessages()
					if (roomManager != null) {
						dos.writeUTF(new NCControlMessage(NCMessageType.OK).encode());
						currentRoom = room;
						processRoomMessages();
					} else dos.writeUTF(new NCControlMessage(NCMessageType.DENIED).encode());
					break;
				default: // Si el mensaje no se encuentra entre los que se pueden enviar:
					NCControlMessage answer = new NCControlMessage(NCMessageType.INVALID_CODE);
					dos.writeUTF(answer.encode());
				}
			}
		} catch (InvalidFormat e) {
			System.out.println("* User " + user + " sent an invalid format message. Reason: " + e.getMessage());
		} catch (Exception e) {
			//If an error occurs with the communications the user is removed from all the managers and the connection is closed
			System.out.println("* User "+ user + " disconnected. " + e.getMessage());
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
	//TODO hacer checkeo de formato inválido (probablemente en función aparte)
	private void receiveAndVerifyNickname() throws IOException, InvalidFormat {
		// La lógica de nuestro programa nos obliga a que haya un nick registrado antes de proseguir
		// Extraer el nick del mensaje
		NCRegisterMessage message = (NCRegisterMessage) NCMessage.readMessageFromSocket(dis); //TODO comprobar que es de este tipo
		// Entramos en un bucle hasta comprobar que alguno de los nicks proporcionados no está duplicado
		// Validar el nick utilizando el ServerManager - addUser()
		while (!serverManager.addUser(message.getName())) {
			dos.writeUTF(new NCControlMessage(NCMessageType.REPEATED).encode());
			message = (NCRegisterMessage) NCMessage.readMessageFromSocket(dis); //TODO comprobar que es de este tipo
		};
		// Contestar al cliente con el resultado (éxito o duplicado)
		user = message.getName();
		dos.writeUTF(new NCControlMessage(NCMessageType.OK).encode());
	}

	//Mandamos al cliente la lista de salas existentes
	private void sendRoomList() throws IOException {
		// La lista de salas debe obtenerse a partir del ServerManager y después enviarse mediante su mensaje correspondiente
		ArrayList<NCRoomDescription> rooms = serverManager.getRoomList();
		NCRoomListMessage answer = new NCRoomListMessage(rooms);
		dos.writeUTF(answer.encode());
	}

	private void processRoomMessages() throws InvalidFormat  { //TODO acabar //TODO quitar invalid format con metodo, as above 
		// Loop until the user exits this room
		boolean exit = false;
		while (!exit) {			
			try {
				// Read new message
				NCMessage message = NCMessage.readMessageFromSocket(dis);
				switch (message.getType()) {
				case INFO:
					NCRoomInfoMessage infoMessage = new NCRoomInfoMessage(roomManager.getDescription());
					dos.writeUTF(infoMessage.encode());
					break;
				case SEND: //TODO revisar jm
					roomManager.broadcastMessage(user, ((NCSendMessage) message).getText()); 
					break;			
				case DM:
					//roomManager.sendMessage(user, ((NCTextMessage) message).getUser(), ((NCTextMessage) message).getText());
					break;
				case QUIT: //TODO no se si ejecutara lo de abajo antes de que haya ningun error de conexion
							// Si lo hay habra que ver como, seguramente poner en catch lo mismo que en la
							// situacion de arriba. Si se quita esto hay que borrar en NCMessagOP, NCControlMessage 
							// NCMessage, NCConnector.disconnect
				case EXIT:
					serverManager.leaveRoom(user, currentRoom); 
					roomManager = null;
					currentRoom = null;
					exit = true;
					break;
				//case KICK: //TODO
				//case UPLOAD: //TODO
				case RENAME: //TODO separar estas cosas en funciones y eso (las que sean largas, las otras se pueden quedar)
					NCControlMessage ans;
					switch(roomManager.rename(user, ((NCRenameMessage) message).getNewName())) {
					case 0:
						ans = new NCControlMessage(NCMessageType.OK);
						break;
					case 1:
						ans = new NCControlMessage(NCMessageType.IMPOSSIBLE);
						break;
					case 2:
					default:
						ans = new NCControlMessage(NCMessageType.DENIED);
						break;
					}
					dos.writeUTF(ans.encode());
					break;
				default: //TODO ver qué hacer si es un mensaje no soportado
					break;
				}
			} catch (IOException e) {
				// TODO ver por qué se genera una excepción
				e.printStackTrace();
			}
		}
	}
}
