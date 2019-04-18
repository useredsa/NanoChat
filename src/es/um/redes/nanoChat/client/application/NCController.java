package es.um.redes.nanoChat.client.application;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;

import es.um.redes.nanoChat.client.comm.NCConnector;
import es.um.redes.nanoChat.client.shell.NCCommands;
import es.um.redes.nanoChat.client.shell.NCShell;
import es.um.redes.nanoChat.directory.connector.DirectoryConnector;
import es.um.redes.nanoChat.messageFV.NCMessage;
import es.um.redes.nanoChat.messageFV.NCMessageOp;
import es.um.redes.nanoChat.messageFV.NCTextMessage;
import es.um.redes.nanoChat.server.roomManager.NCRoomDescription;

public class NCController {
	//Diferentes estados del cliente de acuerdo con el autómata
	private static final byte PRE_CONNECTION = 1;
	private static final byte PRE_REGISTRATION = 2;
	private static final byte OUT_ROOM = 3;
	private static final byte IN_ROOM = 4;
	//Código de protocolo implementado por este cliente
	//TODO Cambiar para cada grupo
	private static final int PROTOCOL = 163;
	//Conector para enviar y recibir mensajes del directorio
	private DirectoryConnector directoryConnector;
	//Conector para enviar y recibir mensajes con el servidor de NanoChat
	private NCConnector ncConnector;
	//Shell para leer comandos de usuario de la entrada estándar
	private NCShell shell;
	//Último comando proporcionado por el usuario
	private NCCommands currentCommand;
	//Nick del usuario
	private String nickname;
	//Sala de chat en la que se encuentra el usuario (si está en alguna)
	private String room;
	//Mensaje enviado o por enviar al chat
	private String chatMessage;
	//Dirección de internet del servidor de NanoChat
	private InetSocketAddress serverAddress;
	//Estado actual del cliente, de acuerdo con el autómata
	private byte clientStatus = PRE_CONNECTION;

	//Constructor
	public NCController() {
		shell = new NCShell();
	}

	//Devuelve el comando actual introducido por el usuario
	public NCCommands getCurrentCommand() {		
		return currentCommand;
	}

	//Establece el comando actual
	public void setCurrentCommand(NCCommands command) {
		currentCommand = command;
	}

	//Registra en atributos internos los posibles parámetros del comando tecleado por el usuario
	public void setCurrentCommandArguments(String[] args) {
		//Comprobaremos también si el comando es válido para el estado actual del autómata //TODO
		switch (currentCommand) {
		case NICK:
			if (clientStatus == PRE_REGISTRATION)
				nickname = args[0];
			break;
		case CREATE:
		case ENTER:
			room = args[0];
			break;
		case SEND:
			chatMessage = args[0];
			break;
		default:
		}
	}

	//Procesa los comandos introducidos por un usuario que aún no está dentro de una sala
	public void processCommand() {
		switch (currentCommand) {
		case NICK:
			if (clientStatus == PRE_REGISTRATION)
				registerNickName();
			else
				System.out.println("* You have already registered a nickname ("+nickname+")");
			break;
		case ROOMLIST:
			// LLamar a getAndShowRooms() si el estado actual del autómata lo permite
			if(clientStatus == OUT_ROOM) //TODO se supone que ya estas fuera, no?
				getAndShowRooms();
			else // O informarle que no le está permitido
				System.out.println("* You can't have the rooms right now");
			break;
		
		case CREATE:
			if (clientStatus == OUT_ROOM)
				createRoom();
			else
				System.out.println("You can only create a room in the chats room.");
			break;
			
		case ENTER:
			// LLamar a enterChat() si el estado actual del autómata lo permite
			if  (clientStatus == OUT_ROOM)
				enterChat();
			else // Si no está permitido informar al usuario
				System.out.println("You can't enter a room now.");
			break;
		//case RENAME: //TODO	
			
		case QUIT:
			//Cuando salimos tenemos que cerrar todas las conexiones y sockets abiertos
			ncConnector.disconnect();	//TODO revisar jm		
			directoryConnector.close();
			break;
		default:
			System.out.println("* You can't use that command here"); //TODO revisar
		}
	}
	
	// Método para registrar el nick del usuario en el servidor de NanoChat
	private void registerNickName() {
		try {
			// Pedimos que se registre el nick (se comprobará si está duplicado)
			boolean registered = ncConnector.registerNickname(nickname); 
			if (registered) {
				// Si el registro fue exitoso pasamos al siguiente estado del autómata
				System.out.println("* Your nickname is now "+nickname);
				clientStatus = OUT_ROOM;
			}
			else
				//En este caso el nick ya existía
				System.out.println("* The nickname is already registered. Try a different one.");			
		} catch (IOException e) {
			System.out.println("* There was an error registering the nickname");
		}
	}

	//Método que solicita al servidor de NanoChat la lista de salas e imprime el resultado obtenido
	private void getAndShowRooms() {
		// Le pedimos al conector que obtenga la lista de salas ncConnector.getRooms()
		ArrayList<NCRoomDescription> descriptions = ncConnector.getRooms(); //TODO check null
		// Una vez recibidas iteramos sobre la lista para imprimir información de cada sala
		System.out.println("There are currently " + descriptions.size() + " rooms:");
		for (NCRoomDescription des : descriptions) {
			System.out.println(des.toPrintableString());
		}
	}
	
	// Process request of type create
	private void createRoom() {
		try {
			boolean success = ncConnector.createRoom(room);
			if (!success) {
				System.out.println("You couldn't create a room called " + room); //TODO manejar casos nombre repetido y demás
				return;
			}
			
			System.out.println("You created the room " + room + ". You are now inside.");
			clientStatus = IN_ROOM;
		} catch (IOException e) {
			System.err.println("Something went wrong while creating a room"); //TODO especificar qué
			e.printStackTrace();
		}
		
		//TODO copiado de abajo, refactorizar
		do {
			readRoomCommandFromShell();
			processRoomCommand();
		} while (currentCommand != NCCommands.EXIT);
	}

	// Método para tramitar la solicitud de acceso del usuario a una sala concreta
	private void enterChat() {
		try {
			// Se solicita al servidor la entrada en la sala correspondiente ncConnector.enterRoom()
			boolean success = ncConnector.enterRoom(room);
			// Si la respuesta es un rechazo entonces informamos al usuario y salimos
			if (!success) {
				System.out.println("You could not enter this room"); //TODO consider put smt if banned?
				return;
			}
			// En caso contrario informamos que estamos dentro y seguimos
			System.out.println("You entered the room.");
			// Cambiamos el estado del autómata para aceptar nuevos comandos
			clientStatus = IN_ROOM;
		} catch (IOException e) {
			System.err.println("Something went wrong while entering a room");
			e.printStackTrace();
		}
		
		do {
			//Pasamos a aceptar sólo los comandos que son válidos dentro de una sala
			readRoomCommandFromShell();
			processRoomCommand();
		} while (currentCommand != NCCommands.EXIT);
	}

	//Método para procesar los comandos específicos de una sala
	private void processRoomCommand() {
		switch (currentCommand) {
		case ROOMINFO:
			//El usuario ha solicitado información sobre la sala y llamamos al método que la obtendrá
			getAndShowInfo();
			break;
		//case DM: //TODO
		case SEND:
			//El usuario quiere enviar un mensaje al chat de la sala
			sendChatMessage();
			break;
		
		case SOCKET_IN:
			//En este caso lo que ha sucedido es que hemos recibido un mensaje desde la sala y hay que procesarlo
			processIncommingMessage();
			break;
		case EXIT:
			//El usuario quiere salir de la sala
			exitTheRoom();
		}		
	}

	//Método para solicitar al servidor la información sobre una sala y para mostrarla por pantalla
	private void getAndShowInfo() {
		//TODO Pedimos al servidor información sobre la sala en concreto
		//TODO Mostramos por pantalla la información
	}

	//Método para notificar al servidor que salimos de la sala
	private void exitTheRoom() {
		// Mandamos al servidor el mensaje de salida
		try {
			ncConnector.leaveRoom();
		} catch (IOException e) {
			// TODO  (Ver por qué pasa)
			e.printStackTrace();
		}
		clientStatus = OUT_ROOM;
		System.out.println("* You are out of the room");
	}

	//Método para enviar un mensaje al chat de la sala
	private void sendChatMessage() {
		//Mandamos al servidor un mensaje de chat //TODO revisar jm
		try {
			ncConnector.sendBroadcastMessage(chatMessage);
		} catch (IOException e) {
			System.out.println("* Your message wasn't sent");
			//e.printStackTrace();
		}
	}

	//Método para procesar los mensajes recibidos del servidor mientras que el shell estaba esperando un comando de usuario
	private void processIncommingMessage() {		
		//Recibir el mensaje
		NCMessage message = ncConnector.receiveMessage();
		//TODO En función del tipo de mensaje, actuar en consecuencia
		switch(message.getOp()){
			case NEW_DM:
				System.out.println(((NCTextMessage)message).getUser()+" [DM]: "+((NCTextMessage)message).getText());
			//(Ejemplo) En el caso de que fuera un mensaje de chat de broadcast mostramos la información de quién envía el mensaje y el mensaje en sí
			case NEW_MESSAGE:
				System.out.println(((NCTextMessage)message).getUser()+": "+((NCTextMessage)message).getText());
			//case KICKED:
			//case INFO: //TODO cual era el NCMessageOp que devolvia un INFO de la sala?
		}
		
	}

	//MNétodo para leer un comando de la sala 
	public void readRoomCommandFromShell() {
		// Pedimos un nuevo comando de sala al shell (pasando el conector por si nos llega un mensaje entrante)
		shell.readChatCommand(ncConnector);
		// Establecemos el comando tecleado (o el mensaje recibido) como comando actual
		setCurrentCommand(shell.getCommand());
		// Procesamos los posibles parámetros (si los hubiera)
		setCurrentCommandArguments(shell.getCommandArguments());
	}

	//Método para leer un comando general (fuera de una sala)
	public void readGeneralCommandFromShell() {
		//Pedimos el comando al shell
		shell.readGeneralCommand();
		//Establecemos que el comando actual es el que ha obtenido el shell
		setCurrentCommand(shell.getCommand());
		//Analizamos los posibles parámetros asociados al comando
		setCurrentCommandArguments(shell.getCommandArguments());
	}

	//Método para obtener el servidor de NanoChat que nos proporcione el directorio
	public boolean getServerFromDirectory(String directoryHostname) {
		//Inicializamos el conector con el directorio y el shell
		System.out.println("* Connecting to the directory...");
		//Intentamos obtener la dirección del servidor de NanoChat que trabaja con nuestro protocolo
		try {
			directoryConnector = new DirectoryConnector(directoryHostname);
			serverAddress = directoryConnector.getServerForProtocol(PROTOCOL);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			serverAddress = null;
		}
		//Si no hemos recibido la dirección entonces nos quedan menos intentos
		if (serverAddress == null) {
			System.out.println("* Check your connection, the directory is not available.");		
			return false;
		}
		else return true;
	}
	
	//Método para establecer la conexión con el servidor de Chat (a través del NCConnector)
	public boolean connectToChatServer() {
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
				clientStatus = PRE_REGISTRATION;
				return true;
			}
			else return false;
	}

	//Método que comprueba si el usuario ha introducido el comando para salir de la aplicación
	public boolean shouldQuit() {
		return currentCommand == NCCommands.QUIT;
	}

}
