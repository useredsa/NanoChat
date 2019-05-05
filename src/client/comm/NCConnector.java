package client.comm;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collection;

import messageFV.NCMessageType;
import messageFV.messages.*;
import server.rooms.NCRoomDescription;

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

	/* Aclaración:
	 * Los métodos son todos prácticamente iguales. Por simplicidad,
	 * los últimos no están comentados, a no ser que haya
	 * algún detalle importante.
	 */
	
	/*
	 * Aclaración:
	 * El servidor no va a enviarnos mensajes con un formato inválido.
	 * No es un problema suponer que el servidor funciona correctamente
	 * y obviar las excepciones de tipo InvalidFormat.
	 * Para ello, utilizamos NCMessage.readFromSocketNoChecks(dis);
	 */
	
	/*
	 * Aclaración:
	 * Tal como está, falta manejar el caso en el que se reciben mensajes
	 * de sala mientras se espera la respuesta a un mensaje de otro tipo.
	 * En ese caso, se debería encolar esos mensajes en una cola aparte 
	 * para procesarlos más tarde, y actualizar los métodos de consulta de
	 * mensajes disponibles y de obtener un mensaje nuevo.
	 */
	
	// Método para registrar el nick en el servidor. Nos informa sobre si la inscripción se hizo con éxito o no.
	public boolean registerNickname(String nick) throws IOException {
		//Funcionamiento resumido: SEND(nick) and RCV(NICK_OK) or RCV(NICK_DUPLICATED)
		//Creamos un mensaje de tipo RoomMessage con opcode OP_NICK en el que se inserte el nick
		NCRegisterMessage message = new NCRegisterMessage(nick);
		// Escribimos el mensaje parseado en el flujo de salida, es decir, provocamos que se envíe por la conexión TCP
		dos.writeUTF(message.encode());
		// Leemos el mensaje recibido como respuesta por el flujo de entrada
		NCMessage answer = NCMessage.readFromSocketNoChecks(dis);
		// Analizamos el mensaje para saber si está duplicado el nick (modificar el return en consecuencia)
		return answer.getType() == NCMessageType.OK;
	}
	
	//Método para obtener la lista de salas del servidor
	public Collection<NCRoomDescription> getRooms() throws IOException {
		//Funcionamiento resumido: SND(GET_ROOMS) and RCV(ROOM_LIST)
		NCControlMessage message = new NCControlMessage(NCMessageType.ROOMS_LIST);
		dos.writeUTF(message.encode());
		NCMessage answer = NCMessage.readFromSocketNoChecks(dis);
		return ((NCRoomListMessage) answer).getRoomsInfo();
	}
	
	// Asks to create a room. If successful, automatically enters the room.
	public boolean createRoom(String name) throws IOException {
		NCCreateMessage mess = new NCCreateMessage(name);
		dos.writeUTF(mess.encode());
		NCMessage answer = NCMessage.readFromSocketNoChecks(dis);
		return answer.getType() == NCMessageType.OK;
	}
	
	// Método para solicitar la entrada en una sala
	public boolean enterRoom(String room) throws IOException {
		// Funcionamiento resumido: SND(ENTER_ROOM<room>) and RCV(IN_ROOM) or RCV(REJECT)
		NCEnterMessage message = new NCEnterMessage(room);
		dos.writeUTF(message.encode());
		NCMessage answer = NCMessage.readFromSocketNoChecks(dis);
		return answer.getType() == NCMessageType.OK;
	}
	
	public NCControlMessage renameRoom(String newName) throws IOException {
		NCRenameMessage mess = new NCRenameMessage(newName);
		dos.writeUTF(mess.encode());
		return (NCControlMessage) NCMessage.readFromSocketNoChecks(dis);
	}
	
	public NCControlMessage promote(String user) throws IOException {
		NCPromoteMessage mess = new NCPromoteMessage(user);
		dos.writeUTF(mess.encode());
		return (NCControlMessage) NCMessage.readFromSocketNoChecks(dis);
	}
	
	public NCControlMessage kick(String user) throws IOException {
		NCKickMessage mess = new NCKickMessage(user);
		dos.writeUTF(mess.encode());
		return (NCControlMessage) NCMessage.readFromSocketNoChecks(dis);
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
	
	//Metodo para mandar un DM a una persona 
	public NCControlMessage sendDirect(String receiver, String message) throws IOException {
		NCDirectMessage send = new NCDirectMessage(receiver, message);
		dos.writeUTF(send.encode());
		return (NCControlMessage) NCMessage.readFromSocketNoChecks(dis);
	}
	//Metodo para mandar un mensaje a toda la sala de chat
	public void sendBroadcast(String text) throws IOException{
		NCSendMessage mess = new NCSendMessage(text); 
		dos.writeUTF(mess.encode());
	}
	
	//Metodo para recibir mensajes de chat de una sala
	public NCMessage receiveMessage() throws IOException { 
		return NCMessage.readFromSocketNoChecks(dis);
	}
	
	//Método para pedir la descripción de una sala
	public NCRoomDescription getRoomInfo() throws IOException {
		//Funcionamiento resumido: SND(GET_ROOMINFO) and RCV(ROOMINFO)
		// Construimos el mensaje de solicitud de información de la sala específica
		NCControlMessage request = new NCControlMessage(NCMessageType.INFO);
		dos.writeUTF(request.encode());
		// Recibimos el mensaje de respuesta
		NCMessage answer = NCMessage.readFromSocketNoChecks(dis);
		// Devolvemos la descripción contenida en el mensaje
		return ((NCRoomInfoMessage) answer).getRoomDescription();
	}
	
	// Método para cerrar la comunicación con la sala
	// (Opcional) Enviar un mensaje de salida del servidor de Chat
	public void disconnect() {
		try {
			dos.writeUTF(new NCControlMessage(NCMessageType.QUIT).encode());
		} catch (IOException e1) {
			System.out.println("* There was an error while you were being disconnected from the server");
			e1.printStackTrace();
		}
		try {
			if (socket != null) {
				socket.close();
			}
		} catch (IOException e) {
		} finally {
			socket = null; 
		}
	}


}
