package es.um.redes.nanoChat.oldmessageFV;

/*
 * NAME
----

operation:<operation>
name:<name>

Defined operations:
Register
Create
Enter
Rename
Kick
Send //TODO revisar jm
*/

public class NCNameMessage extends NCMessage {
	// Campo específico de este tipo de mensaje
	private String name;
	static private final String NAME_FIELD = "Name";

	/**
	 * Creamos un mensaje de tipo Room a partir del código de operación y del nombre
	 */
	public NCNameMessage(NCMessageOp op, String name) {
		this.op = op;
		this.name = name;
	}

	// Pasamos los campos del mensaje a la codificación correcta en field:value
	@Override
	public String toEncodedString() {
		StringBuffer sb = new StringBuffer();
		sb.append(OPCODE_FIELD + DELIMITER + op.getText() + END_LINE); // Construimos el campo
		sb.append(NAME_FIELD + DELIMITER + name + END_LINE); // Construimos el campo
		sb.append(END_LINE); // Marcamos el final del mensaje
		return sb.toString(); // Se obtiene el mensaje

	}

	// Parseamos el mensaje contenido en message con el fin de obtener los distintos
	// campos
	public static NCNameMessage readFromString(NCMessageOp op, String message) {
		String[] lines = message.split(System.getProperty("line.separator"));
		String name = null;
		int idx = lines[1].indexOf(DELIMITER); // Posición del delimitador
		String field = lines[1].substring(0, idx).toLowerCase(); // minúsculas
		String value = lines[1].substring(idx + 1).trim();
		if (field.equalsIgnoreCase(NAME_FIELD))
			name = value;
		return new NCNameMessage(op, name);
	}

	public String getName() {
		return name;
	}
}
