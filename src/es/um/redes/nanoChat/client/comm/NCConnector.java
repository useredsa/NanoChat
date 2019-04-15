package es.um.redes.nanoChat.client.comm;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import es.um.redes.nanoChat.messageFV.NCControlMessage;
import es.um.redes.nanoChat.messageFV.NCInfoMessage;
import es.um.redes.nanoChat.messageFV.NCMessage;
import es.um.redes.nanoChat.messageFV.NCMessageOp;
import es.um.redes.nanoChat.messageFV.NCNameMessage;
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


	// Método para registrar el nick en el servidor. Nos informa sobre si la inscripción se hizo con éxito o no.
	public boolean registerNickname_UnformattedMessage(String nick) throws IOException {
		// Funcionamiento resumido: SEND(nick) and RCV(NICK_OK) or RCV(NICK_DUPLICATED)
		// Enviamos una cadena con el nick por el flujo de salida
		dos.writeUTF(nick);
		// Leemos la cadena recibida como respuesta por el flujo de entrada
		String answer = dis.readUTF();
		// Si la cadena recibida es NICK_OK entonces no está duplicado (en función de ello modificar el return)
		return answer.equals("NICK_OK");
	}

	/* Aclaración:
	 * Los métodos son todos prácticamente iguales. Por simplicidad
	 * comentamos qué hace el código del primero.
	 */
	
	// Método para registrar el nick en el servidor. Nos informa sobre si la inscripción se hizo con éxito o no.
	public boolean registerNickname(String nick) throws IOException {
		//Funcionamiento resumido: SEND(nick) and RCV(NICK_OK) or RCV(NICK_DUPLICATED)
		//Creamos un mensaje de tipo RoomMessage con opcode OP_NICK en el que se inserte el nick
		NCNameMessage message = (NCNameMessage) NCMessage.makeNameMessage(NCMessageOp.REGISTER, nick);
		// Escribimos el mensaje parseado en el flujo de salida, es decir, provocamos que se envíe por la conexión TCP
		dos.writeUTF(message.toEncodedString());
		// Leemos el mensaje recibido como respuesta por el flujo de entrada
		NCMessage answer = NCMessage.readMessageFromSocket(dis);
		// Analizamos el mensaje para saber si está duplicado el nick (modificar el return en consecuencia)
		return answer.getOp() == NCMessageOp.OK;			
	}
	
	//Método para obtener la lista de salas del servidor
	public ArrayList<NCRoomDescription> getRooms() {
		//Funcionamiento resumido: SND(GET_ROOMS) and RCV(ROOM_LIST)
		NCControlMessage message = new NCControlMessage(NCMessageOp.ROOMS_LIST); //TODO ver por qué los mensajes de name se hacen así y otros no
		try {
			dos.writeUTF(message.toEncodedString());
			NCMessage answer = NCMessage.readMessageFromSocket(dis);
			if (answer.getOp() != NCMessageOp.ROOMS_INFO) {
				// Manejo de errores
				System.err.println("I expected room_list but received implosion");
				return null;
			}
			return ((NCInfoMessage) answer).getRooms();
		} catch (IOException e) {
			System.err.println("Problems writting or receiving a message. Got Exception: " + e);
			e.printStackTrace();
		}
		return null;
	}
	
	// Asks to create a room. If successful, automatically enters the room.
	public boolean createRoom(String name) throws IOException {
		NCNameMessage mess = new NCNameMessage(NCMessageOp.CREATE, name);
		dos.writeUTF(mess.toEncodedString());
		NCMessage answer = NCMessage.readMessageFromSocket(dis);
		return answer.getOp() == NCMessageOp.OK;
	}
	
	// Método para solicitar la entrada en una sala
	public boolean enterRoom(String room) throws IOException {
		// Funcionamiento resumido: SND(ENTER_ROOM<room>) and RCV(IN_ROOM) or RCV(REJECT)
		NCNameMessage message = new NCNameMessage(NCMessageOp.ENTER, room);
		dos.writeUTF(message.toEncodedString());
		NCMessage answer = NCMessage.readMessageFromSocket(dis);
		return answer.getOp() == NCMessageOp.OK;
	}
	
	//Método para salir de una sala
	public void leaveRoom(String room) throws IOException {
		//Funcionamiento resumido: SND(EXIT_ROOM)
		//TODO completar el método
	}
	
	//Método que utiliza el Shell para ver si hay datos en el flujo de entrada
	public boolean isDataAvailable() throws IOException {
		return (dis.available() != 0);
	}
	
	//IMPORTANTE!!
	//TODO Es necesario implementar métodos para recibir y enviar mensajes de chat a una sala
	
	//Método para pedir la descripción de una sala
	public NCRoomDescription getRoomInfo(String room) throws IOException {
		//Funcionamiento resumido: SND(GET_ROOMINFO) and RCV(ROOMINFO)
		//TODO Construimos el mensaje de solicitud de información de la sala específica
		//TODO Recibimos el mensaje de respuesta
		//TODO Devolvemos la descripción contenida en el mensaje
		return null;
	}
	
	//Método para cerrar la comunicación con la sala
	//TODO (Opcional) Enviar un mensaje de salida del servidor de Chat
	public void disconnect() {
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
