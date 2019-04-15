package es.um.redes.nanoChat.messageFV;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import es.um.redes.nanoChat.server.roomManager.NCRoomDescription;

/*
 * Rooms Info
----

operation:<operation>
Rooms:<room_name, room_name, ... , room_name>
---Once for each room in Rooms field:---
	Users:<user, user, ... , user>
	Last Message:<time, message>


Defined operations:
ROOMS_INFO
*/

public class NCInfoMessage extends NCMessage {
	// Campo específico de este tipo de mensaje
	private ArrayList<NCRoomDescription> rooms;
	static private final String ROOMS_FIELD = "Rooms";
	static private final String USERS_FIELD = "Users";
	static private final String LAST_MESSAGE_FIELD = "Last Message";

	/**
	 * Creamos un mensaje de tipo Room a partir del código de operación y del nombre
	 */
	public NCInfoMessage(NCMessageOp op, ArrayList<NCRoomDescription> rooms) {
		this.op = op;
		this.rooms = rooms;
	}

	// Pasamos los campos del mensaje a la codificación correcta en field:value
	@Override
	public String toEncodedString() {
		StringBuffer sb = new StringBuffer();
		// Construimos el campo del código de operación
		sb.append(OPCODE_FIELD + DELIMITER + op.getText() + END_LINE);
		// Construimos el campo con los nombres de habitación
		boolean first = true;
		sb.append(ROOMS_FIELD + DELIMITER);
		for (NCRoomDescription des : rooms) {
			if (!first) sb.append(SEPARATOR);
			else first = false;
			sb.append(des.roomName); //TODO make private?
		}
		sb.append(END_LINE);
		// Construimos, para cada sala, los campos users y last:
		first = true;
		for (NCRoomDescription des : rooms) {
			sb.append(USERS_FIELD + DELIMITER);
			for (String user : des.members) {
				if (!first) sb.append(SEPARATOR);
				sb.append(user);
			}
			sb.append(END_LINE);
			sb.append(LAST_MESSAGE_FIELD + DELIMITER + des.timeLastMessage + END_LINE);
		}
		
		
		sb.append(END_LINE); // Marcamos el final del mensaje
		return sb.toString(); // Se obtiene el mensaje

	}

	// Parseamos el mensaje contenido en message con el fin de obtener los distintos
	// campos
	//TODO comprobación de errores y mirar si trim es necesario
	public static NCInfoMessage readFromString(NCMessageOp op, String message) {
		ArrayList<NCRoomDescription> rooms = new ArrayList<NCRoomDescription>();
		// Separamos por líneas
		String[] lines = message.split(System.getProperty("line.separator"));
		
		// Procesamos el campo Rooms
		int pos = lines[1].indexOf(DELIMITER); // Posición del delimitador
		String field = lines[1].substring(0, pos).toLowerCase(); // Campo en minúsculas (rooms)
		// Obtenemos un array con los nombres de las habitaciones
		String value = lines[1].substring(pos+1).trim();
		String[] room_names;
		if (!value.equals(""))
			room_names = value.split(String.valueOf(SEPARATOR));
		else
			room_names = new String[0];
			
		/*while (pos <= lines[1].lastIndexOf(SEPARATOR)) {
			int next_pos = lines[1].indexOf(SEPARATOR, pos+1); // Posición del separador
			if (next_pos == -1) next_pos = lines[1].indexOf(END_LINE); // O el final de línea si no lo encuentra //TODO change
			if (field.equalsIgnoreCase(ROOMS_FIELD))
				room_names.add(lines[1].substring(pos,next_pos).trim());
			pos = next_pos;
		}*/
		
		// Procesamos los campos users y lastmessage para cada habitación 
		for (int i = 0; i < room_names.length; i++) {
			int line = (i+1)*2;
			// Campo Users
			ArrayList<String> members = new ArrayList<String>();
			pos = lines[line].indexOf(DELIMITER); // Posición del delimitador
			field = lines[line].substring(0, pos).toLowerCase(); // Campo en minúsculas (users)
			// Obtenemos un array con los nombres de los usuarios
			String[] users = lines[line].substring(pos+1).split(String.valueOf(SEPARATOR));
			/*while (pos <= lines[line].lastIndexOf(SEPARATOR)) {
				int next_pos = lines[line].indexOf(SEPARATOR, pos+1);
				if (next_pos < 0) next_pos = lines[line].indexOf(END_LINE);
				if (field.equalsIgnoreCase(USERS_FIELD))
					members.add(lines[line].substring(pos,next_pos).trim());
				pos = next_pos;
			}*/
			
			// Campo LastMessage
			pos = lines[line+1].indexOf(DELIMITER);
			field = lines[line+1].substring(0, pos).toLowerCase();
			//TODO ver si cambiar la clase NCRoomDescription
			//para hacer que hour sea string y utilizar DateFormat en ella
			long hour = Long.parseLong(lines[line+1].substring(pos+1).trim());
			if (field.equalsIgnoreCase(LAST_MESSAGE_FIELD)) {
				NCRoomDescription description = new NCRoomDescription(room_names[i], new ArrayList<String>(Arrays.asList(users)), hour);
				rooms.add(description);
			}
				
		}

		return new NCInfoMessage(op, rooms);
	}

	public ArrayList<NCRoomDescription> getRooms() {
		return rooms; //TODO consider change to unmodifiable
	}
}
