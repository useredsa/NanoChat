package es.um.redes.nanoChat.client.comm;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Collections;

import es.um.redes.nanoChat.messageFV.InvalidFormat;
import es.um.redes.nanoChat.messageFV.NCMessageType;
import es.um.redes.nanoChat.messageFV.messages.*;
import es.um.redes.nanoChat.server.roomManager.NCRoomDescription;

//Esta clase proporciona la funcionalidad necesaria para intercambiar mensajes entre el cliente y el servidor de NanoChat
public class NCConnector {
	private Socket socket;
	protected DataOutputStream dos;
	protected DataInputStream dis;
	
	public NCConnector(InetSocketAddress serverAddress) throws UnknownHostException, IOException {
		// Se crea el socket a partir de la dirección proporcionada
		socket = new Socket(serverAddress.getAddress(), serverAddress.getPort());
		// Se extraen los streams de entrada y salida
		dos = new DataOutputStream(socket.getOutputStream());
		dis = new DataInputStream(socket.getInputStream());
	}

	/* Aclaración: //TODO
	 * Los métodos son todos prácticamente iguales. Por simplicidad
	 * comentamos qué hace el código del primero.
	 */
	
	/*
	 * Aclaración:
	 * El servidor no va a enviarnos mensajes con un formato inválido.
	 * No es un problema suponer que el servidor funciona correctamente
	 * y capturar las excepciones del tipo InvalidFormat al recibir un
	 * paquete.
	 * Con respecto al tratamiento, no hay que darle mucha importancia,
	 * porque suponemos que no se va a aplicar; pero en cada sitio se
	 * debería intentar devolver el valor que más cuadre si se diese un
	 * error.
	 */
	
	// Método para registrar el nick en el servidor. Nos informa sobre si la inscripción se hizo con éxito o no.
	public boolean registerNickname(String nick) throws IOException {
		//Funcionamiento resumido: SEND(nick) and RCV(NICK_OK) or RCV(NICK_DUPLICATED)
		//Creamos un mensaje de tipo RoomMessage con opcode OP_NICK en el que se inserte el nick
		NCRegisterMessage message = new NCRegisterMessage(nick);
		// Escribimos el mensaje parseado en el flujo de salida, es decir, provocamos que se envíe por la conexión TCP
		dos.writeUTF(message.encode());
		// Leemos el mensaje recibido como respuesta por el flujo de entrada
		try {
			NCMessage answer = NCMessage.readMessageFromSocket(dis);
			// Analizamos el mensaje para saber si está duplicado el nick (modificar el return en consecuencia)
			return answer.getType() == NCMessageType.OK;			
		} catch (InvalidFormat e) {
			// The server is not gonna send a message with an invalid format
			e.printStackTrace();
			return false;
		}
	}
	
	//Método para obtener la lista de salas del servidor
	public Collection<NCRoomDescription> getRooms() {
		//Funcionamiento resumido: SND(GET_ROOMS) and RCV(ROOM_LIST)
		NCControlMessage message = new NCControlMessage(NCMessageType.ROOMS_LIST);
		try {	//TODO ejemplo de manejo de errores, probablemente no se haga en el usuario, pero lo de implosions mola
			dos.writeUTF(message.encode());
			NCMessage answer = NCMessage.readMessageFromSocket(dis);
			return ((NCRoomListMessage) answer).getRoomsInfo();
		} catch (IOException e) {
			System.err.println("Problems writting or receiving a message.");
			e.printStackTrace();
			return Collections.emptyList();
		} catch (InvalidFormat e) {
			e.printStackTrace();
			return null;
		}
	}
	
	// Asks to create a room. If successful, automatically enters the room.
	public boolean createRoom(String name) throws IOException {
		NCCreateMessage mess = new NCCreateMessage(name);
		dos.writeUTF(mess.encode());
		try {
			NCMessage answer = NCMessage.readMessageFromSocket(dis);
			return answer.getType() == NCMessageType.OK;
		} catch (InvalidFormat e) {
			e.printStackTrace();
			return false;
		}
	}
	
	// Método para solicitar la entrada en una sala
	public boolean enterRoom(String room) throws IOException {
		// Funcionamiento resumido: SND(ENTER_ROOM<room>) and RCV(IN_ROOM) or RCV(REJECT)
		NCEnterMessage message = new NCEnterMessage(room);
		dos.writeUTF(message.encode());
		try {
			NCMessage answer = NCMessage.readMessageFromSocket(dis);
			return answer.getType() == NCMessageType.OK;
		} catch (InvalidFormat e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public NCControlMessage renameRoom(String newName) throws IOException {
		NCRenameMessage mess = new NCRenameMessage(newName);
		dos.writeUTF(mess.encode());
		try {
			return (NCControlMessage) NCMessage.readMessageFromSocket(dis);
		} catch (InvalidFormat e) {
			e.printStackTrace();
			return new NCControlMessage(NCMessageType.IMPOSSIBLE);
		}
	}
	
	public NCControlMessage promote(String user) throws IOException {
		NCPromoteMessage mess = new NCPromoteMessage(user);
		try {
			return (NCControlMessage) NCMessage.readMessageFromSocket(dis);
		} catch (InvalidFormat e) {
			e.printStackTrace();
			return new NCControlMessage(NCMessageType.IMPOSSIBLE);
		}
	}
	
	public NCControlMessage kick(String user) throws IOException {
		NCKickMessage mess = new NCKickMessage(user);
		dos.writeUTF(mess.encode());
		try {
			return (NCControlMessage) NCMessage.readMessageFromSocket(dis);
		} catch (InvalidFormat e) {
			e.printStackTrace();
			return new NCControlMessage(NCMessageType.IMPOSSIBLE);
		}
	}
	
	//Método para salir de una sala
	public void leaveRoom() throws IOException {
		//Funcionamiento resumido: SND(EXIT_ROOM)
		NCControlMessage exitMess = new NCControlMessage(NCMessageType.EXIT); 
		dos.writeUTF(exitMess.encode());
	}
	
	//Método que utiliza el Shell para ver si hay datos en el flujo de entrada
	public boolean isDataAvailable() throws IOException {
		return (dis.available() != 0);
	}
	
	//IMPORTANTE!!
	//Es necesario implementar métodos para recibir y enviar mensajes de chat a una sala
	//TODO revisar jm
	//Metodo para mandar un DM a una persona de la sala de chat
	//emilio: no sera mejor llamarlo con dm?
	public void sendMessage(String receiver, String text) throws IOException{
		//dos.writeUTF(new NCDMMessage(NCMessageOp.DM, receiver, text).encode()); //TODO mensaje dm no implementado
	}
	//Metodo para mandar un mensaje a toda la sala de chat
	public void sendBroadcastMessage(String text) throws IOException{ //TODO me gusta la palabra broadcast o roomMessage, probablemente cambie el send
		NCSendMessage mess = new NCSendMessage(text); 
		dos.writeUTF(mess.encode());
	}
	
	//Metodo para recibir mensajes de chat de una sala
	public NCMessage receiveMessage() throws IOException { 
		try {
			return NCMessage.readMessageFromSocket(dis);
		} catch (InvalidFormat e) { 
			e.printStackTrace();
			return null; //TODO ver si cambiar
		}
	}
	
	//Método para pedir la descripción de una sala
	public NCRoomDescription getRoomInfo() throws IOException {
		//Funcionamiento resumido: SND(GET_ROOMINFO) and RCV(ROOMINFO)
		// Construimos el mensaje de solicitud de información de la sala específica
		NCControlMessage request = new NCControlMessage(NCMessageType.INFO);
		dos.writeUTF(request.encode());
		// Recibimos el mensaje de respuesta
		try {
			NCMessage answer = NCMessage.readMessageFromSocket(dis);
			// Devolvemos la descripción contenida en el mensaje
			return ((NCRoomInfoMessage) answer).getRoomDescription();
		} catch (InvalidFormat e) {
			e.printStackTrace();
			return NCRoomDescription.invalidDescription();
		}
	}
	
	// Método para cerrar la comunicación con la sala
	// (Opcional) Enviar un mensaje de salida del servidor de Chat
	public void disconnect() {
		try {
			dos.writeUTF(new NCControlMessage(NCMessageType.QUIT).encode());
		} catch (IOException e1) {
			System.out.println("* There was an error disconnecting from the server");
			e1.printStackTrace();
		}
		try {
			if (socket != null) {
				socket.close();
			}
		} catch (IOException e) {
		} finally {
			socket = null; //TODO no tengo muy claro que esto sea lo que haya que hacer 
		}
	}


}
