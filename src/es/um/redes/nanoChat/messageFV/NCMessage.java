package es.um.redes.nanoChat.messageFV;

import java.io.DataInputStream;
import java.io.IOException;

import es.um.redes.nanoChat.messageFV.encoding.InvalidFormat;
import es.um.redes.nanoChat.messageFV.encoding.NCMessageDecoder;


public interface NCMessage {
	
	// Extrae la operación del mensaje entrante y usa la subclase para parsear el resto del mensaje
	public static NCMessage readMessageFromSocket(DataInputStream dis) throws IOException, InvalidFormat {
		String message = dis.readUTF();
		NCMessageDecoder dec = new NCMessageDecoder(message);
		NCMessageOp op = dec.getMessageOp();
		switch (op) {
		case CREATE:
			return NCCreateMessage.decode(dec);
		//case DM: //TODO
			
		case ENTER:
			return NCEnterMessage.decode(dec);
		case INVALID_CODE:
			break;
		//case KICK: //TODO
			
		//case NEW_DM: //TODO
			
		//case NEW_FILE: //TODO
			
		case NEW_MESSAGE:
			return NCTextMessage.decode(dec);
		case REGISTER:
			return NCRegisterMessage.decode(dec);
		//case RENAME: //TODO
			
		case ROOM_INFO:
			return NCRoomInfoMessage.decode(dec);
		case ROOMS_LIST:
			return NCRoomListMessage.decode(dec);
		case SEND:
			return NCSendMessage.decode(dec);
		//case UPLOAD: //TODO
			
		case DENIED:
		case EXIT:
		case INFO:
		case KICKED:
		case OK:
		case QUIT:
		case REPEATED:
		case LIST_ROOMS:
			return new NCControlMessage(op);
		default:
			System.err.println("Yet unimplemented method (You fool)");
		}
		return null; // Won't reach here
	}
	
	// Devuelve el opcode del mensaje
	public abstract NCMessageOp getOp();
	
	// Método que debe ser implementado específicamente por cada subclase de NCMessage
	public abstract String encode();
}
