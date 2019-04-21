package es.um.redes.nanoChat.client.comm;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collection;

import es.um.redes.nanoChat.messageFV.*;
import es.um.redes.nanoChat.messageFV.encoding.InvalidFormat;
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

	//TODO esto para que era?
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
	public boolean registerNickname(String nick) throws IOException, InvalidFormat {
		//Funcionamiento resumido: SEND(nick) and RCV(NICK_OK) or RCV(NICK_DUPLICATED)
		//Creamos un mensaje de tipo RoomMessage con opcode OP_NICK en el que se inserte el nick
		NCRegisterMessage message = new NCRegisterMessage(nick);
		// Escribimos el mensaje parseado en el flujo de salida, es decir, provocamos que se envíe por la conexión TCP
		dos.writeUTF(message.encode());
		// Leemos el mensaje recibido como respuesta por el flujo de entrada
		NCMessage answer = NCMessage.readMessageFromSocket(dis);
		// Analizamos el mensaje para saber si está duplicado el nick (modificar el return en consecuencia)
		return answer.getOp() == NCMessageOp.OK;			
	}
	
	//Método para obtener la lista de salas del servidor
	public Collection<NCRoomDescription> getRooms() throws InvalidFormat {
		//Funcionamiento resumido: SND(GET_ROOMS) and RCV(ROOM_LIST)
		NCControlMessage message = new NCControlMessage(NCMessageOp.ROOMS_LIST);
		try {	//TODO ejemplo de manejo de errores, probablemente no se haga en el usuario, pero lo de implosions mola
			dos.writeUTF(message.encode());
			NCMessage answer = NCMessage.readMessageFromSocket(dis);
			if (answer.getOp() != NCMessageOp.ROOMS_LIST) {
				// Manejo de errores
				System.err.println("I expected room_list but received implosion");
				return null;
			}
			return ((NCRoomListMessage) answer).getRoomsInfo();
		} catch (IOException e) {
			System.err.println("Problems writting or receiving a message. Got Exception: " + e);
			e.printStackTrace();
		}
		return null;
	}
	
	// Asks to create a room. If successful, automatically enters the room.
	public boolean createRoom(String name) throws IOException, InvalidFormat {
		NCCreateMessage mess = new NCCreateMessage(name);
		dos.writeUTF(mess.encode());
		NCMessage answer = NCMessage.readMessageFromSocket(dis);
		return answer.getOp() == NCMessageOp.OK;
	}
	
	// Método para solicitar la entrada en una sala
	public boolean enterRoom(String room) throws IOException, InvalidFormat {
		// Funcionamiento resumido: SND(ENTER_ROOM<room>) and RCV(IN_ROOM) or RCV(REJECT)
		NCEnterMessage message = new NCEnterMessage(room);
		dos.writeUTF(message.encode());
		NCMessage answer = NCMessage.readMessageFromSocket(dis);
		return answer.getOp() == NCMessageOp.OK;
	}
	
	//Método para salir de una sala
	public void leaveRoom() throws IOException {
		//Funcionamiento resumido: SND(EXIT_ROOM)
		dos.writeUTF(new NCControlMessage(NCMessageOp.EXIT).encode());
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
		dos.writeUTF(new NCSendMessage(text).encode());
	}
	//Metodo para recibir mensajes de chat de una sala
	public NCMessage receiveMessage() { 
		try {
			NCMessage message = NCMessage.readMessageFromSocket(dis);
			return message;
		} catch (Exception e) {
			// TODO revisar
			System.err.println("* There's been a problem while receiving messages");
			e.printStackTrace();
			return null;
		}
		
	}
	
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
		try {// TODO revisar jm
			dos.writeUTF(new NCControlMessage(NCMessageOp.QUIT).encode());
		} catch (IOException e1) {
			System.out.println("* There was an error while you were being deleted from the server");
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
