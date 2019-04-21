package es.um.redes.nanoChat.oldmessageFV;

import java.util.Arrays;
import java.util.Collection;

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
	static private final String ROOMS_FIELD = "Rooms";
	static private final String USERS_FIELD = "Users";
	static private final String LAST_MESSAGE_FIELD = "Last Message";
	private Collection<NCRoomDescription> rooms;	// Specific field for this type of message

	/**
	 * Creamos un mensaje de tipo Room a partir del código de operación y de las salas
	 */ //TODO quitar operacion? solo hay una
	public NCInfoMessage(NCMessageOp op, Collection<NCRoomDescription> rooms) {
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
		sb.append(ROOMS_FIELD + DELIMITER);
		if (rooms.isEmpty()) sb.append(SEPARATOR); //TODO cambiar arriba y abajo para que funcione con cadenas vacías fácilmente
		else for (NCRoomDescription des : rooms) {
			sb.append(des.name);
			sb.append(SEPARATOR);
		}
		sb.append(END_LINE);
		// Construimos, para cada sala, los campos users y last:
		for (NCRoomDescription des : rooms) {
			sb.append(USERS_FIELD + DELIMITER);
			if (des.members.isEmpty()) sb.append(SEPARATOR);
			else for (String user : des.members) {
				sb.append(user);
				sb.append(SEPARATOR);
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
		String[] lines = message.split(System.getProperty("line.separator")); // Separamos por líneas
		
		// Procesamos el campo Rooms
		int pos = lines[1].indexOf(DELIMITER);						// delimiter position
		String field = lines[1].substring(0, pos).toLowerCase();	// field in lower case (rooms)
		String value = lines[1].substring(pos+1);					// value
		// Obtenemos un array con los nombres de las habitaciones
		String[] room_names = value.split(String.valueOf(SEPARATOR));
		
		NCRoomDescription rooms[] = new NCRoomDescription[room_names.length];
		// Procesamos los campos users y lastmessage para cada habitación 
		for (int i = 0; i < rooms.length; i++) {
			int line = 2*(i+1);
			// Campo Users
			pos = lines[line].indexOf(DELIMITER);					// delimiter position
			field = lines[line].substring(0, pos).toLowerCase();	// field in lower case (users)
			value = lines[line].substring(pos+1);					// value
			// Obtenemos un array con los nombres de los usuarios
			String[] users = value.split(String.valueOf(SEPARATOR));
			
			// Campo LastMessage
			pos = lines[line+1].indexOf(DELIMITER);					// delimiter position
			field = lines[line+1].substring(0, pos).toLowerCase();	// field in lower case (last message) 
			long hour = Long.parseLong(lines[line+1].substring(pos+1).trim());	// value
			//TODO ver si cambiar la clase NCRoomDescription
			//para hacer que hour sea string y utilizar DateFormat en ella
			NCRoomDescription description = new NCRoomDescription(room_names[i], Arrays.asList(users), hour);
			rooms[i] = description;
				
		}

		return new NCInfoMessage(op, Arrays.asList(rooms));
	}

	public Collection<NCRoomDescription> getRooms() {
		return rooms; //TODO consider change to unmodifiable?
	}
}
