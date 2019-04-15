package es.um.redes.nanoChat.messageFV;

/*
 * Control
----

operation:<operation>

Defined operations:
ROOM_LIST
EXIT
INVALID_CODE 
OK 
DENIED 
REPEATED
KICKED
*/
public class NCControlMessage extends NCMessage {
	//TODO considerar hacer la clase NCMessage esta clase
	//TODO considerar que no haya que instanciar de esta clase, puesto que no contiene datos reales
	/**
	 * Creamos un mensaje de tipo Room a partir del código de operación y del nombre
	 */
	public NCControlMessage(NCMessageOp op) {
		this.op = op;
	}

	// Pasamos los campos del mensaje a la codificación correcta en field:value
	@Override
	public String toEncodedString() {
		StringBuffer sb = new StringBuffer();
		sb.append(OPCODE_FIELD + DELIMITER + op.getText() + END_LINE); // Construimos el campo
		sb.append(END_LINE); // Marcamos el final del mensaje
		return sb.toString(); // Se obtiene el mensaje

	}
	
}
