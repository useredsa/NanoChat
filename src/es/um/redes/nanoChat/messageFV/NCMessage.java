package es.um.redes.nanoChat.messageFV;

import java.io.DataInputStream;
import java.io.IOException;


public abstract class NCMessage {
	protected NCMessageOp op;

	//Constantes con los delimitadores de los mensajes de field:value
	public static final char DELIMITER = ':';    //Define el delimitador
	public static final char SEPARATOR = '&';
	public static final char END_LINE = '\n';    //Define el carácter de fin de línea
	
	public static final String OPCODE_FIELD = "operation";

	/**
	 * Transforma una cadena en el opcode correspondiente
	 */
	protected static NCMessageOp operationToOpcode(String opStr) {
		//Busca entre los opcodes si es válido y devuelve su código
		for (NCMessageOp op : NCMessageOp.values())
			if (op.getText().equalsIgnoreCase(opStr))
				return op;
		
		//Si no se corresponde con ninguna cadena entonces devuelve el código de código no válido
		return NCMessageOp.INVALID_CODE;
	}

	/**
	 * Transforma un opcode en la cadena correspondiente
	 */ //TODO borrar
	/*protected static String opcodeToOperation(byte opcode) {
		//Busca entre los opcodes si es válido y devuelve su cadena
		for (NCMessageOp op : NCMessageOp.values()) //TODO todos no son validos? O.o
			if (op.getCode() == opcode)
				return op.getText();
		
		//Si no se corresponde con ningún opcode entonces devuelve null
		return null;
	}*/
	
	
	
	// Devuelve el opcode del mensaje
	public NCMessageOp getOp() {
		return op;
	}

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
			NCMessageOp op = operationToOpcode(value);

			switch (op) {
			case INVALID_CODE:
				return null;
			case REGISTER:
				return NCNameMessage.readFromString(op, message);
			case ROOMS_INFO:
				return NCInfoMessage.readFromString(op, message);
			// Control Messages
			case ROOMS_LIST:
			case EXIT:
			//case INVALID_CODE: //TODO consider if null or wtf 
			case OK:
			case DENIED: 
			case REPEATED:
			case KICKED:
				return new NCControlMessage(op);				
			default:
				System.err.println("Unknown message type received: " + op.getCode() + " " + op.getText());
				return null;
			}
		} else
			return null;
	}

	// Método para construir un mensaje de tipo Name a partir de la operación y del nombre
	public static NCMessage makeNameMessage(NCMessageOp op, String name) {
		return (new NCNameMessage(op, name));
	}
}
