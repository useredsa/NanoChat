package es.um.redes.nanoChat.server;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import es.um.redes.nanoChat.server.roomManager.NCBasicRoom;
import es.um.redes.nanoChat.server.roomManager.NCRoomDescription;
import es.um.redes.nanoChat.server.roomManager.NCRoomManager;

/**
 * Esta clase contiene el estado general del servidor (sin la lógica relacionada con cada sala particular)
 */
public class NCServerManager {
	
	//TODO quitar estos datos para la primera habitación
	//Primera habitación del servidor
	final static byte INITIAL_ROOM = 'A';
	final static String ROOM_PREFIX = "Room";
	private HashSet<String> users = new HashSet<String>(); 									// Registered nicknames
	private HashMap<String, NCRoomManager> rooms = new HashMap<String, NCRoomManager>();	// Available Rooms (and their room managers)
	//TODO El nombre de la sala es una información que ya se encuentra en el NCRoomManager, así que no deberíamos asociarlo de esta manera,
	// sino obtenerlo de ahí. Pienso que hacerlo así es más propenso a que se produzca un error porque a alguien se le olvide algo al 
	// renombrar una sala o similares.
	
	
	NCServerManager() {
	}
	
	//Devuelve la descripción de las salas existentes
	public synchronized ArrayList<NCRoomDescription> getRoomList() {
		// Pregunta a cada RoomManager cuál es la descripción actual de su sala
		// Añade la información al ArrayList
		ArrayList<NCRoomDescription> info_rooms = new ArrayList<NCRoomDescription>();
		for(String s : rooms.keySet()) {
			info_rooms.add(rooms.get(s).getDescription());
		}
		return info_rooms; //TODO ver si es necesario devolver unmodifiable
	}
	
	
	//Intenta registrar al usuario en el servidor.
	public synchronized boolean addUser(String user) {
		// Devuelve true si no hay otro usuario con su nombre
		return users.add(user);
	}
	
	//Elimina al usuario del servidor
	public synchronized void removeUser(String user) {
		// Elimina al usuario del servidor
		//TODO revisar jm
		users.remove(user);
	}
	
	// Process request from user u to create room
	public NCRoomManager createRoom(String room, String u, NCServerThread th) {
		// Check a room with the same name doesn't exist
		if (rooms.containsKey(room))
			return null;
		// Create a new BasicRoom and register its RoomManager
		NCRoomManager rm = new NCBasicRoom(this, room, u, th);
		rooms.put(rm.getRoomName(), rm);
		return rm;
	}
	
	// Llamada por las salas
	public synchronized boolean renameRoom(String oldName, String newName) {
		if (rooms.containsKey(newName))
			return false;
		rooms.put(newName, rooms.get(oldName));
		rooms.remove(oldName);
		return true;
	}
	
	// Process request from user u to enter room
	public synchronized NCRoomManager getRoomManager(String room) {
		// Check the room exists
		if (!rooms.containsKey(room))
			return null; //TODO change
		// Try to register user and return the RoomManager
		return rooms.get(room); 
	}
}
