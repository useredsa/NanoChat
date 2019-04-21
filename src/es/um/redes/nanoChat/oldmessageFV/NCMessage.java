package es.um.redes.nanoChat.oldmessageFV;

import java.io.DataInputStream;
import java.io.IOException;


public abstract class NCMessage {
	//Constantes con los delimitadores de los mensajes de field:value
	public static final char DELIMITER = ':';    //Define el delimitador
	public static final char SEPARATOR = '&';
	public static final char END_LINE = '\n';    //Define el carácter de fin de línea
	public static final String OPCODE_FIELD = "operation";	
	protected NCMessageOp op;	
	
	// Método que debe ser implementado específicamente por cada subclase de NCMessage
	protected abstract String toEncodedString();
	
	// Extrae la operación del mensaje entrante y usa la subclase para parsear el resto del mensaje
	public static NCMessage readMessageFromSocket(DataInputStream dis) throws IOException {
		String message = dis.readUTF();
		String[] lines = message.split(System.getProperty("line.separator"));
		if (!lines[0].equals("")) { // Si la línea no está vacía
			int idx = lines[0].indexOf(DELIMITER); // Posición del delimitador
			String field = lines[0].substring(0, idx).toLowerCase(); // minúsculas
			String value = lines[0].substring(idx + 1).trim();
			if (!field.equalsIgnoreCase(OPCODE_FIELD)) return null;
			NCMessageOp op = NCMessageOp.fromString(value);

			switch (op) {
			//case UPLOAD: // TODO
			//case NEW_FILE: //TODO
			case KICK:
			case SEND:
			case ENTER:
			case RENAME:
			case CREATE:
			case REGISTER:
				return NCNameMessage.readFromString(op, message);
			case INFO:
			case ROOMS_INFO:
				return NCInfoMessage.readFromString(op, message);	
			case NEW_DM:
			case NEW_MESSAGE:
			case DM:
				return NCTextMessage.readFromString(op, message);
			// Control Messages
			case ROOMS_LIST:
			case EXIT:
			case OK:
			case DENIED: 
			case REPEATED:
			case KICKED:
			case QUIT: //TODO revisar jm
				return new NCControlMessage(op);				
			default:
				System.err.println("Unknown message type received: \"" + value + "\"");
				return null;
			}
		} else
			return null;
	}
	
	// Devuelve el opcode del mensaje
	public NCMessageOp getOp() {
		return op;
	}
}
