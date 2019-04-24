package es.um.redes.nanoChat.client.application;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collection;

import es.um.redes.nanoChat.client.comm.NCConnector;
import es.um.redes.nanoChat.client.shell.NCCommands;
import es.um.redes.nanoChat.client.shell.NCShell;
import es.um.redes.nanoChat.directory.connector.DirectoryConnector;
import es.um.redes.nanoChat.messageFV.messages.NCControlMessage;
import es.um.redes.nanoChat.messageFV.messages.NCMessage;
import es.um.redes.nanoChat.messageFV.messages.NCTextMessage;
import es.um.redes.nanoChat.server.roomManager.NCRoomDescription;

public class NCController {
	private static final int PROTOCOL = 163;		// Código de protocolo implementado por este cliente
	private DirectoryConnector directoryConnector;	// Conector para enviar y recibir mensajes del directorio
	private NCConnector ncConnector;				// Conector para enviar y recibir mensajes con el servidor de NanoChat
	private NCShell shell;							// Shell para leer comandos de usuario de la entrada estándar
	private NCCommands currentCommand;				// Último comando proporcionado por el usuario
	private String nickname;						// Nick del usuario (si ya se ha registrado)
	private String room;							// Sala de chat en la que se encuentra el usuario (si está en alguna)
	//private String chatMessage;						// Mensaje enviado o por enviar al chat
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
		while (!shouldQuit()) {
			//Pedimos el comando al shell
			shell.readGeneralCommand();
			//Establecemos que el comando actual es el que ha obtenido el shell
			currentCommand = shell.getCommand();
			//Analizamos los posibles parámetros asociados al comando
			String[] commandArgs = shell.getCommandArguments();
			// Process a command (status PRE_CONNECTION)
			switch (currentCommand) {
			case NICK:
				registerNickName(commandArgs[0]);
			case QUIT:
				break;
			default:
				System.out.println("* You can't perforom that action, you must register first");
			}
			// Change of state:
			if (clientStatus == NCClientStatus.OUT_ROOM)
				outRoomProcessing();
		}
		
		//Cuando salimos tenemos que cerrar todas las conexiones y sockets abiertos
		ncConnector.disconnect();	//TODO revisar jm		
		directoryConnector.close();
	}
	
	public void outRoomProcessing() {
		while (!shouldQuit()) {
			shell.readGeneralCommand();
			currentCommand = shell.getCommand();
			String[] commandArgs = shell.getCommandArguments();
			try {
				switch (currentCommand) {
				case CREATE:
					createRoom(commandArgs[0]);
					break;
				//case DM: //TODO
					
				case ENTER:
					enterChat(commandArgs[0]);
					break;
				//case RENAME: //TODO	
					
				case ROOMLIST:
					getAndShowRooms();
					break;
				case QUIT:
					break;
				default:
					System.out.println("* You can't use that command from outside a room");
				}
				// Possible change of state:
				if (clientStatus == NCClientStatus.IN_ROOM)
					inRoomProcessing();
			} catch (IOException e) {
				//TODO cortar comunicación or whatever
				System.err.println("There was an error with the connection.");
			}
		}
	}
	
	public void inRoomProcessing() {
		// Return condition: 
		while (!shouldQuit() && clientStatus == NCClientStatus.IN_ROOM) {
			shell.readChatCommand(ncConnector);
			currentCommand = shell.getCommand();
			String[] commandArgs = shell.getCommandArguments();
			try {
				switch (currentCommand) {
				//case DM: //TODO
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
					processIncommingMessage();
					break;
				default:
					System.out.println("* You can't use that command inside a room");
					break;
				}
			} catch (IOException e) {
				//TODO cortar comunicación or whatever
				System.err.println("* There was an error with the connection");
			}
		}
	}
	
	//Método para procesar los mensajes recibidos del servidor mientras que el shell estaba esperando un comando de usuario
	@SuppressWarnings("incomplete-switch")
	private void processIncommingMessage() {				
		try {
			//Recibir el mensaje
			NCMessage message = ncConnector.receiveMessage();
			//TODO En función del tipo de mensaje, actuar en consecuencia
			switch(message.getType()){
			case NEW_DM: //TODO DM
				//System.out.println(((NCTextMessage)message).getUser()+" [DM]: "+((NCTextMessage)message).getText());
			//(Ejemplo) En el caso de que fuera un mensaje de chat de broadcast mostramos la información de quién envía el mensaje y el mensaje en sí
			case NEW_MESSAGE:
				System.out.println(((NCTextMessage)message).getUser()+": "+((NCTextMessage)message).getMessage()); //TODO Haz una conversión, no?
			case KICKED:
				processKick();
			} //TODO default?
		} catch (IOException e) {
			System.out.println("* There was an error receiving a new message");
			e.printStackTrace();
		}		
	}

	// Método para registrar el nick del usuario en el servidor de NanoChat
	private void registerNickName(String name) {
		try {
			// Pedimos que se registre el nick (se comprobará si está duplicado)
			if (ncConnector.registerNickname(name)) {
				// Si el registro fue exitoso pasamos al siguiente estado del autómata
				nickname = name;
				System.out.println("* Your nickname is now " + nickname);
				clientStatus = NCClientStatus.OUT_ROOM;
				return;
			}
			//En este caso el nick ya existía
			System.out.println("* The nickname is already registered. Try a different one.");			
		} catch (IOException e) {
			System.out.println("* There was an error registering the nickname");
			//e.printStackTrace();
		}
	}

	//Método que solicita al servidor de NanoChat la lista de salas e imprime el resultado obtenido
	private void getAndShowRooms() {
		// Le pedimos al conector que obtenga la lista de salas ncConnector.getRooms()
		Collection<NCRoomDescription> descriptions = ncConnector.getRooms();
		// Una vez recibidas iteramos sobre la lista para imprimir información de cada sala
		System.out.println("* There are currently " + descriptions.size() + " rooms:");
		for (NCRoomDescription des : descriptions)
			System.out.println(des.toPrintableString());
	}
	
	// Process request of type create
	private void createRoom(String roomName) throws IOException {
		if (ncConnector.createRoom(roomName)) {
			room = roomName;
			System.out.println("* You created the room " + room + ". You are now inside.");
			clientStatus = NCClientStatus.IN_ROOM;
			return;
		}
		System.out.println("* You cannot create a room called " + room); //TODO manejar casos nombre repetido y demás
	}

	// Método para tramitar la solicitud de acceso del usuario a una sala concreta
	private void enterChat(String roomName) throws IOException {
		// Se solicita al servidor la entrada en la sala correspondiente ncConnector.enterRoom()
		if (ncConnector.enterRoom(roomName)) {
			room = roomName;
			// En caso contrario informamos que estamos dentro y seguimos
			System.out.println("* You entered the room.");
			// Cambiamos el estado del autómata para aceptar nuevos comandos
			clientStatus = NCClientStatus.IN_ROOM;
			return;
		}
		// Si la respuesta es un rechazo entonces informamos al usuario
		System.out.println("* The room doesn't exist or you cannot enter that room"); //TODO consider put smt if banned?
	}

	//Método para solicitar al servidor la información sobre una sala y para mostrarla por pantalla
	private void getAndShowInfo() throws IOException {
		// Pedimos al servidor información sobre la sala en concreto
		NCRoomDescription des = ncConnector.getRoomInfo();
		// Mostramos por pantalla la información
		System.out.println(des.toPrintableString());
	}
	
	private void processKick() {
		room = null;
		clientStatus = NCClientStatus.OUT_ROOM;
		System.out.println("* You were kicked out of this room");
	}

	//Método para notificar al servidor que salimos de la sala
	private void exitTheRoom() throws IOException {
		// Mandamos al servidor el mensaje de salida
		ncConnector.leaveRoom();
		room = null;
		clientStatus = NCClientStatus.OUT_ROOM;
		System.out.println("* You are out of the room");
	}
	
	private void renameRoom(String newName) throws IOException {
		NCControlMessage serversAnswer = ncConnector.renameRoom(newName);	
		switch(serversAnswer.getType()) {
		case OK:
			room = newName;
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
			System.out.println("* You made " + user + " an administrator.");
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

	//Método para enviar un mensaje al chat de la sala
	private void sendChatMessage(String chatMessage) {
		//Mandamos al servidor un mensaje de chat //TODO revisar jm
		try {
			ncConnector.sendBroadcastMessage(chatMessage);
		} catch (IOException e) {
			System.out.println("* There was an error sending your message");
			//e.printStackTrace();
		}
	}

	// Método para obtener el servidor de NanoChat que nos proporcione el directorio
	public boolean getServerFromDirectory(String directoryHostname) {
		//Inicializamos el conector con el directorio y el shell
		System.out.println("* Connecting to the directory...");
		//Intentamos obtener la dirección del servidor de NanoChat que trabaja con nuestro protocolo
		try {
			directoryConnector = new DirectoryConnector(directoryHostname);
			serverAddress = directoryConnector.getServerForProtocol(PROTOCOL);
		} catch (IOException e1) {
			serverAddress = null;
		}
		//Si no hemos recibido la dirección entonces nos quedan menos intentos
		if (serverAddress == null) {
			System.out.println("* Check your connection, the directory is not available.");		
			return false;
		}
		return true;
	}
	
	//Método para establecer la conexión con el servidor de Chat (a través del NCConnector)
	public boolean connectToChatServer() {
		if (serverAddress == null) {
			System.err.println("* Usage: Call getServerFromDirectory() first.");
			return false;
		}
		try {
			//Inicializamos el conector para intercambiar mensajes con el servidor de NanoChat (lo hace la clase NCConnector)
			ncConnector = new NCConnector(serverAddress);
		} catch (IOException e) {
			System.out.println("* Check your connection, the chat server is not available.");
			serverAddress = null;
		}
		//Si la conexión se ha establecido con éxito informamos al usuario y cambiamos el estado del autómata
		if (serverAddress != null) {
			System.out.println("* Connected to "+serverAddress);
			clientStatus = NCClientStatus.PRE_REGISTRATION;
			return true;
		}
		return false;
	}
	

	//Devuelve el comando actual introducido por el usuario
	public NCCommands getCurrentCommand() {		
		return currentCommand;
	}

	//Método que comprueba si el usuario ha introducido el comando para salir de la aplicación
	public boolean shouldQuit() {
		return currentCommand == NCCommands.QUIT;
	}

}
