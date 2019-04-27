package es.um.redes.nanoChat.client.application;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collection;

import es.um.redes.nanoChat.client.comm.NCConnector;
import es.um.redes.nanoChat.client.shell.NCCommand;
import es.um.redes.nanoChat.client.shell.NCShell;
import es.um.redes.nanoChat.directory.connector.DirectoryConnector;
import es.um.redes.nanoChat.messageFV.NCMessageType;
import es.um.redes.nanoChat.messageFV.messages.NCControlMessage;
import es.um.redes.nanoChat.messageFV.messages.NCDirectMessage;
import es.um.redes.nanoChat.messageFV.messages.NCMessage;
import es.um.redes.nanoChat.messageFV.messages.NCNotificationMessage;
import es.um.redes.nanoChat.messageFV.messages.NCSecretMessage;
import es.um.redes.nanoChat.messageFV.messages.NCTextMessage;
import es.um.redes.nanoChat.server.roomManager.NCRoomDescription;

public class NCController {
	private static final int PROTOCOL = 163;		// Código de protocolo implementado por este cliente
	private DirectoryConnector directoryConnector;	// Conector para enviar y recibir mensajes del directorio
	private NCConnector ncConnector;				// Conector para enviar y recibir mensajes con el servidor de NanoChat
	private NCShell shell;							// Shell para leer comandos de usuario de la entrada estándar
	private NCCommand currentCommand;				// Último comando proporcionado por el usuario
	private InetSocketAddress serverAddress;		// Dirección de internet del servidor de NanoChat
	private NCClientStatus clientStatus;			// Estado actual del cliente, de acuerdo con el autómata

	//Constructor
	public NCController() {
		shell = new NCShell();
		//TODO currentCommand = NCCommands.NOT_SET;
		clientStatus = NCClientStatus.PRE_CONNECTION;
	}
	
	public void startProcessing() {
		if (clientStatus != NCClientStatus.PRE_REGISTRATION) {
			System.err.println("You must connect to a server before attempting to start the NCController");
			return;
		}
		// Entramos en el bucle para pedirle al controlador que procese comandos del shell
		// hasta que el usuario quiera salir de la aplicación.
		try {
			while (clientStatus != NCClientStatus.QUIT) {
				currentCommand = shell.readGeneralCommand(ncConnector);		// Read valid command //TODO jm
				String[] commandArgs = shell.getCommandArguments();	// and its arguments
				// Process a command (status PRE_CONNECTION)
				switch (currentCommand) {
				case NICK:
					registerNickName(commandArgs[0]);
					break;
				case QUIT:
					clientStatus = NCClientStatus.QUIT;
					break;
				default:
					System.out.println("* You can't perform that action, you must be registered");
					break;
				}
				// Change of state:
				if (clientStatus == NCClientStatus.OUT_ROOM)
					outRoomProcessing();
			}
		} catch (IOException e) {
			System.err.println("* There was a communication error. Finishing now...");
		} finally {
			ncConnector.disconnect();			
		}
	}
	
	public void outRoomProcessing() throws IOException {
		while (clientStatus != NCClientStatus.QUIT) {
			currentCommand = shell.readGeneralCommand(ncConnector);//TODO jm
			String[] commandArgs = shell.getCommandArguments();
			// Process a command (status OUTROOM)
			switch (currentCommand) {
			case CREATE:
				createRoom(commandArgs[0]);
				break;
			case DM: //TODO revisar jm
				sendDirectMessage(commandArgs[0], commandArgs[1]);
				break;
			case ENTER:
				enterChat(commandArgs[0]);
				break;			
			case ROOMLIST:
				getAndShowRooms();
				break;
			case QUIT:
				clientStatus = NCClientStatus.QUIT;
				break;
			case SOCKET_IN://TODO revisar jm
				processIncomingMessage();
			//case NEW_DM: 
				//System.out.println(((NCDirectMessage) message).getUser()+" [DM]: "+((NCDirectMessage)message).getText());
				break;
			default:
				System.err.println("* Received an outroom command from shell not listed in outRoomProcessing");
				break;
			}
			// Possible change of state:
			if (clientStatus == NCClientStatus.IN_ROOM) {
				inRoomProcessing();
			}
		}
	}
	
	public void inRoomProcessing() throws IOException {
		// Return condition: 
		while (!shouldQuit() && clientStatus == NCClientStatus.IN_ROOM) {
			currentCommand = shell.readChatCommand(ncConnector);
			String[] commandArgs = shell.getCommandArguments();
			switch (currentCommand) {
			case DM: //TODO revisar jm
				sendDirectMessage(commandArgs[0], commandArgs[1]);
				break;
			case EXIT:
				exitTheRoom();
				break;
			case INFO:
				getAndShowInfo();
				break;
			case PROMOTE:
				promote(commandArgs[0]);
				break;
			case KICK:
				kick(commandArgs[0]);
				break;
			case RENAME:
				renameRoom(commandArgs[0]);
				break;
			case SEND:
				sendChatMessage(commandArgs[0]);
				break;
			case SOCKET_IN:
				processIncomingMessage();
				break;
			default:
				System.err.println("* Received an inroom command from shell not listed in inRoomProcessing");
				break;
			}
		}
	}
	
	//Método para procesar los mensajes recibidos del servidor mientras que el shell estaba esperando un comando de usuario
	private void processIncomingMessage() throws IOException {
		NCMessage message = ncConnector.receiveMessage();
		// We treat different messages in a different manner
		switch(message.getType()){
		case NOTIFICATION:
			processNotification(message);
			break;
		case NEW_DM: 
			System.out.println(((NCSecretMessage)message).getUser() +" [DM]: "+((NCSecretMessage)message).getText());
			break;
		case NEW_MESSAGE:
			//(Example) If it's a new room message, we print the user and the message itself.
			System.out.println(((NCTextMessage)message).getUser() + ": " + ((NCTextMessage)message).getMessage());
			break;
		case KICKED:
			processKick();
			break;
		default:
			//TODO con nota explicativa de por qué se da el fallo, aunque no se vaya  a dar.
			System.err.println("* Received an unexpected type of incoming message");
			break;
		} 
	}
	
	// Método para registrar el nick del usuario en el servidor de NanoChat
	private void registerNickName(String name) throws IOException {
		if (ncConnector.registerNickname(name)) {
			System.out.println("* Your nickname is now " + name);
			// A successful register causes a change in the automaton state
			clientStatus = NCClientStatus.OUT_ROOM;
			return;
		}
		System.out.println("* The nickname is already registered. Try a different one.");
	}

	//Método que solicita al servidor de NanoChat la lista de salas e imprime el resultado obtenido
	private void getAndShowRooms() throws IOException {
		Collection<NCRoomDescription> descriptions = ncConnector.getRooms();
		System.out.println("* There are currently " + descriptions.size() + " rooms:");
		for (NCRoomDescription des : descriptions)
			System.out.println(des.toPrintableString());
	}
	
	// Process request of type create
	private void createRoom(String roomName) throws IOException {
		if (ncConnector.createRoom(roomName)) {
			System.out.println("* You created the room " + roomName + ". You are now inside.");
			clientStatus = NCClientStatus.IN_ROOM;
			return;
		}//TODO manejar casos que ofrece el servidor (nombre inválido, nombre repetido, etc)
		System.out.println("* You cannot create a room called " + roomName +". Try another.");
	}

	// Método para tramitar la solicitud de acceso del usuario a una sala concreta
	private void enterChat(String roomName) throws IOException {
		if (ncConnector.enterRoom(roomName)) {
			System.out.println("* You entered the room.");
			clientStatus = NCClientStatus.IN_ROOM;
			return;
		} //TODO manejar diferentes respuestas del servidor (como las de create)
		// Si la respuesta es un rechazo entonces informamos al usuario
		System.out.println("* The room doesn't exist or you cannot enter that room");
	}

	//Método para solicitar al servidor la información sobre una sala y para mostrarla por pantalla
	private void getAndShowInfo() throws IOException {
		NCRoomDescription des = ncConnector.getRoomInfo();
		System.out.println(des.toPrintableString());
	}
	
	private void processKick() {
		clientStatus = NCClientStatus.OUT_ROOM;
		System.out.println("* You were kicked out of this room");
	}

	//Método para notificar al servidor que salimos de la sala
	private void exitTheRoom() throws IOException {
		ncConnector.leaveRoom();
		clientStatus = NCClientStatus.OUT_ROOM;
		System.out.println("* You are now out of the room");
	}
	
	private void renameRoom(String newName) throws IOException {
		NCControlMessage serversAnswer = ncConnector.renameRoom(newName);	
		switch(serversAnswer.getType()) {
		case OK:
			System.out.println("* The room name was changed. You are now in the room " + newName);
			break;
		case DENIED:
			System.out.println("* You are not allowed to change this room's name");
			break;
		case REPEATED:
			System.out.println("* There's already a room with this name");
			break;
		case IMPOSSIBLE:
		default:
			System.out.println("* This room can't be renamed");
			break;
		}
	}
	
	private void promote(String user) throws IOException {
		NCControlMessage serversAnswer = ncConnector.promote(user);
		switch(serversAnswer.getType()) {
		case OK:
			System.out.println("* You made " + user + " a new holder of the KICKSWORD.");
			break;
		case DENIED:
			System.out.println("* You are not allowed to promote anyone to administrator in this room.");
			break;
		case REPEATED:
			System.out.println("* " + user + " is already an administrator.");
			break;
		default:
		case IMPOSSIBLE:
			System.out.println("* Users can't be promoted in this room.");
			break;
		}
	}
	
	private void kick(String user) throws IOException {
		NCControlMessage serversAnswer = ncConnector.kick(user);
		switch (serversAnswer.getType()) {
		case OK:
			System.out.println("* You kicked out " + user + " from the room");
			break;
		case DENIED:
			System.out.println("* The user " + user + " can't be kicked of the room or you are not allowed to kick users in this room");
			break;
		case IMPOSSIBLE:
		default:
			System.out.println("* " + user + " is not in the room or users cannot be kicked in this room");
			break;
		}
	}

	private void processNotification(NCMessage message) {
		String user = ((NCNotificationMessage) message).getUser();
		String object = ((NCNotificationMessage) message).getObject();
		NCMessageType action = ((NCNotificationMessage) message).getAction();
		switch (action) { 
			case ENTER:
				System.out.println("* " + user + " has entered the room");
				break;
			case EXIT:
				System.out.println("* " + user + " has left the room");
				break;
			case RENAME:
				System.out.println("* " + user + " changed the room name to " + object);
				break;
			case KICK:
				System.out.println("* " + user +" kicked out the pleb " + object);
				break;
			case PROMOTE:
				System.out.println("* " + object + " is now a holder of the KICKSWORD, thanks to " + user);
				break;
			default:
				System.err.println("* A wild, not yet implemented, notification appears, be aware!"); 
				break;
		}
	}
	
	//Método para enviar un mensaje al chat de la sala
	private void sendChatMessage(String chatMessage) throws IOException {
		ncConnector.sendBroadcast(chatMessage);
	}
	//Método para enviar un mensaje directo a un usuario registrado en el servidor
	private void sendDirectMessage(String receiver, String text) throws IOException { //TODO revisar jm
		NCControlMessage answer = (NCControlMessage) ncConnector.sendDirect(receiver, text);
		switch (answer.getType()) {
			case OK:
				break;
			case DENIED:
				System.out.println("* The user "+receiver+" is not logged into the server");
				break;
			case IMPOSSIBLE:
				System.out.println("* You can't do that ");
				break;
			default:
				System.out.println("* An error occurred while trying to contact with "+receiver);
				break;
		}
	}

	/**
	 * Tries to get a server address from a directory.
	 * This method must be called before attempting to connect to a chat server.
	 * @param directoryHostname The hostname of the directory.
	 * @return true if the operation was successful.
	 */
	public boolean getServerFromDirectory(String directoryHostname) {
		System.out.println("* Connecting to the directory...");
		try {
			directoryConnector = new DirectoryConnector(directoryHostname);
			serverAddress = directoryConnector.getServerForProtocol(PROTOCOL);
		} catch (IOException e) {
			serverAddress = null;
		} finally {
			if (directoryConnector != null) {
				directoryConnector.close();
			}
		}
		if (serverAddress == null) {
			System.out.println("* Check your connection, the directory is not available.");		
			return false;
		}
		System.out.println("* Got server from directory.");
		return true;
	}

	/**
	 * Connects to the chat server.
	 * This method must be called after the controller has a valid address
	 * (see getServerFromDirectory())
	 * @return true if the connection to the server was successful.
	 */
	public boolean connectToChatServer() {
		if (serverAddress == null) {
			System.err.println("* Usage: Call getServerFromDirectory() first.");
			return false;
		}
		try {
			ncConnector = new NCConnector(serverAddress);
		} catch (IOException e) {
			System.out.println("* Check your connection, the chat server is not available.");
			serverAddress = null;
		}
		
		if (serverAddress != null) {
			// If the connection was successful, we change the state of the automata to the starting state
			System.out.println("* Connected to " + serverAddress);
			clientStatus = NCClientStatus.PRE_REGISTRATION;
			return true;
		}
		return false;
	}
	

	public NCCommand getCurrentCommand() {		
		return currentCommand;
	}

	//Método que comprueba si el usuario ha introducido el comando para salir de la aplicación
	public boolean shouldQuit() {
		return currentCommand == NCCommand.QUIT;
	}

}
